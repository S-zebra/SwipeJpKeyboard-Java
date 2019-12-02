abstract class AbstractKey {
  String keyTop;
  float _width, _height, x, y;
  boolean highlighted = false;
  PFont keyFont;

  AbstractKey (String keyTop, PFont font) {
    this.keyTop = keyTop;
    this.keyFont = font;
  }

  boolean isInBounds(float x, float y) {
    return (x >= this.x && x <= this.x + this._width)
      && (y >= this.y && y <= this.y + this._height);
  }

  void draw() {
    if (highlighted) {
      fill(0);
    } else {
      noFill();
    }
    rect(x, y, _width, _height);
    Point centerPos = new Point((x + (x + _width)) / 2, (y + (y + _height)) / 2);
    fill(highlighted ? 255 : 0);
    textFont(keyFont);
    textAlign(CENTER);
    text(keyTop, centerPos.x, centerPos.y + (_height / 5));
  }

  void setHighlighted(boolean highlighted) {
    this.highlighted = highlighted;
  }
}

class CharacterKey extends AbstractKey {
  CharacterKey(String keyTop, PFont font) {
    super(keyTop, font);
  }
  String getPrintableString() {
    return super.keyTop;
  }
}

class NonCharaKey extends AbstractKey {
  FuncKey funcKey;
  NonCharaKey (String keyTop, PFont font, FuncKey fKey) {
    super(keyTop, font);
    this.funcKey = fKey;
  }
}

enum FuncKey {
  SHIFT, ARROW_LEFT, ARROW_RIGHT, ARROW_UP, ARROW_DOWN, RETURN;
}
