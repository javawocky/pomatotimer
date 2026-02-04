package org.example;

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
        int workMinutes = showStartupDialog("Work", 20);
        int breakMinutes = showStartupDialog("Break", 5);

        var jw = new AppWindow();

        while(true) {
            System.out.println("\nStarting timer...");
            Alarm.playBegin();
            jw.startWork();

            int totalSeconds = workMinutes;
            for (int i = totalSeconds; i > 0; i--) {
                String progress = getProgressBar(i, totalSeconds);
                System.out.print("\r" + progress + " " + getTimeRemaining(i));
                jw.setTimeText(getTimeRemaining(i));
                Thread.sleep(1000);
                if(jw.isSkip()) break;
            }

            System.out.println("\nTime for a break!");
            Alarm.playAlarm();
            jw.startBreak();
            
            totalSeconds = breakMinutes;
            for (int i = totalSeconds; i > 0; i--) {
                String progress = getProgressBar(i, totalSeconds);
                System.out.print("\r" + progress + " " + getTimeRemaining(i));
                jw.setTimeText(getTimeRemaining(i));
                Thread.sleep(1000);
                if(jw.isSkip()) break;
            }

            System.out.println("\nTimer finished!");
        }
    }
    private static int getUserInput(String type, int defaultValue) {
        int input;
        do {
            System.out.print("Enter a number between 1 and 59 (" + type + " minutes, default " + defaultValue + "): ");
            String userInput = scanner.nextLine();
            if (userInput.isEmpty()) {
                input = defaultValue;
                break;
            }
            while (!userInput.matches("\\d+") || (input = Integer.parseInt(userInput)) < 1 || input > 59) {
                System.out.println("That's not a valid number!");
                System.out.print("Enter a number between 1 and 59 (" + type + " minutes, default " + defaultValue + "): ");
                userInput = scanner.nextLine();
                if (userInput.isEmpty()) {
                    input = defaultValue;
                    break;
                }
            }
        } while (input < 1 || input > 59);
        return input;
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
