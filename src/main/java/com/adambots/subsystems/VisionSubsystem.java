package com.adambots.subsystems;

import static edu.wpi.first.units.Units.*;

import java.util.Optional;

import com.adambots.Constants.VisionConstants;
import com.adambots.lib.utils.Utils;
import com.adambots.lib.vision.PhotonVision;
import com.adambots.lib.vision.VisionCameraInterface;
import com.adambots.lib.vision.VisionResult;
import com.adambots.lib.vision.VisionTarget;
import com.adambots.lib.vision.config.VisionCameraConfig.CameraPurpose;
import com.adambots.lib.vision.config.VisionConfigBuilder;
import com.adambots.lib.vision.config.VisionSystemConfig;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Vision subsystem using AdambotsLib's PhotonVision for AprilTag-based distance and angle estimation.
 * Implements two approaches side-by-side:
 *   A: Camera-Only (direct tag geometry — local code, not in lib)
 *   B: Pose-Based (via lib's PhotonVision class)
 */
public class VisionSubsystem extends SubsystemBase {

    private final PhotonVision photonVision;

    // Approach A outputs (camera-only)
    private double camDistanceMeters = 0;
    private double camAngleDegrees = 0;
    private boolean camHasTarget = false;

    // Approach B outputs (pose-based)
    private double poseDistanceMeters = 0;
    private double poseAngleDegrees = 0;
    private boolean poseHasTarget = false;
    private Pose2d estimatedPose = new Pose2d();

    // Shared
    private int visibleTagCount = 0;

    // Precomputed hub centers
    private final Translation2d blueHubCenter;
    private final Translation2d redHubCenter;

    // Sim pose stored from simulationPeriodic(), passed to lib's updatePoseEstimation()
    private Pose2d simPose;

    public VisionSubsystem(Field2d field) {
        VisionSystemConfig config = VisionConfigBuilder.create()
            .addCamera(VisionConstants.kCameraName)
                .position(
                    Meters.of(VisionConstants.kCameraForwardOffsetMeters),
                    Meters.of(0),
                    Meters.of(VisionConstants.kCameraHeightMeters))
                .rotation(
                    Degrees.of(0),
                    Degrees.of(Math.toDegrees(VisionConstants.kCameraPitchRadians)),
                    Degrees.of(0))
                .purpose(CameraPurpose.BOTH)
                .done()
            .ambiguityThreshold(VisionConstants.kMaxAmbiguity)
            .build();

        photonVision = new PhotonVision(config, () -> estimatedPose, field);

        // Precompute hub centers using lib method
        blueHubCenter = photonVision.getTagGroupCenter(VisionConstants.kBlueHubTagIds);
        redHubCenter = photonVision.getTagGroupCenter(VisionConstants.kRedHubTagIds);
    }

    public void simulationPeriodic(Pose2d simPose) {
        this.simPose = simPose;
    }

    @Override
    public void periodic() {
        // Approach B: lib handles pose estimation + sim update
        photonVision.updatePoseEstimation(
            (pose, timestamp, stdDevs) -> { this.estimatedPose = pose; },
            () -> Optional.ofNullable(simPose)
        );

        // The lib's sim camera runs at 30 FPS (~33ms) while the robot loop is 20ms.
        // On cycles where no new camera frame is available, the lib's result cache is empty.
        // Keep previous values (matching the old code's "if (results.isEmpty()) return;").
        VisionCameraInterface cam = photonVision.getCamera(VisionConstants.kCameraName);
        if (cam.getLatestResult().isEmpty()) return;

        // Determine alliance and hub center
        boolean isRed = Utils.isOnRedAlliance();
        Translation2d hubCenter = isRed ? redHubCenter : blueHubCenter;
        int[] hubTagIds = isRed ? VisionConstants.kRedHubTagIds : VisionConstants.kBlueHubTagIds;

        // Tag count via lib
        visibleTagCount = photonVision.getVisibleTagCount(hubTagIds, VisionConstants.kMaxAmbiguity);

        // ==================== Approach A: Camera-Only ====================
        updateCameraOnly(hubCenter, hubTagIds);

        // ==================== Approach B: Pose-Based ====================
        if (visibleTagCount > 0 && estimatedPose.getTranslation().getNorm() > 0) {
            poseDistanceMeters = photonVision.getDistanceToPoint(hubCenter);
            poseAngleDegrees = photonVision.getYawToPoint(hubCenter).getDegrees();
            poseHasTarget = poseDistanceMeters <= VisionConstants.kMaxDistanceMeters;
        } else {
            poseHasTarget = false;
        }
    }

    /**
     * Approach A: Compute camera field pose from individual tag transforms,
     * then derive distance and angle to hub center.
     * This stays as local code — the lib doesn't have this camera-only pattern.
     */
    private void updateCameraOnly(Translation2d hubCenter, int[] hubTagIds) {
        VisionCameraInterface cam = photonVision.getCamera(VisionConstants.kCameraName);
        Optional<? extends VisionResult> resultOpt = cam.getLatestResult();

        if (resultOpt.isEmpty() || !resultOpt.get().hasTargets()) {
            camHasTarget = false;
            return;
        }

        VisionResult result = resultOpt.get();

        // We'll average the camera's field position across all visible hub tags.
        // Each tag gives us an independent estimate of where the camera is on the field.
        double sumX = 0, sumY = 0;
        // Use circular mean for heading (cos/sin averaging) to avoid ±180° wraparound bug.
        // Arithmetic mean of +179° and -179° gives 0° (wrong); circular mean gives 180° (correct).
        double sumCos = 0, sumSin = 0;
        int count = 0;

        for (VisionTarget target : result.getTargets()) {
            if (target.getPoseAmbiguity() > VisionConstants.kMaxAmbiguity) continue;

            // Filter: only process hub tags for this alliance
            boolean isHubTag = false;
            for (int hubId : hubTagIds) {
                if (target.getFiducialId() == hubId) { isHubTag = true; break; }
            }
            if (!isHubTag) continue;

            // Look up this tag's known position on the field
            Optional<Pose3d> tagPose3d = PhotonVision.fieldLayout.getTagPose(target.getFiducialId());
            if (tagPose3d.isEmpty()) continue;

            // PhotonVision gives us the transform FROM camera TO the tag.
            // Inverting it and applying to the tag's field pose gives us the camera's field pose.
            Transform3d camToTag = target.getBestCameraToTarget();
            Pose3d cameraPose3d = tagPose3d.get().transformBy(camToTag.inverse());

            sumX += cameraPose3d.getX();
            sumY += cameraPose3d.getY();
            double heading = cameraPose3d.getRotation().toRotation2d().getRadians();
            sumCos += Math.cos(heading);
            sumSin += Math.sin(heading);
            count++;
        }

        if (count == 0) {
            camHasTarget = false;
            return;
        }

        // Average all tag-derived camera poses for a more stable estimate
        double camX = sumX / count;
        double camY = sumY / count;
        double camHeading = Math.atan2(sumSin / count, sumCos / count);

        // Straight-line distance from camera position to hub center on the field
        Translation2d camPosition = new Translation2d(camX, camY);
        camDistanceMeters = camPosition.getDistance(hubCenter);

        // Compute the direction the turret needs to rotate
        double angleToHub = Math.atan2(hubCenter.getY() - camY, hubCenter.getX() - camX);
        camAngleDegrees = Utils.wrapAngleDeg(Math.toDegrees(angleToHub - camHeading));

        camHasTarget = camDistanceMeters <= VisionConstants.kMaxDistanceMeters;
    }

    // ==================== Approach A Getters ====================

    public double getCamDistance() { return camDistanceMeters; }
    public double getCamAngle() { return camAngleDegrees; }
    public boolean camHasTarget() { return camHasTarget; }

    // ==================== Approach B Getters ====================

    public double getPoseDistance() { return poseDistanceMeters; }
    public double getPoseAngle() { return poseAngleDegrees; }
    public boolean poseHasTarget() { return poseHasTarget; }
    public Pose2d getEstimatedPose() { return estimatedPose; }

    // ==================== Shared Getters ====================

    public int getVisibleTagCount() { return visibleTagCount; }

    public String getAllianceColor() {
        if (Utils.isOnRedAlliance()) return "Red";
        if (Utils.isOnBlueAlliance()) return "Blue";
        return "Unknown";
    }

    public Translation2d getHubCenter() {
        return Utils.isOnRedAlliance() ? redHubCenter : blueHubCenter;
    }
}
