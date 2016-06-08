package com.Animator.Animator;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import processing.core.PApplet;
import processing.core.PImage;

public class ExportOverlay extends OverlayTask {    
    private int startUploadingFrame = 0;
    private int imgXOffset = 0;
    private int imgCOffset = 0;    
    PImage icon_globe;

    public ExportOverlay(Animator app) {
        super(app);                        
        app.exportDir.mkdirs();
        icon_globe = app.loadImage("icons/globe.png");
    }

    @Override
    public void onStart() {                
        startUploadingFrame = app.frameCount;
    }

    private void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.flush();
        out.close();
    }
    public void run() {
        System.out.println("RUNNING EXPORT");
        
        String movname = app.movie.dir.getName();
        if (!app.movie.label.isEmpty()) {
            movname += "-" + app.movie.label.replace(' ', '_');
        }
        File outDir = new File (app.exportDir, movname);
        outDir.mkdirs();
        
        PrintWriter out;
        try {                        
            out = new PrintWriter(new File(outDir, "info.txt"));
            out.println("Name: "+app.movie.label);            
            out.println("FPS: "+(1000/app.movie.frameDur));
            out.println("Music: "+app.movie.musicName);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.err.println("Writing ERROR: "+e);            
        }        
        
        try {            
            for (int i = 0; i < app.movie.len(); ++i) {
                Frame fr = app.movie.getFrame(i);
                File inFile = new File(app.movie.dir, fr.fileIndex+".jpg");
                File outFile = new File(outDir, String.format("%06d.jpg", i));
                //File outFile = new File(outDir, fr.fileIndex+".jpg");
                copyFile(inFile, outFile);
            }
        } catch (IOException e) {
            System.err.println("Writing ERROR: "+e);            
        }
        
        
        File movieOutDir = new File(outDir, "movie");
        movieOutDir.mkdir();
        
        String cmd[] = new String[4];
        if (app.OS.equals("windows")) {
            cmd[0] = "encoder\\encode.bat";            
        } else {
            cmd[0] = "encoder/encode.sh";
        }
        cmd[1] = outDir.getPath();
        cmd[2] = new File(movieOutDir, movname+".mp4").getPath();
        cmd[3] = ""+(int)(1000/app.movie.frameDur);
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String s;
            while ((s = stdInput.readLine()) != null) { 
                System.out.println("encoder[std]: "+s);
            }
            
            while ((s = stdError.readLine()) != null) {
                System.err.println("encoder[err]: "+s);
            }
            
            
            if (app.OS.equals("windows")) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(movieOutDir);
            } else {
                Runtime.getRuntime().exec(new String[] {"xdg-open", movieOutDir.getPath()});
            }
        } catch (IOException e) {
            System.err.println("Convert ERROR: "+e);        
        }
        /*try {    
            Thread.sleep(500); // zeby bylo cos widac
        } catch (InterruptedException ex) {            
        }*/
                
        System.out.println("STOP RUNNING EXPORT");
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
