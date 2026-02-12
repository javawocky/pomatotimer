#!/bin/bash

JAR="target/PomatoTimer-1.0-SNAPSHOT-jar-with-dependencies.jar"

echo "Taking screenshot 1: Intro message (2 seconds after start)"
java -jar $JAR --work 1 --break 1 --fastforward --nightmode day --screenshot 2 intro.png

echo ""
echo "Taking screenshot 2: AI learning early game (30 seconds) - showing fitness graph and network"
java -jar $JAR --work 10 --break 1 --fastforward --nightmode day --screenshot 30 ai-learning-30sec.png

echo ""
echo "Taking screenshot 3: AI learning mid game (2 minutes) - network should be more active"
java -jar $JAR --work 10 --break 1 --fastforward --nightmode day --screenshot 120 ai-learning-2min.png

echo ""
echo "Taking screenshot 4: AI learning advanced (5 minutes) - showing evolved behavior"
java -jar $JAR --work 10 --break 1 --fastforward --nightmode day --screenshot 300 ai-learning-5min.png

echo ""
echo "Taking screenshot 5: Night mode with AI (2 minutes)"
java -jar $JAR --work 10 --break 1 --fastforward --nightmode night --screenshot 120 ai-night-2min.png

echo ""
echo "Taking screenshot 6: Break phase with high score table"
java -jar $JAR --work 1 --break 1 --fastforward --nightmode day --screenshot 90 break.png

echo ""
echo "Taking screenshot 7: Late game difficulty (9 minutes)"
java -jar $JAR --work 10 --break 1 --fastforward --nightmode day --screenshot 540 gameplay-9min.png

echo ""
echo "All screenshots saved to screenshots/ folder"
ls -lh screenshots/
