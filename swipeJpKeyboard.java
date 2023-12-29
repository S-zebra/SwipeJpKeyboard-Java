import processing.core.PApplet;

import java.io.IOException;

public class swipeJpKeyboard extends PApplet {
  Keyboard kbd;
  Converter conv;
  Editor edit;

  public void settings(){
    size(600, 400);
    //    pixelDensity(2);
  }

  public void setup() {
    //fullScreen();
    System.out.println(frameRate);
    edit = new Editor(new Point(0, 0), width, height * 0.5f, this);
    try {
      kbd = new Keyboard(height * 0.5f, "layout.csv", this);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    conv = new Converter(this);
    kbd.addKeyEventListener(conv);
    conv.setEditable(edit);
    conv.setConvEventListener(edit);
  }

  public void draw() {
    background(255);
    // Use image instead of draw();
    kbd.draw();
    kbd.monitor();
    conv.draw();
    edit.draw();
  }

  public void mousePressed() {
    kbd.mousePressed();
  }

  public void mouseDragged() {
    kbd.mouseDragged();
  }

  public void mouseReleased() {
    kbd.mouseReleased();
  }

  public static void main(String[] args) {
    PApplet.main(swipeJpKeyboard.class);
  }
}