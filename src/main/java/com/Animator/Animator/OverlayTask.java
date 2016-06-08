package com.Animator.Animator;

/**
 * Overlay with threaded background task (run() method)
 * 
 */
public abstract class OverlayTask extends Overlay implements Runnable {
    protected Thread thread;
    
    public OverlayTask(Animator applet) {
        super(applet);
    }
    
    @Override
    protected void startRun() {
        super.startRun();
        thread = new Thread(this);
        thread.start();
    }
    
    @Override
    public boolean isFinished() {
        return thread == null || !thread.isAlive();
    }
}
