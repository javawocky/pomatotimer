# Space Shooter Mode - Implementation Plan

## Overview
Create a top-down space shooter with Asteroids-style physics where AI-controlled ships must avoid falling meteors. Ships use rotation and thrust to navigate, with momentum-based movement. Uses Kenney's Space Shooter Redux assets and maintains Pomodoro timer integration.

## Game Mechanics Summary

### Core Gameplay
- **Objective**: Survive as long as possible while meteors rain down from top of screen
- **Scoring**: 10 points per meteor that exits bottom of screen
- **Difficulty**: Gradual ramp over ~3 minutes, no cap (infinite scaling)
- **Physics**: Asteroids-style momentum-based movement
- **Resolution**: 320x240 native (same as plane game)

### Ship Controls (AI-Controlled)
- **Rotation**: Left/right rotation (continuous)
- **Thrust**: Forward thrust from rear engine (on/off)
- **Momentum**: Ship maintains velocity, must counter-thrust to slow down
- **Fuel**: Unlimited
- **Boundaries**: Ship constrained within screen edges (can't fly off)

### Meteors
- **Spawn**: Above screen at y = -meteor_height, random X position
- **Movement**: Downward velocity + slight horizontal drift
- **Removal**: When fully off bottom of screen (y > 240)
- **Collision**: Pixel-perfect using alpha channel (irregular shapes)
- **Variety**: Use different meteor sizes from PNG/Meteors/

### AI System
- **Population**: 15 ships per generation (same as current plane game)
- **Colors**: Random ship color per AI from available variants
- **Evolution**: Same genetic algorithm as plane game (EvolutionManager)
- **Fitness**: Survival time + score (meteors passed)
- **Visualization**: Fitness graph + neural network display

### Neural Network
**Inputs (22 total):**
- 18 raycasts (every 20°, distance to nearest meteor, normalized)
- 2 velocity components (vx, vy normalized -1 to 1)
- 2 heading components (sin(angle), cos(angle) for smooth rotation)

**Outputs (2):**
- Rotation command (-1 = left, 0 = none, 1 = right)
- Thrust command (0 = off, 1 = on)

### Visual Elements
- **Background**: Scrolling space background (vertical)
- **Thruster**: Constructed from PNG/Parts/engine*.png, animated when active
- **Raycasts**: Only visible when hitting meteors (toggle with 'R')
- **UI**: Space Shooter UI assets (PNG/UI/ numerals, icons)

## Phase 1: Core Game Mechanics

### 1.1 Ship Physics & Movement
- [ ] Create Ship class with position, velocity, rotation, angular velocity
- [ ] Implement rotation (left/right input changes angular velocity)
- [ ] Implement thrust (forward acceleration based on rotation angle)
- [ ] Apply momentum/inertia (velocity persists, friction optional)
- [ ] Screen boundary collision (stop at edges or bounce)
- [ ] Load random ship sprite from playerShip*.png variants

### 1.2 Thruster System
- [ ] Load engine sprites (PNG/Parts/engine1-5.png)
- [ ] Position thruster at rear of ship based on rotation
- [ ] Animate thruster when thrust is active (cycle through engine sprites)
- [ ] Hide thruster when thrust is off
- [ ] Rotate thruster with ship

### 1.3 Meteor System
- [ ] Load meteor sprites (PNG/Meteors/ - various sizes)
- [ ] Meteor spawning above screen (y = -height, random x)
- [ ] Meteor movement (downward + slight horizontal drift)
- [ ] Meteor removal when off bottom screen
- [ ] Difficulty scaling (spawn rate increases over time)
- [ ] Meteor count increases over ~3 minutes, no cap

### 1.4 Collision Detection
- [ ] Pre-process meteor images to create alpha-based collision masks
- [ ] Bounding box broad-phase collision (ship vs meteors)
- [ ] Pixel-perfect narrow-phase collision using alpha channel
- [ ] Handle irregular meteor shapes
- [ ] Ship death on collision

### 1.5 Scoring
- [ ] Award 10 points when meteor exits bottom of screen
- [ ] Track score per ship (AI mode)
- [ ] High score system (reuse from plane game)
- [ ] Score display using Space Shooter UI numerals

## Phase 2: Visual Polish

### 2.1 Background
- [ ] Load space background from Backgrounds/ folder
- [ ] Implement vertical scrolling
- [ ] Multiple background layers for parallax (optional)
- [ ] Seamless looping

### 2.2 Effects
- [ ] Explosion animation when ship hits meteor (PNG/Effects/fire*.png)
- [ ] Particle effects for meteor destruction (optional)
- [ ] Thruster flame animation
- [ ] Screen shake on collision (optional)

### 2.3 UI Elements
- [ ] Timer display (top-left, reuse plane game logic)
- [ ] Score display (top-right, use PNG/UI/numeral*.png)
- [ ] High score display (top-center)
- [ ] Generation counter (AI mode)
- [ ] Alive ship counter (AI mode)
- [ ] Use Space Shooter UI assets for visual consistency

## Phase 3: Difficulty Progression

### 3.1 Meteor Spawning
- [ ] Start with 2-3 meteors on screen
- [ ] Increase spawn rate over time
- [ ] Increase meteor count every N seconds
- [ ] No difficulty cap (infinite scaling like plane game)
- [ ] Target: ~3 minutes to overwhelming difficulty

### 3.2 Meteor Variety
- [ ] Use different meteor sizes (big, med, small, tiny)
- [ ] Vary meteor speeds slightly
- [ ] Mix brown and grey meteors
- [ ] Larger meteors = harder to avoid but more points (optional)

## Phase 4: AI Integration

### 4.1 Neural Network Adaptation
- [ ] Adapt NeuralNetwork class for 22 inputs, 2 outputs
- [ ] Input layer: 18 raycasts + 2 velocity + 2 heading
- [ ] Output layer: rotation + thrust
- [ ] Hidden layers: reuse existing architecture (16, 8 neurons)

### 4.2 Raycast System
- [ ] Implement 18 raycasts (every 20°) from ship center
- [ ] Detect nearest meteor in each direction
- [ ] Normalize distances to -1 to 1 (or 0 to 1)
- [ ] Visualize raycasts (toggle with 'R', only show when hitting)
- [ ] Color-code by distance (like plane game)

### 4.3 AI Ship Class
- [ ] Create AIShip class (similar to AIPlane)
- [ ] Integrate neural network for decision making
- [ ] Calculate fitness: survival time + score
- [ ] Track alive/dead state
- [ ] Different colors for visual distinction

### 4.4 Evolution Manager
- [ ] Reuse EvolutionManager from plane game
- [ ] 15 ships per generation
- [ ] Genetic algorithm: selection, crossover, mutation
- [ ] Track fitness history
- [ ] Evolve when all ships dead

### 4.5 Visualizations
- [ ] Fitness graph (bottom-left, reuse from plane game)
- [ ] Neural network visualization (bottom-right, adapt for new inputs/outputs)
- [ ] Show best ship's brain activity
- [ ] Toggle with 'N' key

## Phase 5: Game Modes & Integration

### 5.1 AI Learning Mode (Primary)
- [ ] Spawn 15 AI ships at start
- [ ] All ships learn simultaneously
- [ ] Generation evolution on all deaths
- [ ] Display generation number and alive count
- [ ] Turbo mode for faster training ('T' key)

### 5.2 Pomodoro Integration
- [ ] Work phase: AI learning active
- [ ] Break phase: Peaceful space scene (no meteors)
- [ ] Timer display and countdown
- [ ] Alarm system (reuse from plane game)
- [ ] Work/break transitions

### 5.3 Controls & Settings
- [ ] 'R' - Toggle raycast visualization
- [ ] 'N' - Toggle neural network visualization
- [ ] 'T' - Toggle turbo mode
- [ ] 'F' - Toggle fullscreen
- [ ] 'B' - Toggle background rendering (performance)
- [ ] Click - Show/hide UI buttons

## Phase 6: Performance Optimization

### 6.1 Rendering Optimizations
- [ ] Apply lessons from plane game optimizations
- [ ] Cache rotated ship sprites (or use AffineTransform efficiently)
- [ ] Sprite caching for meteors
- [ ] Efficient collision detection (spatial partitioning if needed)
- [ ] Batch rendering where possible

### 6.2 Collision Optimization
- [ ] Pre-compute alpha masks for meteors
- [ ] Spatial grid for broad-phase collision
- [ ] Only check nearby meteors
- [ ] Cache bounding boxes

## Phase 7: Testing & Refinement

### 7.1 Balance
- [ ] Tune difficulty ramp (target ~3 minutes)
- [ ] Adjust meteor spawn rates
- [ ] Balance ship rotation and thrust speeds
- [ ] Test AI learning convergence

### 7.2 Physics Tuning
- [ ] Adjust ship momentum/friction
- [ ] Tune rotation speed
- [ ] Tune thrust acceleration
- [ ] Test feel of controls

### 7.3 AI Training
- [ ] Verify neural network learns effectively
- [ ] Tune mutation rate if needed
- [ ] Adjust fitness function if needed
- [ ] Target: AI learns to survive 1-2 minutes within 5 generations

## Asset Strategy

### Reuse from Plane Game (Shared Code)
- **Neural Network** - NeuralNetwork.java (adapt to 22 inputs, 2 outputs)
- **Evolution Manager** - EvolutionManager.java (reuse genetic algorithm)
- **High Score System** - Existing high score tracking and display logic
- **Alarm/Sound System** - Existing alarm for timer
- **Timer System** - Pomodoro timer logic
- **Optimization Techniques** - Caching, rendering optimizations

### Space Shooter Pack Assets (No Plane Game Assets)
- **UI Elements** - PNG/UI/ (numerals, buttons, life icons)
- **Player Ships** - PNG/playerShip*.png (blue, green, orange, red)
- **Meteors** - PNG/Meteors/ (brown/grey, big/med/small/tiny)
- **Engines** - PNG/Parts/engine*.png (thruster animation)
- **Backgrounds** - Backgrounds/ (black, blue, darkPurple, purple)
- **Effects** - PNG/Effects/ (explosions, fire animations)
- **Sound Effects** - Bonus/ (laser, shield, lose sounds) - optional

### No Asset Sharing
- Space shooter completely self-contained with its own assets
- Can run independently without plane game resources
- All visuals from Space Shooter pack

## Technical Specifications

### Resolution & Display
- Native: 320x240 pixels
- Scales to window size maintaining aspect ratio
- 60 FPS target
- Letterbox black bars if needed

### Physics Constants (Initial Values - Tune During Testing)
- Ship rotation speed: ~3-5 degrees per frame
- Thrust acceleration: ~0.2-0.3 pixels/frame²
- Max velocity: ~5-8 pixels/frame
- Friction: 0.98-0.99 (optional, or zero friction for pure momentum)
- Meteor fall speed: 1-3 pixels/frame (varies by size)
- Meteor horizontal drift: ±0.5 pixels/frame

### Difficulty Scaling
- Start: 2-3 meteors on screen
- Increase: +1 meteor every 10 seconds (tune as needed)
- Spawn rate: Starts at 120 frames, decreases to minimum 30 frames
- Target: Overwhelming by 3 minutes (~18+ meteors)

### Neural Network Architecture
- Input: 22 neurons (18 raycasts + 2 velocity + 2 heading)
- Hidden 1: 16 neurons (tanh activation)
- Hidden 2: 8 neurons (tanh activation)
- Output: 2 neurons (rotation: tanh, thrust: sigmoid)

## Success Criteria
- Smooth Asteroids-style physics with momentum
- AI learns to avoid meteors within 5 generations
- Difficulty scales to overwhelming over ~3 minutes
- 60 FPS performance with 15 ships + many meteors
- Pixel-perfect collision with irregular meteor shapes
- Seamless Pomodoro timer integration
- Visual polish matches plane game quality
