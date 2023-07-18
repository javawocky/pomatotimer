package org.example;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.awt.Color;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException, UnsupportedAudioFileException, LineUnavailableException, IOException {
        int workMinutes = getUserInput("work", 20);
        int breakMinutes = getUserInput("break", 5);

        var jw = new JWindow();
        jw.setText("BZ");

        while(true) {
            System.out.println("\nStarting timer...");

            Alarm.playBegin();

            int totalSeconds = workMinutes * 60;

            jw.setColor(new Color(255, 111, 0) , Color.DARK_GRAY);
            for (int i = totalSeconds; i > 0; i--) {
                String progress = getProgressBar(i, totalSeconds);
                System.out.print("\r" + progress + " " + getTimeRemaining(i));
                jw.setText(getTimeRemaining(i));
                Thread.sleep(1000);
            }

            System.out.println("\nTime for a break!");
            jw.setColor(Color.LIGHT_GRAY, Color.PINK);

            Alarm.playAlarm();
            totalSeconds = breakMinutes * 60;
            for (int i = totalSeconds; i > 0; i--) {
                String progress = getProgressBar(i, totalSeconds);
                System.out.print("\r" + progress + " " + getTimeRemaining(i));
                jw.setText(getTimeRemaining(i));
                Thread.sleep(1000);
            }

            System.out.println("\nTimer finished!");
        }
    }

    private static int getUserInput(String type, int defaultValue) {
        Scanner scanner = new Scanner(System.in);
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
