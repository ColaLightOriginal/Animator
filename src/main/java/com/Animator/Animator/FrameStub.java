package com.Animator.Animator;

public class FrameStub {
    String fileName;
    int sampleIndex;
    public FrameStub(String fileName, int sampleIndex) {
      this.fileName = fileName;
      this.sampleIndex = sampleIndex;
    }

    public static FrameStub fromString(String frameLine) {
      if (frameLine == null) return null;
      frameLine = frameLine.trim();
      if (frameLine.isEmpty()) return null;
      int sep = frameLine.indexOf(':');

      String name;
      String sampleIndex;
      if (sep != -1) {
          name = frameLine.substring(0, sep);
          sampleIndex = frameLine.substring(sep + 1);
      } else {
          name = frameLine;
          sampleIndex = "-1";
      }
      return new FrameStub(name, Integer.parseInt(sampleIndex));
    }
}