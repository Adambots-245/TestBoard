package com.adambots.subsystems;

import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.RotationsPerSecondPerSecond;

import com.adambots.Constants.IntakeConstants;
import com.adambots.lib.actuators.BaseMotor;
import com.adambots.lib.utils.Dash;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/**
 * Intake subsystem using TalonFX onboard PID with gravity compensation.
 *
 * <p>The arm motor runs Motion Magic with Arm_Cosine gravity feedforward at 1kHz
 * on the motor controller, providing stable holding and smooth motion.
 */
public class IntakeSubsystem extends SubsystemBase {

    private final BaseMotor intakeMotor;
    private final BaseMotor intakeArmMotor;
    private final DigitalInput limitSwitch;

    // Tunable PID entries
    private GenericEntry intakeArmPEntry;
    private GenericEntry intakeArmIEntry;
    private GenericEntry intakeArmDEntry;
    private GenericEntry intakeArmKGEntry;
    private GenericEntry intakeArmKSEntry;
    private GenericEntry intakeArmKVEntry;
    private GenericEntry intakeArmKAEntry;

    private double lastP, lastI, lastD, lastKG, lastKS, lastKV, lastKA;

    private double targetPosition = IntakeConstants.kLowerLimit;

    public IntakeSubsystem(BaseMotor intakeMotor, BaseMotor intakeArmMotor, DigitalInput limitSwitch) {
        this.intakeMotor = intakeMotor;
        this.intakeArmMotor = intakeArmMotor;
        this.limitSwitch = limitSwitch;

        configureMotors();
        setupDash();
        configureLimitSwitch();
    }

    private void configureMotors() {
        intakeMotor.setBrakeMode(false);

        // Configure arm motor: brake mode + current limits
        intakeArmMotor.configure()
                .brakeMode(true)
                .currentLimits(IntakeConstants.kStall, 40, IntakeConstants.kRPM)
                .gravity(BaseMotor.GravityType.ARM_COSINE)
                .motionMagic(
                    RotationsPerSecond.of(IntakeConstants.kArmCruiseVelocity),
                    RotationsPerSecondPerSecond.of(IntakeConstants.kArmAcceleration),
                    IntakeConstants.kArmJerk)
                .apply();

        // Set extended PID with feedforward gains (kV, kS, kA, kG)
        intakeArmMotor.setPID(0,
                IntakeConstants.kArmP, IntakeConstants.kArmI, IntakeConstants.kArmD,
                IntakeConstants.kArmKV, IntakeConstants.kArmKS, IntakeConstants.kArmKA,
                IntakeConstants.kArmKG);
    }

    private void configureLimitSwitch() {
        // DIO returns false when switch is closed/triggered
        new Trigger(() -> !limitSwitch.get())
            .onTrue(Commands.runOnce(() -> {
                stopIntakeArm();
                intakeArmMotor.setPosition(IntakeConstants.kUpperLimit);
            }, this).ignoringDisable(true));
    }

    private void setupDash() {
        Dash.add("IntakeMotor Speed", () -> intakeMotor.getVelocity().in(RotationsPerSecond));
        Dash.add("IntakeArmMotor Speed", () -> intakeArmMotor.getVelocity().in(RotationsPerSecond));
        Dash.add("IntakeMotor Position", () -> intakeMotor.getPosition());
        Dash.add("IntakeMotorArm Position", () -> intakeArmMotor.getPosition());
        Dash.add("Arm Target Position", () -> targetPosition);
        Dash.add("Upper Limit", () -> IntakeConstants.kUpperLimit);
        Dash.add("Limit Switch", () -> limitSwitch.get());

        Dash.addCommand("Reset Position", resetIntakeArmPosition());
        Dash.addCommand("Start Intake", runIntakeCommand());
        Dash.addCommand("Reverse Intake", reverseIntakeCommand());
        Dash.addCommand("Stop Intake", stopIntakeCommand());
        Dash.addCommand("Lower Intake", runLowerIntakeArmCommand());
        Dash.addCommand("Raise Intake", runRaiseIntakeArmCommand());
        Dash.addCommand("Stop Arm", stopIntakeArmCommand());
    }

    public void setupTunables() {
        intakeArmPEntry = Dash.addTunable("IntakeArm kP", IntakeConstants.kArmP);
        intakeArmIEntry = Dash.addTunable("IntakeArm kI", IntakeConstants.kArmI);
        intakeArmDEntry = Dash.addTunable("IntakeArm kD", IntakeConstants.kArmD);
        intakeArmKGEntry = Dash.addTunable("IntakeArm kG", IntakeConstants.kArmKG);
        intakeArmKSEntry = Dash.addTunable("IntakeArm kS", IntakeConstants.kArmKS);
        intakeArmKVEntry = Dash.addTunable("IntakeArm kV", IntakeConstants.kArmKV);
        intakeArmKAEntry = Dash.addTunable("IntakeArm kA", IntakeConstants.kArmKA);

        lastP = IntakeConstants.kArmP;
        lastI = IntakeConstants.kArmI;
        lastD = IntakeConstants.kArmD;
        lastKG = IntakeConstants.kArmKG;
        lastKS = IntakeConstants.kArmKS;
        lastKV = IntakeConstants.kArmKV;
        lastKA = IntakeConstants.kArmKA;
    }

