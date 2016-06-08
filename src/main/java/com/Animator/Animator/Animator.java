package com.Animator.Animator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.MediaLocator;
import javax.media.cdm.CaptureDeviceManager;

import codeanticode.gsvideo.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;

import fullscreen.*;
import ddf.minim.*;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.ParseException;
import processing.core.PImage;
import processing.core.PApplet;

public class Animator extends PApplet {

	JpegImagesToMovie saveFilm = new JpegImagesToMovie();
	
    private static CommandLine cmdLine = null;
    boolean debugMode = false;
    public String OS = "unknown"; // operating system: "unknown", "linux" or "windows"    
    String baseDir = "anims"; // change with argument: -s path
    public File exportDir = new File("exports");
    boolean soundsEnabled = true;
    
    // capture:
    String cameraName_linux_default = "/dev/video0"; // empty string for default
    String cameraName_windows_default = "";          // empty string for default    
    int capW = 640;
    int capH = 480;
    GSCapture video;
    int capFrame = 0;
    int capFrameLast = -1;
    TimedBool doFrameCapture = new TimedBool(false);
    long futureCaptureT = 0;
    
    // display:
    SoftFullScreen fs;
    Rectangle frameView;
    
    // storage:
    MovieCollection movies;
    Movie movie;
    
    // keys:
    KeysLayout keys = new KeysLayout();

    // modes:
    enum RunningMode { //indicates drawing routines
        PLAY,
        RECORD
    }
    RunningMode mode = RunningMode.RECORD;
    boolean ghostMode = false;
    TimedDing playbackStarted = new TimedDing();
    int playbackLastDrawnFrame = -1;
    int cursorBeforePlayback = -1;
    TimedDing recModeStarted = new TimedDing();
    boolean labelEditMode = false;
    long changeFrameClickT = -10000; // for video fadein after playback/preview
    
    // playback speeds:
    public final static long frameDurAvail[] = new long[]{33, 40, 50, 66, 83, 125, 167, 250, 500, 1000, 2000};
    int frameDurI = 7;
    
    // quickpeek mode:
    boolean quickPeek = false;
    int quickPeekFrame = 0;
    long quickPeekFrameT = 0;
    
    // undo:
    TimedObject<Frame> removedFrame = new TimedObject<Frame>(); // last removed frame
    ArrayList<Frame> undoStack = new ArrayList<Frame>();
    static int undoMax = 100;
    
    // osd icons:
    PImage icon_ghostMode;
    PImage icon_playMode;
    PImage icon_playbackSpeed;
    PImage icon_previewMode;
    PImage icon_recordMode;
    PImage icon_recordModeFlash;
    PImage icon_sound;
    PImage icon_play;
    PImage icon_changeFilm;
    PImage icon_changeMusic;
    PImage icon_addFilm;
    PImage icon_saveFilm;
    
    // sound:
    Minim minim = null;
    AudioSample sound_shutter = null;
    AudioSample samples[];
    AudioPlayer musicPlayer = null;
    File musicDir = new File("data/music");
    
    // else:
    long exitT = 0;
    ArrayList<Overlay> overlays = new ArrayList<Overlay>();

    void addOverlay(Overlay task) {
        overlays.add(task);
        task.start();
    }

