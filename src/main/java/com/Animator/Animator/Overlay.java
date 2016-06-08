package com.Animator.Animator;

/**
 * Interactive overlay
 * 
 */
public class Overlay {    
    protected Animator app;
    private boolean finishedFlag = false;
    private TimedDing timer;
    
    public Overlay(Animator applet) {
        app = applet;
        timer = new TimedDing();
    }
    
    protected void startRun() {        
    }
    
    public final void start() {
        onStart();
        timer.touch();
        startRun();
    }
    
    /**
     * 
     * @return Milliseconds since onStart was called
     */
    public final long dur() {
        return timer.dur();
    }
    
    protected final void setFinished() {
        finishedFlag = true;
    }
        
    public boolean isFinished() {
        return finishedFlag;
    }
    
    // Override those:
    
    public void onDraw() {
    }
    
    /**
     * 
     * @param key
     * @param keyCode
     * @return true if keypress has been handled. If task shouldn't be interrupted it is safe to return true.
     */
    public boolean onKeyPressed(char key, int keyCode) {
        return true;
    }
    
    public void onStart() {
    }
    
    public void onEnd() {
    }    
}
