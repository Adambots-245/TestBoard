package com.adambots.subsystems;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.ExternalFeedbackConfigs;
import com.ctre.phoenix6.configs.TalonFXSConfiguration;
import com.ctre.phoenix6.configs.CommutationConfigs;
import com.ctre.phoenix6.hardware.TalonFXS;
import com.ctre.phoenix6.signals.ExternalFeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.MotorArrangementValue;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Direct Phoenix 6 test subsystem — bypasses AdambotsLib entirely.
 * Uses raw TalonFXS API to configure and read a Through Bore encoder
 * on the data port via pulse-width feedback.
 */
public class RawEncoderTestSubsystem extends SubsystemBase {

    private final TalonFXS motor;

    // Cached readings updated in periodic()
    private double position = 0;
    private double velocity = 0;
    private double rotorPosition = 0;
    private double rawPulseWidthPosition = 0;
    private String configStatus = "Not applied";

    public RawEncoderTestSubsystem(int canId) {
        motor = new TalonFXS(canId);

        // Factory reset
        StatusCode resetStatus = motor.getConfigurator().apply(new TalonFXSConfiguration(), 0.100);
        DriverStation.reportWarning("RawEncoder: Factory reset status: " + resetStatus, false);

        // Set motor arrangement
        CommutationConfigs commutation = new CommutationConfigs();
        commutation.MotorArrangement = MotorArrangementValue.Minion_JST;
        StatusCode commStatus = motor.getConfigurator().apply(commutation, 0.100);
        DriverStation.reportWarning("RawEncoder: Commutation status: " + commStatus, false);

        // Configure external pulse-width sensor
        ExternalFeedbackConfigs feedback = new ExternalFeedbackConfigs();
        feedback.ExternalFeedbackSensorSource = ExternalFeedbackSensorSourceValue.PulseWidth;
        feedback.SensorToMechanismRatio = 1.0;
        feedback.AbsoluteSensorOffset = 0.0;
        feedback.AbsoluteSensorDiscontinuityPoint = 1.0;
        StatusCode fbStatus = motor.getConfigurator().apply(feedback, 0.100);
        configStatus = "Feedback apply: " + fbStatus;
        DriverStation.reportWarning("RawEncoder: " + configStatus, false);

        // Verify config was applied by reading it back
        ExternalFeedbackConfigs readback = new ExternalFeedbackConfigs();
        StatusCode refreshStatus = motor.getConfigurator().refresh(readback, 0.100);
        DriverStation.reportWarning("RawEncoder: Readback status: " + refreshStatus
            + ", source: " + readback.ExternalFeedbackSensorSource, false);

        // Set update frequencies AFTER config
        motor.getPosition().setUpdateFrequency(50);
        motor.getVelocity().setUpdateFrequency(50);
        motor.getRotorPosition().setUpdateFrequency(50);
        motor.getRawPulseWidthPosition().setUpdateFrequency(50);
        motor.optimizeBusUtilization();
    }

    @Override
    public void periodic() {
        position = motor.getPosition().getValueAsDouble();
        velocity = motor.getVelocity().getValueAsDouble();
        rotorPosition = motor.getRotorPosition().getValueAsDouble();
        rawPulseWidthPosition = motor.getRawPulseWidthPosition().getValueAsDouble();
    }

    public double getPosition() { return position; }
    public double getVelocity() { return velocity; }
    public double getRotorPosition() { return rotorPosition; }
    public double getRawPulseWidthPosition() { return rawPulseWidthPosition; }
    public String getConfigStatus() { return configStatus; }
}
