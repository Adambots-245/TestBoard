package com.adambots;

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

// import com.adambots.Constants.EncoderTestConstants;
// import com.adambots.subsystems.EncoderTestSubsystem;
import com.adambots.subsystems.RawEncoderTestSubsystem;

// import edu.wpi.first.networktables.GenericEntry;

/**
 * RobotContainer for TestBoard - a subsystem testing platform.
 */
public class RobotContainer {

    // ==================== Subsystems ====================
    // Lib-based subsystem (commented out for debugging)
    // private final EncoderTestSubsystem encoderTest;

    // Raw Phoenix 6 subsystem (bypasses lib)
    private final RawEncoderTestSubsystem rawEncoder;

    // Encoder test tunables
    // private GenericEntry sensorRatioEntry;
    // private GenericEntry offsetEntry;
    // private GenericEntry discontinuityEntry;
    // private GenericEntry openLoopSpeedEntry;

    public RobotContainer() {
        // encoderTest = new EncoderTestSubsystem(RobotMap.encoderTestMotor);
        rawEncoder = new RawEncoderTestSubsystem(RobotMap.kEncoderTestPort);

        setupDashboard();
    }

    private void setupDashboard() {
        // ==================== Raw Encoder Test Tab ====================
        ShuffleboardTab rawTab = Shuffleboard.getTab("Raw Encoder Test");

        // Row 0: All position signals side by side
        rawTab.addNumber("Position (rot)", rawEncoder::getPosition)
            .withPosition(0, 0).withSize(2, 1);
        rawTab.addNumber("Rotor Position (rot)", rawEncoder::getRotorPosition)
            .withPosition(2, 0).withSize(2, 1);
        rawTab.addNumber("Raw PW Position (rot)", rawEncoder::getRawPulseWidthPosition)
            .withPosition(4, 0).withSize(2, 1);
        rawTab.addNumber("Velocity (RPS)", rawEncoder::getVelocity)
            .withPosition(6, 0).withSize(2, 1);

        // Row 1: Config status
        rawTab.addString("Config Status", rawEncoder::getConfigStatus)
            .withPosition(0, 1).withSize(4, 1);
    }

    public Command getAutonomousCommand() {
        return Commands.none();
    }
}
