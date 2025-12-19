package MazeGame;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

public class SoundManager {
    private static Clip bgmClip;
    private static float currentVolume = 0.0f;

    public static void setVolume(int value) {
        if (value <= 0) {
            currentVolume = -80.0f;
        } else {
            currentVolume = 20.0f * (float) Math.log10(value / 100.0);
        }

        if (bgmClip != null && bgmClip.isOpen()) {
            updateClipVolume(bgmClip);
        }
    }

    private static void updateClipVolume(Clip clip) {
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(currentVolume);
        } catch (Exception e) {
        }
    }

    public static void playSFX(String filename) {
        try {
            URL url = SoundManager.class.getResource("assets/" + filename);
            if (url == null) url = SoundManager.class.getResource("/MazeGame/assets/" + filename);
            if (url == null) return;

            InputStream bufferedIn = new BufferedInputStream(url.openStream());
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            updateClipVolume(clip);

            clip.start();
        } catch (Exception e) { }
    }

    public static void playMusic(String filename) {
        stopMusic();
        try {
            URL url = SoundManager.class.getResource("assets/" + filename);
            if (url == null) url = SoundManager.class.getResource("/MazeGame/assets/" + filename);
            if (url == null) return;

            InputStream bufferedIn = new BufferedInputStream(url.openStream());
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);

            updateClipVolume(bgmClip);

            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception e) { }
    }

    public static void stopMusic() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }
}