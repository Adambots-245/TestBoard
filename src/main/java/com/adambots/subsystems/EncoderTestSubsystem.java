package com.adambots.subsystems;

import static edu.wpi.first.units.Units.*;

import com.adambots.Constants.EncoderTestConstants;
import com.adambots.lib.actuators.BaseMotor;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Test subsystem for validating a REV Through Bore encoder plugged directly
 * into a TalonFXS (MinionMotor) data port via pulse-width feedback.
 *
 * After configureExternalPulseWidthSensor(), getPosition() returns the
 * absolute encoder reading — no separate RIO wiring needed.
 */
public class EncoderTestSubsystem extends SubsystemBase {

    private final BaseMotor motor;
    private double openLoopSpeed = EncoderTestConstants.kOpenLoopSpeed;

    public EncoderTestSubsystem(BaseMotor motor) {
        this.motor = motor;
        configureMotor();
    }

    private void configureMotor() {
        motor.setBrakeMode(true);
        motor.configureExternalPulseWidthSensor(
            EncoderTestConstants.kDefaultSensorRatio,
            EncoderTestConstants.kDefaultOffset,
            EncoderTestConstants.kDefaultDiscontinuity);
    }

    /**
     * Re-apply pulse-width sensor config with new parameters.
     * Call from Shuffleboard "Apply Config" button after changing tunables.
     */
    public void applyConfig(double sensorRatio, double offset, double discontinuity) {
        motor.configureExternalPulseWidthSensor(sensorRatio, offset, discontinuity);
    }

    public void setOpenLoopSpeed(double speed) {
        this.openLoopSpeed = speed;
    }

    public void runForward() {
        motor.set(openLoopSpeed);
    }

    public void runReverse() {
        motor.set(-openLoopSpeed);
    }

    public void stop() {
        motor.set(0);
    }

    // ==================== Getters ====================

    public double getPosition() {
        return motor.getPosition();
    }

    public double getVelocityRPS() {
        return motor.getVelocity().in(RotationsPerSecond);
    }

    public double getDutyCycle() {
        return motor.getOutputPercent();
    }

    public double getCurrentAmps() {
        return motor.getCurrentDraw().in(Amps);
    }

    // ==================== Command Factory Methods ====================

    public Command runForwardCommand() {
        return runEnd(this::runForward, this::stop).withName("Run Forward");
    }

    public Command runReverseCommand() {
        return runEnd(this::runReverse, this::stop).withName("Run Reverse");
    }

    public Command stopCommand() {
        return runOnce(this::stop).withName("Stop");
    }

    @Override
    public void periodic() {
        // Telemetry handled via Shuffleboard lambdas in RobotContainer
    }
}
