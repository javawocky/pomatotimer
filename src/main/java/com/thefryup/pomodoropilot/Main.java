package com.thefryup.pomodoropilot;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.Color;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    
    private static int showStartupDialog(String type, int defaultValue) {
        String input = JOptionPane.showInputDialog(
            null,
            type + " minutes (1-59 or decimal like 0.2):",
            "Pomodoro Timer Setup",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (input == null || input.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            double value = Double.parseDouble(input.trim());
            if (value > 0 && value <= 59) {
                return (int)(value * 60);
            }
        } catch (NumberFormatException e) {
        }
        
        return defaultValue;
    }
        
    public static void main(String[] args) throws InterruptedException, UnsupportedAudioFileException, LineUnavailableException, IOException {
        int workMinutes = 20 * 60;
        int breakMinutes = 5 * 60;
        double screenshotAt = -1;
        String screenshotName = "screenshot.png";
        boolean fastForward = false;
        String nightMode = null;
        
        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--work") && i + 1 < args.length) {
                workMinutes = (int)(Double.parseDouble(args[i + 1]) * 60);
                i++;
            } else if (args[i].equals("--break") && i + 1 < args.length) {
                breakMinutes = (int)(Double.parseDouble(args[i + 1]) * 60);
                i++;
            } else if (args[i].equals("--screenshot") && i + 2 < args.length) {
                screenshotAt = Double.parseDouble(args[i + 1]);
                screenshotName = args[i + 2];
                i += 2;
            } else if (args[i].equals("--fastforward")) {
                fastForward = true;
            } else if (args[i].equals("--nightmode") && i + 1 < args.length) {
                nightMode = args[i + 1];
                i++;
            }
        }
        
        // If no command line args, show dialog
        if (args.length == 0) {
            workMinutes = showStartupDialog("Work", 20);
            breakMinutes = showStartupDialog("Break", 5);
        }
        
        System.out.println("Work time: " + (workMinutes / 60.0) + " minutes");
        System.out.println("Break time: " + (breakMinutes / 60.0) + " minutes");
        if (screenshotAt >= 0) {
            System.out.println("Screenshot at: " + screenshotAt + " seconds -> " + screenshotName);
        }
        if (fastForward) {
            System.out.println("Fast forward mode enabled");
        }
        if (nightMode != null) {
            System.out.println("Night mode: " + nightMode);
        }

        var jw = new AppWindow();
        int elapsedSeconds = 0;
        boolean screenshotTaken = false;
        
        if (fastForward) {
            jw.setSpeedMultiplier(60); // 60x speed
        }
        
        if (nightMode != null) {
            jw.setNightMode(nightMode);
        }

        while(true) {
            System.out.println("\nStarting timer...");
            Alarm.playBegin();
            jw.startWork();

            int totalSeconds = workMinutes;
            for (int i = totalSeconds; i > 0; i--) {
                elapsedSeconds++;
                String progress = getProgressBar(i, totalSeconds);
                System.out.print("\r" + progress + " " + getTimeRemaining(i));
                jw.setTimeText(getTimeRemaining(i));
                
                Thread.sleep(fastForward ? 17 : 1000);
                
                // Take screenshot if at the right time
                if (screenshotAt >= 0 && !screenshotTaken && elapsedSeconds >= screenshotAt) {
                    Thread.sleep(100); // Let UI update
                    jw.saveScreenshot(screenshotName);
                    screenshotTaken = true;
                    if (screenshotAt > 0) {
                        System.exit(0);
                    }
                }
                
                if(jw.isSkip()) break;
            }

            System.out.println("\nTime for a break!");
            Alarm.playAlarm();
            jw.startBreak();
            
            totalSeconds = breakMinutes;
            for (int i = totalSeconds; i > 0; i--) {
                elapsedSeconds++;
                String progress = getProgressBar(i, totalSeconds);
                System.out.print("\r" + progress + " " + getTimeRemaining(i));
                jw.setTimeText(getTimeRemaining(i));
                
                Thread.sleep(fastForward ? 17 : 1000);
                
                // Take screenshot if at the right time
                if (screenshotAt >= 0 && !screenshotTaken && elapsedSeconds >= screenshotAt) {
                    Thread.sleep(100); // Let UI update
                    jw.saveScreenshot(screenshotName);
                    screenshotTaken = true;
                    if (screenshotAt > 0) {
                        System.exit(0);
                    }
                }
                
                if(jw.isSkip()) break;
            }

            System.out.println("\nTimer finished!");
        }
    }

    private static String getProgressBar(int remainingSeconds, int totalSeconds) {
        int numFilled = (totalSeconds - remainingSeconds) * 7 / totalSeconds;
        int numEmpty = 7 - numFilled;
        String[] spinners = {"|", "\\", "-", "/"};
        int spinnerIndex = remainingSeconds % spinners.length;
        return "[" + "#".repeat(numFilled) + spinners[spinnerIndex] + "-".repeat(numEmpty) + "]" ;
    }

    private static String getTimeRemaining(int remainingSeconds) {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
