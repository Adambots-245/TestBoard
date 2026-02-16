# Intake Arm Gravity Compensation Tuning Guide

This guide covers tuning the intake arm's onboard PID with gravity feedforward. The arm motor (Minion on TalonFXS) runs Motion Magic at 1kHz on the motor controller with `Arm_Cosine` gravity compensation.

## How It Works

The motor controller applies this output every 1ms:

```
output = kP * error + kD * d(error)/dt + kG * cos(arm_angle) + kS * sign(velocity) + kV * velocity + kA * acceleration
```

- **kG * cos(angle)** is the key: maximum compensation when horizontal, zero when vertical
- **Motion Magic** generates a smooth trapezoidal profile (cruise velocity + acceleration limits)
- **PID** tracks the profile, **feedforward** does the heavy lifting

## Prerequisites

1. Deploy the code to the robot
2. Open Shuffleboard and find the tunable entries:
   - `IntakeArm kP`, `IntakeArm kI`, `IntakeArm kD`
   - `IntakeArm kG`, `IntakeArm kS`, `IntakeArm kV`, `IntakeArm kA`
3. Ensure the limit switch is working (verify `Limit Switch` widget toggles when pressed)
4. Reset the arm position with "Reset Position" button

## Tuning Order

**Always tune in this order.** Each gain builds on the previous.

### Step 1: Find kG (Gravity Compensation)

This is the most important gain. Get this right and everything else is easier.

1. Set all gains to 0 (`kP=0, kI=0, kD=0, kS=0, kV=0, kA=0, kG=0`)
2. Manually hold the arm **horizontal** (parallel to the ground) - this is where gravity is strongest
3. Slowly increase `kG` in Shuffleboard (start at 0.05, increment by 0.05)
4. Release the arm after each change
5. **Target:** Find the value where the arm **just barely holds itself horizontal** without drifting
6. The arm shouldn't rise (kG too high) or fall (kG too low)

> **Note:** The controller uses the current measured position (not the setpoint) for gravity output, so it holds steady at any angle automatically.

### Step 2: Find kS (Static Friction)

1. With kG set, the arm should hold position but won't move to a target yet
2. Command a small position change (e.g., use "Lower Intake" or "Raise Intake")
3. If the arm doesn't start moving, increase `kS` from 0 in small increments (0.05)
4. **Target:** The minimum voltage needed to overcome friction and start moving

> For a well-built mechanism, kS may be very small (0.05-0.15). Skip this if the arm moves fine without it.

### Step 3: Tune kP (Proportional)

1. Start with `kP = 1.0`
2. Command the arm to a position (use "Raise Intake" / "Lower Intake")
3. Watch if it reaches the target:
   - **Doesn't reach target or moves too slowly:** Increase kP
   - **Oscillates around target:** Decrease kP
   - **Overshoots then settles:** kP is close, may need kD
4. Increase in doublings: 1.0 -> 2.0 -> 4.0 -> 8.0 until oscillation, then back off

### Step 4: Tune kD (Derivative / Damping)

Only needed if the arm oscillates with your chosen kP.

1. Start with `kD = 0.05`
2. Command position changes and watch for oscillation
3. Increase kD until oscillation is damped out
4. **Too much kD:** Arm feels sluggish or vibrates at high frequency
5. Typical range: 0.05 - 0.5 (usually 5-10% of kP)

### Step 5: Motion Magic Profile (if needed)

These are set in `Constants.java` and require a redeploy:

| Parameter | Current Value | Effect |
|-----------|--------------|--------|
| `kArmCruiseVelocity` | 2.0 rps | Max speed during motion |
| `kArmAcceleration` | 1.0 rps/s | How fast it speeds up/slows down |
| `kArmJerk` | 0.0 | Smoothing (0 = trapezoidal, >0 = S-curve) |

- If the arm moves too fast, reduce cruise velocity
- If it jerks at start/stop, reduce acceleration or add jerk limiting
- Conservative values are better for an arm fighting gravity

### Step 6: kV and kA (Usually Leave at 0)

These are for velocity and acceleration feedforward. For a gravity-loaded arm with Motion Magic:
- **kV:** Only needed if the arm consistently lags behind the profile at speed
- **kA:** Only needed if the arm can't keep up during acceleration phases
- Start at 0 and only add if you see tracking error during motion

### kI: Almost Never Needed

With proper kG, there should be no steady-state error. If the arm consistently stops slightly short of the target:
1. First check if kG is correct
2. Then check if kS needs adjustment
3. Only as a last resort, add tiny kI (0.001) with anti-windup

## Quick Reference: Starting Values

| Gain | Starting Value | What It Does |
|------|---------------|-------------|
| kG | 0.15 (tune first!) | Counteracts gravity via cos(angle) |
| kS | 0.0 | Overcomes static friction |
| kP | 4.8 | Drives toward target position |
| kI | 0.0 | Eliminates steady-state error (avoid) |
| kD | 0.1 | Dampens oscillation |
| kV | 0.0 | Velocity tracking (usually not needed) |
| kA | 0.0 | Acceleration tracking (usually not needed) |

## Troubleshooting

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| Arm falls when disabled | kG too low or brake mode off | Increase kG; verify brake mode |
| Arm drifts up slowly | kG too high | Decrease kG |
| Arm oscillates at target | kP too high or kD too low | Reduce kP or increase kD |
| Arm doesn't reach target | kP too low or kS too low | Increase kP or kS |
| Arm moves in wrong direction | Motor inverted incorrectly | Check motor inversion in RobotMap |
| Arm holds but won't move | kS too low, friction too high | Increase kS |
| Violent shaking | kD too high | Reduce kD |

## Saving Tuned Values

Once you're happy with the tuning, update the constants in `Constants.java`:

```java
public static final double kArmP = <your value>;
public static final double kArmI = 0.0;
public static final double kArmD = <your value>;
public static final double kArmKV = <your value>;
public static final double kArmKS = <your value>;
public static final double kArmKA = <your value>;
public static final double kArmKG = <your value>;
```

Redeploy to lock in the values.
