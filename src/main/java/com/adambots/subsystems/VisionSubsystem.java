package com.adambots.subsystems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.VisionSystemSim;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;

import com.adambots.Constants.VisionConstants;
import com.adambots.Robot;
import com.adambots.lib.utils.Utils;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Vision subsystem using PhotonVision for AprilTag-based distance and angle estimation.
 * Implements two approaches side-by-side:
 *   A: Camera-Only (direct tag geometry)
 *   B: Pose-Based (PhotonPoseEstimator)
 */
public class VisionSubsystem extends SubsystemBase {

    private final PhotonCamera camera;
    private final AprilTagFieldLayout fieldLayout;
    private final PhotonPoseEstimator poseEstimator;

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

    // Simulation support (only initialized when running in sim)
    private VisionSystemSim visionSim;
    private PhotonCameraSim cameraSim;

    public VisionSubsystem() {
        camera = new PhotonCamera(VisionConstants.kCameraName);

        fieldLayout = AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltWelded);

        poseEstimator = new PhotonPoseEstimator(
            fieldLayout,
            PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
            VisionConstants.kRobotToCamera);
        poseEstimator.setMultiTagFallbackStrategy(PoseStrategy.LOWEST_AMBIGUITY);

        blueHubCenter = computeHubCenter(VisionConstants.kBlueHubTagIds);
        redHubCenter = computeHubCenter(VisionConstants.kRedHubTagIds);

        if (Robot.isSimulation()) {
            simulationInit();
        }
    }

    private void simulationInit() {
        visionSim = new VisionSystemSim("main");
        visionSim.addAprilTags(fieldLayout);
        cameraSim = new PhotonCameraSim(camera);
        visionSim.addCamera(cameraSim, VisionConstants.kRobotToCamera);
    }

    public void simulationPeriodic(Pose2d simPose) {
        visionSim.update(simPose);
    }

    /**
     * Computes the average XY position of all tags in the given ID set.
     */
    private Translation2d computeHubCenter(int[] tagIds) {
        double x = 0, y = 0;
        int count = 0;
        for (int id : tagIds) {
            Optional<Pose3d> pose = fieldLayout.getTagPose(id);
            if (pose.isPresent()) {
                x += pose.get().getX();
                y += pose.get().getY();
                count++;
            }
        }
        if (count == 0) return new Translation2d();
        return new Translation2d(x / count, y / count);
    }

    @Override
    public void periodic() {
        List<PhotonPipelineResult> results = camera.getAllUnreadResults();
        if (results.isEmpty()) return;

        // Use the most recent result
        PhotonPipelineResult result = results.get(results.size() - 1);

        // Determine alliance and hub center
        boolean isRed = Utils.isOnRedAlliance();
        Translation2d hubCenter = isRed ? redHubCenter : blueHubCenter;
        int[] hubTagIds = isRed ? VisionConstants.kRedHubTagIds : VisionConstants.kBlueHubTagIds;

        // Filter targets: keep hub tags with low ambiguity
        List<PhotonTrackedTarget> validTargets = new ArrayList<>();
        for (PhotonTrackedTarget target : result.getTargets()) {
            if (target.getPoseAmbiguity() > VisionConstants.kMaxAmbiguity) continue;
            int fid = target.getFiducialId();
            for (int hubId : hubTagIds) {
                if (fid == hubId) {
                    validTargets.add(target);
                    break;
                }
            }
        }

        visibleTagCount = validTargets.size();

        // ==================== Approach A: Camera-Only ====================
        updateCameraOnly(validTargets, hubCenter);

        // ==================== Approach B: Pose-Based ====================
        updatePoseBased(result, hubCenter);
    }

    /**
     * Approach A: Compute camera field pose from individual tag transforms,
     * then derive distance and angle to hub center.
     */
    private void updateCameraOnly(List<PhotonTrackedTarget> validTargets, Translation2d hubCenter) {
        if (validTargets.isEmpty()) {
            camHasTarget = false;
            return;
        }

        // We'll average the camera's field position across all visible tags.
        // Each tag gives us an independent estimate of where the camera is on the field.
        double sumX = 0, sumY = 0, sumHeading = 0;
        int count = 0;

        for (PhotonTrackedTarget target : validTargets) {
            // Look up this tag's known position on the field (from the field layout JSON)
            Optional<Pose3d> tagPose3d = fieldLayout.getTagPose(target.getFiducialId());
            if (tagPose3d.isEmpty()) continue;

            // PhotonVision gives us the transform FROM camera TO the tag.
            // Inverting it and applying to the tag's field pose gives us the camera's field pose.
            // tagFieldPose + inverse(camToTag) = cameraFieldPose
            Transform3d camToTag = target.getBestCameraToTarget();
            Pose3d cameraPose3d = tagPose3d.get().transformBy(camToTag.inverse());

            sumX += cameraPose3d.getX();
            sumY += cameraPose3d.getY();
            sumHeading += cameraPose3d.getRotation().toRotation2d().getRadians();
            count++;
        }

        if (count == 0) {
            camHasTarget = false;
            return;
        }

        // Average all tag-derived camera poses for a more stable estimate
        double camX = sumX / count;
        double camY = sumY / count;
        double camHeading = sumHeading / count;

        // Straight-line distance from camera position to hub center on the field
        Translation2d camPosition = new Translation2d(camX, camY);
        camDistanceMeters = camPosition.getDistance(hubCenter);

        // Compute the direction the turret needs to rotate:
        // angleToHub = absolute field angle from camera to hub center (atan2)
        // Subtracting the camera's heading gives the relative angle the turret must turn.
        // Positive = hub is to the right of where the camera is facing.
        double angleToHub = Math.atan2(hubCenter.getY() - camY, hubCenter.getX() - camX);
        camAngleDegrees = Utils.wrapAngleDeg(Math.toDegrees(angleToHub - camHeading));

        camHasTarget = camDistanceMeters <= VisionConstants.kMaxDistanceMeters;
    }

    /**
     * Approach B: Use PhotonPoseEstimator to get field pose,
     * then compute distance and angle to hub center.
     */
    private void updatePoseBased(PhotonPipelineResult result, Translation2d hubCenter) {
        // PhotonPoseEstimator fuses all visible tags (multi-tag PnP) to estimate
        // the robot's full field pose. This is the same pipeline a real robot would use.
        Optional<EstimatedRobotPose> estimatedOpt = poseEstimator.update(result);

        if (estimatedOpt.isEmpty()) {
            poseHasTarget = false;
            return;
        }

        // Convert the 3D pose estimate to 2D (we only need X, Y, and heading)
        EstimatedRobotPose estimated = estimatedOpt.get();
        estimatedPose = estimated.estimatedPose.toPose2d();

        // Straight-line distance from estimated robot position to hub center
        poseDistanceMeters = estimatedPose.getTranslation().getDistance(hubCenter);

        // Compute the absolute turret angle needed to aim at the hub:
        // angleToHub = absolute field angle from robot to hub center
        // Subtracting the robot's heading gives the turret angle relative to the robot's front.
        // Unlike Approach A (relative offset), this is an absolute turret setpoint —
        // the turret should go directly to this angle.
        double angleToHub = Math.atan2(
            hubCenter.getY() - estimatedPose.getY(),
            hubCenter.getX() - estimatedPose.getX());
        poseAngleDegrees = Utils.wrapAngleDeg(
            Math.toDegrees(angleToHub - estimatedPose.getRotation().getRadians()));

        poseHasTarget = poseDistanceMeters <= VisionConstants.kMaxDistanceMeters;
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
