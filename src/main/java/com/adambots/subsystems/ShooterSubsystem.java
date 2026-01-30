package com.adambots.subsystems;

import com.adambots.Constants.ShooterConstants;
import com.adambots.RobotMap;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Shooter prototype subsystem with two Kraken X60 motors (left/right)
 * and one Kraken X44 uptake motor.
 *
 * This serves as a template for creating other subsystems.
 *
 * Key concepts demonstrated:
 * - Motor configuration with current limits
 * - Leader/follower motor setup
 * - Command factory methods (runShooterCommand, etc.)
 * - Using runEnd() for "while held" commands
 * - Using runOnce() for instant commands
 */
public class ShooterSubsystem extends SubsystemBase {

    // Motors
    private final TalonFX leftMotor;
    private final TalonFX rightMotor;
    private final TalonFX uptakeMotor;

    public ShooterSubsystem() {
        // Create motor controllers
        leftMotor = new TalonFX(RobotMap.kShooterLeftPort);
        rightMotor = new TalonFX(RobotMap.kShooterRightPort);
        uptakeMotor = new TalonFX(RobotMap.kUptakePort);

        // Configure motors
        configureMotors();
    }

    /**
     * Configure all motors with current limits and control modes.
     */
    private void configureMotors() {
        // Configure left motor (leader)
        TalonFXConfiguration leftConfig = new TalonFXConfiguration();
        leftConfig.CurrentLimits.SupplyCurrentLimit = ShooterConstants.kShooterCurrentLimit.in(edu.wpi.first.units.Units.Amps);
        leftConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
        leftConfig.MotorOutput.NeutralMode = NeutralModeValue.Coast;
        leftMotor.getConfigurator().apply(leftConfig);

        // Configure right motor (follower)
        TalonFXConfiguration rightConfig = new TalonFXConfiguration();
        rightConfig.CurrentLimits.SupplyCurrentLimit = ShooterConstants.kShooterCurrentLimit.in(edu.wpi.first.units.Units.Amps);
        rightConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
        rightConfig.MotorOutput.NeutralMode = NeutralModeValue.Coast;
        rightMotor.getConfigurator().apply(rightConfig);

        // Set right motor to follow left motor (opposed direction for shooter wheels)
        rightMotor.setControl(new Follower(RobotMap.kShooterLeftPort, MotorAlignmentValue.Opposed));

        // Configure uptake motor
        TalonFXConfiguration uptakeConfig = new TalonFXConfiguration();
        uptakeConfig.CurrentLimits.SupplyCurrentLimit = ShooterConstants.kUptakeCurrentLimit.in(edu.wpi.first.units.Units.Amps);
        uptakeConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
        uptakeConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;
        uptakeMotor.getConfigurator().apply(uptakeConfig);
    }

    // ==================== Basic Motor Control Methods ====================

    /**
     * Run the shooter at the configured speed.
     */
    public void runShooter() {
        leftMotor.set(ShooterConstants.kShooterSpeed);
    }

    /**
     * Run the shooter in reverse.
     */
    public void reverseShooter() {
        leftMotor.set(-ShooterConstants.kShooterSpeed);
    }

    /**
     * Stop the shooter motors.
     */
    public void stopShooter() {
        leftMotor.set(0);
    }

    /**
     * Run the uptake at the configured speed.
     */
    public void runUptake() {
        uptakeMotor.set(ShooterConstants.kUptakeSpeed);
    }

    /**
     * Run the uptake in reverse.
     */
    public void reverseUptake() {
        uptakeMotor.set(-ShooterConstants.kUptakeSpeed);
    }

    /**
     * Stop the uptake motor.
     */
    public void stopUptake() {
        uptakeMotor.set(0);
    }

    // ==================== Command Factory Methods ====================
    // These return Command objects that can be bound to buttons or SmartDashboard

    /**
     * Command to run the shooter while held.
     * Uses runEnd() - runs the first method continuously, calls second method when interrupted.
     */
    public Command runShooterCommand() {
        return runEnd(this::runShooter, this::stopShooter)
            .withName("Run Shooter");
    }

    /**
     * Command to reverse the shooter while held.
     */
    public Command reverseShooterCommand() {
        return runEnd(this::reverseShooter, this::stopShooter)
            .withName("Reverse Shooter");
    }

    /**
     * Command to stop the shooter (instant).
     * Uses runOnce() - runs the method once and finishes immediately.
     */
    public Command stopShooterCommand() {
        return runOnce(this::stopShooter)
            .withName("Stop Shooter");
    }

    /**
     * Command to run the uptake while held.
     */
    public Command runUptakeCommand() {
        return runEnd(this::runUptake, this::stopUptake)
            .withName("Run Uptake");
    }

    /**
     * Command to reverse the uptake while held.
     */
    public Command reverseUptakeCommand() {
        return runEnd(this::reverseUptake, this::stopUptake)
            .withName("Reverse Uptake");
    }

    /**
     * Command to stop the uptake (instant).
     */
    public Command stopUptakeCommand() {
        return runOnce(this::stopUptake)
            .withName("Stop Uptake");
    }

    @Override
    public void periodic() {
        // Add telemetry here if needed
        // Example:
        // SmartDashboard.putNumber("Shooter/Left Velocity", leftMotor.getVelocity().getValueAsDouble());
        // SmartDashboard.putNumber("Shooter/Left Current", leftMotor.getSupplyCurrent().getValueAsDouble());
    }
}
