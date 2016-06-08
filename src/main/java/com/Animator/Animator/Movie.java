package com.Animator.Animator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PImage;

public class Movie {
    public String label = "";
    public String musicName = "";
    public long frameDur = 250;
    protected ArrayList<Frame> frames;
    
    protected File dir;
    protected int cur; // cursor - current frame index
    protected int fileIndexMax = 0;
    
    public enum PlaybackMode {
        NORMAL, LOOP, BOUNCE_LOOP
    }
    
    public Movie()
    {
        dir = new File("");
        frames = new ArrayList<Frame>();
    }
    
    public Frame newFrame(PApplet app, PImage im) {
        int ind = fileIndexMax++;
        File f = new File(getDir(), ind+".jpg");
        im.save(f.getAbsolutePath());
        Frame fr = new Frame(app);    
        fr.fileIndex = ind;
        fr.loadFromFile(f.getAbsoluteFile());
        fr.touch();
        return fr;
    }
    
    @Deprecated
    public ArrayList<Frame> getFrames()
    {
        return frames;
    }
    
    public File getDir() {
        return dir;
    }
    
    public String getName() {
        return dir.getName();
    }
    
    public static ArrayList<FrameStub> loadFrameStubs(File dir) throws FileNotFoundException {
        ArrayList<FrameStub> out = new ArrayList<FrameStub>();
        if (!dir.isDirectory()) return out;
        File framesFile = new File(dir, "frames.txt");
        if (!framesFile.isFile()) return out;
        
        try {
            BufferedReader f = new BufferedReader(new FileReader(framesFile));
             
            while (true) {
                String frameline = f.readLine();
                if (frameline == null) {
                    break;
                }

                FrameStub stub = FrameStub.fromString(frameline);
                if (stub == null) {
                    continue;
                }
                
                out.add(stub);
            }
            f.close();  
        } catch (IOException e) {
            System.err.println("Loading frame stubs ERROR: "+e);
        }
        
        return out;
    }
    
    public boolean load(Animator app, String newDir) {        
        return load(app, new File(newDir));
    }
    
    public boolean load(Animator app, File newDir) {
        System.err.println("Loading movie: '"+newDir.toString()+"'");
        dir = newDir;
        dir.mkdirs();        
                
        if (frames != null) {
            /*for (int i = 0; i < frames.size(); ++i) {
            g.removeCache(frames.get(i).im);
            }*/
        }
        
        frames = new ArrayList<Frame>();
        fileIndexMax = 1;
        frameDur = 250;
        label = "";
        cur = 0;
        try {
            ArrayList<FrameStub> stubs = Movie.loadFrameStubs(dir);            
            
            for (FrameStub stub: stubs) {
                Frame fr = new Frame(app);
                fr.fileIndex = Integer.parseInt(stub.fileName);
                fr.sampleIndex = stub.sampleIndex;
                if (fileIndexMax <= fr.fileIndex) {
                    fileIndexMax = fr.fileIndex + 1;
                }
                System.err.println("Loading frame: '" + stub.fileName + ".jpg'");
                fr.loadFromFile(new File(dir, stub.fileName + ".jpg"));
                frames.add(fr);
            }
        } catch (FileNotFoundException e) {            
            return false;
        } catch (IOException e) {
            System.err.println("Loading frame ERROR: "+e);
        }
        
        try {
            BufferedReader f = new BufferedReader(new FileReader(new File(dir, "label.txt")));            
            if (f != null) {
                label = f.readLine();
                if (label == null) label = "";
                musicName = f.readLine();
                if (musicName == null) musicName = "";
                String frameDurS = f.readLine();
                if (frameDurS != null) {
                    long d = Long.parseLong(frameDurS);
                    frameDur = d <= 0 ? 1 : d;
                }
                f.close();
            }
        } catch (FileNotFoundException e) {
            // do nothing, assume ""
        } catch (IOException e) {
            System.err.println("Loading labels ERROR: "+e);
        } catch (NumberFormatException e) {
            frameDur = 250;
        }
        fileIndexMax += 1;
        cur = frames.isEmpty() ? 0 : frames.size() - 1;
        return true;
    }
    
