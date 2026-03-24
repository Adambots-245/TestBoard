package com.adambots;

import com.adambots.lib.actuators.BaseMotor;
import com.adambots.lib.actuators.MinionMotor;

/**
 * RobotMap defines all hardware port assignments and motor instances for TestBoard.
 *
 * This is a subsystem testing platform - add your motor CAN IDs here.
 * Motors are created here and passed to subsystems via constructor injection.
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

    // Encoder Test (MinionMotor / TalonFXS with ThroughBore on data port)
    public static final int kEncoderTestPort = 30;
    public static final BaseMotor encoderTestMotor = new MinionMotor(kEncoderTestPort);
}
