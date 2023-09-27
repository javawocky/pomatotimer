package org.example;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.*;

public class Alarm {

    public static void playAlarm() throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
        String filename = "airplanebeep.wav";
        InputStream inputStream = new BufferedInputStream(Main.class.getResourceAsStream("/" + filename));
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
        // Thread.sleep(clip.getMicrosecondLength() / 1000);
    }

    public static void playBegin() throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
        String filename = "begin.wav";
        InputStream inputStream = new BufferedInputStream(Main.class.getResourceAsStream("/" + filename));
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
        // Thread.sleep(clip.getMicrosecondLength() / 1000);
    }

}
