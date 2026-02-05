package com.thefryup.pomodoropilot;

import java.io.IOException;
import java.io.InputStream;

public class Alarm {

    private static void playSound(String filename) throws IOException {
        InputStream inputStream = Main.class.getResourceAsStream("/" + filename);
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile(filename.replace(".wav", ""), ".wav");
        java.nio.file.Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        inputStream.close();
        
        String os = System.getProperty("os.name").toLowerCase();
        String audioPlayer = os.contains("mac") ? "afplay" : "aplay";
        
        ProcessBuilder pb = new ProcessBuilder(audioPlayer, tempFile.toString());
        pb.start();
    }

    public static void playAlarm() throws IOException {
        playSound("airplanebeep.wav");
    }

    public static void playBegin() throws IOException {
        playSound("begin.wav");
    }

}
