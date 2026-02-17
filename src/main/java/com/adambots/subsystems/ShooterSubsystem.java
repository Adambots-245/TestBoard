package com.adambots.subsystems;

import static edu.wpi.first.units.Units.*;

import com.adambots.Constants.ShooterTestConstants;
import com.adambots.RobotMap;
import com.adambots.lib.actuators.BaseMotor;
import com.adambots.lib.actuators.BaseMotor.ControlMode;
import com.adambots.lib.utils.Dash;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.interpolation.InterpolatingDoubleTreeMap;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Shooter subsystem with PID velocity-controlled flywheel and position-controlled turret.
 * All closed-loop control runs on the motor controller at 1kHz.
 * All dashboard interaction through Dash.
 */
public class ShooterSubsystem extends SubsystemBase {

    private final BaseMotor leftFlywheel;
    private final BaseMotor rightFlywheel;
    private final BaseMotor turretMotor;

    private double targetRPS = 0;
    private boolean useInterpolationMode = true;

    // Interpolation table for distance -> RPS lookup
    private final InterpolatingDoubleTreeMap interpolationTable = new InterpolatingDoubleTreeMap();

    // ==================== Tunable GenericEntry Fields ====================
    private GenericEntry flywheelPEntry;
    private GenericEntry flywheelIEntry;
    private GenericEntry flywheelDEntry;
    private GenericEntry flywheelFFEntry;
    private GenericEntry turretPEntry;
    private GenericEntry turretIEntry;
    private GenericEntry turretDEntry;
    private GenericEntry targetDistanceEntry;
    private GenericEntry flywheelToleranceEntry;
    private GenericEntry exitVelocityMultiplierEntry;
    private GenericEntry exitHeightEntry;
    private GenericEntry turretAngleEntry;

    // Interpolation table tunable entries (5 rows of distance + RPS)
    private final GenericEntry[] tableDistanceEntries = new GenericEntry[5];
    private final GenericEntry[] tableRPSEntries = new GenericEntry[5];

    // Last-applied PID values to avoid flooding CAN bus
    private double lastFlywheelP, lastFlywheelI, lastFlywheelD, lastFlywheelFF;
    private double lastTurretP, lastTurretI, lastTurretD;

    public ShooterSubsystem(BaseMotor leftFlywheel, BaseMotor rightFlywheel, BaseMotor turretMotor) {
        this.leftFlywheel = leftFlywheel;
        this.rightFlywheel = rightFlywheel;
        this.turretMotor = turretMotor;
        configureMotors();
        initializeInterpolationTable();
    }

    private void configureMotors() {
            
        leftFlywheel.configure()
            .pid(ShooterTestConstants.kFlywheelP, ShooterTestConstants.kFlywheelI,
                 ShooterTestConstants.kFlywheelD, ShooterTestConstants.kFlywheelFF)
            .brakeMode(false)  // coast for flywheels
            .inverted(true)
            .currentLimits(ShooterTestConstants.kFlywheelStallCurrentLimit,
                           ShooterTestConstants.kFlywheelFreeCurrentLimit, 3000)
            .apply();
        
        // leftFlywheel.setInverted(true);
            rightFlywheel.setStrictFollower(RobotMap.kShooterLeftPort, true);
            rightFlywheel.setBrakeMode(false);

        turretMotor.configure()
            .pid(ShooterTestConstants.kTurretP, ShooterTestConstants.kTurretI,
                 ShooterTestConstants.kTurretD, 0)
            .brakeMode(true)   // hold position
            .currentLimits(ShooterTestConstants.kTurretStallCurrentLimit,
                           ShooterTestConstants.kTurretFreeCurrentLimit, 3000)
            .apply();
        turretMotor.configureSoftLimits(
            ShooterTestConstants.kTurretForwardLimit,
            ShooterTestConstants.kTurretReverseLimit, true);
        turretMotor.setPosition(0);  // zero at startup

        // Initialize last-applied PID values
        lastFlywheelP = ShooterTestConstants.kFlywheelP;
        lastFlywheelI = ShooterTestConstants.kFlywheelI;
        lastFlywheelD = ShooterTestConstants.kFlywheelD;
        lastFlywheelFF = ShooterTestConstants.kFlywheelFF;
        lastTurretP = ShooterTestConstants.kTurretP;
        lastTurretI = ShooterTestConstants.kTurretI;
        lastTurretD = ShooterTestConstants.kTurretD;
    }

