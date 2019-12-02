import http.requests.*;
Keyboard kbd;
Converter conv;
Editor edit;

void setup() {
  size(600, 400);
  //fullScreen();
  edit = new Editor(new Point(0, 0), width, height * 0.5);
  try {
    kbd = new Keyboard(height * 0.5, "/Users/kazu/Documents/Processing/swipeJpKeyboard/data/layout.csv");
    conv = new Converter();
    kbd.addKeyEventListener(conv);
    conv.setEditable(edit);
    conv.setConvEventListener(edit);
  } 
  catch (IOException ioe) {
    ioe.printStackTrace();
  }
}

void draw() {
  background(255);
  kbd.draw();
  kbd.monitor();
  conv.draw();
  edit.draw();
}

void mousePressed() {
  kbd.mousePressed();
}

void mouseDragged() {
  kbd.mouseDragged();
}

void mouseReleased() {
  kbd.mouseReleased();
}

class Point {
  float x, y;
  Point (float x, float y) {
    this.x = x;
    this.y = y;
  }
}

class Path implements Cloneable {
  Point bp, ep;
  Path(Point bp, Point ep) {
    this.bp = bp;
    this.ep = ep;
  }
}
