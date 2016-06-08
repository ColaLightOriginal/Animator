package com.Animator.Animator;

import processing.core.PApplet;

public class KeysLayout2_Win extends KeysLayout {
    @Override
    public boolean capture(int keyCode) {
        return keyCode == PApplet.ENTER || keyCode == PApplet.RETURN;
    }
    
    @Override
    public boolean play(int keyCode) {
        return keyCode == 96 || keyCode == 155;
    }
    
    @Override
    public boolean slower(int keyCode) {
        return keyCode == 97 || keyCode == 35;
    }
     
    @Override
    public boolean faster(int keyCode, char key) {
        return keyCode == 100 || keyCode == 37 || key == '\\';
    }
    
    @Override
    public boolean removeFrame(int keyCode) {
        return keyCode == 39 || keyCode == 102;
    }    
        
    @Override
    public boolean removeFrameUndo(int keyCode) {
        return keyCode == 34 || keyCode == 99;
    }
    
    @Override
    public boolean prevFrame(int keyCode) {
        return keyCode == 103 || keyCode == 36;
    }
    
    @Override
    public boolean nextFrame(int keyCode) {
        return keyCode == 104 || keyCode == 38;
    }
    
    @Override
    public boolean lastFrame(int keyCode) {
        return keyCode == 12 || keyCode == 101;
    }
    
    @Override
    public boolean quickPeak(int keyCode) {
        return keyCode == 127 || keyCode == 110;
    }

    @Override
    public boolean ghostMode(int keyCode) {
        return keyCode == 8;
    }
            
    @Override
    public boolean newMovie(int keyCode) {
        return keyCode == 111;
    }
    
    @Override
    public boolean upload(char key) {
        return key == ']';
    }
     
    @Override
    public boolean movieSelector(int keyCode) {
        return keyCode == 106;
    }
    
    @Override
    public boolean musicSelector(int keyCode) {
        return keyCode == 105 || keyCode == 33;
    }
     
    @Override
    public boolean delayedCapture(int keyCode) {
        return false;
    }
    
    @Override
    public boolean upArrow(int keyCode) {
        return keyCode == 109;
    }
    
    @Override
    public boolean downArrow(int keyCode) {
        return keyCode == 107;
    }
    
    @Override
    public boolean submit(int keyCode) {
        return false;
    }
    
    @Override
    public boolean home(int keyCode) {
        return false;
    }
}
