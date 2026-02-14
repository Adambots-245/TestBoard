package com.adambots.subsystems;

import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.RotationsPerSecond;

import com.adambots.Constants.IntakeConstants;
import com.adambots.lib.actuators.BaseMotor;
import com.adambots.lib.actuators.BaseMotor.ControlMode;
import com.adambots.lib.utils.Dash;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Intake subsystem for intaking game peices.
 */
public class IntakeSubsystem extends SubsystemBase {

    private final BaseMotor intakeMotor;
    private final BaseMotor intakeArmMotor;

    private GenericEntry intakeArmPEntry;
    private GenericEntry intakeArmIEntry;
    private GenericEntry intakeArmDEntry;
    private GenericEntry intakeArmFEntry;

    private double lastP, lastI, lastD, lastF;

    public IntakeSubsystem(BaseMotor intakeMotor, BaseMotor intakeArmMotor) {
        this.intakeMotor = intakeMotor;
        this.intakeArmMotor = intakeArmMotor;

        configureMotors();
        setupDash();
    }

    private void configureMotors() {
        // TODO(vx-clutch): configure intakeMotor
        intakeMotor.setBrakeMode(false);
        intakeArmMotor.setBrakeMode(true);
        intakeArmMotor.configure().pid(
                IntakeConstants.P,
                IntakeConstants.I,
                IntakeConstants.D,
                IntakeConstants.F).apply();;
    }

    private void setupDash() {
        Dash.add("IntakeMotor Speed", () -> intakeMotor.getVelocity().in(RotationsPerSecond));
        Dash.add("IntakeArmMotor Speed", () -> intakeArmMotor.getVelocity().in(RotationsPerSecond));
        Dash.add("IntakeMotor Position", () -> intakeMotor.getPosition());
        Dash.add("IntakeMotorArm Position", () -> intakeArmMotor.getPosition());
        
        Dash.addCommand("Reset Positon", resetIntakeArmPositon());
        Dash.addCommand("Start Intake", runIntakeCommand());
        Dash.addCommand("Reverse Intake", reverseIntakeCommand());
        Dash.addCommand("Stop Intake", stopIntakeCommand());
        Dash.addCommand("Lower Intake", runLowerIntakeArmCommand());
        Dash.addCommand("Raise Intake", runRaiseIntakeArmCommand());
    }

    public void setupTunables() {
        intakeArmPEntry = Dash.addTunable("IntakeArm kP", IntakeConstants.P);
        intakeArmIEntry = Dash.addTunable("IntakeArm kI", IntakeConstants.I);
        intakeArmDEntry = Dash.addTunable("IntakeArm kD", IntakeConstants.D);
        intakeArmFEntry = Dash.addTunable("IntakeArm kF", IntakeConstants.F);

        lastP = IntakeConstants.P;
        lastI = IntakeConstants.I;
        lastD = IntakeConstants.D;
        lastF = IntakeConstants.F;
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
        intakeArmMotor.set(ControlMode.POSITION, IntakeConstants.kLowerLimit);
    }

    /**
     * Raise the intakeArm at the configured speed.
     */
    public void raiseIntakeArm() {
        intakeArmMotor.set(ControlMode.POSITION, IntakeConstants.kUpperLimit);
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
     * Reset arm position
     */
    public Command resetIntakeArmPositon() {
        return runOnce(() -> {intakeArmMotor.setPosition(0);}).withName("Rest Intake Positon");
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
        return Commands.runOnce(() -> lowerIntakeArm())
                .withName("Lower Intake Arm");
    }

    /**
     * Command to run the carousel while held.
     */
    public Command runRaiseIntakeArmCommand() {
        return Commands.runOnce(() -> raiseIntakeArm())
                .withName("Raise Intake Arm");
    }

    @Override
    public void periodic() {
        if (intakeArmPEntry != null) {
            double p = intakeArmPEntry.getDouble(IntakeConstants.P);
            double i = intakeArmPEntry.getDouble(IntakeConstants.I);
            double d = intakeArmPEntry.getDouble(IntakeConstants.D);
            double f = intakeArmPEntry.getDouble(IntakeConstants.F);

            if (p != lastP || i != lastI || d != lastD || f != lastF) {
                intakeArmMotor.setPID(0, p, i, d, f);
                lastP = p;
                lastI = i;
                lastD = d;
                lastF = f;
            }
        }
    }
}
