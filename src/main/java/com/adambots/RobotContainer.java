package com.adambots;

import static edu.wpi.first.units.Units.*;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

import com.adambots.commands.ShootCommand;
import com.adambots.lib.utils.Dash;
import com.adambots.subsystems.ShooterSubsystem;
import com.adambots.subsystems.UptakeSubsystem;

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

    public RobotContainer() {
        // Initialize subsystems with motors from RobotMap
        shooter = new ShooterSubsystem(RobotMap.shooterLeftMotor, RobotMap.shooterRightMotor, RobotMap.turretMotor);
        uptake = new UptakeSubsystem(RobotMap.uptakeMotor);

        // Setup Shuffleboard tabs with commands
        setupDashboard();
    }

    /**
     * Create Shuffleboard tabs for each subsystem with commands pre-arranged.
     * Tabs persist between runs - just rearrange widgets as needed.
     */
    private void setupDashboard() {
        // ==================== Flywheel Tab (via Dash) ====================
        Dash.useTab("Flywheel");
        shooter.setupFlywheelTunables();

        // Telemetry
        Dash.add("Left RPS", shooter::getLeftRPS);
        Dash.add("Right RPS", shooter::getRightRPS);
        Dash.add("Target RPS", shooter::getTargetRPS);
        Dash.add("At Speed", shooter::isAtSpeed);
        Dash.add("Mode", () -> shooter.isUsingInterpolationMode() ? "TABLE" : "CALCULATOR");
        Dash.add("Table RPS", () -> shooter.getRPSFromTable(shooter.getTunableDistance()));
        Dash.add("Calc RPS", () -> shooter.getRPSFromCalculator(shooter.getTunableDistance()));
        Dash.add("Flywheel Amps", () -> RobotMap.shooterLeftMotor.getCurrentDraw().in(Amps));
        Dash.add("Flywheel Temp", () -> RobotMap.shooterLeftMotor.getTemperature());
        Dash.add("Duty Cycle", () -> RobotMap.shooterLeftMotor.getOutputPercent());
        Dash.add("Left Position", () -> RobotMap.shooterLeftMotor.getPosition());

        // Commands
        Dash.addCommand("Spin 50 RPS", shooter.spinUpCommand(50));
        Dash.addCommand("Spin 75 RPS", shooter.spinUpCommand(75));
        Dash.addCommand("Spin For Distance", shooter.spinForDistanceCommand());
        Dash.addCommand("Stop Flywheel", shooter.stopFlywheelCommand());
        Dash.addCommand("Toggle Mode", shooter.toggleModeCommand());

        // Debug commands
        Dash.addCommand("Open Loop +50%", shooter.openLoopForwardCommand());
        Dash.addCommand("Open Loop -50%", shooter.openLoopReverseCommand());
        Dash.addCommand("Spin -75 RPS", shooter.spinNegativeCommand(75));

        // ==================== Turret Tab (via Dash) ====================
        Dash.useTab("Turret");
        shooter.setupTurretTunables();

        // Telemetry
        Dash.add("Turret Angle", shooter::getTurretAngleDegrees);
        Dash.add("Turret Amps", () -> RobotMap.turretMotor.getCurrentDraw().in(Amps));

        // Commands
        Dash.addCommand("Turret 0 deg", shooter.aimTurretCommand(0));
        Dash.addCommand("Turret 90 deg", shooter.aimTurretCommand(90));
        Dash.addCommand("Turret 180 deg", shooter.aimTurretCommand(180));
        Dash.addCommand("Aim Turret Manual", shooter.aimTurretManualCommand());
        Dash.addCommand("Stop Turret", shooter.stopTurretCommand());

        // ==================== Uptake Tab (via Dash) ====================
        Dash.useTab("Uptake");

        // Telemetry
        Dash.add("Uptake RPM", uptake::getUptakeRPM);
        Dash.add("Uptake Amps", () -> RobotMap.uptakeMotor.getCurrentDraw().in(Amps));

        // Commands
        Dash.addCommand("Run Uptake", uptake.runUptakeCommand());
        Dash.addCommand("Reverse Uptake", uptake.reverseUptakeCommand());
        Dash.addCommand("Stop Uptake", uptake.stopUptakeCommand());
        Dash.addCommand("Shoot With Uptake", ShootCommand.shootWithUptake(shooter, uptake));
        Dash.addCommand("Reverse All", ShootCommand.reverseAll(shooter, uptake));
        Dash.addCommand("Stop All", ShootCommand.stopAll(shooter, uptake));
    }

    /**
     * Get the autonomous command (not typically used on test board).
     */
    public Command getAutonomousCommand() {
        return Commands.none();
    }
}
