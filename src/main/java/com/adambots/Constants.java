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
     * Shooter constants for shooter wheel motors.
     */
    public static final class ShooterConstants {
        // Motor speed (duty cycle -1.0 to 1.0)
        public static final double kShooterSpeed = 0.75;

        // Current limit
        public static final Current kShooterCurrentLimit = Amps.of(60);
    }

    /**
     * Hopper constants for uptake and carousel motors.
     */
    public static final class HopperConstants {
        // Motor speed (duty cycle -1.0 to 1.0)
        public static final double kUptakeSpeed = 0.5;
        public static final double kCarouselSpeed = 0.3; // Slower to feed ball to uptake

        // Current limit
        public static final Current kUptakeCurrentLimit = Amps.of(40);
        public static final Current kCarouselCurrentLimit = Amps.of(40);
    }

    public static final class IntakeConstants {
        public static final int kIntakePort = 20; // Kraken X44
        public static final int kIntakeArmPort = 23; // Minion
        
        public static final Current kIntakeCurrentLimit = Amps.of(40);
        public static final Current kIntakeArmCurrentLimit = Amps.of(40);

        public static final double kIntakeSpeed = 0.5;
        public static final double kIntakeArmSpeed = 0.5;

        public static final double P = 0.05;
        public static final double I = 0;
        public static final double D = 0;
        public static final double F = 0.12;

        public static final double kLowSpeed = 0.3;
        public static final double kHighSpeed = 0.5; 

        public static final double kMinDegrees = 0.0;
        public static final double kMaxDegrees = 90;

        public static final double kUpperLimit = (kMaxDegrees / 360);
        public static final double kLowerLimit = (kMinDegrees / 360);

        public static final int kStall = 20;
        public static final int kRPM = 3000;
    }

    // ==================== Add Your Constants Here ====================
    // Example:
    // public static final class IntakeConstants {
    // public static final double kIntakeSpeed = 0.8;
    // public static final Current kIntakeCurrentLimit = Amps.of(30);
    // }
    //
    // public static final class ArmConstants {
    // public static final Angle kMinAngle = Degrees.of(0);
    // public static final Angle kMaxAngle = Degrees.of(90);
    // public static final double kP = 1.0;
    // public static final double kI = 0.0;
    // public static final double kD = 0.0;
    // }
}
