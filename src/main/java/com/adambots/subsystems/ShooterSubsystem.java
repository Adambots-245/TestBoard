package com.adambots.subsystems;

import static edu.wpi.first.units.Units.RPM;

import com.adambots.Constants.ShooterConstants;
import com.adambots.RobotMap;
import com.adambots.lib.actuators.BaseMotor;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Shooter subsystem with two motors (left/right) for shooter wheels.
 */
public class ShooterSubsystem extends SubsystemBase {

    private final BaseMotor leftMotor;
    private final BaseMotor rightMotor;

    public ShooterSubsystem(BaseMotor leftMotor, BaseMotor rightMotor) {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        configureMotors();
    }

    private void configureMotors() {
        leftMotor.setBrakeMode(false);  // Coast mode for shooter wheels
        rightMotor.setBrakeMode(false);

        // Right motor follows left motor in opposite direction (for shooter wheels)
        rightMotor.setInverted(true);
        rightMotor.setStrictFollower(RobotMap.kShooterLeftPort);
    }

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
     * Get the left shooter motor RPM.
     */
    public double getLeftRPM() {
        return leftMotor.getVelocity().in(RPM);
    }

    /**
     * Get the right shooter motor RPM.
     */
    public double getRightRPM() {
        return rightMotor.getVelocity().in(RPM);
    }

    // ==================== Command Factory Methods ====================

    /**
     * Command to run the shooter while held.
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
     */
    public Command stopShooterCommand() {
        return runOnce(this::stopShooter)
            .withName("Stop Shooter");
    }

    @Override
    public void periodic() {
        // Add telemetry here if needed
    }
}
