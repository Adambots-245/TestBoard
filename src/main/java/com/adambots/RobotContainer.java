package com.adambots;

import static edu.wpi.first.units.Units.*;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

import com.adambots.lib.utils.Dash;
import com.adambots.subsystems.ShooterSubsystem;

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

    public RobotContainer() {
        // Initialize subsystems with motors from RobotMap
        shooter = new ShooterSubsystem(RobotMap.shooterLeftMotor, RobotMap.shooterRightMotor, RobotMap.turretMotor);

        // Setup Shuffleboard tabs with commands
        setupDashboard();
    }

    /**
     * Create Shuffleboard tabs for each subsystem with commands pre-arranged.
     * Tabs persist between runs - just rearrange widgets as needed.
     */
    private void setupDashboard() {
        // ==================== Shooter Test Tab (via Dash) ====================
        Dash.useTab("Shooter Test");
        shooter.setupTunables();  // registers all tunable GenericEntry fields

        // Telemetry (auto-updating)
        Dash.add("Left RPS", shooter::getLeftRPS);
        Dash.add("Right RPS", shooter::getRightRPS);
        Dash.add("Target RPS", shooter::getTargetRPS);
        Dash.add("Turret Angle", shooter::getTurretAngleDegrees);
        Dash.add("At Speed", shooter::isAtSpeed);
        Dash.add("Mode", () -> shooter.isUsingInterpolationMode() ? "TABLE" : "CALCULATOR");
        Dash.add("Table RPS", () -> shooter.getRPSFromTable(shooter.getTunableDistance()));
        Dash.add("Calc RPS", () -> shooter.getRPSFromCalculator(shooter.getTunableDistance()));
        Dash.add("Flywheel Amps", () -> RobotMap.shooterLeftMotor.getCurrentDraw().in(Amps));
        Dash.add("Turret Amps", () -> RobotMap.turretMotor.getCurrentDraw().in(Amps));
        Dash.add("Flywheel Temp", () -> RobotMap.shooterLeftMotor.getTemperature());

        // Commands
        Dash.addCommand("Spin 50 RPS", shooter.spinUpCommand(50));
        Dash.addCommand("Spin 75 RPS", shooter.spinUpCommand(75));
        Dash.addCommand("Spin For Distance", shooter.spinForDistanceCommand());
        Dash.addCommand("Stop Flywheel", shooter.stopFlywheelCommand());
        Dash.addCommand("Turret 0 deg", shooter.aimTurretCommand(0));
        Dash.addCommand("Turret 90 deg", shooter.aimTurretCommand(90));
        Dash.addCommand("Turret 180 deg", shooter.aimTurretCommand(180));
        Dash.addCommand("Aim Turret Manual", shooter.aimTurretManualCommand());
        Dash.addCommand("Stop Turret", shooter.stopTurretCommand());
        Dash.addCommand("Toggle Mode", shooter.toggleModeCommand());
    }

    /**
     * Get the autonomous command (not typically used on test board).
     */
    public Command getAutonomousCommand() {
        return Commands.none();
    }
}
