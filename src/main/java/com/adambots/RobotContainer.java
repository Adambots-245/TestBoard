package com.adambots;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

import com.adambots.subsystems.ShooterSubsystem;

/**
 * RobotContainer for TestBoard - a subsystem testing platform.
 *
 * This is designed for testing prototype mechanisms using Shuffleboard.
 * All commands are exposed on SmartDashboard so you can run them from
 * Shuffleboard without needing a controller.
 *
 * To add a new subsystem:
 * 1. Create the subsystem class in the subsystems package
 * 2. Instantiate it here
 * 3. Add its commands to SmartDashboard in setupDashboard()
 */
public class RobotContainer {

    // ==================== Subsystems ====================
    private final ShooterSubsystem shooter;

    // Add your subsystems here:
    // private final IntakeSubsystem intake;
    // private final ArmSubsystem arm;

    public RobotContainer() {
        // Initialize subsystems
        shooter = new ShooterSubsystem();

        // Add your subsystem initialization here:
        // intake = new IntakeSubsystem();
        // arm = new ArmSubsystem();

        // Setup SmartDashboard commands for Shuffleboard testing
        setupDashboard();
    }

    /**
     * Add commands to SmartDashboard for Shuffleboard testing.
     *
     * Commands will appear under their group name (e.g., "Shooter/Run Shooter").
     * You can drag these onto your Shuffleboard layout and click to run them.
     */
    private void setupDashboard() {
        // ==================== Shooter Commands ====================
        SmartDashboard.putData("Shooter/Run Shooter", shooter.runShooterCommand());
        SmartDashboard.putData("Shooter/Stop Shooter", shooter.stopShooterCommand());
        SmartDashboard.putData("Shooter/Reverse Shooter", shooter.reverseShooterCommand());
        SmartDashboard.putData("Shooter/Run Uptake", shooter.runUptakeCommand());
        SmartDashboard.putData("Shooter/Stop Uptake", shooter.stopUptakeCommand());
        SmartDashboard.putData("Shooter/Reverse Uptake", shooter.reverseUptakeCommand());

        // Add the subsystem itself for Shuffleboard to show default command status
        SmartDashboard.putData("Shooter", shooter);

        // ==================== Add Your Subsystem Commands Here ====================
        // Example:
        // SmartDashboard.putData("Intake/Run Intake", intake.runIntakeCommand());
        // SmartDashboard.putData("Intake/Stop Intake", intake.stopIntakeCommand());
        // SmartDashboard.putData("Intake", intake);
    }

    /**
     * Get the autonomous command (not typically used on test board).
     */
    public Command getAutonomousCommand() {
        return Commands.none();
    }
}