    private void initializeInterpolationTable() {
        for (double[] entry : ShooterTestConstants.kDefaultInterpolationTable) {
            interpolationTable.put(entry[0], entry[1]);
        }
    }

    // ==================== Tunable Setup ====================

    /**
     * Registers flywheel tunable GenericEntry fields on the current Dash tab.
     * Call after Dash.useTab() in RobotContainer.
     * @param pos position tracker {col, row}, updated in place
     * @param cols max columns per row before wrapping
     */
    public void setupFlywheelTunables(int[] pos, int cols) {
        flywheelPEntry = Dash.addTunable("Flywheel kP", ShooterTestConstants.kFlywheelP, pos[0], pos[1]);
        advance(pos, cols);
        flywheelIEntry = Dash.addTunable("Flywheel kI", ShooterTestConstants.kFlywheelI, pos[0], pos[1]);
        advance(pos, cols);
        flywheelDEntry = Dash.addTunable("Flywheel kD", ShooterTestConstants.kFlywheelD, pos[0], pos[1]);
        advance(pos, cols);
        flywheelFFEntry = Dash.addTunable("Flywheel kF", ShooterTestConstants.kFlywheelFF, pos[0], pos[1]);
        advance(pos, cols);
        targetDistanceEntry = Dash.addTunable("Target Distance (m)", 3.0, pos[0], pos[1]);
        advance(pos, cols);
        flywheelToleranceEntry = Dash.addTunable("Flywheel Tolerance (RPS)", ShooterTestConstants.kFlywheelToleranceRPS, pos[0], pos[1]);
        advance(pos, cols);
        exitVelocityMultiplierEntry = Dash.addTunable("Exit Vel Multiplier", ShooterTestConstants.kExitVelocityMultiplier, pos[0], pos[1]);
        advance(pos, cols);
        exitHeightEntry = Dash.addTunable("Exit Height (m)", ShooterTestConstants.kExitHeightMeters, pos[0], pos[1]);
        advance(pos, cols);

        // Table distances in one row, RPS values directly below
        newRow(pos);
        for (int i = 0; i < 5; i++) {
            tableDistanceEntries[i] = Dash.addTunable(
                "Table Dist " + (i + 1), ShooterTestConstants.kDefaultInterpolationTable[i][0], pos[0], pos[1]);
            advance(pos, cols);
        }
        newRow(pos);
        for (int i = 0; i < 5; i++) {
            tableRPSEntries[i] = Dash.addTunable(
                "Table RPS " + (i + 1), ShooterTestConstants.kDefaultInterpolationTable[i][1], pos[0], pos[1]);
            advance(pos, cols);
        }
    }

    /**
     * Registers turret tunable GenericEntry fields on the current Dash tab.
     * Call after Dash.useTab() in RobotContainer.
     * @param pos position tracker {col, row}, updated in place
     * @param cols max columns per row before wrapping
     */
    public void setupTurretTunables(int[] pos, int cols) {
        turretPEntry = Dash.addTunable("Turret kP", ShooterTestConstants.kTurretP, pos[0], pos[1]);
        advance(pos, cols);
        turretIEntry = Dash.addTunable("Turret kI", ShooterTestConstants.kTurretI, pos[0], pos[1]);
        advance(pos, cols);
        turretDEntry = Dash.addTunable("Turret kD", ShooterTestConstants.kTurretD, pos[0], pos[1]);
        advance(pos, cols);
        turretAngleEntry = Dash.addTunable("Turret Angle (deg)", 90.0, pos[0], pos[1]);
        advance(pos, cols);
    }

    /** Advances the grid position tracker, wrapping to the next row when needed. */
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

    // ==================== Flywheel Control ====================

    public void setFlywheelRPS(double rps) {
        targetRPS = rps;
        leftFlywheel.set(ControlMode.VELOCITY, rps * ShooterTestConstants.kFlywheelDirection);
    }

    public void stopFlywheel() {
        targetRPS = 0;
        leftFlywheel.set(0);
    }

