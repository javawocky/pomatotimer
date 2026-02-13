# Space Shooter Mode - Implementation Plan

## Overview
Create a top-down space shooter game mode using Kenney's Space Shooter Redux assets, maintaining the Pomodoro timer integration and AI learning capabilities from the plane game.

## Phase 1: Core Game Mechanics
### 1.1 Player Ship
- [ ] Load player ship sprite from PNG assets
- [ ] Implement horizontal movement (left/right arrow keys or A/D)
- [ ] Implement vertical movement (up/down arrow keys or W/S)
- [ ] Keep ship within screen boundaries
- [ ] Add ship animation/rotation if applicable

### 1.2 Shooting Mechanics
- [ ] Implement bullet/laser firing (spacebar)
- [ ] Load bullet/laser sprite
- [ ] Bullet movement (upward)
- [ ] Bullet lifecycle (remove when off-screen)
- [ ] Fire rate limiting/cooldown

### 1.3 Enemy System
- [ ] Load enemy ship sprites (multiple types)
- [ ] Enemy spawning system (top of screen)
- [ ] Enemy movement patterns (downward, side-to-side, etc.)
- [ ] Enemy health system
- [ ] Enemy destruction animation/effects

### 1.4 Collision Detection
- [ ] Player bullet vs enemy collision
- [ ] Enemy vs player collision (game over)
- [ ] Enemy bullet vs player collision (if enemies shoot back)

### 1.5 Scoring
- [ ] Points per enemy destroyed
- [ ] High score tracking (reuse existing system)
- [ ] Score display

## Phase 2: Visual Polish
### 2.1 Background
- [ ] Load space background from Backgrounds folder
- [ ] Scrolling starfield effect
- [ ] Multiple background layers for parallax

### 2.2 Effects
- [ ] Explosion animations (from Bonus folder)
- [ ] Particle effects for hits
- [ ] Screen shake on player hit
- [ ] Muzzle flash for shooting

### 2.3 UI Elements
- [ ] Health/lives display
- [ ] Score display (reuse existing text system)
- [ ] Timer display (existing Pomodoro timer)
- [ ] Game over screen

## Phase 3: Difficulty Progression
### 3.1 Enemy Waves
- [ ] Wave-based spawning system
- [ ] Increase enemy count over time
- [ ] Increase enemy speed over time
- [ ] Introduce tougher enemy types progressively

### 3.2 Difficulty Scaling
- [ ] Similar to plane game: ramp up every N seconds
- [ ] More enemies per wave
- [ ] Faster enemy movement
- [ ] Enemies start shooting back (optional)

## Phase 4: AI Integration
### 4.1 Neural Network Adaptation
- [ ] Adapt existing neural network for top-down movement
- [ ] Input sensors: enemy positions, bullet positions, player position
- [ ] Output: movement direction (4-way or 8-way), shoot decision
- [ ] Fitness function: survival time + enemies destroyed

### 4.2 AI Training Mode
- [ ] Multiple AI ships learning simultaneously (like plane mode)
- [ ] Different colored ships for visual distinction
- [ ] Genetic algorithm for evolution (reuse existing EvolutionManager)
- [ ] Fitness graph display (reuse existing)
- [ ] Neural network visualization (adapt existing)

### 4.3 AI Behaviors
- [ ] Dodge incoming enemies
- [ ] Target and shoot enemies
- [ ] Avoid screen edges
- [ ] Prioritize high-value targets

## Phase 5: Game Modes
### 5.1 Work Phase
- [ ] AI learning mode (default)
- [ ] Manual control mode (press key to take over)
- [ ] Difficulty increases throughout work session
- [ ] Landing/victory sequence in final 5 seconds

### 5.2 Break Phase
- [ ] Peaceful space scene with drifting ships
- [ ] High score table scrolling display
- [ ] No combat during break

### 5.3 Mode Toggle
- [ ] Key to switch between plane game and space shooter
- [ ] Or separate game mode selection at start

## Phase 6: Integration & Polish
### 6.1 Pomodoro Integration
- [ ] Maintain existing timer functionality
- [ ] Work/break phase transitions
- [ ] Timer display
- [ ] Alarm system (reuse existing)

### 6.2 Settings & Controls
- [ ] Control scheme display
- [ ] Difficulty settings
- [ ] Toggle AI mode vs manual mode
- [ ] Fullscreen support (existing)
- [ ] Turbo mode for AI training (existing)

### 6.3 Performance
- [ ] Optimize rendering (apply lessons from plane game)
- [ ] Sprite caching
- [ ] Efficient collision detection
- [ ] Night mode support (optional - space is already dark!)

## Phase 7: Testing & Refinement
- [ ] Balance difficulty curve
- [ ] Tune AI learning parameters
- [ ] Test performance with many entities on screen
- [ ] Playtest manual mode
- [ ] Verify Pomodoro timer accuracy
- [ ] Cross-platform testing (macOS/Linux/Windows)

## Asset Strategy

### Reuse from Plane Game (Shared Code)
- **Neural Network** - NeuralNetwork.java (adapt inputs/outputs)
- **Evolution Manager** - EvolutionManager.java (reuse genetic algorithm)
- **High Score System** - Existing high score tracking and display
- **Alarm/Sound System** - Existing alarm for timer
- **Text Rendering** - Existing letter/number image system OR use Space Shooter UI numerals
- **Timer System** - Pomodoro timer logic
- **Optimization Techniques** - Caching, rendering optimizations

### Space Shooter Pack Assets (No Plane Game Assets)
- **UI Elements** - Use PNG/UI/ folder (numerals, buttons, life icons, cursor)
- **Player Ships** - PNG/playerShip*.png (blue, green, orange, red variants)
- **Enemies** - PNG/Enemies/ folder (black, blue, green, red enemy ships)
- **Lasers/Bullets** - PNG/Lasers/ folder (blue, green, red lasers)
- **Backgrounds** - Backgrounds/ folder (black, blue, darkPurple, purple space)
- **Effects** - PNG/Effects/ (explosions, shields, stars, fire animations)
- **Power-ups** - PNG/Power-ups/ (optional for future features)
- **Meteors** - PNG/Meteors/ (optional obstacles)
- **Sound Effects** - Bonus/ folder (laser, shield, lose sounds)

### No Asset Sharing
- Space shooter will be completely self-contained with its own assets
- Can run independently without plane game resources
- Text/UI uses Space Shooter pack numerals and UI elements

## Technical Considerations
- Reuse existing GamePanel architecture OR create SpaceShooterPanel class
- Leverage existing optimization techniques (caching, rendering, etc.)
- Maintain 320x240 native resolution with scaling
- 60 FPS target
- Reuse neural network and evolution manager with adapted inputs/outputs
- Keep existing high score system (code only, use space shooter UI assets)
- Use Space Shooter sound effects instead of plane game sounds

## Success Criteria
- Fun and engaging gameplay in manual mode
- AI learns to play effectively within 2-3 minutes
- Smooth 60 FPS performance
- Seamless Pomodoro timer integration
- Difficulty scales infinitely like plane game
- Visual polish matches or exceeds plane game quality
