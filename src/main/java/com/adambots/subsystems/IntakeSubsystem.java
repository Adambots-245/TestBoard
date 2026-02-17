package com.adambots.subsystems;

import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.RotationsPerSecond;

import com.adambots.Constants.IntakeConstants;
import com.adambots.lib.actuators.BaseMotor;
import com.adambots.lib.utils.Dash;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/**
 * Intake subsystem for intaking game peices.
 */
public class IntakeSubsystem extends SubsystemBase {

    private final BaseMotor intakeMotor;
    private final BaseMotor intakeArmMotor;
    private final DigitalInput limitSwitch;

    private final PIDController armPID;

    private GenericEntry intakeArmPEntry;
    private GenericEntry intakeArmIEntry;
    private GenericEntry intakeArmDEntry;
    private GenericEntry intakeArmFFEntry;

    private double lastP, lastI, lastD, lastFF;

    private double targetPosition = IntakeConstants.kLowerLimit;
    private boolean positionControlActive = false;

    public IntakeSubsystem(BaseMotor intakeMotor, BaseMotor intakeArmMotor, DigitalInput limitSwitch) {
        this.intakeMotor = intakeMotor;
        this.intakeArmMotor = intakeArmMotor;
        this.limitSwitch = limitSwitch;

        armPID = new PIDController(IntakeConstants.kArmP, IntakeConstants.kArmI, IntakeConstants.kArmD);
        armPID.setTolerance(0.1);

        configureMotors();
        setupDash();
        configureLimitSwitch();
    }

    private void configureMotors() {
        // TODO(vx-clutch): configure intakeMotor
        intakeMotor.setBrakeMode(false);
        intakeArmMotor.configure()
                .brakeMode(true)
                .currentLimits(IntakeConstants.kStall, 40, IntakeConstants.kRPM)
                .apply();
    }

    private void configureLimitSwitch() {
        // DIO returns false when switch is closed/triggered
        // No subsystem requirement so this always runs, even if another command holds the subsystem
        new Trigger(() -> limitSwitch.get())
            .onTrue(Commands.runOnce(() -> {
                stopIntakeArm();
                intakeArmMotor.setPosition(IntakeConstants.kUpperLimit);
            }).ignoringDisable(true));
    }

    private void setupDash() {
        Dash.add("IntakeMotor Speed", () -> intakeMotor.getVelocity().in(RotationsPerSecond));
        Dash.add("IntakeArmMotor Speed", () -> intakeArmMotor.getVelocity().in(RotationsPerSecond));
        Dash.add("IntakeMotor Position", () -> intakeMotor.getPosition());
        Dash.add("IntakeMotorArm Position", () -> intakeArmMotor.getPosition());
        Dash.add("Upper Limit", () -> IntakeConstants.kUpperLimit);
        Dash.add("Limit Switch", () -> limitSwitch.get());

        Dash.addCommand("Reset Positon", resetIntakeArmPositon());
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
        intakeArmFFEntry = Dash.addTunable("IntakeArm kGravityFF", IntakeConstants.kGravityFF);

        lastP = IntakeConstants.kArmP;
        lastI = IntakeConstants.kArmI;
        lastD = IntakeConstants.kArmD;
        lastFF = IntakeConstants.kGravityFF;
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
     * Lower the intakeArm to the lower position.
     */
    public void lowerIntakeArm() {
        targetPosition = IntakeConstants.kLowerLimit;
        positionControlActive = true;
        armPID.reset();
    }

    /**
     * Raise the intakeArm to the upper position.
     */
    public void raiseIntakeArm() {
        targetPosition = IntakeConstants.kUpperLimit;
        positionControlActive = true;
        armPID.reset();
    }

    /**
     * Stop the intake arm motor.
     */
    public void stopIntakeArm() {
        System.out.println("Stopped Intake Arm.");
        positionControlActive = false;
        intakeArmMotor.set(0);
    }

    /**
     * Calculate constant gravity feedforward.
     * Negative because the arm must be driven in the negative direction to raise.
     */
    private double calculateGravityFF() {
        return -lastFF;
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
     * Command to lower the intake arm.
     */
    public Command runLowerIntakeArmCommand() {
        return Commands.runOnce(() -> lowerIntakeArm())
                .withName("Lower Intake Arm");
    }

    /**
     * Command to raise the intake arm.
     */
    public Command runRaiseIntakeArmCommand() {
        return Commands.runOnce(() -> raiseIntakeArm())
                .withName("Raise Intake Arm");
    }

    /**
     * Command to stop the intake arm and disable PID position control.
     */
    public Command stopIntakeArmCommand() {
        return Commands.runOnce(() -> stopIntakeArm())
                .withName("Stop Intake Arm");
    }

    @Override
    public void periodic() {
        // WPILib PID + gravity feedforward control loop
        if (positionControlActive) {
            double currentPosition = intakeArmMotor.getPosition();
            double pidOutput = armPID.calculate(currentPosition, targetPosition);
            double gravityFF = calculateGravityFF();
            double outputWithFF = pidOutput;
            if (targetPosition == IntakeConstants.kUpperLimit){
                outputWithFF = pidOutput + gravityFF;
            } //else {
            //     outputWithFF = pidOutput - gravityFF;
            // }
            double output = MathUtil.clamp(pidOutput + gravityFF, -1.0, 1.0);
            System.out.println("PID + FF OUTPUT: " + outputWithFF);
            System.out.println("Target Position: " + targetPosition);
            intakeArmMotor.set(output);
        }

        // Update PID gains from Shuffleboard tunables
        if (intakeArmPEntry != null) {
            double p = intakeArmPEntry.getDouble(IntakeConstants.kArmP);
            double i = intakeArmIEntry.getDouble(IntakeConstants.kArmI);
            double d = intakeArmDEntry.getDouble(IntakeConstants.kArmD);
            double ff = intakeArmFFEntry.getDouble(IntakeConstants.kGravityFF);

            if (p != lastP || i != lastI || d != lastD) {
                armPID.setPID(p, i, d);
                lastP = p;
                lastI = i;
                lastD = d;
            }
            if (ff != lastFF) {
                lastFF = ff;
            }
        }
    }
}
