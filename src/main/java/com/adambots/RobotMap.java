package com.adambots;

/**
 * RobotMap defines all hardware port assignments for TestBoard.
 *
 * This is a subsystem testing platform - add your motor CAN IDs here.
 *
 * CAN ID Assignment Guidelines:
 *   0-9:   Reserved for drivetrain (if ever added)
 *   10-19: Intake/indexer mechanisms
 *   20-29: Shooter/launcher mechanisms
 *   30-39: Arm/elevator mechanisms
 *   40-49: Climber mechanisms
 *   50+:   Miscellaneous
 */
public class RobotMap {

    // ==================== Shooter Prototype ====================
    // Two Kraken X60 motors for shooter wheels (left is leader, right follows)
    public static final int kShooterLeftPort = 21;   // Kraken X60
    public static final int kShooterRightPort = 22;  // Kraken X60 (follower)

    // Kraken X44 for uptake/feeder
    public static final int kUptakePort = 20;        // Kraken X44

    // ==================== Add Your Motors Here ====================
    // Example:
    // public static final int kIntakePort = 10;
    // public static final int kArmLeftPort = 30;
    // public static final int kArmRightPort = 31;
}