    public boolean save() {
        return save(dir);
    }
            
    
    public boolean save(File new_dir) {
        dir = new_dir;
        if (frames.size() <= 0) {
            return false;
        }
        PrintWriter out;
        try {
            out = new PrintWriter(new File(dir, "frames.txt"));
            for (int i = 0; i < frames.size(); ++i) {
                out.println("" + frames.get(i).fileIndex + ":" + frames.get(i).sampleIndex);
            }
            out.flush();
            out.close();
            
            out = new PrintWriter(new File(dir, "label.txt"));
            out.println(label);
            out.println(musicName);
            out.println(frameDur);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.err.println("Writing ERROR: "+e);
            return false;
        }
        return true;
    }
    
    public int len() {
        return frames.size();
    }
    
    public boolean isEmpty() {
        return frames.isEmpty();
    }
    
    private int constrainIndex(int index, boolean loop) 
    {
        if (loop) {
            index = (index % len() + len()) % len(); // account for negative index
        } else {            
            if (index < 0) {
                index = 0;
            }            
            if (index >= len()) {
                index = len() - 1;
            }
        }
        return index;
    }
    
    Frame getFrame() {
        return getFrame(cur);
    }
    
    Frame getFrame(int index) {
        return getFrame(index, false);
    }
    
    Frame getFrame(int index, boolean loop) {
        index = constrainIndex(index, loop);        
        return index >= 0 ? frames.get(index) : null;
    }
    
    void appendFrame(Frame fr) {
        if (cur > len()) {
            cur = len();
        }
        if (cur < 0) {
            cur = 0;
        }        
        frames.add(fr);
    }    
    
    void insertFrame(Frame fr) 
    {
        insertFrame(fr, true);
    }
    
    void insertFrame(Frame fr, boolean cursorFollows) 
    {
        int index = cur < 0 || frames.isEmpty() 
                ? 0 
                : cur >= frames.size()
                    ? frames.size()
                    : cur + 1;
        insertFrame(index, fr, cursorFollows);        
        
        //appendFrame(fr);
    }
    
    void insertFrame(int index, Frame fr, boolean cursorFollows) 
    {
        if (!cursorFollows && frames.size() > 0 && cur >= index) {
            cur += 1;
        }
        frames.add(index, fr);
        if (cursorFollows) {
            cur = index;
        }
    }
    
    Frame removeCurrentFrame() {
        Frame f = removeFrame(cur);
        if (f == null) return null;        
        
        cur = constrainIndex(cur, false);
        return f;
    }
    
    Frame removeFrame(int index) {
        if (index < 0 || len() <= 0 || index >= len()) return null;
            
        Frame f = frames.get(index);
        frames.remove(index);
        return f;
    }
    
    void setCur(int index) 
    {
        cur = index;
    }
    
    int getCur() 
    {
        return cur;
    }
    
    void shiftCur(int delta) 
    {
        shiftCur(delta, false);
    }
    
    void shiftCur(int delta, boolean loop) 
    {
        cur = constrainIndex(cur + delta, loop);
    }
    
    // playback
    
    private int bounceDir = 1;
    
    public void playbackSetup(int startAt) {
        cur = startAt;
        bounceDir = 1;
    }
    
    public boolean playbackStep() {
        return playbackStep(PlaybackMode.NORMAL);
    }
    
    public boolean playbackStep(PlaybackMode mode) {
        if (mode != PlaybackMode.BOUNCE_LOOP || len() <= 1) {
            cur += 1;
            if (cur >= len()) {
                if (mode == PlaybackMode.LOOP) {
                    cur = 0;
                } else {                    
                    cur = len() - 1;
                    return false;                    
                }
            }
        } else {
            cur += bounceDir;
            if (cur <= 0) {
                cur = 0;
                bounceDir = 1;
            } else if (cur >= len() - 1) {
                cur = len() - 1;
                bounceDir = -1;
            }
        }
        return true;
    }
}
