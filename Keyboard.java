import processing.core.PApplet;
import processing.core.PFont;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Keyboard {

  List<List<AbstractKey>> layoutTable;
  final int FONT_SIZE = 14;

  PFont keyboardFont;
  Path curPath;
  List<Path> paths;

  int stayStartTime;
  float moveDistance;

  List<KeyEventListener> listeners;

  AbstractKey curKey;

  boolean swipeBeginDispatched;
  boolean isSwiping;
  private PApplet context;

  public Keyboard(float _height, String layoutFilePath, PApplet context) throws IOException {
    this.context = context;
    paths = new ArrayList<Path>();
    File f = new File(layoutFilePath);
    System.out.println(f.getAbsolutePath());
    layoutTable = parseLayoutFile(f);

    float nRows = layoutTable.size();
    float nCols = layoutTable.get(0).size();
    float keyWidPx = context.width / nCols;
    float keyHeiPx = _height / (nRows + 2);
    float startPos = context.height - _height + keyHeiPx;

    keyboardFont = context.createFont("SansSerif", FONT_SIZE, true);
    PFont controlKeyFont = context.createFont("SansSerif", FONT_SIZE - 2f, true);

    List<AbstractKey> controlRow = new ArrayList<AbstractKey>();
    FuncKey[] funcKeys = FuncKey.values();
    float fkWidth = (float) context.width / funcKeys.length;
    for (FuncKey fk : funcKeys) {
      NonCharaKey newKey = new NonCharaKey(fk.getLabel(), controlKeyFont, fk, context);
      controlRow.add(newKey);
      newKey.x = fkWidth * fk.ordinal();
      newKey.y = startPos;
      newKey._height = keyHeiPx;
      // TODO: Let Keyboard know its width
      newKey._width = fkWidth;
    }
    layoutTable.add(controlRow);

    for (int i = 0; i < layoutTable.size() - 1; i++) {
      float top = keyHeiPx * (i + 1) + startPos;
      for (int j = 0; j < layoutTable.get(i).size(); j++) {
        float left = keyWidPx * j;
        AbstractKey cKey = layoutTable.get(i).get(j);
        cKey.x = left;
        cKey.y = top;
        cKey._height = keyHeiPx;
        cKey._width = keyWidPx;
        cKey.keyFont = keyboardFont;
      }
    }
  }

  void draw() {
    context.stroke(0);
    context.strokeWeight(1);
    for (Path p : paths) {
      context.line(p.bp.x, p.bp.y, p.ep.x, p.ep.y);
    }
    for (List<AbstractKey> row : layoutTable) {
      for (AbstractKey kb : row) {
        kb.draw();
      }
    }
  }

  void addKeyEventListener(KeyEventListener listener) {
    if (this.listeners == null) {
      this.listeners = new ArrayList<KeyEventListener>();
    }
    this.listeners.add(listener);
  }

  void monitor() {
    if (context.mousePressed) {
      if (curPath != null) {
        context.line(curPath.bp.x, curPath.bp.y, curPath.ep.x, curPath.ep.y);
      }
      // ステイ開始: キーを追加
      if (context.millis() - stayStartTime >= 50 && moveDistance >= 20) {
        paths.add(curPath);
        AbstractKey _key = getKeyAt(context.mouseX, context.mouseY);
        if (_key == null) return;
        dispatchKeyEvent(new KeyEvent(KeyEventType.KEY_DOWN, _key));
        curPath = new Path(new Point(context.mouseX, context.mouseY), new Point(context.mouseX, context.mouseY));
        moveDistance = 0;
      }
    }
  }

  private AbstractKey getKeyAt(float x, float y) {
    for (List<AbstractKey> row : layoutTable) {
      for (AbstractKey kb : row) {
        if (kb.isInBounds(x, y)) {
          return kb;
        }
      }
    }
    return null;
  }

  private void dispatchKeyEvent(KeyEvent e) {
    if (this.listeners == null) return;
    for (KeyEventListener l : listeners) {
      l.onKeyEvent(e);
    }
  }

  private List<List<AbstractKey>> parseLayoutFile(File file) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(file));
    List<List<AbstractKey>> keyLayout = new ArrayList<List<AbstractKey>>();
    String curLine;
    while ((curLine = br.readLine()) != null) {
      List<AbstractKey> row = new ArrayList<AbstractKey>();
      for (String s : curLine.split(",")) {
        row.add(new CharacterKey(s, keyboardFont, context));
      }
      keyLayout.add(row);
    }
    br.close();
    return keyLayout;
  }

  void mousePressed() {
    AbstractKey pressedKey = getKeyAt(context.mouseX, context.mouseY);
    if (pressedKey == null) return;
    if (pressedKey.getClass() == NonCharaKey.class) {
      pressedKey.setHighlighted(true);
    }
    curPath = new Path(new Point(context.mouseX, context.mouseY), new Point(context.mouseX, context.mouseY));
    dispatchKeyEvent(new KeyEvent(KeyEventType.KEY_DOWN, pressedKey));
    stayStartTime = context.millis();
  }

  void mouseDragged() {
    curPath.ep.x = context.mouseX;
    curPath.ep.y = context.mouseY;
    stayStartTime = context.millis();
    moveDistance++;
    if (!swipeBeginDispatched && moveDistance >= 30) {
      dispatchKeyEvent(new KeyEvent(KeyEventType.SWIPE_BEGIN, null));
      swipeBeginDispatched = true;
    }
  }

  void mouseReleased() {
    paths.clear();
    moveDistance = 0;
    swipeBeginDispatched = false;

    AbstractKey releasedKey = getKeyAt(context.mouseX, context.mouseY);
    if (releasedKey == null) return;
    if (releasedKey instanceof NonCharaKey) {
      releasedKey.setHighlighted(false);
    }
    if (releasedKey instanceof CharacterKey) {
      dispatchKeyEvent(new KeyEvent(KeyEventType.SWIPE_END, releasedKey));
    }
  }
}