    /**
     * Spin the flywheel for a given distance using the selected RPS mode.
     */
    public void spinForDistance(double meters) {
        double rps;
        if (useInterpolationMode) {
            rps = getRPSFromTable(meters);
        } else {
            rps = getRPSFromCalculator(meters);
        }
        setFlywheelRPS(rps);
    }

    // ==================== Turret Control ====================

    public void setTurretAngle(double degrees) {
        degrees = MathUtil.clamp(degrees,
            ShooterTestConstants.kTurretMinDegrees, ShooterTestConstants.kTurretMaxDegrees);
        double rotations = (degrees / 360.0) * ShooterTestConstants.kTurretGearRatio;
        turretMotor.set(ControlMode.POSITION, rotations);
    }

    public void stopTurret() {
        turretMotor.set(0);
    }

    public double getTurretAngleDegrees() {
        return (turretMotor.getPosition() / ShooterTestConstants.kTurretGearRatio) * 360.0;
    }

    // ==================== RPS Calculation Modes ====================

    /**
     * Table mode: linearly interpolates between calibrated distance-RPS entries.
     */
    public double getRPSFromTable(double distanceMeters) {
        return interpolationTable.get(distanceMeters);
    }

    /**
     * Calculator mode: projectile motion physics.
     * v^2 = (g * d^2) / (2 * cos^2(theta) * (d * tan(theta) - (hubHeight - exitHeight)))
     * RPS = v / (2 * pi * flywheelRadius * exitVelocityMultiplier)
     * Returns 0 if shot is physically impossible.
     */
    public double getRPSFromCalculator(double distanceMeters) {
        double exitHeight = (exitHeightEntry != null)
            ? exitHeightEntry.getDouble(ShooterTestConstants.kExitHeightMeters)
            : ShooterTestConstants.kExitHeightMeters;
        double exitVelMult = (exitVelocityMultiplierEntry != null)
            ? exitVelocityMultiplierEntry.getDouble(ShooterTestConstants.kExitVelocityMultiplier)
            : ShooterTestConstants.kExitVelocityMultiplier;

        double theta = Math.toRadians(ShooterTestConstants.kHoodAngleDegrees);
        double cosTheta = Math.cos(theta);
        double tanTheta = Math.tan(theta);
        double g = ShooterTestConstants.kGravity;
        double d = distanceMeters;
        double heightDiff = ShooterTestConstants.kHubHeightMeters - exitHeight;

        double denominator = 2.0 * cosTheta * cosTheta * (d * tanTheta - heightDiff);
        if (denominator <= 0) {
            return 0; // physically impossible shot
        }

        double vSquared = (g * d * d) / denominator;
        if (vSquared <= 0) {
            return 0;
        }

        double v = Math.sqrt(vSquared);
        double rps = v / (2.0 * Math.PI * ShooterTestConstants.kFlywheelRadiusMeters * exitVelMult);

        return Math.min(rps, ShooterTestConstants.kMaxFreeSpeedRPS);
    }

    // ==================== Telemetry Getters ====================

    public double getLeftRPS() {
        return leftFlywheel.getVelocity().in(RotationsPerSecond);
    }

    public double getRightRPS() {
        return rightFlywheel.getVelocity().in(RotationsPerSecond);
    }

    public double getTargetRPS() {
        return targetRPS;
    }

    public boolean isAtSpeed() {
        double tolerance = (flywheelToleranceEntry != null)
            ? flywheelToleranceEntry.getDouble(ShooterTestConstants.kFlywheelToleranceRPS)
            : ShooterTestConstants.kFlywheelToleranceRPS;
        return targetRPS > 0 && Math.abs(Math.abs(getLeftRPS()) - targetRPS) < tolerance;
    }

    public boolean isUsingInterpolationMode() {
        return useInterpolationMode;
    }

    public double getTunableDistance() {
        return (targetDistanceEntry != null)
            ? targetDistanceEntry.getDouble(3.0) : 3.0;
    }

    // ==================== Periodic ====================

