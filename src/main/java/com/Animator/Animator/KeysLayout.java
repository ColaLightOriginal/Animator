package com.Animator.Animator;

import processing.core.PApplet;

public class KeysLayout {
    public boolean capture(int keyCode) {
        return keyCode == PApplet.ENTER || keyCode == PApplet.RETURN;
    }
    
    public boolean play(int keyCode) {
        return keyCode == 96 || keyCode == 155;
    }
    
    public boolean slower(int keyCode) {
        return keyCode == 97 || keyCode == 40;
    }
     
    public boolean faster(int keyCode, char key) {
        return keyCode == 100 || keyCode == 38 || keyCode == 226 || key == '\\';
    }
    
    public boolean removeFrame(int keyCode) {
        //return keyCode == 227 || keyCode == 102;
    	return keyCode == 127; //delete
    }
    
    public boolean removeFrameUndo(int keyCode) {
        return false;
    }
    
    public boolean prevFrame(int keyCode) {
        return keyCode == 37;
    }
    
    public boolean nextFrame(int keyCode) {
        return keyCode == 39;
    }
    
    public boolean lastFrame(int keyCode) {
        return false;
    }
    
    public boolean quickPeak(int keyCode) {
        return keyCode == 108;
    }

    public boolean ghostMode(int keyCode) {
        return keyCode == 99 || keyCode == 34;
    }
            
    public boolean newMovie(int keyCode) {
        return keyCode == 103 || keyCode == 36;
    }
    
    public boolean upload(char key) {
        return key == ']';
    }
     
    public boolean movieSelector(int keyCode) {
        return keyCode == 224 || keyCode == 104;
    }
    
    public boolean musicSelector(int keyCode) {
        return keyCode == 111;
    }
     
    public boolean delayedCapture(int keyCode) {
        return keyCode == PApplet.BACKSPACE;
    }
    
    public boolean upArrow(int keyCode) {
        return keyCode == 106 || keyCode == 38;
    }
    
    public boolean downArrow(int keyCode) {
        return keyCode == 33 || keyCode == 105 || keyCode == 40;
    }
    
    public boolean submit(int keyCode) {
        return keyCode == 107;
    }
    
    public boolean home(int keyCode) {
        return keyCode == 109;
    }
}
