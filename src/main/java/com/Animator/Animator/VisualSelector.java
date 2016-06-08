package com.Animator.Animator;

import processing.core.PApplet;

public abstract class VisualSelector extends Overlay {
    String list[] = new String[0];
    String labels[] = new String[0];
    TimedInteger selected = new TimedInteger(0);
    float y_shift;
    final int hspac = 50;
    
    public VisualSelector(Animator app) {
        super(app);
    }
    
    protected abstract void populateList();
    protected abstract void onSubmit();
    protected void onSelect(int i) { }
    protected void onCancel() { }
    protected abstract void drawBackground();

    @Override
    public void onStart() {        
        populateList();      

        y_shift = app.height;
    }

    private void select(int i) {        
        i = i < 0 ? 0 : i > list.length ? list.length : i;
        boolean n = i != selected.get();
        selected.set(i);
        if (n) {
            onSelect(i);
        }
    }
    
    protected void drawLabel(String text, float x, float y, boolean selected, float visible)
    {
        if (selected) {
            app.fill(visible*255, visible*255, 255);
        } else {
            app.fill(visible*0);
        }
        app.text(text, x, y);
        
    }

    @Override
    public void onDraw() {
        if (list.length < 1) {
            setFinished();
            return;
        }
        
        drawBackground();

        final float sel_base_y = app.height*2/3;                
        float sel_y = selected.get()*hspac;
        float y_shift_target = sel_base_y - sel_y;
        y_shift += 0.3*(y_shift_target - y_shift);
        if (Math.abs(y_shift - y_shift_target) < 2) y_shift = y_shift_target;

        float a = dur() < 800 ? dur()/800.f : 1;

        app.textSize(hspac*0.8f);
        app.noStroke();                                
        for (int i = 0; i < list.length + 1; ++i) {
            float y = y_shift + i*hspac;
            if (y + hspac < 0) continue;
            if (y >= app.height + 2*hspac) break;
            
            boolean iSelected = i == selected.get();
            if (i < list.length) {
                drawLabel(labels[i], 40, y, iSelected, a);
            } else {
                drawLabel("<- Powrót", 30, y + 10, iSelected, a);
            }
        }
    }

    @Override
    public boolean onKeyPressed(char key, int keyCode) {
        if (app.keys.upArrow(keyCode) || (app.debugMode && keyCode == PApplet.UP)) {
            // up arrow
            select(selected.get() - 1);
        } else if (app.keys.downArrow(keyCode) || (app.debugMode && keyCode == PApplet.DOWN)) {
            // down arrow
            select(selected.get() + 1);
        } else if (app.keys.play(keyCode) || (app.debugMode && key == 'p') 
                || app.keys.capture(keyCode)
                || app.keys.submit(keyCode) || (app.debugMode && (keyCode == PApplet.ENTER || keyCode == PApplet.RETURN))) {
            // OK button
            if (selected.get() < list.length) {
                onSubmit();
            } else {
                onCancel();
            }
            setFinished();
        } else if (keyCode == 144) {
            // ignore - numpad prefix
        } else {
            onCancel();
            setFinished();
        }
        return true;
    }
}
