#!/bin/bash

JAR="target/PomatoTimer-1.0-SNAPSHOT-jar-with-dependencies.jar"

echo "Taking screenshot 1: Intro message (2 seconds after start)"
java -jar $JAR --work 1 --break 1 --fastforward --nightmode day --screenshot 2 intro.png

echo ""
echo "Taking screenshot 2: Middle of break (1 min work + 30 sec into break)"
java -jar $JAR --work 1 --break 1 --fastforward --nightmode day --screenshot 90 break.png

echo ""
echo "Taking screenshot 3: 2 minutes into game"
java -jar $JAR --work 10 --break 1 --fastforward --nightmode day --screenshot 120 gameplay-2min.png

echo ""
echo "Taking screenshot 4: 3 minutes into game"
java -jar $JAR --work 10 --break 1 --fastforward --nightmode day --screenshot 180 gameplay-3min.png

echo ""
echo "Taking screenshot 5: 9 minutes into game"
java -jar $JAR --work 10 --break 1 --fastforward --nightmode day --screenshot 540 gameplay-9min.png

echo ""
echo "Taking screenshot 6: Night mode gameplay (2 minutes)"
java -jar $JAR --work 10 --break 1 --fastforward --nightmode night --screenshot 120 gameplay-night-2min.png

echo ""
echo "Taking screenshot 7: Night mode gameplay (5 minutes)"
java -jar $JAR --work 10 --break 1 --fastforward --nightmode night --screenshot 300 gameplay-night-5min.png

echo ""
echo "All screenshots saved to screenshots/ folder"
ls -lh screenshots/
