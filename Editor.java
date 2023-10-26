import processing.core.PApplet;
import processing.core.PFont;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Editor implements Editable, ConverterEventListener {
  float _width, _height;
  private StringBuilder textContent;
  private PFont fieldFont;
  Point textLTPosition;
  private List<Point> charPositions;
  int caretIndex;
  boolean wrapLine = true;

  // Using this as drawing context.
  // Needed when converting to Java
  private PApplet context;
  private boolean drawCaret;
  private boolean lastDrawCaret;

  Editor(Point position, float _width, float _height, PApplet context) {
    this.context = context;
    fieldFont = context.createFont("SansSerif", 24, true);
    textContent = new StringBuilder();
    this._width = _width;
    this._height = _height;
    textLTPosition = new Point(position.x + 4, fieldFont.getSize() + 4);
    caretIndex = 0;
    charPositions = new ArrayList<Point>();
    charPositions.add(textLTPosition);
    new Timer().scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        drawCaret = !drawCaret;
      }
    }, 500, 500);
  }

  void draw() {
    context.textFont(fieldFont);
    context.textAlign(PApplet.LEFT);
    context.fill(0);
    for (int i = 0; i < textContent.length(); i++) {
      Point cp = charPositions.get(i);
      context.text(textContent.charAt(i), cp.x, cp.y);
    }
    if (drawCaret) {
      context.strokeWeight(1);
      Point cp = charPositions.get(charPositions.size() - 1);
      context.line(cp.x, cp.y - fieldFont.getSize(), cp.x, cp.y);
    }
  }

  void calculateCharPositions() {
    float offX = 0, offY = 0;
    charPositions.clear();
    charPositions.add(textLTPosition);
    for (int i = 0; i < textContent.length(); i++) {
      // サロゲートペアには対応しない
      char ch = textContent.charAt(i);
      int charWidth = fieldFont.getGlyph(ch).width + 5;
      if (wrapLine && textLTPosition.x + offX + charWidth >= _width) {
        offY += fieldFont.getSize() + 4;
        offX = 0;
      } else {
        offX += charWidth;
      }
      charPositions.add(new Point(textLTPosition.x + offX, textLTPosition.y + offY));
    }
  }

  void onStringConfirmed(String str) {
    this.textContent.append(str);
    calculateCharPositions();
  }

  public Point getCharPositionBefore(int i) {
    return charPositions.get(caretIndex - i);
  }

  public Point getCaretPosition() {
    return charPositions.get(caretIndex - 1);
  }

  public PFont getFont() {
    return fieldFont;
  }

  public void onCharEntered(char chr) {
    System.out.println(chr);
    textContent.append(chr);
    calculateCharPositions();
    caretIndex = textContent.length();
  }

  /**
   * (変換用メソッド)<br>
   * <code>beginIndex</code>からカーソル位置までの文字列を消去し、
   * <code>newString</code>を挿入します。
   *
   * @param beginIndex 置換開始位置 0 - (len - 1)
   * @param newString  置換後文字
   */
  public void onStringChanged(int beginIndex, String newString) {
    textContent.replace(Math.max(0, caretIndex - beginIndex), caretIndex, newString);
    System.out.printf("String Changed: %s%n", newString);
    calculateCharPositions();
    caretIndex = textContent.length();
  }

  public void onConvertionCanceled(int len) {
    textContent.delete(caretIndex - len, caretIndex);
  }
}

