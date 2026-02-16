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
        public static final int kIntakePort = 33; // Kraken X44
        public static final int kIntakeArmPort = 32; // Minion

        public static final Current kIntakeCurrentLimit = Amps.of(40);
        public static final Current kIntakeArmCurrentLimit = Amps.of(40);

        public static final double kIntakeSpeed = 0.5;
        public static final double kIntakeArmSpeed = 0.5;

        // Onboard PID gains (TalonFX 1kHz loop)
        // Tuning order: kG first (hold horizontal), then kS, kP, kD
        public static final double kArmP = 4.8;    // Proportional gain (start low, increase until arm tracks)
        public static final double kArmI = 0.0;     // Integral gain (almost never needed with proper kG)
        public static final double kArmD = 0.1;     // Derivative gain (damping to prevent oscillation)
        public static final double kArmKV = 0.12;   // Velocity feedforward
        public static final double kArmKS = 0.25;   // Static feedforward (overcomes friction)
        public static final double kArmKA = 0.01;   // Acceleration feedforward
        public static final double kArmKG = 0.15;   // Gravity feedforward (tune: hold arm horizontal, find voltage to hold steady)

        // Motion Magic profile constraints
        public static final double kArmCruiseVelocity = 2.0;  // rotations per second
        public static final double kArmAcceleration = 1.0;     // rotations per second^2
        public static final double kArmJerk = 0.0;             // 0 = no jerk limiting

        public static final double kIntakeMotorRatio = 24.0 / 36.0;
        public static final double kIntakeArmMotorRatio = 18.0 / 36.0;

        public static final double kLowSpeed = 0.3;
        public static final double kHighSpeed = 0.5;

        public static final double kMinDegrees = 0.0;
        public static final double kMaxDegrees = 90;

        public static final double kUpperLimit = -7.0; //(kMaxDegrees / 360.0) * kIntakeArmMotorRatio;
        public static final double kLowerLimit = 0; //(kMinDegrees / 360.0) * kIntakeArmMotorRatio;

        // Current limits
        public static final int kStall = 20;
        public static final int kRPM = 3200;

        public static final int kLimitSwitchPort = 0; // DIO port for arm limit switch
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
