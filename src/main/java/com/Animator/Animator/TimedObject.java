package com.Animator.Animator;

public class TimedObject<T> extends TimedDing {
    protected T val;
    
    public TimedObject() {
        super();
        val = null;
    }
    
    public TimedObject(T nval) {
        super();
        val = nval;
        touch();
    }
    
    public T get() {
        return val;
    }
    
    public void set(T nval) {
        if (val == nval) return;
        val = nval;
        touch();
    }
    
    @Override
    public String toString() {
        return val.toString()+":"+dur()+"ms";
    }
}
