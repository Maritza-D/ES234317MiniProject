package SnakeandLadderGame;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {
    private Clip bgmClip;
    private Clip winClip;
    private Clip stepClip;

    private float bgmVolume = 0.5f;
    private float sfxVolume = 1.0f;

    // --- BGM CONTROLS ---
    public void playBGM() {
        if (bgmClip != null && bgmClip.isRunning()) return;

        // Cukup panggil nama filenya saja, path diurus oleh loadClip
        bgmClip = loadClip("mcsnd.wav");

        if (bgmClip != null) {
            setClipVolume(bgmClip, bgmVolume);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        }
    }

    public void stopBGM() {
        if (bgmClip != null) { bgmClip.stop(); bgmClip.close(); bgmClip = null; }
    }

    // --- WIN MUSIC CONTROLS ---
    public void playWinMusic() {
        stopBGM();
        if (winClip != null) winClip.close();

        winClip = loadClip("win.wav");

        if (winClip != null) {
            setClipVolume(winClip, bgmVolume);
            winClip.start();
        }
    }

    public void stopWinMusic() {
        if (winClip != null) { winClip.stop(); winClip.close(); winClip = null; }
    }

    // --- SFX CONTROLS ---
    public void playSFX(String name) {
        new Thread(() -> {
            // name = "click", "damage", "level_up"
            Clip c = loadClip(name + ".wav");
            if (c != null) {
                setClipVolume(c, sfxVolume);
                c.start();
            }
        }).start();
    }

    // --- STEP SOUND CONTROL ---
    public void playStep() {
        if (stepClip != null && stepClip.isRunning()) {
            stepClip.stop();
        }

        stepClip = loadClip("step.wav");

        if (stepClip != null) {
            setClipVolume(stepClip, sfxVolume);
            stepClip.start();
        }
    }

    public void stopStep() {
        if (stepClip != null) {
            stepClip.stop();
            stepClip.close();
        }
    }

    // --- VOLUME SETTERS ---
    public void setMusicVolume(float v) {
        this.bgmVolume = v;
        if (bgmClip != null && bgmClip.isOpen()) setClipVolume(bgmClip, v);
        if (winClip != null && winClip.isOpen()) setClipVolume(winClip, v);
    }

    public void setSFXVolume(float v) {
        this.sfxVolume = v;
    }

    private void setClipVolume(Clip clip, float volume) {
        if (clip == null) return;
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log10(volume) * 20.0);
            if (volume <= 0.0001f) dB = -80.0f;
            gainControl.setValue(dB);
        } catch (Exception e) {
            // Ignore if control not supported
        }
    }

    // --- LOAD CLIP (BAGIAN YANG DIPERBAIKI) ---
    private Clip loadClip(String filename) {
        try {
            // Kita arahkan ke dalam package
            String path = "/SnakeandLadderGame/assets/" + filename;

            URL url = getClass().getResource(path);
            if (url == null) {
                System.out.println("Audio missing: " + path);
                return null;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getMusicVolInt() { return (int)(bgmVolume * 100); }
    public int getSFXVolInt() { return (int)(sfxVolume * 100); }
}