    @Override
    public void setup() {
        int screen_width = 800;
        int screen_height = 600;
        int screen_no = 1;
        String cameraName = "";

        { // detect OS and setting system defaults:
            String s = System.getProperty("os.name");
            if (s.contains("Linux")) {
                OS = "linux";
                
                Runtime run = Runtime.getRuntime();
        		String com = "ls /dev";
        		Process pr;
				try {
					pr = run.exec(com);
					pr.waitFor();
	        		BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
	        		String line = buf.readLine();
	        		while ( line != null ) {
	        		  if(line.contains("video")){
	        			  cameraName = "/dev/" + line;
	        		  }
	        		  line = buf.readLine();
	        		}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
            } else if (s.contains("Windows")) {
                OS = "windows";
                cameraName = cameraName_windows_default;
            } else {
                OS = "unknown";
            }
        }

        // handle command line options:
        boolean opt_fullScreen = true;
        if (cmdLine != null) {
            if (cmdLine.hasOption("debug")) {
                debugMode = true;
            }
            if (debugMode) {
                opt_fullScreen = false;
            }
            if (cmdLine.hasOption("full-screen")) {
                opt_fullScreen = true;
            }
            if (cmdLine.hasOption("windowed")) {
                opt_fullScreen = false;
            }
            if (cmdLine.hasOption("camera")) {
                cameraName = cmdLine.getOptionValue("camera");
            }
            if (cmdLine.hasOption("store-path")) {
                baseDir = cmdLine.getOptionValue("store-path");
                baseDir = new File(baseDir).getAbsolutePath();
            }
            if (cmdLine.hasOption("export-path")) {
                exportDir = new File(cmdLine.getOptionValue("export-path")).getAbsoluteFile();
            }
            if (cmdLine.hasOption("keys-layout")) {
                int num = 1;
                try { num = Integer.parseInt(cmdLine.getOptionValue("keys-layout")); } 
                catch (NumberFormatException e) { System.err.println(e); }
                switch (num) {
                    case 1: keys = new KeysLayout(); break;
                    case 2: keys = new KeysLayout_Vertical(); break;
                    case 3: keys = OS.equals("windows") ? new KeysLayout2_Win() : new KeysLayout2(); break;
                    default:keys = new KeysLayout(); break;
                }
            }
            if (cmdLine.hasOption("width")) {
                try { screen_width = Integer.parseInt(cmdLine.getOptionValue("width")); } 
                catch (NumberFormatException e) { System.err.println(e); }
            }
            if (cmdLine.hasOption("height")) {
                try { screen_height = Integer.parseInt(cmdLine.getOptionValue("height")); } 
                catch (NumberFormatException e) { System.err.println(e); }
            }
            if (cmdLine.hasOption("screen")) {
                try { screen_no = Integer.parseInt(cmdLine.getOptionValue("screen")); } 
                catch (NumberFormatException e) { System.err.println(e); }
            }
        }

        // setup display
        size(screen_width, screen_height);
        frameRate(30);
        if (cmdLine.hasOption("antialiasing")) {
            smooth();
        }
        
        {
            int w = 640;
            int h = 480;
            
            if (cmdLine.hasOption("view-width")) {
                try { w = Integer.parseInt(cmdLine.getOptionValue("view-width")); } 
                catch (NumberFormatException e) { System.err.println(e); }
            }
            if (cmdLine.hasOption("view-height")) {
                try { h = Integer.parseInt(cmdLine.getOptionValue("view-height")); } 
                catch (NumberFormatException e) { System.err.println(e); }
            }            
            
            frameView = new Rectangle((width - w) / 2, 0, w, h);
        }

        fs = new SoftFullScreen(this, screen_no);
        if (opt_fullScreen) {
            fs.enter();
        }

        // setup sounds
        if (soundsEnabled) {
            minim = new Minim(this);

            sound_shutter = minim.loadSample("sounds/shutter.wav");
            String sampleNames[] = new String[]{
                "afuche.wav",
                "conga.wav",
                "tabla-lo-geh-gliss.wav", 
               "tambourine-rock-single.wav",
                "ukelele damped buzz pluck.wav",
                "woodblock.wav",
                "voice-manGM-24.wav",
                "voice-womanKP-08.wav",
                "voice-womanKP-17.wav",
                "voice-womanMA-11.wav",
                "voice-womanMA-21.wav"
            };
            samples = new AudioSample[sampleNames.length];
            for (int i = 0; i < sampleNames.length; ++i) {
                samples[i] = minim.loadSample("samples/" + sampleNames[i]);
            }
        } else {
            minim = null;
            samples = new AudioSample[0];
        }

        // load display props:
        icon_ghostMode = loadImage("icons/ghostmode.png");
        icon_playMode = loadImage("icons/StartFilm.png");
        icon_playbackSpeed = loadImage("icons/playbackspeed.png");
        icon_previewMode = loadImage("icons/previewmode.png");
        icon_recordMode = loadImage("icons/CameraLogo.png");
        icon_recordModeFlash = loadImage("icons/CameraLogoFlash.png");
        icon_sound = loadImage("icons/sound.png");
        icon_play = loadImage("icons/StartFilm.png");
        icon_changeFilm = loadImage("icons/fraps.png");
        icon_changeMusic = loadImage("icons/note.png");
        icon_addFilm = loadImage("icons/AddFilm.png");
        icon_saveFilm = loadImage("icons/DiskSave.png");
        // start video capture:
        System.err.println("VIDEO INITIALIZATION");
        if (OS.equals("windows")) {
            String[] cameraNames = GSCapture.list();
            System.out.println("Available cameras:");
            for (int i = 0; i < cameraNames.length; ++i) {
                System.out.println(i + ": " + cameraNames[i]);
            }
            try {
                int num = Integer.parseInt(cameraName);
                if (num < 0 || num >= cameraNames.length) {
                    System.err.println("Selected unavailable camera. Valid numbers from 0 to " + cameraNames.length);
                }
                cameraName = cameraNames[num];
            } catch (NumberFormatException e) {
                // skipping
            }
        }
        
        {
            if (cmdLine.hasOption("cap-width")) {
                try { capW = Integer.parseInt(cmdLine.getOptionValue("cap-width")); } 
                catch (NumberFormatException e) { System.err.println(e); }
            }
            if (cmdLine.hasOption("cap-height")) {
                try { capH = Integer.parseInt(cmdLine.getOptionValue("cap-height")); } 
                catch (NumberFormatException e) { System.err.println(e); }
            }            
        }

        if (cameraName == null || cameraName.isEmpty()) {
            video = new GSCapture(this, capW, capH, 30);
        } else {
            video = new GSCapture(this, capW, capH, cameraName, 30);
        }
        video.setEventHandlerObject(this);
        video.start();
        
        
        System.err.println("LOADING LAST MOVIE");
        // load last movie:    
        movies = new MovieCollection(baseDir);
        movie = new Movie();
        loadMovie(movies.getLast());
        System.err.println("SETUP COMPLETE");
    }

    public void captureEvent(GSCapture c) {
        capFrame += 1;
        c.read();
    }

    @Override
    public void draw() {
        if (handleOverlays()) {
            return;
        }

        if (futureCaptureT != 0 && mode == RunningMode.RECORD && futureCaptureT <= millis()) {
            futureCaptureT = 0;
            scheduleCaptureFrame();
        }

        if (doFrameCapture.get()) {
            if (capFrame != capFrameLast) {
                capFrameLast = capFrame;
                handleFrameCapture();
                doFrameCapture.set(false);
            } else {
                return;
            }
        }

        drawIcons();

        if (mode == RunningMode.RECORD) {
            handleRecord();
        } else { // play mode:
            handlePlayback();
        }

        drawOSD();

        if (frameCount % 120 == 0) {
            System.gc();
            for (int i = 0; i < movie.len(); ++i) {
                movie.getFrame(i).flushCache();
            }
        }
    }

    void setRecMode() {
        futureCaptureT = 0;
        mode = RunningMode.RECORD;
        movie.setCur(cursorBeforePlayback >= 0 ? cursorBeforePlayback : movie.len() - 1);
        recModeStarted.touch();
        if (musicPlayer != null) {
            musicPlayer.rewind();
            musicPlayer.pause();
        }
    }

    void setPlayMode() {
        setPlayMode(0);
    }

    void setPlayMode(int startAt) {
        if (playbackStarted.dur() < 500) {
            return;
        }
        cursorBeforePlayback = movie.getCur();
        playbackLastDrawnFrame = -1;
        movie.playbackSetup(startAt);
        mode = RunningMode.PLAY;
        futureCaptureT = 0;
        if (musicPlayer != null) {
            musicPlayer.setVolume(1);
            musicPlayer.play();
        }
        playbackStarted.touch();
    }

    private boolean handleOverlays() {
        if (overlays.isEmpty()) {
            return false;
        }

        int i = 0;
        while (i < overlays.size()) {
            Overlay t = overlays.get(i);
            t.onDraw();

            if (t.isFinished()) {
                t.onEnd();
                overlays.remove(i);
                continue;
            }
            i += 1;
        }

        return !overlays.isEmpty();
    }

    private void handleFrameCapture() {
        video.loadPixels();
        PImage im = createImage(video.width, video.height, RGB);
        im.loadPixels();
        for (int i = 0; i < im.pixels.length; ++i) {
            im.pixels[i] = video.pixels[i];
        }
        im.updatePixels();
        movie.insertFrame(movie.newFrame(this, im));
        movie.save();
    }

    private void handlePlayback() {
        if (movie.isEmpty()) {
            // show nothing
            background(0, 0, 0);
            return;
        }
        int f = (int) (playbackStarted.dur() / movie.frameDur);
        if (f < 0) {
            f = 0;
        }
        if (f >= movie.len()) {
            f = movie.len() - 1;
            changeFrameClickT = millis();
            setRecMode();
        }
        movie.setCur(f);
        if (playbackLastDrawnFrame == f) {
            return;
        }
        playbackLastDrawnFrame = f;        

        Frame fr = movie.getFrame();
        if (fr != null) {
            if (fr.sampleIndex >= 0 && fr.sampleTriggered.dur() > movie.frameDur * 2) {
                fr.sampleTriggered.touch();
                if (fr.sampleIndex < samples.length) {
                    samples[fr.sampleIndex].trigger();
                }
            }
            fr.paint(frameView.x, 0, frameView.width, frameView.height);
        }
    }

    // record mode:
    private void handleRecord() {
        if (removedFrame.get() != null) {
            // frame was just removed - animate frame deletion
            fill(0);
            rect(frameView.x, 0, frameView.width, frameView.height);
            if (removedFrame.dur() < 500) {
                float t = pow(removedFrame.dur()/500.f, 2);
                imageMode(CENTER);
                removedFrame.get().paint(frameView.x + frameView.width / 2, frameView.height / 2, frameView.width * (1 - t), frameView.height * (1 - t));
                imageMode(CORNER);
            } else {
                removedFrame.set(null);
            }
        } else if (!doFrameCapture.get() && doFrameCapture.dur() < 300) {
            // frame was just captured - animate "Flash" and show captured frame
            movie.getFrame().paint(frameView.x, 0, frameView.width, frameView.height);
            float a = 1 - doFrameCapture.dur() / 300.f;
            noStroke();
            fill(255, 255 * abs(sin(a * 300)));
            rect(frameView.x, 0, frameView.width, frameView.height);
        } else if (millis() - changeFrameClickT < 900) {
            // playback or quickpeek mode just ended - transition from last image to video
            if (movie.len() > 0) {
                movie.getFrame().paint(frameView.x, 0, frameView.width, frameView.height);
            }
            if (millis() - changeFrameClickT > 600) {
                tint(255, (millis() - (changeFrameClickT + 600)) * 255 / 300);
                image(video, frameView.x, 0, frameView.width, frameView.height);
                noTint();
            }
        } else if (quickPeek) {
            // quick peek mode - show last frames
            long now = millis();
            int f = quickPeekFrame;
            if (now - quickPeekFrameT >= movie.frameDur * 2 / 3) {
                quickPeekFrame += 1;
                quickPeekFrameT = now;
            }
            if (f >= movie.len()) {
                // quick peek finished
                f = movie.len() - 1;
                quickPeekStop();
            }

            Frame fr = movie.getFrame(f);
            if (fr != null) {
                if (fr.sampleIndex >= 0 && fr.sampleTriggered.dur() > movie.frameDur * 2) {
                    fr.sampleTriggered.touch();
                    if (fr.sampleIndex < samples.length) {
                        samples[fr.sampleIndex].trigger();
                    }
                }
                fr.paint(frameView.x, 0, frameView.width, frameView.height);
            }
        } else {
            if (!ghostMode || movie.isEmpty()) {
                // record mode, standard
                image(video, frameView.x, 0, frameView.width, frameView.height);
            } else {
                // ghost mode, overlay last frame and, if possible future frame
                movie.getFrame().paint(frameView.x, 0, frameView.width, frameView.height);
                tint(255, 128);
                if (movie.getCur() + 1 < movie.len()) {
                    movie.getFrame(movie.getCur() + 1).paint(frameView.x, 0, frameView.width, frameView.height);
                }
                image(video, frameView.x, 0, frameView.width, frameView.height);
                noTint();
            }
            // red frame around video feed view indicating record mode
            noFill();
            stroke(255, 0, 0);
            strokeWeight(1);
            rect(frameView.x, 0, frameView.width - 1, frameView.height - 1);
            noStroke();
        }
    }

    void drawOSD() {
        noStroke();
        fill(0);
        rect(0, frameView.height, width, height - frameView.height + 1);

        textSize(16);
        if (mode == RunningMode.RECORD) {
            fill(128);
        } else if (mode == RunningMode.PLAY) {
            fill(0, 128, 0);
        }
        text("" + (movie.isEmpty() ? 0 : movie.getCur() + 1) + "/" + movie.len(), 4, height - 4);
        text(mode == RunningMode.RECORD
                ? (movie.getCur() < movie.len() - 1 && movie.len() > 0
                ? "wstawianie"
                : "dodawanie")
                : "odtwarzanie (" + (1000 / movie.frameDur) + "fps)",
                70, height - 4);
        boolean editBlink = labelEditMode && (millis() % 1000 < 500);
        text("Film: " + (movie.label.length() > 0
                ? movie.label + (editBlink ? "|" : "")
                : (editBlink ? "|" : (labelEditMode ? "" : "bez nazwy #" + movie.getName()))), 260, height - 4); 

        if (mode == RunningMode.RECORD && doFrameCapture.dur() > 300) {
            drawThumbs();
        }

        if (debugMode) {
            noStroke();
            fill(255, 0, 0);
            text("D", 10, 10);
        }
    }

    private void drawIcons() {
        int frameX = frameView.x;

        fill(0, 0, 0);
        noStroke();
        rect(0, 0, frameX, frameView.height);
        rect(frameX + frameView.width, 0, frameX, frameView.height);

        if (mode == RunningMode.RECORD) {
        	image(icon_play, frameView.x - 80, frameView.height-80);
        	image(icon_changeFilm, frameView.x - 69, frameView.height-450);
        	image(icon_changeMusic, frameX + frameView.width + 10, frameView.height-470);
        	image(icon_addFilm, frameView.x - 83, frameView.height - 330);
            image(icon_ghostMode, frameView.x - 85, frameView.height - 200);
            image(icon_saveFilm, frameX + frameView.width + 10, frameView.height-370);
            if (!quickPeek) {
                if (doFrameCapture.dur() < 300) {
                    image(icon_recordModeFlash, frameX + frameView.width + 5, frameView.height - 70);
                } else {
                    image(icon_recordMode, frameX + frameView.width + 5, frameView.height - 70);
                }
                if (futureCaptureT != 0 && millis() < futureCaptureT && millis() % 400 < 200) {
                    textSize(40);
                    fill(255);
                    long timeToGo = (futureCaptureT - millis()) / 1000;
                    text(timeToGo + "", frameX + frameView.width + 30, frameView.height - 15);
                }
            } else {
                image(icon_previewMode, frameX + frameView.width, frameView.height - 55);
            }
        }/* else if (mode == RunningMode.PLAY) {  
        }*/
        image(icon_playbackSpeed, frameX + frameView.width, frameView.height - 270);
        noStroke();
        fill(153);
        rectMode(CENTER);
        rect(frameX + frameView.width + 40 + 1, frameView.height - 270 + 35 + frameDurI * (132 - 35) / (frameDurAvail.length - 1), 27, 10);
        rectMode(CORNER);
    }

    private float targetFrame = 0;
    private int wasF = 0;

    private void drawThumbs() {
        final int thumbM = 5;
        final int thumbW = 640 / 5, thumbH = 480 / 5;

        int f = movie.getCur() + 2;
        targetFrame += 0.2 * (movie.getCur() - targetFrame);
        float dx = targetFrame - movie.getCur();
        if (abs(dx) < 0.05) {
            dx = 0;
        }
        float x = width;
        if (f >= movie.len()) {
            f = movie.len() - 1;
        }
        if (movie.len() - wasF > 1) {
            x -= dx * thumbW;
        }
        wasF = f;
        if (f < 0) {
            return;
        }
        if (x < width) { // && f < frames.size() - 1) {
            x += thumbW;
            f += 1;
        }
        
        boolean videoDrawn = false;
        long startDrawT = millis();
        while (x > -thumbW) {
            x -= thumbW;
            if (!videoDrawn && f == movie.getCur()) {
                image(video, x + thumbM+10, frameView.height + thumbM+4, thumbW - 2 * thumbM-20, thumbH - 2 * thumbM-15);
                noFill();
                stroke(255, 0, 0);
                strokeWeight(1);
                rect(x + thumbM+9, frameView.height + thumbM+3, thumbW - 1 - 2 * thumbM-18, thumbH - 1 - 2 * thumbM-13);
                noStroke();
                videoDrawn = true;
                continue;
            }
            Frame fr = movie.getFrame(f);
            if (fr != null) {
                //image(fr.im, x + thumbM, frameView.height + thumbM, thumbW - 2*thumbM, thumbH - 2*thumbM);
                if (f == movie.getCur() && !doFrameCapture.get() && doFrameCapture.dur() > 300 && doFrameCapture.dur() < 1000) {
                    x += thumbW*(1 - (doFrameCapture.dur() - 300)/700.f);
                }
                fr.lazyPaint(x + thumbM, frameView.height + thumbM, thumbW - 2 * thumbM, thumbH - 2 * thumbM, 2);
                if (fr.sampleIndex != -1) {
                    image(icon_sound, x + thumbM + thumbW - 2 * thumbM - 50, frameView.height + thumbM);
                }
            }
            if (f == movie.getCur()) {
                noFill();
                stroke(255, 255, 255);
                strokeWeight(1);
                rect(x + thumbM, frameView.height + thumbM, thumbW - 1 - 2 * thumbM, thumbH - 1 - 2 * thumbM);
                stroke(255, 255, 255);
                {
                    float tx = x + thumbW/2;
                    float ty = frameView.height + thumbM + thumbH - 1 - 2 * thumbM;
                    triangle(tx, ty, tx - 10, ty + 13, tx + 10, ty + 13);
                }
                noStroke();
            }
            if (ghostMode && f == movie.getCur() + 1) {
                noFill();
                stroke(0, 128, 128);
                strokeWeight(1);
                rect(x + thumbM, frameView.height + thumbM, thumbW - 1 - 2 * thumbM, thumbH - 1 - 2 * thumbM);
            }
            if (quickPeek && f == quickPeekFrame && quickPeekFrame <= movie.getCur()) {
                noFill();
                stroke(255, 255, 0);
                strokeWeight(1);
                rect(x + thumbM, frameView.height + thumbM, thumbW - 1 - 2 * thumbM, thumbH - 1 - 2 * thumbM);
            }
            f -= 1;
            if (f < 0) {
                break;
            }
            /*if (millis() - startDrawT > 500) {
                return;
            }*/
        }
    }

    void quickPeekStart() {
        int n = 5;
        if (quickPeek) {
            quickPeekFrame -= n;
            quickPeekFrameT = millis();
        } else {
            quickPeekFrame = movie.getCur() - n + 1;
            quickPeekFrameT = millis();
        }
        if (quickPeekFrame < 0) {
            quickPeekFrame = 0;
        }
        quickPeek = quickPeekFrame <= movie.getCur();
    }

    void quickPeekStop() {
        // quick peek finished        
        quickPeek = false;
        changeFrameClickT = millis();
    }

    void moveToFramePrev() {
        mode = RunningMode.RECORD;
        changeFrameClickT = millis();
        movie.shiftCur(-1, true);
    }

    void moveToFrameNext() {
        mode = RunningMode.RECORD;
        changeFrameClickT = millis();
        movie.shiftCur(1, true);
    }
    
    void moveToFrameLast() {
        mode = RunningMode.RECORD;
        changeFrameClickT = millis();
        movie.setCur(movie.len() > 0 ? movie.len() - 1 : 0);
    }

    void scheduleCaptureFrame() {
        doFrameCapture.set(true);
        if (sound_shutter != null) {
            sound_shutter.trigger();
        } else {
            Runtime r = Runtime.getRuntime();
            try {
                r.exec("aplay /home/rudy/dev/animator/animator-nb/data/sounds/shutter.wav");
            } catch (IOException ex) {
                // not importaint
            }
        }
        //movie.setCur(movie.getCur() + 1);
    }

    void changeFrameDuration(long dur) 
    {
        if (dur == movie.frameDur) return;
        movie.frameDur = dur;
        if (mode == RunningMode.PLAY) {
            int c = movie.getCur();
            playbackStarted.touch(TimedDing.millis() - c*dur);
        }
    }
    
    void setFrameRateSlower() {
        frameDurI += 1;
        if (frameDurI >= frameDurAvail.length) {
            frameDurI = frameDurAvail.length - 1;
        }
        changeFrameDuration(frameDurAvail[frameDurI]);
    }

    void setFrameRateFaster() {
        frameDurI -= 1;
        if (frameDurI < 0) {
            frameDurI = 0;
        }
        changeFrameDuration(frameDurAvail[frameDurI]);
    }

    void removeCurrentFrame() {
        if (mode != RunningMode.RECORD) { 
            return;
        }        
        Frame f = movie.removeCurrentFrame();
        if (f == null) return;
        removedFrame.set(f);
        if (undoStack.size() > 0) {
            Frame lf = undoStack.get(undoStack.size() - 1);
            if (lf != null) {
                lf.flushCache(true);
            }
        }        
        if (removedFrame.get() != null) {
            undoStack.add(removedFrame.get());
        }
        while (undoStack.size() > undoMax) {
            undoStack.remove(0);
        }        
    }
    
    void undoRemoveFrame() {
        if (mode != RunningMode.RECORD || undoStack.isEmpty()) {
            return;
        }
        Frame f = undoStack.get(undoStack.size() - 1);
        undoStack.remove(undoStack.size() - 1);
        movie.insertFrame(f);
        movie.save();
    }

    void nextMovie() {
        loadMovie(movies.getAfter(movie.getName()));
    }

    void prevMovie() {
        loadMovie(movies.getBefore(movie.getName()));
    }

    void toggleGhostMode() {
        if (mode == RunningMode.RECORD) {
            ghostMode = !ghostMode;
        }
    }
    
    //mousePressed attributes
    @Override
    public void mousePressed()
    {
    	int heightTurtleCar = 30;
    	//slow frame duration
    	if((mouseX > frameView.x + frameView.width) &&
    			(mouseX < frameView.x + frameView.width + icon_playbackSpeed.width)
    			&& (mouseY > frameView.height - 270 + icon_playbackSpeed.height - heightTurtleCar) &&
    			(mouseY < frameView.height - 270 + icon_playbackSpeed.height)){
    			setFrameRateSlower();
    	}
    	
    	//faster frame duration
    	if((mouseX > frameView.x + frameView.width) &&
    	    	(mouseX < frameView.x + frameView.width + icon_playbackSpeed.width)
    	    	&& (mouseY > frameView.height - 270) &&
    	    	(mouseY < frameView.height - 270 + heightTurtleCar)){
    	    	setFrameRateFaster();
    	}
    	
    	//take picture
    	if((mouseX > frameView.x + frameView.width) &&
    			(mouseX < frameView.x + frameView.width + icon_recordMode.width) &&
    			(mouseY > frameView.height - 70)&&
    			(mouseY < frameView.height - 70 + icon_recordMode.height)){
	    		if (doFrameCapture.dur() > 1000) {
	                scheduleCaptureFrame();
	            }
    	}
    	
    	//ghost mode
    	if((mouseX > frameView.x - 85) && 
    			(mouseX < frameView.x -85 + icon_ghostMode.width) &&
    			(mouseY > frameView.height -200) &&
    			(mouseY < frameView.height -200 + icon_ghostMode.height)){
    			toggleGhostMode();
    	}
    	
    	if((mouseX > frameView.x - 80) && 
    			(mouseX < frameView.x -80 + icon_play.width) &&
    			(mouseY > frameView.height -80) &&
    			(mouseY < frameView.height -80 + icon_play.height)){
    		if( !movie.isEmpty()){setPlayMode();
    		}
    	}
    	
    	if((mouseX > frameView.x + frameView.width + 5) &&
    			(mouseX < frameView.x + frameView.width + icon_changeMusic.width) &&
    			(mouseY > frameView.height - 470)&&
    			(mouseY < frameView.height - 470 + icon_changeMusic.height)){
	    	if (mode == RunningMode.RECORD && soundsEnabled) {
	            addOverlay(new MusicSelector(this));
	        }
    	}
    	
    	if((mouseX > frameView.x - 80) && 
    			(mouseX < frameView.x -80 + icon_changeFilm.width) &&
    			(mouseY > frameView.height -450) &&
    			(mouseY < frameView.height -450 + icon_changeFilm.height)){
	    	if (mode == RunningMode.RECORD) {
	            startMovieSelector();
	        }
    	}
    	
    	if((mouseX > 260) && (mouseX < 390) && (mouseY > frameView.height + 90)){
	        if(labelEditMode == false){
	            labelEditMode = true;
	        }
    	}
    	
    	if((mouseX > frameView.x - 83) &&
    			(mouseX < frameView.x + 83 + icon_addFilm.width) &&
    			(mouseY > frameView.height - 330)&&
    			(mouseY < frameView.height - 330 + icon_addFilm.height)){
    		if (mode == RunningMode.RECORD) {
                newMovie();
            }
    	}
    	
    	if((mouseX > frameView.x + frameView.width + 10) &&
    			(mouseX < frameView.x + frameView.width + icon_saveFilm.width) &&
    			(mouseY > frameView.height - 370)&&
    			(mouseY < frameView.height - 370 + icon_saveFilm.height)){
    		saveMovie();
    	}
    	
	    	if(mouseX > 100){
	    		cameraSelect();
	    	}
        }
    
    @Override
    public void keyPressed() {
        for (Overlay o : overlays) {
            if (o.onKeyPressed(key, keyCode)) {
                return;
            }
        }

        if (!overlays.isEmpty()) {
            return;
        }

        if (labelEditMode) {
            if (keyCode == ENTER || keyCode == RETURN || keyCode == ESC) {
                labelEditMode = false;
                movie.save();
            } else if (key >= 'a' && key <= 'z' || key >= 'A' && key <= 'Z' || key == ' ' || (key >= '0' && key <= '9')
                    || key == ',' || key == '.' || key == '(' || key == ')' || key == '@' || key == ':' || key == '"' || key == '\''
                    || key == '!' || key == '*' || key == '-' || key == '[')  {
                // append letter to set label
                movie.label += key;
                movie.label = movie.label.toUpperCase();
            } else if (keyCode == BACKSPACE) {
                // remove letter from label
                if (movie.label.length() > 0) {
                    movie.label = movie.label.substring(0, movie.label.length() - 1);
                }
            }
            return;
        }

        if (key == ' ') {
            labelEditMode = true;
        } else if (key == 'e') {
            addOverlay(new ExportOverlay(this));
        } else if (keys.capture(keyCode)) {
            futureCaptureT = 0;
            if (mode == RunningMode.RECORD) {
                if (quickPeek) {
                    quickPeekStop();
                } else {
                    if (doFrameCapture.dur() > 1000) {
                        scheduleCaptureFrame();
                    }
                }
            } else if (mode == RunningMode.PLAY) {
                setRecMode();
            }
        } else if (keys.play(keyCode) || (debugMode && key == 'p')) {
            if (mode == RunningMode.PLAY) {
                if (playbackStarted.dur() > 500) { // fix for '000' key on modern numpads
                    setRecMode();
                }
            } else {
                if (quickPeek) {
                    quickPeekStop();
                } else {
                    setPlayMode();
                }
            }
        } else if (keys.slower(keyCode) || (debugMode && key == '-')) { 
            setFrameRateSlower();
        } else if (keys.faster(keyCode, key) || (debugMode && key == '=')) {
            setFrameRateFaster();
        } else if (keys.removeFrame(keyCode) || (debugMode && key == 'x')) { // 0/ins
            if (mode == RunningMode.RECORD) {
                removeCurrentFrame();
            }
        } else if (keys.removeFrameUndo(keyCode) || (debugMode && key == 'r')) {
            if (mode == RunningMode.RECORD) {
                undoRemoveFrame();
            }
        } else if (keys.nextFrame(keyCode) || (debugMode && (keyCode == 65368 || keyCode == 101 || keyCode == RIGHT))) {
            moveToFrameNext();
        } else if ((keys.prevFrame(keyCode)) || (debugMode && (keyCode == 225 || keyCode == 98 || keyCode == LEFT))) {
            moveToFramePrev();
        } else if ((keys.lastFrame(keyCode)) || (debugMode && key == 'e')) {
            moveToFrameLast();
        } else if (keys.quickPeak(keyCode) || (debugMode && key == 'q')) { // ./del            
            quickPeekStart();
        } else if (keys.ghostMode(keyCode) || (debugMode && key == 'g')) {
            toggleGhostMode();
        } else if (debugMode && key == ',') { // 
            prevMovie();
        } else if (debugMode && key == '.') {
            nextMovie();
        } else if (keys.newMovie(keyCode) || (debugMode && key == 'n')) {
            if (mode == RunningMode.RECORD) {
                newMovie();
            }
        } else if (keys.upload(key) || (debugMode && key == '[')) {
            uploadCurrentMovie();
        } else if (keys.movieSelector(keyCode) || (debugMode && key == 'f')) {
            // open movie
            if (mode == RunningMode.RECORD) {
                startMovieSelector();
            }
        } else if (keys.musicSelector(keyCode) || (debugMode && key == 'm')) {
            // select sound track
            if (mode == RunningMode.RECORD && soundsEnabled) {
                addOverlay(new MusicSelector(this));
            }
        } else if (keyCode == 9) { // TAB
            debugMode = !debugMode;        
        } else if (keys.delayedCapture(keyCode)) {
            if (mode == RunningMode.RECORD) {
                if (futureCaptureT == 0) {
                    futureCaptureT = millis() + 5000;
                } else {
                    futureCaptureT += 5000;
                }
            }        
        } else if (debugMode && keyCode == 123) {
            if (!movie.isEmpty()) {
                Frame fr = movie.getFrame(movie.getCur());
                if (fr != null) {
                    fr.sampleIndex = -1;
                }
            }
        } else if (debugMode && keyCode >= 112 && keyCode <= 122) {
            if (!movie.isEmpty()) {
                int si = keyCode - 112;
                Frame fr = movie.getFrame(movie.getCur());
                if (fr != null) {
                    fr.sampleIndex = si;
                    if (si < samples.length) {
                        samples[si].trigger();
                    }
                }
            }
        }
    }
    
    void cameraSelect(){
	    Vector info = CaptureDeviceManager.getDeviceList(null);
	    if (info == null)
	      System.out.println("No Capture devices known to JMF");
	    else {
	      System.out.println("The following " + info.size()
	          + " capture devices are known to the JMF");
	      for (int i = 0; i < info.size(); i++){
	        //System.out.println("\t" + (CaptureDeviceInfo)info.elementAt(i));
	        System.out.println(info.elementAt(i));
	      }
	    }
    }
    void newMovie() {
        if (movie.isEmpty()) {
            return;
        }
        movie.save();
        movies.reload();
        movie.load(this, movies.getMoviePath(movies.getNew()));
        mode = RunningMode.RECORD;
    }
    
    void saveMovie()
    {
    	MediaLocator oml;
		oml = saveFilm.createMediaLocator(movie.getDir() + "/film.mov");
		Vector<String> inFiles = new Vector<String>();
		
		File file = movie.getDir();
		try {
			ArrayList<FrameStub> stubs = Movie.loadFrameStubs(file);
			for (FrameStub stub: stubs) {
				inFiles.addElement(movie.getDir() + "/" + stub.fileName + ".jpg");
    		}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   		
		saveFilm.doIt(640, 480,frameDurI,inFiles,oml);
    }

    void loadMovie(String newMovieName) {
        loadMovie(newMovieName, false);
    }

    void loadMovie(String newMovieName, boolean force) {
        if (!force && movie.getName().equals(newMovieName)) {
            return;
        }

        addOverlay(new MovieLoadingOverlay(this, newMovieName));
    }

    void uploadCurrentMovie() {
        if (movie.len() <= 10) {
            return;
        }
        if (!overlays.isEmpty()) {
            return;
        }

        addOverlay(new UploadOverlay(this));
    }

    void startMovieSelector() {
        addOverlay(new MovieSelector(this));
    }

    @Override
    public void exit() {
        if (exitT == 0 || millis() - exitT > 800) {
            exitT = millis();
            return;
        }
        movie.save();
        super.exit();
    }

    public static void main(String[] args) {
        Options runOpts = new Options(); // runtime Options
        runOpts
                .addOption("d", "debug", false, "Debug mode")
                .addOption("f", "full-screen", false, "Full screen mode")
                .addOption("w", "windowed", false, "Windowed mode")
                .addOption("c", "camera", true, "Name/Path of camera device")
                .addOption("s", "store-path", true, "Path to movies collection directory")
                .addOption("e", "export-path", true, "Export directory path")
                .addOption("k", "keys-layout", true, "Layout: 1(numpad) or 2(rotated keyboard)")
                .addOption("W", "width", true, "Window width")
                .addOption("H", "height", true, "window height")
                .addOption("", "cap-width", true, "capture width")
                .addOption("", "cap-height", true, "capture height")
                .addOption("", "view-width", true, "view width")
                .addOption("", "view-height", true, "view height")
                .addOption("A", "antialiasing", false, "Antialiasing")
                .addOption("S", "screen", true, "Screen number (for full screen)");
        PosixParser parser = new PosixParser();
        try {
            cmdLine = parser.parse(runOpts, args);
        } catch (ParseException ex) {
            Logger.getLogger(Animator.class.getName()).log(Level.WARNING, null, ex);
        }

        PApplet.main(new String[]{Animator.class.getName()});
    }
}
