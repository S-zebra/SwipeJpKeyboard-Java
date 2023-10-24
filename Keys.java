import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

abstract class AbstractKey {
  String keyTop;
  float _width, _height, x, y;
  boolean highlighted = false;
  PFont keyFont;
  PApplet context;

  AbstractKey (String keyTop, PFont font, PApplet context) {
    this.keyTop = keyTop;
    this.keyFont = font;
    this.context = context;
  }

  boolean isInBounds(float x, float y) {
    return (x >= this.x && x <= this.x + this._width)
      && (y >= this.y && y <= this.y + this._height);
  }

  void draw() {
    if (highlighted) {
      context.fill(0);
    } else {
      context.noFill();
    }
    context.rect(x, y, _width, _height);
    Point centerPos = new Point((x + (x + _width)) / 2, (y + (y + _height)) / 2);
    context.fill(highlighted ? 255 : 0);
    context.textFont(keyFont);
    context.textAlign(context.CENTER);
    context.text(keyTop, centerPos.x, centerPos.y + (_height / 5));
  }

  void setHighlighted(boolean highlighted) {
    this.highlighted = highlighted;
  }
}

class CharacterKey extends AbstractKey {
  CharacterKey(String keyTop, PFont font, PApplet context) {
    super(keyTop, font, context);
  }
  String getPrintableString() {
    return super.keyTop;
  }
}

class NonCharaKey extends AbstractKey {
  FuncKey funcKey;
  NonCharaKey (String keyTop, PFont font, FuncKey fKey, PApplet context) {
    super(keyTop, font, context);
    this.funcKey = fKey;
  }
}

enum FuncKey {

  SHIFT("SHIFT"),
  ARROW_LEFT("←"),
  ARROW_RIGHT("→"),
  ARROW_UP("↑"),
  ARROW_DOWN("↓"),
  RETURN("Rtn"),
  BACKSPACE("BS");

  private final String label;
  FuncKey(String label){
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
