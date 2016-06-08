package com.Animator.Animator;

public class TimedBool extends TimedDing {
    protected boolean val = false;
    
    public TimedBool() {
        super();
    }
    
    public TimedBool(boolean nval) {
        super();
        val = nval;
    }
    
    public boolean get() {
        return val;
    }
    
    public void set(boolean nval) {
        if (val == nval) return;
        val = nval;
        touch();
    }
    
    public void toggle() {
        set(!get());
    }
    
    @Override
    public String toString() {
        return val+":"+dur()+"ms";
    }
}
