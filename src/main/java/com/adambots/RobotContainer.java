package com.adambots;

import static edu.wpi.first.units.Units.*;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

import com.adambots.commands.ShootCommand;
import com.adambots.lib.utils.Dash;
import com.adambots.lib.utils.Utils;
import com.adambots.subsystems.ShooterSubsystem;
import com.adambots.subsystems.UptakeSubsystem;
import com.adambots.subsystems.VisionSubsystem;

/**
 * RobotContainer for TestBoard - a subsystem testing platform.
 *
 * This is designed for testing prototype mechanisms using Shuffleboard.
 * Each subsystem gets its own tab with commands pre-arranged.
 *
 * To add a new subsystem:
 * 1. Create the subsystem class in the subsystems package
 * 2. Create motors in RobotMap and pass them to the subsystem constructor
 * 3. Instantiate the subsystem here
 * 4. Create a Shuffleboard tab and add commands in setupDashboard()
 */
public class RobotContainer {

    // ==================== Subsystems ====================
    private final ShooterSubsystem shooter;
    private final UptakeSubsystem uptake;
    private final VisionSubsystem vision;

    // Sim-only tunables (Vision tab)
    private GenericEntry simXEntry;
    private GenericEntry simYEntry;
    private GenericEntry simHeadingEntry;
    private Field2d field;

    public RobotContainer() {
        // Initialize subsystems with motors from RobotMap
        shooter = new ShooterSubsystem(RobotMap.shooterLeftMotor, RobotMap.shooterRightMotor, RobotMap.turretMotor);
        uptake = new UptakeSubsystem(RobotMap.uptakeMotor);
        vision = new VisionSubsystem();

        // Setup Shuffleboard tabs with commands
        setupDashboard();
    }

    /**
     * Create Shuffleboard tabs for each subsystem with commands pre-arranged.
     * Tabs persist between runs - just rearrange widgets as needed.
     */
    private void setupDashboard() {
        // Change this to adjust how many widgets fit in one row
        final int COLS = 9;

        setupFlywheelTab(COLS);
        setupTurretTab(COLS);
        setupUptakeTab(COLS);
        setupVisionTab(COLS);
    }

