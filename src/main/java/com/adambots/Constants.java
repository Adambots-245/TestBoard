package com.adambots;

import static edu.wpi.first.units.Units.*;
import edu.wpi.first.units.measure.*;

/**
 * Constants for TestBoard subsystem testing platform.
 *
 * Add constants for your prototype mechanisms here.
 * Use WPILib Units library for type-safe measurements.
 */
public final class Constants {

    /**
     * Shooter prototype constants.
     *
     * Two Kraken X60 motors for shooter wheels + one Kraken X44 for uptake.
     */
    public static final class ShooterConstants {
        // Motor speeds (duty cycle -1.0 to 1.0)
        public static final double kShooterSpeed = 0.75;
        public static final double kUptakeSpeed = 0.5;

        // Current limits
        public static final Current kShooterCurrentLimit = Amps.of(60);
        public static final Current kUptakeCurrentLimit = Amps.of(40);
    }

    // ==================== Add Your Constants Here ====================
    // Example:
    // public static final class IntakeConstants {
    //     public static final double kIntakeSpeed = 0.8;
    //     public static final Current kIntakeCurrentLimit = Amps.of(30);
    // }
    //
    // public static final class ArmConstants {
    //     public static final Angle kMinAngle = Degrees.of(0);
    //     public static final Angle kMaxAngle = Degrees.of(90);
    //     public static final double kP = 1.0;
    //     public static final double kI = 0.0;
    //     public static final double kD = 0.0;
    // }
}
