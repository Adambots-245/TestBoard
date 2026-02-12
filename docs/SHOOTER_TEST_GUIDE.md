# Shooter Mechanism Test Guide

Step-by-step procedure for testing the flywheel + turret prototype on the test board. Work through each phase in order -- don't skip ahead until the current phase passes.

---

## Prerequisites

- Test board powered on, roboRIO connected
- Driver Station running, robot enabled in Teleop
- Shuffleboard open -- select the **"Shooter Test"** tab
- AdvantageScope connected to the robot (File > Connect to Robot)
- Safety glasses on, clear area around the shooter

---

## Phase 1: Motor Smoke Test

**Goal:** Verify all three motors spin in the correct direction with no mechanical binding.

### 1.1 Flywheel Check

1. In Shuffleboard, click **"Spin 50 RPS"** (hold the button -- it runs while held)
2. Verify:
   - Left flywheel spins (watch the wheel, not just the motor)
   - Right flywheel spins in the **opposite direction** (follower in reverse)
   - No grinding, vibration, or unusual noise
   - "Left RPS" and "Right RPS" telemetry show values ramping up (they won't hit 50 yet -- PID isn't tuned)
3. Release the button -- both motors should coast to a stop
4. Check "Flywheel Amps" stays below 60A during spin-up

**If a motor doesn't spin:** Check CAN wiring, verify CAN IDs 21/22 in Phoenix Tuner.
**If right spins the same direction as left:** The follower inversion is wrong -- check `setStrictFollower` in code.

### 1.2 Turret Check

1. Click **"Turret 90 deg"**
2. Verify the turret moves toward the 90-degree position (halfway through range)
3. Gently try to push the turret by hand -- it should resist (brake mode)
4. Click **"Turret 0 deg"** -- turret returns to start
5. Click **"Turret 180 deg"** -- turret moves to other end of range
6. Test manual aim: set **"Turret Angle (deg)"** slider to 45, then hold **"Aim Turret Manual"** -- turret should move to 45 degrees. Drag the slider while holding -- turret should track. Release -- turret should hold position.
7. Click **"Stop Turret"**
8. Check "Turret Amps" -- should be low (< 5A) when holding position

