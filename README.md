# TestBoard

A WPILib project for testing prototype mechanisms on a test board (RoboRIO + motors only).

## Purpose

This project is designed for students to test subsystems independently, without needing a full robot drivetrain. All commands are exposed on SmartDashboard so you can run them from Shuffleboard.

## Current Subsystems

### Intake
- **Roller Motor** (CAN 33): Kraken X44
- **Arm Motor** (CAN 32): Minion (TalonFXS) — Motion Magic with Arm_Cosine gravity compensation
- **Limit Switch** (DIO 0): Arm home position
- **Simulation**: Mechanism2d visualization with SingleJointedArmSim gravity physics
- See [Intake Arm Tuning Guide](docs/intake-arm-tuning.md) for PID tuning, gear ratio calibration, and AdvantageScope visualization

## How to Add a New Subsystem

1. **Add CAN IDs to `RobotMap.java`**
   ```java
   public static final int kMyMotorPort = 30;
   ```

2. **Add Constants to `Constants.java`**
   ```java
   public static final class MySubsystemConstants {
       public static final double kMotorSpeed = 0.5;
       public static final Current kCurrentLimit = Amps.of(40);
   }
   ```

3. **Create the Subsystem class in `subsystems/`**
   - Copy `ShooterSubsystem.java` as a template
   - Configure your motors
   - Add command factory methods

4. **Register in `RobotContainer.java`**
   - Instantiate your subsystem
   - Add commands to SmartDashboard

## Running

### On hardware
1. Deploy code to the RoboRIO
2. Open Shuffleboard and navigate to the **Intake** tab
3. Click command buttons to control the subsystem

### In simulation
1. Run `./gradlew simulateJava`
2. In Glass, enable teleop from the sim DriverStation
3. Open Shuffleboard — the **Intake** tab has all telemetry, commands, and tunables
4. The **Mechanism2d** widget ("Intake Arm Sim") shows the arm responding to gravity and commands

## Project Structure

```
src/main/java/com/adambots/
├── Main.java           # Entry point
├── Robot.java          # Robot lifecycle
├── RobotContainer.java # Subsystem setup & dashboard commands
├── RobotMap.java       # CAN IDs and port assignments
├── Constants.java      # Subsystem constants (IntakeConstants, SimConstants)
└── subsystems/
    └── IntakeSubsystem.java  # Intake roller + arm with sim support
```

```
docs/
└── intake-arm-tuning.md  # PID tuning, gear ratio calibration, AdvantageScope
```
