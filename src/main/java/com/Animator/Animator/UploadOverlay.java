package com.Animator.Animator;

import java.io.File;
import processing.core.PApplet;
import processing.core.PImage;

public class UploadOverlay extends OverlayTask {
    Encoder encoder = new Encoder(app.movie.getFrames(), app.movie.getDir().getAbsolutePath()+"/", app.movie.label);
    private int startUploadingFrame = 0;
    private int imgXOffset = 0;
    private int imgCOffset = 0;
    String tmpDir = ".temp";
    PImage icon_globe;

    public UploadOverlay(Animator app) {
        super(app);                
        tmpDir = app.baseDir + "/.temp";
        new File(tmpDir).mkdir();
        icon_globe = app.loadImage("icons/globe.png");
    }

    @Override
    public void onStart() {                
        startUploadingFrame = app.frameCount;
    }

    public void run() {
        System.out.println("RUNNING UPLOADING");
        encoder.encode(tmpDir, (1000.0 / app.movie.frameDur), this.app.OS);
        
        Uploader uploader = new Uploader();
        uploader.upload(app.movie.getName(), encoder.getOutFile());
                
        System.out.println("STOP RUNNING UPLOADING");
    }

    @Override
    public void onDraw() {
        int frame = app.frameCount - startUploadingFrame;

        if (frame < 17) {
            app.fill(app.color(0, 0, 0, 25));
            app.rect(0, 0, app.width, app.height);
            return;
        } 
        app.background(0);
        if (app.movie.len() >= 10) {
            int fx = imgXOffset;
            if (imgXOffset > 185) {
                imgXOffset = 0;
                imgCOffset += 1;
            }
            fx -= 100;
            imgXOffset += 5;
            app.rectMode(PApplet.CENTER);
            app.imageMode(PApplet.CENTER);
            for (int fc = 10 ; fc >=0 ; fc--) {
                PImage img = app.movie.getFrame(fc + imgCOffset, true).getImage();
//                   fill(255,255,255);
//                    text(String.valueOf((fc + imgCOffset) % frames.size()), fx, 100);
                float scale = 150f / img.width;
                scale *= PApplet.map((float) fx, 0f, (float) app.width - 200, 1f, .0f);
                app.image(img, fx, app.width / 2 - (scale * img.height), scale * img.width, scale * img.height);
                app.noFill();
                app.stroke(app.color(255, 255, 255));
                app.rect(fx, app.width / 2 - (scale * img.height), scale * img.width, scale * img.height);
                fx += scale * img.width + 10;

            }
            app.rectMode(PApplet.CORNER);
            app.imageMode(PApplet.CORNER);
        }
        app.image(icon_globe, app.width - 190, (app.height /2) + 20, 150, 150);                
    }
}
