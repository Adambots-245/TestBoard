package com.adambots.simulation;

import static edu.wpi.first.units.Units.*;

import java.util.List;

import org.dyn4j.geometry.Circle;
import org.ironmaple.simulation.SimulatedArena;
import org.ironmaple.simulation.gamepieces.GamePieceOnFieldSimulation.GamePieceInfo;
import org.ironmaple.simulation.gamepieces.GamePieceProjectile;

import com.adambots.Constants.ShooterTestConstants;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructArrayPublisher;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Handles FUEL projectile simulation for 3D visualization in AdvantageScope.
 *
 * <p>Creates realistic ball trajectories using maple-sim's GamePieceProjectile.
 * Trajectories are logged to NetworkTables for AdvantageScope 3D visualization.
 */
public class FuelProjectile {

    // Sim-specific exit velocity scale factor. The theoretical multiplier (0.85) overpredicts
    // real exit velocity because it doesn't account for ball compression, wheel slip, and
    // friction losses. This value is tuned so the sim trajectory matches the real-robot
    // interpolation table (i.e. table RPS at a given distance should land on the hub).
    // Derived by back-calculating from the 2m/45RPS and 5m/60RPS table entries.
    private static final double SIM_EXIT_VELOCITY_MULTIPLIER = 0.43;

    private static final Angle HOOD_ANGLE = Degrees.of(ShooterTestConstants.kHoodAngleDegrees);
    private static final Distance LAUNCH_HEIGHT = Meters.of(ShooterTestConstants.kExitHeightMeters);
    private static final double LAUNCH_FORWARD_OFFSET_M = 0.25;

    private static final GamePieceInfo FUEL_INFO = new GamePieceInfo(
        "FUEL",
        new Circle(ShooterTestConstants.kFuelDiameterMeters / 2.0),
        Meters.of(ShooterTestConstants.kFuelDiameterMeters),
        Kilograms.of(ShooterTestConstants.kFuelMassKg),
        0.8,   // linearDamping
        0.4,   // angularDamping
        0.6    // coefficientOfRestitution
    );

    private static final Translation3d TARGET_TOLERANCE = new Translation3d(0.5, 0.5, 0.5);

    private static StructArrayPublisher<Pose3d> successfulShotPublisher;
    private static StructArrayPublisher<Pose3d> missedShotPublisher;

    static {
        var table = NetworkTableInstance.getDefault().getTable("Shooter");
        successfulShotPublisher = table.getStructArrayTopic("SuccessfulShot", Pose3d.struct).publish();
        missedShotPublisher = table.getStructArrayTopic("MissedShot", Pose3d.struct).publish();
    }

    /**
     * Launches a FUEL projectile from the robot's position toward a hub target.
     *
     * @param robotPose      Current robot pose on the field
     * @param shootDirection World-space shooting direction (robot heading + turret angle)
     * @param exitVelocityMPS Ball exit velocity in meters per second
     * @param hubCenter2d    Hub center position (alliance-aware)
     */
    public static void launch(Pose2d robotPose, Rotation2d shootDirection, double exitVelocityMPS, Translation2d hubCenter2d) {
        Translation2d shooterOffset = new Translation2d(LAUNCH_FORWARD_OFFSET_M, 0);
        LinearVelocity launchSpeed = MetersPerSecond.of(exitVelocityMPS);

        Translation3d hubTarget = new Translation3d(
            hubCenter2d.getX(),
            hubCenter2d.getY(),
            ShooterTestConstants.kHubHeightMeters
        );

        // Build pose facing shoot direction (turret world heading, not robot heading)
        Pose2d launchPose = new Pose2d(robotPose.getTranslation(), shootDirection);

        GamePieceProjectile projectile = new GamePieceProjectile(
            FUEL_INFO,
            launchPose.getTranslation(),
            shooterOffset,
            new ChassisSpeeds(),          // stationary
            launchPose.getRotation(),     // shoot direction
            LAUNCH_HEIGHT,
            launchSpeed,
            HOOD_ANGLE
        )
        .withTargetPosition(() -> hubTarget)
        .withTargetTolerance(TARGET_TOLERANCE)
        .withProjectileTrajectoryDisplayCallBack(
            (List<Pose3d> trajectory) -> {
                successfulShotPublisher.set(trajectory.toArray(new Pose3d[0]));
            },
            (List<Pose3d> trajectory) -> {
                missedShotPublisher.set(trajectory.toArray(new Pose3d[0]));
            }
        );

        SimulatedArena.getInstance().addGamePieceProjectile(projectile);

        SmartDashboard.putNumber("Shooter/ExitVelocity", exitVelocityMPS);
        SmartDashboard.putNumber("Shooter/LaunchAngleDeg", ShooterTestConstants.kHoodAngleDegrees);
    }

    /**
     * Calculates exit velocity from flywheel RPS.
     * v = 2 * PI * RPS * wheelRadius * multiplier
     */
    public static double calculateExitVelocity(double flywheelRPS) {
        double omega = 2 * Math.PI * flywheelRPS;
        return omega * ShooterTestConstants.kFlywheelRadiusMeters * SIM_EXIT_VELOCITY_MULTIPLIER;
    }
}
