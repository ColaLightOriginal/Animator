package com.Animator.Animator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

public class MovieCollection {
    private File baseDir;
    protected String[] movieNames;
    
    public MovieCollection() {
        baseDir = new File(".");
        movieNames = new String[0];
    }
    
    public MovieCollection(File dir) {
        baseDir = dir;
        reload();
    }
    
    public MovieCollection(String dirPath) {
        this(new File(dirPath));
    }
    
    public final void reload() {
        reload(baseDir);
    }
    
    public void reload(String dirPath) {
        reload(new File(dirPath));
    }
    
    public void reload(File dir) {
        baseDir = dir;        
        
        System.err.println("Loading movie collection: " + baseDir.getAbsolutePath());
        movieNames = baseDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith(".") && new File(dir, name).isDirectory();
            }
        });
        if (movieNames == null) {
            movieNames = new String[0];
        }
        Arrays.sort(movieNames, null);
    }
    
    public String[] getAll() {
        return movieNames.clone();
    }
    
    public String getLabel(String movieName) {
        File dir = new File(baseDir, movieName);
        String label = null;
        
        try {
            BufferedReader f = new BufferedReader(new FileReader(new File(dir, "label.txt")));            
            if (f != null) {
                label = f.readLine();
                f.close();
            }
        } catch (FileNotFoundException e) {            
        } catch (IOException e) {
            System.err.println("Loading labels ERROR: "+e);
        }
        
        if (label == null) label = "";
        
        return label;
    }
    
    public String getLast()
    {
        return movieNames.length > 0 ? movieNames[movieNames.length - 1] : "0001";
    }
    
    public String getMoviePath(String movieName) 
    {
        return new File(baseDir, movieName).getAbsolutePath();
    }
    
    public String getAfter(String current)
    {
        return getAfter(current, true);
    }
    
    public String getAfter(String current, boolean loop) {                
        if (movieNames.length <= 0) {
            return "0001";
        } else {
            int ind = Arrays.binarySearch(movieNames, current, null);
            if (ind < 0) {
                ind = movieNames.length - 1;
            }
            ind += 1;
            if (ind >= movieNames.length) {                
                ind = loop ? 0 : movieNames.length - 1;
            }
            return movieNames[ind];
        }        
    }
    
    public String getBefore(String current) {
        return getBefore(current, true);
    }
    
    public String getBefore(String current, boolean loop) {                        
        if (movieNames.length <= 0) {
            return "0001";
        } else {
            int ind = Arrays.binarySearch(movieNames, current, null);
            if (ind < 0) {
                return current;
            }
            ind -= 1;
            if (ind < 0) {
                ind = loop ? movieNames.length - 1 : 0;
            }
            return movieNames[ind];
        }
    }
    
    public String getNew() {        
        if (movieNames.length <= 0) {
            return "0001";
        } else {            
            return String.format("%04d", Integer.parseInt(movieNames[movieNames.length - 1]) + 1);
        }        
    }
}