    @Override
    public void periodic() {
        // Read tunable PID values and apply only when changed
        if (flywheelPEntry != null) {
            double p = flywheelPEntry.getDouble(ShooterTestConstants.kFlywheelP);
            double i = flywheelIEntry.getDouble(ShooterTestConstants.kFlywheelI);
            double d = flywheelDEntry.getDouble(ShooterTestConstants.kFlywheelD);
            double f = flywheelFFEntry.getDouble(ShooterTestConstants.kFlywheelFF);

            if (p != lastFlywheelP || i != lastFlywheelI || d != lastFlywheelD || f != lastFlywheelFF) {
                leftFlywheel.setPID(0, p, i, d, f);
                lastFlywheelP = p;
                lastFlywheelI = i;
                lastFlywheelD = d;
                lastFlywheelFF = f;
            }
        }

        if (turretPEntry != null) {
            double p = turretPEntry.getDouble(ShooterTestConstants.kTurretP);
            double i = turretIEntry.getDouble(ShooterTestConstants.kTurretI);
            double d = turretDEntry.getDouble(ShooterTestConstants.kTurretD);

            if (p != lastTurretP || i != lastTurretI || d != lastTurretD) {
                turretMotor.setPID(0, p, i, d, 0);
                lastTurretP = p;
                lastTurretI = i;
                lastTurretD = d;
            }
        }

        // Rebuild interpolation table from tunable entries
        if (tableDistanceEntries[0] != null) {
            interpolationTable.clear();
            for (int idx = 0; idx < 5; idx++) {
                double dist = tableDistanceEntries[idx].getDouble(
                    ShooterTestConstants.kDefaultInterpolationTable[idx][0]);
                double rps = tableRPSEntries[idx].getDouble(
                    ShooterTestConstants.kDefaultInterpolationTable[idx][1]);
                interpolationTable.put(dist, rps);
            }
        }
    }

    // ==================== Command Factories ====================

    public Command spinUpCommand(double rps) {
        return runEnd(() -> setFlywheelRPS(rps), this::stopFlywheel)
            .withName("Spin " + rps + " RPS");
    }

    public Command spinForDistanceCommand() {
        return runEnd(
            () -> spinForDistance(getTunableDistance()),
            this::stopFlywheel
        ).withName("Spin For Distance");
    }

    public Command stopFlywheelCommand() {
        return runOnce(this::stopFlywheel)
            .withName("Stop Flywheel");
    }

    // Turret and toggle commands use Commands static factories (no subsystem requirement)
    // so they can run independently of flywheel commands without interrupting them.

    public Command aimTurretCommand(double degrees) {
        return Commands.runOnce(() -> setTurretAngle(degrees))
            .withName("Turret " + degrees + " deg");
    }

    /**
     * Continuously aims the turret to the angle set by the Shuffleboard slider.
     * Hold to keep tracking; on release the turret holds its last position (brake mode).
     */
    public Command aimTurretManualCommand() {
        return Commands.run(() -> setTurretAngle(
                turretAngleEntry != null ? turretAngleEntry.getDouble(90.0) : 90.0))
            .withName("Aim Turret Manual");
    }

    public Command stopTurretCommand() {
        return Commands.runOnce(this::stopTurret)
            .withName("Stop Turret");
    }

    public Command toggleModeCommand() {
        return Commands.runOnce(() -> useInterpolationMode = !useInterpolationMode)
            .withName("Toggle Mode");
    }

    // ==================== Debug Commands ====================

    public Command openLoopForwardCommand() {
        return runEnd(() -> leftFlywheel.set(0.5), this::stopFlywheel)
            .withName("Open Loop +50%");
    }

    public Command openLoopReverseCommand() {
        return runEnd(() -> leftFlywheel.set(-0.5), this::stopFlywheel)
            .withName("Open Loop -50%");
    }

    public Command spinNegativeCommand(double rps) {
        return runEnd(() -> setFlywheelRPS(-rps), this::stopFlywheel)
            .withName("Spin -" + rps + " RPS");
    }

    // ==================== Backward-Compatible Wrappers (for ShootCommand.java) ====================

    public Command runShooterCommand() {
        return spinUpCommand(50.0).withName("Run Shooter");
    }

    public Command reverseShooterCommand() {
        return runEnd(() -> leftFlywheel.set(0.5 * ShooterTestConstants.kFlywheelDirection), this::stopFlywheel)
            .withName("Reverse Shooter");
    }

    public Command stopShooterCommand() {
        return stopFlywheelCommand().withName("Stop Shooter");
    }
}
