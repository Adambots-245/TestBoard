package com.adambots.subsystems;

import com.adambots.Constants.HopperConstants;
import com.adambots.lib.actuators.BaseMotor;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Hopper subsystem with uptake motor for feeding game pieces.
 */
public class HopperSubsystem extends SubsystemBase {

    private final BaseMotor uptakeMotor;

    public HopperSubsystem(BaseMotor uptakeMotor) {
        this.uptakeMotor = uptakeMotor;
        configureMotor();
    }

    private void configureMotor() {
        uptakeMotor.setBrakeMode(true);
    }

    /**
     * Run the uptake at the configured speed.
     */
    public void runUptake() {
        uptakeMotor.set(HopperConstants.kUptakeSpeed);
    }

    /**
     * Run the uptake in reverse.
     */
    public void reverseUptake() {
        uptakeMotor.set(-HopperConstants.kUptakeSpeed);
    }

    /**
     * Stop the uptake motor.
     */
    public void stopUptake() {
        uptakeMotor.set(0);
    }

    // ==================== Command Factory Methods ====================

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
    }
}