**If the turret doesn't move:** Check CAN ID 25, verify MinionMotor wiring.
**If the turret moves the wrong amount:** The gear ratio constant (`kTurretGearRatio = 100.0`) needs updating. See [Adjusting Constants](#adjusting-constants) below.

---

## Phase 2: Flywheel PID Tuning

**Goal:** Get the flywheel to reach and hold a target RPS with minimal overshoot and fast settling.

### 2.1 Set Up AdvantageScope for Flywheel

1. In AdvantageScope, open a **Line Chart** (drag from the sidebar or View > New Tab > Line Chart)
2. From the NetworkTables tree on the left, drag these signals onto the chart:
   - `/Shooter Test/Left RPS` (actual velocity)
   - `/Shooter Test/Target RPS` (setpoint)
3. Set the time axis to about **5-10 seconds** visible (zoom with scroll wheel)
4. You should now see two lines: the setpoint (flat) and the actual (ramping/oscillating)

### 2.2 Initial Spin-Up Test

1. Click **"Spin 50 RPS"** in Shuffleboard and hold it
2. Watch the AdvantageScope chart -- you'll see the actual RPS ramp toward 50
3. What you're looking for in the curve:

| Curve Shape | Problem | Fix |
|-------------|---------|-----|
| Slow ramp, never reaches target | kF (feedforward) too low | Increase **Flywheel kF** |
| Reaches target but slowly | kP too low | Increase **Flywheel kP** |
| Overshoots then oscillates | kP too high | Decrease **Flywheel kP** |
| Oscillates forever | kP way too high, or kD needed | Decrease **kP**, try small **kD** |
| Reaches target with small steady offset | kF slightly off | Fine-tune **kF** |

4. Release the button when done observing

### 2.3 Tuning Procedure

Start with the defaults (kP=0.1, kI=0, kD=0, kF=0.12). Tune in this order:

**Step 1 -- Feedforward (kF) first:**
- kF does most of the work for velocity control. It's the "open-loop" portion.
- Set kP=0 temporarily (in Shuffleboard, change "Flywheel kP" slider to 0)
- Click "Spin 50 RPS" and watch the steady-state RPS in AdvantageScope
- Adjust "Flywheel kF" until the flywheel *almost* reaches 50 RPS (within ~5 RPS)
- Formula estimate: kF ~ 1 / max_free_speed_RPS. For 100 RPS free speed, kF ~ 0.01. But motor controller kF units vary -- use the AdvantageScope response to dial it in.

**Step 2 -- Proportional (kP):**
- Set kP back to 0.1 (or start lower at 0.05)
- Click "Spin 50 RPS" -- the RPS should now reach and hold 50
- If it overshoots and oscillates, cut kP in half
- If it's slow to settle, increase kP by 50%
- Goal: reaches target within **1-2 seconds**, < 5% overshoot, no oscillation

**Step 3 -- Derivative (kD), only if needed:**
- If you can't eliminate overshoot with kP alone, add a small kD (try 0.01)
- kD dampens oscillation but can amplify sensor noise -- keep it small
- Increase until overshoot is gone, back off if the motor output gets jerky

**Step 4 -- Integral (kI), usually leave at 0:**
- Only add kI if there's a persistent steady-state error after kF + kP tuning
- If needed, use a very small value (0.001) -- kI can cause windup and instability

### 2.4 Verify Across Speeds

Once tuned at 50 RPS, verify the PID works at other speeds:
1. Click **"Spin 75 RPS"** -- should track smoothly
2. Use "Spin For Distance" with different target distances (1m, 3m, 5m) to test the full RPS range
3. The AdvantageScope curves should look similar at all speeds -- if not, kF may need tweaking

### 2.5 Record Your Final Values

Write down the final PID values from Shuffleboard:
- Flywheel kP: ______
- Flywheel kI: ______
- Flywheel kD: ______
- Flywheel kF: ______

Update these in `Constants.java` > `ShooterTestConstants` so they persist across deploys.

---

## Phase 3: Turret PID Tuning

**Goal:** Get the turret to move to commanded angles accurately without oscillation.

### 3.1 Set Up AdvantageScope for Turret

1. Open a new Line Chart tab in AdvantageScope
2. Drag onto the chart:
   - `/Shooter Test/Turret Angle` (actual position in degrees)
3. There's no "target angle" telemetry signal, so you'll visually compare against the commanded value

### 3.2 Tuning Procedure

Turret position control typically only needs **kP**.

**Step 1 -- Test with default kP=0.05:**
1. Click **"Turret 0 deg"** to zero the turret
2. Click **"Turret 90 deg"**
3. Watch the AdvantageScope chart:
   - Turret angle should ramp from 0 to 90 degrees
   - Should settle at 90 without oscillating back and forth

**Step 2 -- Adjust kP:**

| Behavior | Fix |
|----------|-----|
| Moves slowly, takes > 2 sec to reach target | Increase kP (try 0.1) |
| Overshoots and bounces around target | Decrease kP (try 0.02) |
| Reaches target and holds steady | kP is good |

**Step 3 -- Add kD only if oscillating:**
- If the turret oscillates around the target position even with low kP, add a small kD (try 0.005)
- The turret has a lot of inertia from the gear ratio, so this is usually not needed

**Step 4 -- Test the full range:**
1. Command 0 -> 90 -> 180 -> 90 -> 0 in sequence using the preset buttons
2. Each move should be smooth and settle within 1-2 seconds
3. Final position should match command (check "Turret Angle" telemetry)
4. Also test **"Aim Turret Manual"** -- hold it and slowly sweep the "Turret Angle (deg)" slider from 0 to 180. The turret should track smoothly without jerking.

### 3.3 Verify Soft Limits

1. Click "Turret 0 deg" -- turret should stop at 0, not keep going
2. Click "Turret 180 deg" -- turret should stop at 180, not keep going
3. If the turret doesn't reach the full range or goes past it, the gear ratio constant needs updating

### 3.4 Record Your Final Values

- Turret kP: ______
- Turret kI: ______ (probably 0)
- Turret kD: ______ (probably 0)

Update in `Constants.java` > `ShooterTestConstants`.

---

## Phase 4: Measure and Calibrate Constants

**Goal:** Set the physical constants to match the actual robot geometry.

### 4.1 Measure Exit Height

This is the most important constant to get right for the calculator mode.

1. Place a ball in the shooter at the exit point
2. Measure from the **floor** to the **center of the ball** in meters
3. Update "Exit Height (m)" in Shuffleboard (or update `kExitHeightMeters` in Constants.java)

Example: 18 inches = 0.4572 meters

### 4.2 Verify Gear Ratio

If the turret angle telemetry doesn't match the physical angle:

1. Mark the turret at its zero position
2. Command "Turret 90 deg"
3. Measure the actual physical angle with a protractor or angle finder
4. If it's off, calculate the correction:
   - If physical = 45 degrees but telemetry says 90: actual ratio = 100 * (90/45) = 200
   - If physical = 180 degrees but telemetry says 90: actual ratio = 100 * (90/180) = 50
5. Update `kTurretGearRatio` in Constants.java, rebuild and redeploy

### 4.3 Set Up Distance Markers

For the interpolation table calibration, set up distance markers on the floor:

1. Place tape marks at **1m, 2m, 3m, 4m, and 5m** from the hub AprilTag
2. These match the default table entries
3. Always measure from the **shooter exit point** to the **AprilTag on the hub face**
4. Use a tape measure -- be consistent

---

## Phase 5: Distance Calibration (Calculator First, Then Table)

**Goal:** Use the physics calculator as a starting point at each distance, then fine-tune with the table. Track the offset between calculator and reality to see if the model is usable.

### 5.1 Setup

1. Place the test board at the **3m mark** (start mid-range, not the extremes)
2. Aim the turret at the hub: hold **"Aim Turret Manual"** and adjust the **"Turret Angle (deg)"** slider until aligned, then release
3. Set "Target Distance (m)" to **3.0** in Shuffleboard
4. Load a ball into the shooter manually

### 5.2 Per-Distance Procedure

Repeat the following for each distance. Start at 3m, then do 2m, 4m, 1m, 5m (mid-range first so you can catch gross errors early).

**Step A -- Try the calculator prediction:**

1. Ensure mode shows **"CALCULATOR"** (click "Toggle Mode" if it shows "TABLE")
2. Read the **"Calc RPS"** value in Shuffleboard -- this is the physics prediction. Write it down.
3. Click **"Spin For Distance"** and hold
4. Wait for **"At Speed" = true**
5. Feed a ball and observe the result:

| Result | What it tells you |
|--------|-------------------|
| Ball hits the target | Calculator is accurate at this distance |
| Ball falls short | Calculator RPS is too low -- needs more speed |
| Ball overshoots | Calculator RPS is too high -- needs less speed |

6. Release "Spin For Distance"
7. Record: Distance = ____ m, Calc RPS = ____, Result = (short / hit / long)

**Step B -- Switch to table mode and fine-tune:**

1. Click **"Toggle Mode"** to switch to **"TABLE"**
2. Find the table entry closest to your current distance (e.g., "Table Dist 3" and "Table RPS 3")
3. Set "Table RPS 3" to the calculator's value as a starting point
4. Click **"Spin For Distance"** and hold, feed a ball
5. Adjust the table RPS based on result:

| Result | Adjustment |
|--------|-----------|
| Ball falls short of hub | Increase table RPS by 2-3 |
| Ball overshoots hub | Decrease table RPS by 2-3 |
| Ball hits the target | This distance is dialed in |

6. Release, adjust, and repeat until you get **3 consecutive makes**
7. Record the final tuned RPS

**Step C -- Note the offset:**

Calculate: `offset = tuned_RPS - calculator_RPS`

This tells you how far off the physics model is at this distance.

8. Move to the next distance and repeat from Step A.

### 5.3 Analyze the Offsets

After calibrating all distances, fill in this table:

| Distance (m) | Calc RPS | Tuned RPS | Offset (Tuned - Calc) |
|---------------|----------|-----------|----------------------|
| 1.0 | ________ | _________ | ________ |
| 2.0 | ________ | _________ | ________ |
| 3.0 | ________ | _________ | ________ |
| 4.0 | ________ | _________ | ________ |
| 5.0 | ________ | _________ | ________ |

**Interpreting the offsets:**

| Pattern | Meaning | Fix |
|---------|---------|-----|
| All offsets are near zero (< 3 RPS) | Calculator is accurate -- you can trust it at untested distances | No change needed |
| All offsets are roughly the same positive number | Calculator consistently underestimates by a fixed amount | Decrease "Exit Vel Multiplier" (ball loses more energy than modeled) |
| All offsets are roughly the same negative number | Calculator consistently overestimates | Increase "Exit Vel Multiplier" |
| Offsets grow with distance | Air resistance (not modeled) matters at longer range | Use the table for real shots; calculator is only useful at short range |
| Offsets are random / inconsistent | Ball feed or mechanism inconsistency | Focus on repeatable ball feeding; re-test with more shots per distance |

If you find a consistent offset pattern, adjust **"Exit Vel Multiplier"** in Shuffleboard and re-run the calculator at each distance to see if the offsets shrink. The goal is to get the calculator close enough that it gives a useful starting RPS at distances you haven't explicitly calibrated.

### 5.4 Record Final Table

Update the table entries in Shuffleboard to your tuned values, then record them here:

| Distance (m) | RPS |
|---------------|-----|
| _____ | _____ |
| _____ | _____ |
| _____ | _____ |
| _____ | _____ |
| _____ | _____ |

Update `kDefaultInterpolationTable` in Constants.java with these values.
If you adjusted Exit Vel Multiplier, update `kExitVelocityMultiplier` too.

---

## Phase 6: Full Shooting Sequence Test

**Goal:** Run through a complete shooting sequence to validate the full system.

### 6.1 Static Shooting (Turret Fixed)

1. Place board at 3m from hub
2. Click **"Turret 90 deg"** to aim center
3. Set "Target Distance (m)" to 3.0
4. Click **"Spin For Distance"** and hold
5. Wait for "At Speed" = true
6. Feed ball -- observe result
7. Release "Spin For Distance"

### 6.2 Turret Sweep Test

Turret commands run independently of flywheel commands, so you can aim and spin simultaneously.

1. Click **"Turret 0 deg"** to start at one end
2. Click **"Spin 50 RPS"** and hold (flywheel spins up while turret stays at 0)
3. Once at speed, click **"Turret 90 deg"** -- turret moves while flywheel keeps spinning
4. Feed ball at 90 degrees
5. Click **"Turret 180 deg"** -- turret moves to other end
6. Feed another ball at 180 degrees
7. Release spin, click **"Stop Turret"**

You can also use **"Aim Turret Manual"** with the slider to sweep to arbitrary angles while the flywheel holds speed.

### 6.3 Multi-Distance Run

Run through all your calibrated distances in sequence:
1. Start at 1m, spin for distance, shoot, verify make
2. Move to 2m, repeat
3. Continue through all distances
4. Track your make percentage at each distance -- you want **> 80%** before declaring the table calibrated

---

## Adjusting Constants

After testing, update these values in `src/main/java/com/adambots/Constants.java` inside `ShooterTestConstants` so they persist across code deploys:

```java
// From Phase 2 -- PID gains
public static final double kFlywheelP = /* your value */;
public static final double kFlywheelI = /* your value */;
public static final double kFlywheelD = /* your value */;
// kFlywheelFF is calculated (kNominalVoltage / kMotorFreeSpeedRPS).
// Only change kMotorFreeSpeedRPS if you swap motor type.

// From Phase 3
public static final double kTurretP = /* your value */;

// From Phase 4
public static final double kExitHeightMeters = /* measured value */;
public static final double kTurretGearRatio = /* verified ratio */;

// From Phase 5
public static final double[][] kDefaultInterpolationTable = {
    {1.0, /* tuned RPS */},
    {2.0, /* tuned RPS */},
    {3.0, /* tuned RPS */},
    {4.0, /* tuned RPS */},
    {5.0, /* tuned RPS */}
};

// From Phase 5 (if adjusted)
public static final double kExitVelocityMultiplier = /* adjusted value */;
```

---

## Troubleshooting

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| Motor doesn't spin | Bad CAN wiring or wrong ID | Check Phoenix Tuner, verify CAN IDs |
| Flywheel oscillates wildly | kP too high | Cut kP in half |
| Flywheel never reaches speed | kF too low | Increase kF |
| Turret overshoots position | kP too high or gear ratio wrong | Lower kP; verify gear ratio |
| Turret drifts after stopping | Brake mode not set | Check `brakeMode(true)` in code |
| "At Speed" never shows true | Tolerance too tight or PID not tuned | Increase "Flywheel Tolerance" or tune PID first |
| Shots inconsistent at same distance | Ball feed inconsistency or flywheel not at speed | Wait longer for "At Speed", feed balls consistently |
| Flywheel Amps spikes > 60A | Mechanical jam or current limit too high | Stop immediately, check for obstruction |
| Flywheel Temp > 80C | Running too long at high RPS | Let motors cool, shorter test runs |
