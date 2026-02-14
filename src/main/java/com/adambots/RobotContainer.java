package com.adambots;

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

import com.adambots.commands.ShootCommand;
import com.adambots.subsystems.HopperSubsystem;
import com.adambots.subsystems.IntakeSubsystem;
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
    private final IntakeSubsystem intake;

    // Add your subsystems here:
    // private final IntakeSubsystem intake;
    // private final ArmSubsystem arm;

    public RobotContainer() {
        // Initialize subsystems with motors from RobotMap
        shooter = new ShooterSubsystem(RobotMap.shooterLeftMotor, RobotMap.shooterRightMotor);
        intake = new IntakeSubsystem(RobotMap.intakeMotor, RobotMap.intakeArmMotor);

        // Add your subsystem initialization here:
        // intake = new IntakeSubsystem(RobotMap.intakeMotor);
        // arm = new ArmSubsystem(RobotMap.armLeftMotor, RobotMap.armRightMotor);

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

        // RPM telemetry (row 1)
        shooterTab.addNumber("Left RPM", shooter::getLeftRPM)
            .withPosition(0, 1).withSize(2, 1);
        shooterTab.addNumber("Right RPM", shooter::getRightRPM)
            .withPosition(2, 1).withSize(2, 1);

        // Subsystem status (row 2)
        shooterTab.add("Shooter Subsystem", shooter)
            .withPosition(0, 2).withSize(3, 2);

        // ==================== Hopper Tab ====================
        // ShuffleboardTab hopperTab = Shuffleboard.getTab("Hopper");

        // Uptake commands (row 0)
        // hopperTab.add("Run Uptake", hopper.runUptakeCommand())
        //     .withPosition(0, 0).withSize(2, 1);
        // hopperTab.add("Stop Uptake", hopper.stopUptakeCommand())
        //     .withPosition(2, 0).withSize(2, 1);
        // hopperTab.add("Reverse Uptake", hopper.reverseUptakeCommand())
        //     .withPosition(4, 0).withSize(2, 1);

        // // Carousel commands (row 1)
        // hopperTab.add("Run Carousel", hopper.runCarouselCommand())
        //     .withPosition(0, 1).withSize(2, 1);
        // hopperTab.add("Stop Carousel", hopper.stopCarouselCommand())
        //     .withPosition(2, 1).withSize(2, 1);
        // hopperTab.add("Reverse Carousel", hopper.reverseCarouselCommand())
        //     .withPosition(4, 1).withSize(2, 1);

        // // Combined hopper commands (row 2)
        // hopperTab.add("Run Hopper", hopper.runHopperCommand())
        //     .withPosition(0, 2).withSize(2, 1);
        // hopperTab.add("Stop Hopper", hopper.stopHopperCommand())
        //     .withPosition(2, 2).withSize(2, 1);
        // hopperTab.add("Reverse Hopper", hopper.reverseHopperCommand())
        //     .withPosition(4, 2).withSize(2, 1);

        // // RPM telemetry (row 3)
        // hopperTab.addNumber("Uptake RPM", hopper::getUptakeRPM)
        //     .withPosition(0, 3).withSize(2, 1);
        // hopperTab.addNumber("Carousel RPM", hopper::getCarouselRPM)
        //     .withPosition(2, 3).withSize(2, 1);

        // // Subsystem status (row 4)
        // hopperTab.add("Hopper Subsystem", hopper)
        //     .withPosition(0, 4).withSize(3, 2);

        // // ==================== Combo Tab ====================
        // ShuffleboardTab comboTab = Shuffleboard.getTab("Combo");

        // // Combined commands (row 0)
        // comboTab.add("Shoot With Hopper", ShootCommand.shootWithHopper(shooter, hopper))
        //     .withPosition(0, 0).withSize(2, 1);
        // comboTab.add("Stop All", ShootCommand.stopAll(shooter, hopper))
        //     .withPosition(2, 0).withSize(2, 1);
        // comboTab.add("Reverse All", ShootCommand.reverseAll(shooter, hopper))
        //     .withPosition(4, 0).withSize(2, 1);

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
