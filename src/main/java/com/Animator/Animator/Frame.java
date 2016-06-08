package com.Animator.Animator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import processing.core.PApplet;
import processing.core.PImage;

//Uncomment for Turbo jpeg version and uncomment the code below, in getImage() function:
//import org.libjpegturbo.turbojpeg.TJ;
//import org.libjpegturbo.turbojpeg.TJDecompressor;

public class Frame {
  int fileIndex;
  byte data[];
  int sampleIndex;
  TimedDing sampleTriggered;
  
  private PImage im;
  private long usedT;
  private PApplet pApplet;
  private int lazyPaintCount = 0, lazyPaintLast = 0;
  
  private static long cacheTimer = 0;
  private static long maxCachedFrames = 100;
  
  Frame(PApplet pApplet) {
    im = null;
    fileIndex = 0;
    data = null;
    sampleIndex = -1;
    sampleTriggered = new TimedDing(true);
    this.pApplet = pApplet;
  }
  
  boolean loadFromFile(File file)
  {
    try {
      data = new byte[(int)file.length()];
      System.err.println("Loading frame '"+file.toString()+"': "+data.length+" bytes");
      DataInputStream dis = new DataInputStream(new FileInputStream(file));         
      
      dis.readFully(data);
      dis.close();
    } catch (IOException e) {
      System.err.println("Loading frame '"+file.toString()+"' ERROR: "+e);
      return false;
    }
    return true;
  }
  
  void flushCache()
  {
      flushCache(false);
  }
  
  void flushCache(boolean force) 
  {
      flushCache(cacheTimer - maxCachedFrames, force);
  }
  
  void flushCache(long olderThan, boolean force)
  {
    if (im == null) return;
    if (usedT < olderThan || force) {
      //System.out.println("Clearing cache: "+fileIndex);
      im = null;      
    }
  }
  
  PImage getImage()
  {
    usedT = cacheTimer++;
    if (im != null) {
      return im;
    }
    //System.out.println("Reading cache: "+fileIndex);
    try {
      //Uncomment for Turbo jpeg version and comment the block below        
      /*TJDecompressor decoder = new TJDecompressor(data); // turbo jpeg decompressor
      BufferedImage bimg = decoder.decompress(0, 0, BufferedImage.TYPE_INT_RGB, TJ.FLAG_FASTDCT);
      im = pApplet.createImage(decoder.getWidth(), decoder.getHeight(), processing.core.PConstants.RGB);      
      bimg.getRGB(0, 0, im.width, im.height, im.pixels, 0, im.width);
      im.updatePixels();*/
        
      //Remove this block for Turbo jpeg version:      
        ByteArrayInputStream bis = new ByteArrayInputStream(data); 
        BufferedImage bimg = ImageIO.read(bis); 
        im = pApplet.createImage(bimg.getWidth(), bimg.getHeight(), processing.core.PConstants.RGB);
        bimg.getRGB(0, 0, im.width, im.height, im.pixels, 0, im.width);
        im.updatePixels();
      
    }
    catch(Exception e) {
      System.err.println("Can't create image from buffer");
      e.printStackTrace();
    }
    return im;
  }
  
  void touch()
  {
    getImage();
  }
  
  void paint(float x, float y)
  {
    pApplet.image(getImage(), x, y);
  }
  
  void paint(float x, float y, float w, float h)
  {
    pApplet.image(getImage(), x, y, w, h);
  }
  
  void lazyPaint(float x, float y, float w, float h, int after)
  {
      int t = pApplet.frameCount;      
      if (lazyPaintLast >= t - 1) {
          lazyPaintCount += 1;
      } else {
          lazyPaintCount = 1;
      }
      lazyPaintLast = t;
      if (im == null && lazyPaintCount <= after) {
          pApplet.fill(96);
          pApplet.rect(x, y, w, h);
          return;
      }
      paint(x, y, w, h);
  }
}