    private void setupFlywheelTab(int cols) {
        Dash.useTab("Flywheel");
        int[] pos = {0, 0};

        // --- Tunable PID & parameters ---
        shooter.setupFlywheelTunables(pos, cols);

        // --- Telemetry ---
        newRow(pos);
        Dash.add("Left RPS", shooter::getLeftRPS, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Right RPS", shooter::getRightRPS, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Target RPS", shooter::getTargetRPS, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("At Speed", shooter::isAtSpeed, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Mode", () -> shooter.isUsingInterpolationMode() ? "TABLE" : "CALCULATOR", pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Table RPS", () -> shooter.getRPSFromTable(shooter.getTunableDistance()), pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Calc RPS", () -> shooter.getRPSFromCalculator(shooter.getTunableDistance()), pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Flywheel Amps", () -> RobotMap.shooterLeftMotor.getCurrentDraw().in(Amps), pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Flywheel Temp", () -> RobotMap.shooterLeftMotor.getTemperature(), pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Duty Cycle", () -> RobotMap.shooterLeftMotor.getOutputPercent(), pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Left Position", () -> RobotMap.shooterLeftMotor.getPosition(), pos[0], pos[1]);
        advance(pos, cols);

        // --- Commands ---
        newRow(pos);
        Dash.addCommand("Spin 50 RPS", shooter.spinUpCommand(50), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Spin 75 RPS", shooter.spinUpCommand(75), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Spin For Distance", shooter.spinForDistanceCommand(), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Stop Flywheel", shooter.stopFlywheelCommand(), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Toggle Mode", shooter.toggleModeCommand(), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Open Loop +50%", shooter.openLoopForwardCommand(), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Open Loop -50%", shooter.openLoopReverseCommand(), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Spin -75 RPS", shooter.spinNegativeCommand(75), pos[0], pos[1]);
        advance(pos, cols);
    }

    private void setupTurretTab(int cols) {
        Dash.useTab("Turret");
        int[] pos = {0, 0};

        // --- Tunable PID & parameters ---
        shooter.setupTurretTunables(pos, cols);

        // --- Telemetry ---
        newRow(pos);
        Dash.add("Turret Angle", shooter::getTurretAngleDegrees, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Turret Amps", () -> RobotMap.turretMotor.getCurrentDraw().in(Amps), pos[0], pos[1]);
        advance(pos, cols);

        // --- Commands ---
        newRow(pos);
        Dash.addCommand("Turret 0 deg", shooter.aimTurretCommand(0), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Turret 90 deg", shooter.aimTurretCommand(90), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Turret 180 deg", shooter.aimTurretCommand(180), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Aim Turret Manual", shooter.aimTurretManualCommand(), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Stop Turret", shooter.stopTurretCommand(), pos[0], pos[1]);
        advance(pos, cols);
    }

    private void setupUptakeTab(int cols) {
        Dash.useTab("Uptake");
        int[] pos = {0, 0};

        // --- Telemetry ---
        Dash.add("Uptake RPM", uptake::getUptakeRPM, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Uptake Amps", () -> RobotMap.uptakeMotor.getCurrentDraw().in(Amps), pos[0], pos[1]);
        advance(pos, cols);

        // --- Commands ---
        newRow(pos);
        Dash.addCommand("Run Uptake", uptake.runUptakeCommand(), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Reverse Uptake", uptake.reverseUptakeCommand(), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Stop Uptake", uptake.stopUptakeCommand(), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Shoot With Uptake", ShootCommand.shootWithUptake(shooter, uptake), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Reverse All", ShootCommand.reverseAll(shooter, uptake), pos[0], pos[1]);
        advance(pos, cols);
        Dash.addCommand("Stop All", ShootCommand.stopAll(shooter, uptake), pos[0], pos[1]);
        advance(pos, cols);
    }

    private void setupVisionTab(int cols) {
        Dash.useTab("Vision");
        int[] pos = {0, 0};

        // --- Row 0: Telemetry (both approaches side by side) ---
        Dash.add("Cam Distance", vision::getCamDistance, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Cam Angle", vision::getCamAngle, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Cam Target", vision::camHasTarget, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Pose Distance", vision::getPoseDistance, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Pose Angle", vision::getPoseAngle, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Pose Target", vision::poseHasTarget, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Tags", vision::getVisibleTagCount, pos[0], pos[1]);
        advance(pos, cols);
        Dash.add("Alliance", vision::getAllianceColor, pos[0], pos[1]);
        advance(pos, cols);

        // --- Row 1: Approach A Commands (Camera-Only) ---
        newRow(pos);
        Dash.addCommand("A: Set Distance",
            Commands.runOnce(() -> {
                if (vision.camHasTarget()) shooter.setTunableDistance(vision.getCamDistance());
            }).withName("A: Set Distance"),
            pos[0], pos[1]);
        advance(pos, cols);

        Dash.addCommand("A: Aim Turret",
            Commands.run(() -> {
                if (vision.camHasTarget()) {
                    shooter.setTurretAngle(shooter.getTurretAngleDegrees() + vision.getCamAngle());
                }
            }).withName("A: Aim Turret"),
            pos[0], pos[1]);
        advance(pos, cols);

        Dash.addCommand("A: Set + Aim",
            Commands.run(() -> {
                if (vision.camHasTarget()) {
                    shooter.setTunableDistance(vision.getCamDistance());
                    shooter.setTurretAngle(shooter.getTurretAngleDegrees() + vision.getCamAngle());
                }
            }).withName("A: Set + Aim"),
            pos[0], pos[1]);
        advance(pos, cols);

        // --- Row 2: Approach B Commands (Pose-Based) ---
        newRow(pos);
        Dash.addCommand("B: Set Distance",
            Commands.runOnce(() -> {
                if (vision.poseHasTarget()) shooter.setTunableDistance(vision.getPoseDistance());
            }).withName("B: Set Distance"),
            pos[0], pos[1]);
        advance(pos, cols);

        Dash.addCommand("B: Aim Turret",
            Commands.run(() -> {
                if (vision.poseHasTarget()) {
                    shooter.setTurretAngle(vision.getPoseAngle());
                }
            }).withName("B: Aim Turret"),
            pos[0], pos[1]);
        advance(pos, cols);

        Dash.addCommand("B: Set + Aim",
            Commands.run(() -> {
                if (vision.poseHasTarget()) {
                    shooter.setTunableDistance(vision.getPoseDistance());
                    shooter.setTurretAngle(vision.getPoseAngle());
                }
            }).withName("B: Set + Aim"),
            pos[0], pos[1]);
        advance(pos, cols);

        // --- Row 3: Sim Tunables (only meaningful in simulation) ---
        if (Robot.isSimulation()) {
            newRow(pos);
            simXEntry = Dash.addTunable("Sim X (m)", 14.0, pos[0], pos[1]);
            advance(pos, cols);
            simYEntry = Dash.addTunable("Sim Y (m)", 4.0, pos[0], pos[1]);
            advance(pos, cols);
            simHeadingEntry = Dash.addTunable("Sim Heading (deg)", 180.0, pos[0], pos[1]);
            advance(pos, cols);
            Dash.add("Expected Dist", this::getExpectedDistance, pos[0], pos[1]);
            advance(pos, cols);
            Dash.add("Expected Angle", this::getExpectedAngle, pos[0], pos[1]);
            advance(pos, cols);

            // Field2d visualization — hub center is updated each cycle in simulationPeriodic()
            field = new Field2d();
            SmartDashboard.putData("Field", field);
        }
    }

    // ==================== Grid Layout Helpers ====================

    /** Advances to the next column, wrapping to a new row when cols is exceeded. */
    private static void advance(int[] pos, int cols) {
        pos[0]++;
        if (pos[0] >= cols) {
            pos[0] = 0;
            pos[1]++;
        }
    }

    /** Jumps to column 0 of the next row (no-op if already at column 0). */
    private static void newRow(int[] pos) {
        if (pos[0] != 0) {
            pos[0] = 0;
            pos[1]++;
        }
    }

    // ==================== Simulation Support ====================

    private Pose2d getSimPose() {
        return new Pose2d(
            simXEntry.getDouble(0),
            simYEntry.getDouble(0),
            Rotation2d.fromDegrees(simHeadingEntry.getDouble(180)));
    }

    private double getExpectedDistance() {
        Pose2d simPose = getSimPose();
        Translation2d hubCenter = vision.getHubCenter();
        return simPose.getTranslation().getDistance(hubCenter);
    }

    private double getExpectedAngle() {
        Pose2d simPose = getSimPose();
        Translation2d hubCenter = vision.getHubCenter();
        double angleToHub = Math.atan2(
            hubCenter.getY() - simPose.getY(),
            hubCenter.getX() - simPose.getX());
        return Utils.wrapAngleDeg(
            Math.toDegrees(angleToHub - simPose.getRotation().getRadians()));
    }

    public void simulationPeriodic() {
        Pose2d simPose = getSimPose();
        vision.simulationPeriodic(simPose);
        field.setRobotPose(simPose);
        field.getObject("Hub Center").setPose(new Pose2d(vision.getHubCenter(), new Rotation2d()));
    }

    public Command getAutonomousCommand() {
        return Commands.none();
    }
}
