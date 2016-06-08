package com.Animator.Animator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class MusicSelector extends VisualSelector {    
    String musicPreview = "";
    
    public MusicSelector(Animator app) {
        super(app);
    }
    
    
    @Override
    protected void populateList()
    {
        list = app.musicDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".mp3") && new File(dir, name).isFile();
            }
        });        
        
        {   // HACK TODO
            if (list == null) list = new String[0];
            String[] list2 = new String[list.length + 1];
            list2[0] = "";
            for (int i = 0; i < list.length; ++i) {
                list2[i + 1] = list[i];
            }
            list = list2;
        }        
        
        Arrays.sort(list, null);
                        
        labels = new String[list.length];
        for (int i = 0; i < list.length; ++i) {
            labels[i] = list[i].replace('_', ' ').replace(".mp3", "");
            if (labels[i].isEmpty()) {
                labels[i] = "#"+i+" brak nazwy";
            }
            if (!app.movie.musicName.isEmpty() && list[i].equals(app.movie.musicName)) {
                selected.set(i);
                musicPreview = list[i];
            }
        }        
        labels[0] = " cisza"; // HACK TODO
        if (app.musicPlayer != null) {
            app.musicPlayer.setVolume(1);
        }
    }
    
    @Override
    protected void onSelect(int i) 
    {
        if (i >= list.length || i == 0) {
            musicPreview = "";
            app.musicPlayer.close();
            app.musicPlayer = null;
            return;
        }
        
        if (app.musicPlayer != null) {
            app.musicPlayer.close();
        }

        musicPreview = list[selected.get()];
        if (app.minim != null) {
            app.musicPlayer = app.minim.loadFile(new File(app.musicDir, musicPreview).getAbsolutePath());
            if (app.musicPlayer == null) return;
            app.musicPlayer.play();
        }        
    }
    
    @Override
    protected void onSubmit()
    {
        if (app.musicPlayer != null) {
            app.musicPlayer.pause();
            app.musicPlayer.close();
            app.musicPlayer = null;
        }
        app.movie.musicName = list[selected.get()];
        if (!app.movie.musicName.isEmpty() && app.minim != null) {
            app.musicPlayer = app.minim.loadFile(new File(app.musicDir, app.movie.musicName).getAbsolutePath());
        }
    }
    
    @Override
    protected void onCancel()
    {
        if (app.musicPlayer != null) {
            app.musicPlayer.pause();
            app.musicPlayer.close();
            app.musicPlayer = null;
        }
        if (!app.movie.musicName.isEmpty() && app.minim != null) {
            app.musicPlayer = app.minim.loadFile(new File(app.musicDir, app.movie.musicName).getAbsolutePath());
        }
    }
    
    @Override
    protected void drawBackground()
    {
        app.background(128, 128, 128);
    }
}
