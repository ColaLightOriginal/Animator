package com.Animator.Animator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PImage;

public class MovieSelector extends VisualSelector {    
    private ArrayList<FrameStub> frameStubs = null; 
    private int previewFrame = 0;
    private String previewPath = "";
    private int lastSelected = -1;
    private TimedObject<PImage> previewImg = new TimedObject<PImage>();
    
    public MovieSelector(Animator app) {
        super(app);
    }
    
    @Override
    protected void populateList()
    {
        app.movies.reload();
        list = app.movies.getAll();
        selected.set(list.length - 1);
        labels = new String[list.length];
        for (int i = 0; i < list.length; ++i) {
            labels[i] = app.movies.getLabel(list[i]);
            if (labels[i].isEmpty()) {
                labels[i] = "#"+i+" brak nazwy";
            }
            if (!app.movie.isEmpty() && list[i].equals(app.movie.getName())) {
                selected.set(i);
            }
        }
        lastSelected = -1;
    }
    
    @Override
    protected void onSubmit()
    {
        app.loadMovie(list[selected.get()]);
        
    }
    
    @Override
    protected void drawBackground()
    {
        app.background(128, 128, 128);
        
        if (selected.dur() < 500) return;
        if (selected.get() != lastSelected) {
            lastSelected = selected.get();
            previewImg.set(null);
            frameStubs = null;
            if (selected.get() >= list.length || selected.get() < 0) {
                return;
            }
            previewPath = app.movies.getMoviePath(list[selected.get()]);
            try {
                frameStubs = Movie.loadFrameStubs(new File(previewPath));
                previewFrame = 0;
            } catch (FileNotFoundException e) {
                frameStubs = null;
                return;
            }
        }
        if (frameStubs == null || frameStubs.isEmpty()) {
            return;
        }
        if (previewImg.get() == null || previewImg.dur() > 200) {
            String path = previewPath + "/" + frameStubs.get(previewFrame).fileName + ".jpg";
            previewImg.set(app.loadImage(path));
            previewFrame = (previewFrame + 3) % frameStubs.size();
        }
        if (previewImg.get() != null) {
            app.image(previewImg.get(), app.width - 340, app.height - 300, 320, 240);
        }
    }
}
