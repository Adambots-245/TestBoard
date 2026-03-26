package com.adambots;

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

import com.adambots.Constants.EncoderTestConstants;
import com.adambots.subsystems.EncoderTestSubsystem;

import edu.wpi.first.networktables.GenericEntry;

/**
 * RobotContainer for TestBoard - a subsystem testing platform.
 *
 * This is designed for testing prototype mechanisms using Shuffleboard.
 * Each subsystem gets its own tab with commands pre-arranged.
 *
 * To add a new subsystem:
 * 1. Create the subsystem class in the subsystems package
 * 2. Create motors in RobotMap and pass them to the subsystem constructor
 * 3. Instantiate the subsystem here
 * 4. Create a Shuffleboard tab and add commands in setupDashboard()
 */
public class RobotContainer {

    // ==================== Subsystems ====================
    private final EncoderTestSubsystem encoderTest;

    // Encoder test tunables
    private GenericEntry sensorRatioEntry;
    private GenericEntry offsetEntry;
    private GenericEntry discontinuityEntry;
    private GenericEntry openLoopSpeedEntry;

    public RobotContainer() {
        encoderTest = new EncoderTestSubsystem(RobotMap.encoderTestMotor);

        // Setup Shuffleboard tabs with commands
        setupDashboard();
    }

    /**
     * Create Shuffleboard tabs for each subsystem with commands pre-arranged.
     * Tabs persist between runs - just rearrange widgets as needed.
     */
    private void setupDashboard() {
        // ==================== Encoder Test Tab ====================
        ShuffleboardTab encTab = Shuffleboard.getTab("Encoder Test");

        // Row 0: Tunables
        sensorRatioEntry = encTab.add("Sensor Ratio", EncoderTestConstants.kDefaultSensorRatio)
            .withPosition(0, 0).withSize(2, 1).getEntry();
        offsetEntry = encTab.add("Offset (rot)", EncoderTestConstants.kDefaultOffset)
            .withPosition(2, 0).withSize(2, 1).getEntry();
        discontinuityEntry = encTab.add("Discontinuity", EncoderTestConstants.kDefaultDiscontinuity)
            .withPosition(4, 0).withSize(2, 1).getEntry();
        openLoopSpeedEntry = encTab.add("Open Loop Speed", EncoderTestConstants.kOpenLoopSpeed)
            .withPosition(6, 0).withSize(2, 1).getEntry();

        // Row 1: Telemetry
        encTab.addNumber("Ext Position (rot)", encoderTest::getPosition)
            .withPosition(0, 1).withSize(2, 1);
        encTab.addNumber("Rotor Position (rot)", encoderTest::getRotorPosition)
            .withPosition(2, 1).withSize(2, 1);
        encTab.addNumber("Velocity (RPS)", encoderTest::getVelocityRPS)
            .withPosition(4, 1).withSize(2, 1);
        encTab.addNumber("Duty Cycle", encoderTest::getDutyCycle)
            .withPosition(6, 1).withSize(2, 1);
        encTab.addNumber("Current (A)", encoderTest::getCurrentAmps)
            .withPosition(8, 1).withSize(2, 1);

        // Row 2: Commands
        encTab.add("Run Forward", encoderTest.runForwardCommand())
            .withPosition(0, 2).withSize(2, 1);
        encTab.add("Run Reverse", encoderTest.runReverseCommand())
            .withPosition(2, 2).withSize(2, 1);
        encTab.add("Stop", encoderTest.stopCommand())
            .withPosition(4, 2).withSize(2, 1);
        encTab.add("Apply Config", Commands.runOnce(() -> {
            encoderTest.applyConfig(
                sensorRatioEntry.getDouble(EncoderTestConstants.kDefaultSensorRatio),
                offsetEntry.getDouble(EncoderTestConstants.kDefaultOffset),
                discontinuityEntry.getDouble(EncoderTestConstants.kDefaultDiscontinuity));
            encoderTest.setOpenLoopSpeed(
                openLoopSpeedEntry.getDouble(EncoderTestConstants.kOpenLoopSpeed));
        }).ignoringDisable(true).withName("Apply Config"))
            .withPosition(6, 2).withSize(2, 1);
    }

    /**
     * Get the autonomous command (not typically used on test board).
     */
    public Command getAutonomousCommand() {
        return Commands.none();
    }
}