    /**
     * Run the intake roller at the configured speed.
     */
    public void runIntake() {
        intakeMotor.set(IntakeConstants.kLowSpeed);
    }

    /**
     * Run the intake roller in reverse.
     */
    public void reverseIntake() {
        intakeMotor.set(-IntakeConstants.kLowSpeed);
    }

    /**
     * Stop the intake roller motor.
     */
    public void stopIntake() {
        intakeMotor.set(0);
    }

    /**
     * Lower the intake arm using onboard Motion Magic with gravity compensation.
     */
    public void lowerIntakeArm() {
        targetPosition = IntakeConstants.kLowerLimit;
        intakeArmMotor.set(BaseMotor.ControlMode.MOTION_MAGIC, targetPosition);
    }

    /**
     * Raise the intake arm using onboard Motion Magic with gravity compensation.
     */
    public void raiseIntakeArm() {
        targetPosition = IntakeConstants.kUpperLimit;
        intakeArmMotor.set(BaseMotor.ControlMode.MOTION_MAGIC, targetPosition);
    }

    /**
     * Stop the intake arm motor.
     */
    public void stopIntakeArm() {
        intakeArmMotor.set(0);
    }

    /**
     * Get the intake roller motor RPM.
     */
    public double getintakeRPM() {
        return intakeMotor.getVelocity().in(RPM);
    }

    /**
     * Get the intake arm motor position.
     */
    public double getIntakeArmPosition() {
        return intakeArmMotor.getPosition();
    }

    // ==================== Command Factory Methods ====================

    /**
     * Command to run the intake while held.
     */
    public Command runIntakeCommand() {
        return runEnd(this::runIntake, this::stopIntake)
                .withName("Run Intake");
    }

    /**
     * Reset arm position encoder to 0.
     */
    public Command resetIntakeArmPosition() {
        return runOnce(() -> intakeArmMotor.setPosition(0))
                .withName("Reset Intake Position");
    }

    /**
     * Command to reverse the intake while held.
     */
    public Command reverseIntakeCommand() {
        return runEnd(this::reverseIntake, this::stopIntake)
                .withName("Reverse Intake");
    }

    /**
     * Command to stop the intake (instant).
     */
    public Command stopIntakeCommand() {
        return runOnce(this::stopIntake)
                .withName("Stop Intake");
    }

    /**
     * Command to lower the intake arm using onboard Motion Magic.
     */
    public Command runLowerIntakeArmCommand() {
        return Commands.runOnce(() -> lowerIntakeArm())
                .withName("Lower Intake Arm");
    }

    /**
     * Command to raise the intake arm using onboard Motion Magic.
     */
    public Command runRaiseIntakeArmCommand() {
        return Commands.runOnce(() -> raiseIntakeArm())
                .withName("Raise Intake Arm");
    }

    /**
     * Command to stop the intake arm.
     */
    public Command stopIntakeArmCommand() {
        return Commands.runOnce(() -> stopIntakeArm())
                .withName("Stop Intake Arm");
    }

    @Override
    public void periodic() {
        // No PID loop here - the motor controller handles everything at 1kHz.
        // We only check for tunable gain updates from Shuffleboard.

        if (intakeArmPEntry != null) {
            double p = intakeArmPEntry.getDouble(IntakeConstants.kArmP);
            double i = intakeArmIEntry.getDouble(IntakeConstants.kArmI);
            double d = intakeArmDEntry.getDouble(IntakeConstants.kArmD);
            double kG = intakeArmKGEntry.getDouble(IntakeConstants.kArmKG);
            double kS = intakeArmKSEntry.getDouble(IntakeConstants.kArmKS);
            double kV = intakeArmKVEntry.getDouble(IntakeConstants.kArmKV);
            double kA = intakeArmKAEntry.getDouble(IntakeConstants.kArmKA);

            if (p != lastP || i != lastI || d != lastD ||
                kG != lastKG || kS != lastKS || kV != lastKV || kA != lastKA) {

                intakeArmMotor.setPID(0, p, i, d, kV, kS, kA, kG);

                lastP = p;
                lastI = i;
                lastD = d;
                lastKG = kG;
                lastKS = kS;
                lastKV = kV;
                lastKA = kA;
            }
        }
    }
}
