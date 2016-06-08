package com.Animator.Animator;

import processing.core.PApplet;

public class KeysLayout_Vertical extends KeysLayout {
    @Override
    public boolean capture(int keyCode) {
        return keyCode == PApplet.ENTER || keyCode == PApplet.RETURN;
    }
    
    @Override
    public boolean play(int keyCode) {
        return keyCode == 107;
    }
    
    @Override
    public boolean slower(int keyCode) {
        return keyCode == 109;
    }
     
    @Override
    public boolean faster(int keyCode, char key) {
        return keyCode == 106;
    }
    
    @Override
    public boolean removeFrame(int keyCode) {
        return keyCode == 34 || keyCode == 99;
    }
    
    @Override
    public boolean quickPeak(int keyCode) {
        return keyCode == 102 || keyCode == 227;
    }

    @Override
    public boolean ghostMode(int keyCode) {
        return keyCode == 108 || keyCode == 127;
    }
            
    @Override
    public boolean newMovie(int keyCode) {
        return keyCode == 35 || keyCode == 97;
    }
    
    @Override
    public boolean upload(char key) {
        return key == ']';
    }
     
    @Override
    public boolean movieSelector(int keyCode) {
        return keyCode == 226 || keyCode == 100;
    }
    
    @Override
    public boolean musicSelector(int keyCode) {
        return keyCode == 101 || keyCode == 65368;
    }
     
    @Override
    public boolean delayedCapture(int keyCode) {
        return keyCode == 155 || keyCode == 96;
    }
    
    @Override
    public boolean upArrow(int keyCode) {
        return keyCode == 103 || keyCode == 36;
    }
    
    @Override
    public boolean downArrow(int keyCode) {
        return keyCode == 224 || keyCode == 104;
    }
    
    @Override
    public boolean submit(int keyCode) {
        return keyCode == 111;
    }
    
    @Override
    public boolean home(int keyCode) {
        return keyCode == 98 || keyCode == 225;
    }
}
