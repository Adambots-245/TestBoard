# Vision System Test Guide (OrangePi + PhotonVision)

Step-by-step procedure for setting up and testing the AprilTag vision system on the test board using an OrangePi coprocessor. Work through each phase in order.

---

## Prerequisites

- OrangePi wired to the robot radio or directly to the roboRIO via Ethernet
- USB camera connected to the OrangePi
- Test board powered on, roboRIO connected
- Laptop on the same network as the robot
- A printed AprilTag (or the actual hub with tags) visible from the camera's position

---

## Phase 1: PhotonVision Installation

**Goal:** Get PhotonVision running on the OrangePi.

1. Flash the OrangePi with the latest PhotonVision image:
   - Download from [photonvision.org/downloads](https://photonvision.org)
   - Flash to the OrangePi's SD card using Balena Etcher or `dd`
2. Power on the OrangePi and connect it to the robot network via Ethernet
3. Wait ~60 seconds for it to boot
4. Open a browser and navigate to `http://photonvision.local:5800`
   - If `.local` doesn't resolve, find the OrangePi's IP (check your router or use `arp -a`) and go to `http://<ip>:5800`
5. Verify the PhotonVision web dashboard loads and shows a camera feed

**If no camera feed:** Check USB connection, try a different USB port. Verify the camera appears in the PhotonVision Hardware tab.

---

## Phase 2: Camera Configuration

**Goal:** Name the camera and configure basic settings so the code can find it.

### 2.1 Set Camera Name

1. In PhotonVision web UI, go to **Settings > Camera**
2. Set the camera name to **`OrangePi`** (must match `VisionConstants.kCameraName` exactly — case-sensitive)
3. Click Save

### 2.2 Set Pipeline to AprilTag

1. Go to the **Vision** tab
2. Select or create a pipeline of type **AprilTag**
3. Set it as the active pipeline
4. Under pipeline settings:
   - **Tag Family:** `36h11` (standard for FRC)
   - **Decimate:** 2 (good balance of speed and range — increase to 1 for more range but slower)
5. Verify that if any AprilTags are visible, they appear as green outlines in the camera feed

### 2.3 Set Team Number

1. Go to **Settings > Networking**
2. Set Team Number to your team number
3. This ensures NetworkTables connects properly to the roboRIO

---

## Phase 3: Camera Calibration

**Goal:** Calibrate the camera's intrinsic parameters (focal length, distortion) so PhotonVision can accurately compute 3D poses from tag detections.

### 3.1 Print a Calibration Board

1. In the PhotonVision web UI, go to **Cameras > Calibration**
2. Download the recommended charuco or checkerboard pattern
3. Print it on a **flat, rigid surface** (tape it to a clipboard or foam board)
4. Measure the actual square size with calipers and enter it in the calibration dialog — don't trust the printer scaling

### 3.2 Run Calibration

1. Select the resolution you plan to use (e.g., 640x480 or 1280x720)
   - Higher resolution = more range but slower framerate
   - 640x480 at 30fps is a good starting point
2. Click **Start Calibration**
3. Hold the calibration board in front of the camera at varying:
   - **Distances** (close, mid, far)
   - **Angles** (tilted left, right, up, down)
   - **Positions** (corners and center of the frame)
4. Capture at least **12-15 images** covering the full frame
5. Click **Finish Calibration** and wait for it to process

### 3.3 Verify Calibration Quality

1. Check the **reprojection error** — should be < 1.0 px, ideally < 0.5 px
2. If error is > 1.0 px, recalibrate with more images and better coverage
3. A bad calibration will cause inaccurate distance and pose estimates

| Reprojection Error | Quality | Action |
|-------------------|---------|--------|
| < 0.3 px | Excellent | Good to go |
| 0.3 - 0.5 px | Good | Acceptable |
| 0.5 - 1.0 px | Fair | Consider recalibrating |
| > 1.0 px | Poor | Recalibrate — results will be unreliable |

---

## Phase 4: Measure Camera Mount Constants

**Goal:** Measure the physical camera position on the robot so the code can transform camera coordinates to robot coordinates.

These measurements go into `Constants.java` > `VisionConstants`.

### 4.1 Camera Forward Offset

1. Measure from the **robot center** (turret pivot) to the **camera lens**, along the forward axis
2. Positive = camera is in front of robot center
3. Update `kCameraForwardOffsetMeters` (currently marked `TODO: measure`)

### 4.2 Camera Height

1. Measure from the **floor** to the **camera lens center**, in meters
2. Update `kCameraHeightMeters` (currently marked `TODO: measure`)

### 4.3 Camera Pitch

1. If the camera is tilted up or down, measure the angle from horizontal
2. Positive pitch = camera tilted **down** (common for seeing tags above you)
3. Update `kCameraPitchRadians` — use `Math.toRadians(degrees)` for convenience

### 4.4 Lateral Offset (if applicable)

1. If the camera is not centered left-right on the robot, measure the offset
2. Positive Y = camera is to the **left** of robot center (WPILib convention)
3. Update the `Translation3d` Y value in `kRobotToCamera` (currently 0)

### 4.5 Record Your Measurements

```
Camera Forward Offset: ______ m
Camera Height:         ______ m
Camera Pitch:          ______ degrees
Camera Lateral Offset: ______ m (0 if centered)
```

Update the constants in `VisionConstants` and rebuild.

---

## Phase 5: Verify Camera Connection

**Goal:** Confirm the roboRIO code can see the camera and receive tag detections.

### 5.1 Deploy and Enable

1. Build and deploy: `./gradlew deploy`
2. Open Driver Station, connect to the robot
3. Open Shuffleboard and select the **Vision** tab
4. Enable **Teleop** mode

### 5.2 Check Basic Connectivity

1. Point the camera at an AprilTag (hold one in front of the camera if no hub is available)
2. Watch the Vision tab in Shuffleboard:

| Widget | Expected Value |
|--------|---------------|
| Tags | > 0 (number of visible hub tags) |
| Alliance | Red or Blue (must match your DS setting) |
| Cam Target | Green (if tag is a hub tag and within 8m) |
| Pose Target | Green (if tag is a hub tag and within 8m) |

3. If **Tags = 0** but PhotonVision web UI shows detections:
   - Verify camera name matches (`OrangePi` in both PhotonVision and Constants)
   - Check that the detected tag IDs are in the hub tag set for your alliance
   - Check NetworkTables connection (PhotonVision Settings > Networking)

4. If **Tags > 0** but targets are red:
   - Distance is likely > `kMaxDistanceMeters` (8.0m) — move closer
   - Or ambiguity is too high — check `kMaxAmbiguity` (0.2) and recalibrate if needed

---

## Phase 6: Distance and Angle Verification

**Goal:** Verify the vision system reports accurate distance and angle to the hub.

### 6.1 Set Up Distance Markers

1. Place tape marks on the floor at **1m, 2m, 3m, 4m, and 5m** from the hub center
2. Measure from the **robot center** (not the camera) to the **center of the hub face**

### 6.2 Straight-On Test (Angle ≈ 0°)

Place the robot at each distance, directly facing the hub:

1. Place robot at the **3m mark**, aimed straight at the hub
2. Read values from the Vision tab:

| Widget | What to Check |
|--------|--------------|
| Cam Distance | Should be slightly less than 3m (camera is forward of robot center) |
| Pose Distance | Should be ≈ 3.0m |
| Cam Angle | Should be ≈ 0° |
| Pose Angle | Should be ≈ 0° |
| Tags | Number of visible tags (more = more accurate) |

3. Record values and repeat at 1m, 2m, 4m, 5m:

| Distance (m) | Cam Dist | Pose Dist | Cam Angle | Pose Angle | Tags |
|--------------|----------|-----------|-----------|------------|------|
| 1.0 | ________ | _________ | _________ | __________ | ____ |
| 2.0 | ________ | _________ | _________ | __________ | ____ |
| 3.0 | ________ | _________ | _________ | __________ | ____ |
| 4.0 | ________ | _________ | _________ | __________ | ____ |
| 5.0 | ________ | _________ | _________ | __________ | ____ |

**Expected:** Pose Distance should match tape-measured distance within ~0.1m. Cam Distance will be offset by `kCameraForwardOffsetMeters`.

### 6.3 Angled Test

1. Place the robot at 3m, but offset to the side so the hub is at ~30° and ~45°
2. Verify Cam Angle and Pose Angle report reasonable values
3. The turret should need to rotate by approximately that angle to face the hub

### 6.4 Compare Approaches

After collecting data, decide which approach to use for competition:

| Approach A (Camera-Only) | Approach B (Pose-Based) |
|--------------------------|------------------------|
| Distance is from camera, not robot center | Distance is from robot center |
| Angle is relative to current heading | Angle is absolute turret setpoint |
| Works with single tags | Better with multiple tags (multi-tag PnP) |
| No pose estimator latency | Slight latency from pose estimation |

---

## Phase 7: Integration Test with Shooter

**Goal:** Use vision-reported distance and angle to aim the turret and set flywheel speed.

### 7.1 Approach A (Camera-Only)

1. Place robot at 3m from hub, slightly off-center
2. Click **"A: Set + Aim"** and hold
3. Verify:
   - Turret rotates toward the hub
   - "Target Distance" updates to the camera distance
   - Flywheel spins to the RPS for that distance
4. Feed a ball while holding the button
5. Observe accuracy — does the ball hit the target?

### 7.2 Approach B (Pose-Based)

1. Same setup as above
2. Click **"B: Set + Aim"** and hold
3. Verify the same behaviors
4. Feed a ball and observe

### 7.3 Compare

Test both approaches at 2m, 3m, and 4m with some angular offset. Note which approach:
- Aims the turret more accurately
- Reports more stable distance readings
- Results in more consistent shots

---

## Troubleshooting

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| PhotonVision UI doesn't load | OrangePi not booted or wrong IP | Wait 60s, check Ethernet, try IP directly |
| Camera feed is black | USB disconnected or wrong port | Reconnect USB, try different port |
| Tags = 0 but tags visible in PV UI | Camera name mismatch | Verify `OrangePi` in both PV settings and Constants |
| Tags = 0, nothing in PV UI | Pipeline not set to AprilTag | Switch pipeline type in PV |
| Tags > 0 but targets red | Distance > 8m or ambiguity too high | Move closer; check calibration quality |
| Distance readings jump around | Poor calibration or low tag count | Recalibrate camera; use more tags |
| Distance consistently off by fixed amount | Camera mount offset wrong | Re-measure `kCameraForwardOffsetMeters` |
| Angle consistently off | Camera yaw not accounted for | Check if camera is rotated; update `kRobotToCamera` rotation |
| Pose Distance accurate but Cam Distance off | Expected — different reference points | Cam measures from camera, Pose from robot center |
| Everything worked in sim but not on real robot | Camera mount constants are placeholder values | Complete Phase 4 measurements |
| Lag between robot movement and readings | Network or processing delay | Check PV framerate; reduce resolution if needed |

---

## Simulation vs Real Robot

Before connecting hardware, you can verify all the vision math using the built-in simulation:

1. Run `./gradlew simulateJava`
2. Open Shuffleboard, go to the **Vision** tab
3. **Enable Teleop** in the Driver Station sim (important — alliance defaults to blue otherwise)
4. Adjust **Sim X**, **Sim Y**, and **Sim Heading** to place the virtual robot on the field
5. **Expected Dist** and **Expected Angle** show pure-geometry ground truth
6. Compare against Cam/Pose readings to verify the math is correct
7. The **Field** widget (on SmartDashboard) shows robot position and hub center visually

If sim values match but real values don't, the issue is in camera calibration or mount measurements — not the code.
