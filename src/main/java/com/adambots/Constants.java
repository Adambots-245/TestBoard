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

        // Onboard PID gains (TalonFX 1kHz loop, MotionMagicVoltage)
        // All feedforward gains are in Volts (since we use voltage-based control)
        // Tuning order: kG first (hold horizontal), then kS, kP, kD
        public static final double kArmP = 4.8;    // Volts per rotation of error
        public static final double kArmI = 0.0;     // Volts per rotation*second of error (almost never needed with proper kG)
        public static final double kArmD = 0.1;     // Volts per rotation/second of error (damping)
        public static final double kArmKV = 0.12;   // Volts per rotation/second of velocity
        public static final double kArmKS = 0.25;   // Volts to overcome static friction
        public static final double kArmKA = 0.01;   // Volts per rotation/second^2 of acceleration
        public static final double kArmKG = 0.35;   // Volts to hold arm horizontal (tune with Phoenix Tuner X)

        // Motion Magic profile constraints
        public static final double kArmCruiseVelocity = 2.0;  // rotations per second
        public static final double kArmAcceleration = 1.0;     // rotations per second^2
        public static final double kArmJerk = 0.0;             // 0 = no jerk limiting

        // Intake arm gear ratio — two-stage reduction (motor → mechanism)
        // Stage 1: Planetary gearbox on Minion motor
        // Stage 2: Belt-driven pulley from gearbox output to arm pivot
        // Total ratio = stage1 * stage2 (motor rotations per mechanism rotation)
        // TODO: Get actual values from mech team
        public static final double kArmPlanetaryRatio = 20.0;  // e.g., 5.0 for 5:1 planetary
        public static final double kArmBeltRatio = 2.0;        // e.g., 2.0 for 36T:18T belt
        public static final double kArmTotalGearRatio = kArmPlanetaryRatio * kArmBeltRatio;

        public static final double kLowSpeed = 0.3;
        public static final double kHighSpeed = 0.5;

        public static final double kMinDegrees = 0.0;
        public static final double kMaxDegrees = 90;

        public static final double kArmRaisedPosition = -7.0;  // motor rotations when arm is raised
        public static final double kArmLoweredPosition = 0.0; // motor rotations when arm is lowered (home)

        // Arm motor current limits
        public static final int kArmStatorCurrentLimit = 40;  // stator amps (torque limiting)
        public static final int kArmSupplyCurrentLimit = 30;  // supply amps (must be ≤ PDH breaker)

        public static final int kLimitSwitchPort = 0; // DIO port for arm limit switch
    }

    /**
     * Simulation constants — approximate values for code testing, not hardware tuning.
     */
    public static final class SimConstants {
        public static final double kArmLengthMeters = 0.4;
        public static final double kArmMassKg = 2.0;
        public static final double kArmMinAngleRad = Math.toRadians(-10);   // slightly below horizontal
        public static final double kArmMaxAngleRad = Math.toRadians(100);   // just past vertical
        public static final double kArmStartAngleRad = Math.toRadians(0);   // start horizontal (lowered)

        // Estimated physical gear ratio for sim only.
        // Inferred from: -7.0 motor rotations ≈ 90° arm travel → ratio ≈ 28:1
        // This is separate from IntakeConstants.kArmTotalGearRatio (which controls
        // sensorToMechanismRatio and must stay 1.0 until position targets are recalibrated).
        public static final double kSimGearRatio = 28.0;
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
