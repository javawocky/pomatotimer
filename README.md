# Pomato Timer

A Pomodoro timer with an integrated Flappy Bird-style game to keep you focused during work sessions.

## How to Play

### Game Modes

**Work Phase (AI Mode)**
- The game starts with AI controlling the plane
- Watch the plane navigate through obstacles automatically
- The AI demonstrates optimal gameplay

**Work Phase (Manual Mode)**
- Press **SPACE** to take control and jump
- Navigate through mountain obstacles
- Avoid hitting the top/bottom of the screen
- Each obstacle passed earns 10 points
- Game ends on collision or falling off screen

**Break Phase**
- Relax and watch planes fly across the screen
- View the scrolling high score table
- Prepare for the next work session

### Controls

- **SPACE** - Jump (take manual control during work phase)
- **B** - Toggle background rendering on/off

### Features

- **Dynamic Difficulty**: Obstacles increase every 3 seconds, spawn rate accelerates
- **Day/Night Cycle**: Randomly switches between day and night modes every 15 seconds with smooth 5-second transitions
- **High Score Table**: Top 5 scores are saved and displayed
- **Random Elements**: Unique pilot names, varied terrain (Grass/Ice/Snow), different plane colors
- **Landing Sequence**: Plane lands on platform in final 5 seconds of work phase

## Running the Game

### Basic Usage

```bash
java -jar PomatoTimer-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Uses default settings: 25 minutes work, 5 minutes break

### Command Line Options

```bash
java -jar PomatoTimer-1.0-SNAPSHOT-jar-with-dependencies.jar <work_minutes> <break_minutes>
```

**Examples:**

```bash
# 5 minute work, 1 minute break
java -jar PomatoTimer-1.0-SNAPSHOT-jar-with-dependencies.jar 5 1

# 1 minute work, 1 minute break (testing)
java -jar PomatoTimer-1.0-SNAPSHOT-jar-with-dependencies.jar 1 1

# 50 minute work, 10 minute break
java -jar PomatoTimer-1.0-SNAPSHOT-jar-with-dependencies.jar 50 10
```

## Building from Source

```bash
mvn clean package
```

The compiled JAR will be in `target/PomatoTimer-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Game Mechanics

### Difficulty Progression

- **Obstacle Count**: Starts with 2, increases by 1 every 3 seconds (no limit)
- **Spawn Rate**: Starts at 120 frames between spawns, reaches maximum speed (24 frames) in 30 seconds
- **Gap Size**: 80 pixels vertical gap between obstacles
- **Obstacle Range**: Gaps spawn between Y positions 50-170

### Physics

- **Gravity**: 0.5 pixels/frame²
- **Jump Strength**: -4.5 pixels/frame
- **Scroll Speed**: 3 pixels/frame
- **Plane Position**: Fixed at X=80

### Scoring

- **10 points** per obstacle passed
- High scores persist across sessions
- Top 5 scores displayed during break phase

## Technical Details

- Built with Java Swing
- 60 FPS game loop
- 320x240 native resolution (scales to window size)
- Pixel art graphics with dynamic color filtering
- Collision detection using triangle geometry for mountain peaks
