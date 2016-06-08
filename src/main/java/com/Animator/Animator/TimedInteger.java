package com.Animator.Animator;

public class TimedInteger extends TimedDing {
    protected int val = 0;
    
    public TimedInteger() {
        super();
    }
    
    public TimedInteger(int nval) {
        super();
        val = nval;
    }
    
    public int get() {
        return val;
    }
    
    public void set(int nval) {
        if (val == nval) return;
        val = nval;
        touch();
    }
    
    @Override
    public String toString() {
        return val+":"+dur()+"ms";
    }
}
