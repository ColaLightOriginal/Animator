package com.Animator.Animator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Encoder implements Runnable {
    private String dataDir;
    private ArrayList<Frame> frames;
    private String title;
    private String outFile=null;
    
    
    public String getOutFile(){
        return outFile;
    }
    
    private static HashSet<String> uploadedVideos = new HashSet<String>();

    public Encoder(ArrayList<Frame> frames, String dataDir, String title) {        
        this.dataDir = dataDir;
        this.frames = frames;
        this.title = title;        
    }
    
    
    public String getEncodingHashCode(){
        //TODO: add hwaddr here
        return String.valueOf((this.title + String.valueOf(this.frames.size())).hashCode());
    }

    public Encoder encode(String outDir, double fps, String OS) {
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("Lista plików pusta");
        }
        try {
            String file_list = "";
            for (Frame frame : this.frames) {
                file_list += String.valueOf(frame.fileIndex) + ".jpg ";
            }
            outFile = outDir + "/" + this.getEncodingHashCode() + ".avi";
            
            
            String cmd = "./encode.sh "+ String.valueOf(fps) +" " + dataDir + " " + outFile + " " + file_list;
            System.err.println("CMD: "+cmd);
            // to na razie nie działa 
//            if (OS == "windows") {
//                cmd = "./encode.bat "+ String.valueOf(fps) +" " + dataDir + " " + outFile;
//            }
//            else if(OS == "linux"){
//                cmd = "./encode.sh "+ String.valueOf(fps) +" " + dataDir + " " + outFile;
//            }
            
            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//            BufferedWriter inWriter = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
//            
//            for (Frame frame : this.frames) {
//                inWriter.write(frame.fileIndex + ".jpg");
//                inWriter.newLine();
//            }
//            inWriter.flush();
//            inWriter.close();

            String s = null;
            while ((s = stdInput.readLine()) != null) 
                System.out.println(s);
            
            while ((s = stdError.readLine()) != null) 
                System.out.println(s);


        } catch (IOException ex) {
            Logger.getLogger(Encoder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }

    public Encoder upload() {
        try{
            if (uploadedVideos.contains(this.getEncodingHashCode())) return null;
            
            String cmd = "./upload_video.py --file " + outFile + " --privacyStatus private " + " --title " + title.replace(' ', '_') + " " +
                        " --description Film_wykonany_przy_użyciu_Prostego_Animatora";
            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null) 
                System.out.println(s);
            
            while ((s = stdError.readLine()) != null) 
                System.out.println(s);
            
            uploadedVideos.add(this.getEncodingHashCode());

        } catch (IOException ex) {
            Logger.getLogger(Encoder.class.getName()).log(Level.SEVERE, null, ex);
        }                    
        return this;
    }
    
    
    public void run() {
    }
}
