# Pomodoro Timer Game Enhancement Tasks

## Overview
Transform the boring Swing timer into a fun animated game using Kenny's Tappy Plane assets.
- Work time: Plane flies through scrolling sky with obstacles (auto-playing flappy bird style)
- Break time: Plane lands and rests
- Window size: 320x240

## Tasks

### 1. Create GamePanel class ✅
- Custom JPanel with double buffering for smooth animation
- Load and manage all image assets (plane, background, rocks, ground)
- Handle animation timing with Timer
- Size: 320x240

### 2. Implement scrolling background during work time ✅
- Load background.png and make it scroll horizontally (right to left)
- Seamless looping background
- Scroll speed: ~2-3 pixels per frame

### 3. Implement plane animation ✅
- Load plane sprites (planeBlue1, planeBlue2, planeBlue3 for animation)
- Animate plane propeller by cycling through sprites
- Plane position: fixed X (around 80px), animated Y with sine wave for smooth flight
- Bobbing motion during work time

### 4. Add auto-playing flappy bird obstacles ✅
- Spawn rock obstacles (rock.png, rockDown.png) at intervals
- Obstacles scroll from right to left
- Plane automatically "navigates" through gaps using sine wave pattern
- Random gap positions but always passable

### 5. Implement break time landing animation ✅
- Plane descends smoothly to ground level
- Show ground sprite (groundGrass.png)
- Plane stays stationary on ground
- Background stops scrolling

### 6. Integrate GamePanel into AppWindow ✅
- Replace text-based timer display with GamePanel
- Overlay timer text on game (semi-transparent or corner display)
- Keep existing skip/exit buttons functionality
- Set window to 320x240

### 7. Copy assets to resources folder ✅
- Copy required PNG images from images/PNG/ to src/main/resources/
- Ensure images are bundled in JAR

### 8. Polish and testing ✅
- Smooth transitions between work/break states
- Verify timer accuracy with animations
- Test skip functionality
- Ensure proper resource cleanup

## Status: COMPLETE

---

## Testing Phase ✅

### Test Infrastructure ✅
Created `GamePanelTest` class that:
- Instantiates GamePanel without displaying GUI
- Provides public accessor methods to:
  - `step(int frames)` - advance game by N frames (each frame = 30ms)
  - `getPlaneY()` - read current plane Y position
  - `getObstacles()` - read list of obstacles with their positions
  - `getFrameCount()` - read current frame counter
  - `isWorking()` - check if in work mode
  - `setObstacle(int x, int gapY)` - manually add obstacle for testing

### Test Cases ✅

#### T1: Obstacle Spawning ✅
- **Setup**: Start work mode, frame counter at 0
- **Action**: Step 120 frames (4 seconds)
- **Expected**: At least 1 obstacle spawned at x=320
- **Verify**: `getObstacles().size() > 0`
- **Result**: PASS

#### T2: Obstacle Movement ✅
- **Setup**: Manually add obstacle at x=320, step 10 frames
- **Action**: Check obstacle position
- **Expected**: Obstacle moved left by ~30 pixels (3px/frame * 10 frames)
- **Verify**: `obstacle.x == 290` (±2 tolerance)
- **Result**: PASS

#### T3: Obstacle Removal ✅
- **Setup**: Add obstacle, step until x < -50
- **Action**: Check obstacle list
- **Expected**: Obstacle removed from list
- **Verify**: `getObstacles().size() == 0`
- **Result**: PASS

#### T4: Plane Stays in Bounds ✅
- **Setup**: Start work mode, step 300 frames (10 seconds)
- **Action**: Check plane Y position throughout
- **Expected**: Plane Y always between 10 and 200
- **Verify**: `planeY >= 10 && planeY <= 200` for all steps
- **Result**: PASS

#### T5: Plane Navigates to Gap ✅
- **Setup**: Plane at y=100, manually add obstacle with gap at y=80
- **Action**: Step 20 frames
- **Expected**: Plane moves upward toward gap (planeY decreases)
- **Verify**: `planeY < 100` after stepping
- **Result**: PASS

#### T6: Plane Avoids Top Pipe ✅
- **Setup**: Plane at y=50, obstacle with gap at y=120 (top pipe blocks)
- **Action**: Step 30 frames
- **Expected**: Plane moves down away from top pipe
- **Verify**: `planeY > 50` after stepping
- **Result**: PASS

#### T7: Plane Avoids Bottom Pipe ✅
- **Setup**: Plane at y=150, obstacle with gap at y=80 (bottom pipe blocks)
- **Action**: Step 30 frames
- **Expected**: Plane moves up away from bottom pipe
- **Verify**: `planeY < 150` after stepping
- **Result**: PASS

#### T8: Break Mode Stops Obstacles ✅
- **Setup**: Start work, spawn obstacles, then call startBreak()
- **Action**: Step 100 frames
- **Expected**: No new obstacles spawn, existing ones still move
- **Verify**: `getObstacles().size()` stays constant
- **Result**: PASS

#### T9: Break Mode Plane Lands ✅
- **Setup**: Start break mode at planeY=100
- **Action**: Step 60 frames
- **Expected**: Plane descends to y=200 (landing position)
- **Verify**: `planeY >= 200` after 60 frames
- **Result**: PASS (Fixed landing speed to 4px/frame)

#### T10: Work Mode Resets State ✅
- **Setup**: Add obstacles, set planeY=50, call startWork()
- **Action**: Check state
- **Expected**: Obstacles cleared, plane reset to y=100, frameCounter reset
- **Verify**: `getObstacles().size() == 0 && getPlaneY() == 100 && getFrameCount() == 0`
- **Result**: PASS

### Test Execution ✅
- All 10 tests pass successfully
- GamePanel logic verified and working correctly
- Landing animation fixed to ensure plane reaches y=200


## High Score Table Feature

### Requirements:
1. **Name Generation**
   - Create array of ~20 first names (flying-themed: Ace, Sky, Jet, Maverick, etc.)
   - Create array of ~20 last names (flying-themed: Bomber, Diver, Eagle, Falcon, etc.)
   - Assign random name to player when game starts (not displayed yet)

2. **High Score Table**
   - Store in memory as array/list of 5 entries
   - Each entry: name + score
   - On app start: populate with 5 random names and scores (200, 150, 100, 75, 50 - multiples of 10)
   - When player achieves high score: insert at appropriate position, remove lowest score
   - Keep only top 5 scores

3. **Display During Break Phase**
   - Scroll high score table along bottom of screen at ground image height
   - Use existing letter/number images (same as timer/score display)
   - Format: "1: ACE BOMBER 200  2: SKY EAGLE 150..." (using colon since no period available)
   - Hide when work phase begins
   - Show throughout entire break period

4. **Persistence**
   - Consider saving high score table to file (like current high score)
   - Load on startup if exists
