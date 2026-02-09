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

---

## Evolutionary AI Learning System

### Overview
Replace the deterministic AI with a neural network-based system that learns through evolutionary algorithms. Multiple planes (population of 10) compete simultaneously, with the best performers breeding the next generation.

### Phase 1: Raycast Sensor System

#### 1.1 Implement Raycast Sensors
- Add 6 raycasts from plane position:
  - **Ray 0**: Straight ahead (0°) - 150 pixels
  - **Ray 1**: 25° up - 150 pixels
  - **Ray 2**: 25° down - 150 pixels
  - **Ray 3**: 45° up - 150 pixels
  - **Ray 4**: 45° down - 150 pixels
  - **Ray 5**: Straight down (90°) - detects ground/bottom of screen
- Each ray returns distance to collision (0.0 to 1.0, normalized by max distance)
- Collision detection against:
  - Top/bottom obstacles (mountains)
  - Top of screen (y=0)
  - Bottom of screen (y=240)

#### 1.2 Visual Debug Display
- Draw raycasts as lines from plane
- **Green line**: No collision detected (full ray length)
- **Red line**: Collision detected (stops at collision point)
- Toggle visibility with existing debug controls
- Only show rays for visible planes

### Phase 2: Neural Network Architecture

#### 2.1 Network Structure
- **Input layer**: 7 neurons
  - 6 raycast distances (normalized 0.0-1.0)
  - 1 vertical velocity (normalized -1.0 to 1.0)
- **Hidden layer**: 10 neurons (single hidden layer)
  - Activation: tanh or sigmoid
- **Output layer**: 1 neuron
  - Activation: sigmoid (0.0-1.0)
  - Threshold: >0.5 = jump, ≤0.5 = no jump
- Total weights: (7×10) + (10×1) = 80 weights + 11 biases = 91 parameters

#### 2.2 Implementation Options
- **Option A**: Code from scratch (simple feedforward network, ~100 lines)
- **Option B**: Use lightweight Java library (Neuroph, DeepNets, or similar)
- Decision: Choose based on simplicity and JAR size impact

#### 2.3 Jump Debouncing
- Implement same debouncing as player input
- Minimum 10-15 frames between jumps
- Prevents unnatural rapid jumping

### Phase 3: Evolutionary Algorithm

#### 3.1 Population Management
- **Population size**: 10 planes
- Each plane gets unique color from available plane sprites
- Each plane has its own neural network (randomly initialized weights)
- All planes start at same position when generation begins

#### 3.2 Fitness Function
- **Score component**: Points earned (obstacles passed × 10)
- **Survival component**: Frames survived
- **Formula**: `fitness = score + (survivalTime / 10)`
- Higher fitness = better performance

#### 3.3 Generation Lifecycle
1. All 10 planes fly simultaneously
2. Plane crashes when:
   - Hits obstacle
   - Hits top/bottom of screen
   - Falls off screen
3. Generation ends when all planes crash OR work timer ends
4. Rank planes by fitness
5. Breed next generation

#### 3.4 Selection & Breeding
- **Selection**: Top 3 planes become parents
- **Breeding strategy**:
  - Best plane (rank 1): Clone directly (elitism) → 1 offspring
  - Ranks 1-3: Crossover pairs → 6 offspring
    - Parent1 + Parent2 → 2 children
    - Parent1 + Parent3 → 2 children
    - Parent2 + Parent3 → 2 children
  - Random mutations: 3 offspring from random parents
- **Crossover**: Uniform crossover (50% chance per weight from each parent)
- **Mutation rate**: 15% chance per weight
- **Mutation amount**: Add random value from Gaussian distribution (mean=0, stddev=0.3)

### Phase 4: UI Integration

#### 4.1 Mode Toggle
- Press **'A'** key to toggle between AI modes:
  - **Default**: Evolutionary AI (10 planes) - ML learning mode
  - **Classic**: Original deterministic AI (single plane)
- Mode persists during work session, resets to ML mode on new work session

#### 4.2 Visual Display
- Show all 10 planes simultaneously
- Each plane renders with different color
- Show raycasts for all planes (can be toggled off for performance)
- Display generation stats overlay:
  - Current generation number
  - Planes alive / total
  - Best fitness this generation
  - Best fitness all time

#### 4.3 State Management
- Neural networks and fitness scores kept in memory only
- No persistence between app restarts
- Reset to generation 1 when starting new work session

### Phase 5: Testing & Tuning

#### 5.1 Verify Raycast Accuracy
- Visual inspection: rays turn red when near obstacles
- Test edge cases: corners, screen boundaries

#### 5.2 Monitor Learning Progress
- Generation 1: Random flailing, most crash immediately
- Generation 5-10: Some planes learn to jump
- Generation 20+: Consistent obstacle navigation
- If no improvement after 20 generations: adjust mutation rate or network architecture

#### 5.3 Performance Optimization
- Ensure 60 FPS with 10 planes + raycasts
- Consider disabling raycast rendering after debugging
- Profile neural network forward pass performance

### Implementation Order
1. Raycast system + visual debug (Phase 1)
2. Neural network implementation (Phase 2)
3. Single plane with NN (test before population)
4. Population + evolutionary algorithm (Phase 3)
5. UI toggle and stats display (Phase 4)
6. Testing and tuning (Phase 5)
