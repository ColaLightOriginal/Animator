package com.Animator.Animator;

public class TimedDing {
    protected long time;
            
    public TimedDing() {
        time = Integer.MIN_VALUE; // intentional, not Long; Long would break arithmetic in case is dur() called on untouched Ding
    }
    
    public TimedDing(boolean touchNow)            
    {
        if (touchNow) touch();
    }
    
    public final void touch() {
        touch(millis());
    }
    
    public final void touch(long now) {
        time = now;
    }
    
    public long dur() {
        return dur(millis());
    }
    
    public long dur(long now) {
        return now - time;
    }
    
    public static long millis() {
        return System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return dur()+"ms";
    }
}
