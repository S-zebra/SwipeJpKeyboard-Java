class Editor implements Editable, ConverterEventListener {
  float _width, _height;
  private StringBuilder textContent;
  private PFont fieldFont;
  Point textLTPosition;
  private List<Point> charPositions;
  int caretIndex;
  boolean wrapLine = true;

  Editor (Point position, float _width, float _height) {
    fieldFont = createFont("SansSerif", 24, true);
    textContent = new StringBuilder();
    this._width = _width;
    this._height = _height;
    textLTPosition = new Point(position.x + 4, fieldFont.getSize() + 4);
    caretIndex = 0;
    charPositions = new ArrayList<Point>();
    charPositions.add(textLTPosition);
  }

  void draw() {
    textFont(fieldFont);
    textAlign(LEFT);
    for (int i = 0; i < textContent.length(); i++) {
      Point cp = charPositions.get(i);
      text(textContent.charAt(i), cp.x, cp.y);
    }
    if (frameCount % frameRate >= frameRate / 2) {
      strokeWeight(1);
      Point cp = charPositions.get(charPositions.size() - 1);
      line(cp.x, cp.y - fieldFont.getSize(), cp.x, cp.y);
    }
  }

  void calculateCharPositions() {
    float offX = 0, offY = 0;
    charPositions.clear();
    charPositions.add(textLTPosition);
    for (int i = 0; i < textContent.length(); i++) {
      // サロゲートペアには対応しない
      char ch = textContent.charAt(i);
      if (wrapLine && textLTPosition.x + offX > _width) {
        offY += fieldFont.getSize() + 4;
        offX = 0;
      } else {
        offX += fieldFont.getGlyph(ch).width + 5;
      }
      charPositions.add(new Point(textLTPosition.x + offX, textLTPosition.y + offY));
    }
  }

  void onStringConfirmed(String str) {
    this.textContent.append(str);
    calculateCharPositions();
  }

  Point getCharPositionBefore(int i) {
    return charPositions.get(caretIndex - i);
  }

  Point getCaretPosition() {
    return charPositions.get(caretIndex - 1);
  }

  PFont getFont() {
    return fieldFont;
  }

  void onCharEntered(char chr) {
    println(chr);
    textContent.append(chr);
    calculateCharPositions();
    caretIndex = textContent.length();
  }

  void onStringChanged(int prevLen, String newString) {
    textContent.replace(caretIndex - prevLen, caretIndex, newString);
    calculateCharPositions();
    caretIndex = textContent.length();
  }

  void onConvertionCanceled(int len) {
    textContent.delete(caretIndex - len, caretIndex);
  }
}

interface Editable {
  Point getCaretPosition();
  PFont getFont();
  Point getCharPositionBefore(int i);
}
