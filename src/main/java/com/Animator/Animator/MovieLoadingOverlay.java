package com.Animator.Animator;

import java.io.File;

public class MovieLoadingOverlay extends OverlayTask {
    String movieName;

    public MovieLoadingOverlay(Animator applet, String newMovieName) {
        super(applet);
        movieName = newMovieName;
    }
            
    public void run() {
        app.movie.save();
        app.movies.reload();        
        app.movie.load(app, app.movies.getMoviePath(movieName));
        if (app.musicPlayer != null) {
            app.musicPlayer.close();
            app.musicPlayer = null;
        }
        if (!app.movie.musicName.isEmpty() && app.minim != null) {
            File f = new File(app.musicDir, app.movie.musicName);
            if (f.exists()) {
                app.musicPlayer = app.minim.loadFile(f.getAbsolutePath());
            }
        }
                
        app.changeFrameClickT = app.millis();
        app.setRecMode();
    }

    @Override
    public void onDraw() {
        app.background(0);
        app.textSize(20);
        app.fill(128);
        //app.text("Ładowanie filmu... '"+movieName+"'", app.width/5, app.height/2+8);
        app.text("Ładowanie filmu... '"+movieName+"'", app.width/5, app.height/2+8);
    }    
}
