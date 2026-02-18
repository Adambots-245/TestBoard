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
2. Open Shuffleboard and navigate to the **Intake** tab. All widgets are organized in rows:
   - **Row 0 — Telemetry:** Roller Speed, Roller Position, Arm Speed, Arm Position, Arm Target, Raised Position, Limit Switch, Sim Voltage, Sim Angle Deg
   - **Row 1 — Commands:** Start Intake, Reverse Intake, Stop Intake, Lower Arm, Raise Arm, Stop Arm, Reset Position
   - **Row 2 — Tunables:** kP, kI, kD, kG, kS, kV, kA, Zero Tunables, Reset Tunables
3. Ensure the limit switch is working (verify `Limit Switch` widget toggles when pressed)
4. Reset the arm position with "Reset Position" button

## Simulation

You can practice the full tuning workflow in sim before touching hardware:

1. Run `./gradlew simulateJava`
2. In Glass, enable teleop from the sim DriverStation
3. Open Shuffleboard and navigate to the **Intake** tab
4. The **Mechanism2d** visualization ("Intake Arm Sim" in SmartDashboard) shows a yellow arm that responds to gravity and commands
5. Click **Zero Tunables** and follow the tuning order below — watch the arm droop, hold, and track positions

The tuning workflow is identical to real hardware. Gain values won't transfer directly (different motor model, estimated gear ratio), but the process and behavior are the same.

## Tuning Order

**Always tune in this order.** Each gain builds on the previous.

Use **Zero Tunables** to start from scratch, **Reset Tunables** to restore code constants.

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

## Gear Ratio Calibration

The gear ratio (`kArmTotalGearRatio`) is a two-stage reduction: planetary gearbox × belt drive. It must be set correctly for `Arm_Cosine` gravity compensation to work — the cosine must cycle once per arm revolution, not once per motor revolution.

### Method 1: Calculate from specs

Get these from the mechanical team:
- **Planetary ratio** — stamped on the gearbox or in its datasheet (e.g., 5:1)
- **Belt/pulley ratio** — count teeth on both pulleys: `motor-side teeth ÷ arm-side teeth` (e.g., 18T driving 36T = 2:1)
- **Total** = planetary × belt (e.g., 5 × 5.6 = 28:1)

### Method 2: Measure empirically

This catches assembly mistakes (wrong pulley, wrong gearbox stage, etc.). **Always verify with this method.**

1. Deploy with `kArmTotalGearRatio = 1.0` (so position reports raw motor rotations)
2. Move the arm to the lowered (horizontal) position
3. Click **Reset Position** to zero the encoder
4. Physically rotate the arm exactly **90°** (use a protractor or square)
5. Read **Arm Position** from the Intake tab
6. Gear ratio = `|position reading| / 0.25`

Example: position reads -7.0 → gear ratio = 7.0 / 0.25 = **28:1**

### After updating the gear ratio

Once you set `kArmTotalGearRatio` to the real value (e.g., 28.0), `getPosition()` reports **mechanism rotations** instead of motor rotations. Position targets must be recalibrated:

1. Update `kArmPlanetaryRatio` and `kArmBeltRatio` in `Constants.java`
2. Redeploy
3. Move arm to the lowered (horizontal) position
4. Click **Reset Position** to zero the encoder
5. Move arm to the raised (90°) position
6. Read **Arm Position** — this is the new `kArmRaisedPosition` (should be approximately -0.25)
7. Update `kArmRaisedPosition` and `kArmLoweredPosition` in `Constants.java`
8. Also update `SimConstants.kSimGearRatio` to match the real gear ratio
9. Redeploy and re-tune PID gains (they may need adjustment for the new position scale)

## Visualizing with AdvantageScope

AdvantageScope can plot telemetry over time, which is essential for evaluating motion profile quality and tuning.

### Setup

1. Open AdvantageScope
2. Connect to `localhost:1735` (sim) or the robot's IP address
3. In the left sidebar, expand **Shuffleboard > Intake** to find telemetry values
4. Create a **Line Graph** tab

### Recommended plots

**Position tracking (most important):**
- **Left axis:** Arm Position, Arm Target
- Shows how well the arm tracks the commanded position
- The gap between the two curves is your position error

**Motion profile shape:**
- **Left axis:** Arm Position
- **Right axis:** Arm Speed
- During a Raise/Lower command, the velocity trace reveals the profile shape:
  - **Trapezoidal** (kArmJerk = 0): ramp up → flat cruise → ramp down
  - **S-curve** (kArmJerk > 0): smooth ramp up → cruise → smooth ramp down
- Note: the sim uses a simple PD+kG model, so the trapezoidal profile is only visible on real hardware where CTRE Motion Magic generates the trajectory

**Motor effort:**
- **Left axis:** Sim Voltage (sim) or motor output (real hardware)
- Should spike at the start of a move and settle to a steady kG value when holding
- Sustained saturation at ±12V means the move is too aggressive for the motor

### What to look for

| Observation | Meaning | Fix |
|-------------|---------|-----|
| Position reaches target with no overshoot | Well-tuned | None needed |
| Position overshoots then rings | kP too high or kD too low | Reduce kP or increase kD |
| Position undershoots (never reaches target) | kP too low or kS too low | Increase kP or kS |
| Velocity curve is jagged/spiky | Oscillation or noise | Increase kD, reduce kP |
| Velocity curve is smooth bell/trapezoid | Good Motion Magic profile | None needed |
| Voltage saturated at ±12V for extended time | Move too aggressive | Reduce cruise velocity or acceleration |

### Phoenix Tuner X (real hardware only)

For deeper Motion Magic analysis, Phoenix Tuner X plots CTRE's internal signals:
- **ClosedLoopReference** — the position the profile is targeting at each instant
- **ClosedLoopOutput** — the PID + feedforward output
- These internal signals are not available through NetworkTables
