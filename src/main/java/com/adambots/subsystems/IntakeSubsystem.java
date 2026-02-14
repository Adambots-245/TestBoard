package com.adambots.subsystems;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.RotationsPerSecond;

import com.adambots.Constants.HopperConstants;
import com.adambots.lib.Constants.IntakeConstants;
import com.adambots.lib.actuators.BaseMotor;
import com.adambots.lib.utils.Dash;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import groovyjarjarantlr4.v4.parse.ANTLRParser.finallyClause_return;

/**
 * Intake subsystem for intaking game peices.
 */
public class IntakeSubsystem extends SubsystemBase {

    private final BaseMotor intakeMotor;
    private final BaseMotor intakeArmMotor;
    private final Trigger isLowered = new Trigger(() -> getIntakeArmPosition() == 0);
    private final Trigger isRaised = new Trigger(() -> getIntakeArmPosition() == 90);

    public IntakeSubsystem(BaseMotor intakeMotor, BaseMotor intakeArmMotor) {
        this.intakeMotor = intakeMotor;
        this.intakeArmMotor = intakeArmMotor;
        configureMotors();
        setupDash();
    }

    private void configureMotors() {
        intakeMotor.setBrakeMode(false);
        intakeArmMotor.setBrakeMode(true);
    }

    private void setupDash() {
        Dash.add("IntakeMotor Speed", () -> intakeMotor.getVelocity().in(RotationsPerSecond));
        Dash.add("IntakeArmMotor Speed", () -> intakeMotor.getVelocity().in(RotationsPerSecond));
        Dash.add("IntakeMotor Position", () -> intakeMotor.getPosition());
        Dash.add("IntakeMotorArm Position", () -> intakeArmMotor.getPosition());
    }

    /**
     * Run the uptake at the configured speed.
     */
    public void runIntake() {
        intakeMotor.set(IntakeConstants.kLowSpeed);
    }

    /**
     * Run the uptake in reverse.
     */
    public void reverseIntake() {
        intakeMotor.set(-IntakeConstants.kLowSpeed);
    }

    /**
     * Stop the uptake motor.
     */
    public void stopIntake() {
        intakeMotor.set(0);
    }

    /**
     * Lower the intakeArm at the configured speed.
     */
    public void lowerIntakeArm() {
        intakeArmMotor.set(-IntakeConstants.kLowSpeed);
    }

    /**
     * Raise the intakeArm at the configured speed.
     */
    public void raiseIntakeArm() {
        intakeArmMotor.set(IntakeConstants.kLowSpeed);
    }

    /**
     * Stop the uptake motor.
     */
    public void stopIntakeArm() {
        intakeArmMotor.set(0);
    }

    /**
     * Get the uptake motor RPM.
     */
    public double getintakeRPM() {
        return intakeMotor.getVelocity().in(RPM);
    }

    /**
     * Get the intakeArmMotor position.
     */
    public double getIntakeArmPosition() {
        return intakeArmMotor.getPosition();
    }

    // ==================== Command Factory Methods ====================

    /**
     * Command to run the uptake while held.
     */
    public Command runIntakeCommand() {
        return runEnd(this::runIntake, this::stopIntake)
                .withName("Run Intake");
    }

    /**
     * Command to reverse the uptake while held.
     */
    public Command reverseIntakeCommand() {
        return runEnd(this::reverseIntake, this::stopIntake)
                .withName("Reverse Intake");
    }

    /**
     * Command to stop the uptake (instant).
     */
    public Command stopIntakeCommand() {
        return runOnce(this::stopIntake)
                .withName("Stop Intake");
    }

    /**
     * Command to run the carousel while held.
     */
    public Command runLowerIntakeArmCommand() {
        return Commands.run(() -> lowerIntakeArm())
                .until(isLowered)
                .withName("Lower Intake Arm");
    }

    /**
     * Command to run the carousel while held.
     */
    public Command runRaiseIntakeArmCommand() {
        return Commands.run(() -> raiseIntakeArm())
                .until(isRaised)
                .withName("Raise Intake Arm");
    }

    @Override
    public void periodic() {
        // Add telemetry here if needed
    }
}
