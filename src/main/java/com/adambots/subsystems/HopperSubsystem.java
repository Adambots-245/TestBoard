package com.adambots.subsystems;

import static edu.wpi.first.units.Units.RPM;

import com.adambots.Constants.HopperConstants;
import com.adambots.lib.actuators.BaseMotor;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Hopper subsystem with uptake and carousel motors for feeding game pieces.
 */
public class HopperSubsystem extends SubsystemBase {

    private final BaseMotor uptakeMotor;
    private final BaseMotor carouselMotor;

    public HopperSubsystem(BaseMotor uptakeMotor, BaseMotor carouselMotor) {
        this.uptakeMotor = uptakeMotor;
        this.carouselMotor = carouselMotor;
        configureMotors();
    }

    private void configureMotors() {
        uptakeMotor.setBrakeMode(true);
        carouselMotor.setBrakeMode(true);
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

    /**
     * Run the carousel at the configured speed.
     */
    public void runCarousel() {
        carouselMotor.set(HopperConstants.kCarouselSpeed);
    }

    /**
     * Run the carousel in reverse.
     */
    public void reverseCarousel() {
        carouselMotor.set(-HopperConstants.kCarouselSpeed);
    }

    /**
     * Stop the carousel motor.
     */
    public void stopCarousel() {
        carouselMotor.set(0);
    }

    /**
     * Get the uptake motor RPM.
     */
    public double getUptakeRPM() {
        return uptakeMotor.getVelocity().in(RPM);
    }

    /**
     * Get the carousel motor RPM.
     */
    public double getCarouselRPM() {
        return carouselMotor.getVelocity().in(RPM);
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

    /**
     * Command to run the carousel while held.
     */
    public Command runCarouselCommand() {
        return runEnd(this::runCarousel, this::stopCarousel)
            .withName("Run Carousel");
    }

    /**
     * Command to reverse the carousel while held.
     */
    public Command reverseCarouselCommand() {
        return runEnd(this::reverseCarousel, this::stopCarousel)
            .withName("Reverse Carousel");
    }

    /**
     * Command to stop the carousel (instant).
     */
    public Command stopCarouselCommand() {
        return runOnce(this::stopCarousel)
            .withName("Stop Carousel");
    }

    /**
     * Command to run both carousel and uptake together while held.
     */
    public Command runHopperCommand() {
        return runEnd(
            () -> { runCarousel(); runUptake(); },
            () -> { stopCarousel(); stopUptake(); }
        ).withName("Run Hopper");
    }

    /**
     * Command to reverse both carousel and uptake together while held.
     */
    public Command reverseHopperCommand() {
        return runEnd(
            () -> { reverseCarousel(); reverseUptake(); },
            () -> { stopCarousel(); stopUptake(); }
        ).withName("Reverse Hopper");
    }

    /**
     * Command to stop both carousel and uptake (instant).
     */
    public Command stopHopperCommand() {
        return runOnce(() -> { stopCarousel(); stopUptake(); })
            .withName("Stop Hopper");
    }

    @Override
    public void periodic() {
        // Add telemetry here if needed
    }
}
