package com.adambots;

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

import com.adambots.subsystems.ShooterSubsystem;

/**
 * RobotContainer for TestBoard - a subsystem testing platform.
 *
 * This is designed for testing prototype mechanisms using Shuffleboard.
 * Each subsystem gets its own tab with commands pre-arranged.
 *
 * To add a new subsystem:
 * 1. Create the subsystem class in the subsystems package
 * 2. Instantiate it here
 * 3. Create a Shuffleboard tab and add commands in setupDashboard()
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

        // Setup Shuffleboard tabs with commands
        setupDashboard();
    }

    /**
     * Create Shuffleboard tabs for each subsystem with commands pre-arranged.
     * Tabs persist between runs - just rearrange widgets as needed.
     */
    private void setupDashboard() {
        // ==================== Shooter Tab ====================
        ShuffleboardTab shooterTab = Shuffleboard.getTab("Shooter");

        // Shooter wheel commands (row 0)
        shooterTab.add("Run Shooter", shooter.runShooterCommand())
            .withPosition(0, 0).withSize(2, 1);
        shooterTab.add("Stop Shooter", shooter.stopShooterCommand())
            .withPosition(2, 0).withSize(2, 1);
        shooterTab.add("Reverse Shooter", shooter.reverseShooterCommand())
            .withPosition(4, 0).withSize(2, 1);

        // Uptake commands (row 1)
        shooterTab.add("Run Uptake", shooter.runUptakeCommand())
            .withPosition(0, 1).withSize(2, 1);
        shooterTab.add("Stop Uptake", shooter.stopUptakeCommand())
            .withPosition(2, 1).withSize(2, 1);
        shooterTab.add("Reverse Uptake", shooter.reverseUptakeCommand())
            .withPosition(4, 1).withSize(2, 1);

        // Subsystem status (row 2)
        shooterTab.add("Shooter Subsystem", shooter)
            .withPosition(0, 2).withSize(3, 2);

        // ==================== Add Your Subsystem Tabs Here ====================
        // Example:
        // ShuffleboardTab intakeTab = Shuffleboard.getTab("Intake");
        // intakeTab.add("Run Intake", intake.runIntakeCommand())
        //     .withPosition(0, 0).withSize(2, 1);
        // intakeTab.add("Stop Intake", intake.stopIntakeCommand())
        //     .withPosition(2, 0).withSize(2, 1);
        // intakeTab.add("Intake Subsystem", intake)
        //     .withPosition(0, 1).withSize(3, 2);
    }

    /**
     * Get the autonomous command (not typically used on test board).
     */
    public Command getAutonomousCommand() {
        return Commands.none();
    }
}
