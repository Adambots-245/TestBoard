# TestBoard

A WPILib project for testing prototype mechanisms on a test board (RoboRIO + motors only).

## Purpose

This project is designed for students to test subsystems independently, without needing a full robot drivetrain. All commands are exposed on SmartDashboard so you can run them from Shuffleboard.

## Current Subsystems

### Shooter
- **Left Motor** (CAN 21): Kraken X60 - Leader
- **Right Motor** (CAN 22): Kraken X60 - Follower (opposed direction)
- **Uptake Motor** (CAN 20): Kraken X44

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

## Running Tests

1. Deploy code to the RoboRIO
2. Open Shuffleboard
3. Find your commands under the subsystem name (e.g., "Shooter/Run Shooter")
4. Click the command button to run it

## Project Structure

```
src/main/java/com/adambots/
├── Main.java           # Entry point
├── Robot.java          # Robot lifecycle
├── RobotContainer.java # Subsystem setup & dashboard commands
├── RobotMap.java       # CAN IDs and port assignments
├── Constants.java      # Subsystem constants
└── subsystems/
    └── ShooterSubsystem.java
```
