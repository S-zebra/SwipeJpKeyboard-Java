import java.io.FileReader;
import java.util.List;

class Keyboard {

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

  public Keyboard (float _height, String layoutFilePath) throws IOException {
    paths = new ArrayList<Path>();
    layoutTable = parseLayoutFile(new File(layoutFilePath));

    float nRows = layoutTable.size();
    float nCols = layoutTable.get(0).size();
    float keyWidPx = width / nCols;
    float keyHeiPx = _height / (nRows + 2);
    float startPos = height - _height + keyHeiPx;

    keyboardFont = createFont("SansSerif", FONT_SIZE, true);
    PFont controlKeyFont = createFont("SansSerif", FONT_SIZE - 2, true);

    List<AbstractKey> controlRow = new ArrayList<AbstractKey>();
    for (FuncKey fk : FuncKey.values()) {
      NonCharaKey newKey = new NonCharaKey(fk.toString(), controlKeyFont, fk);
      controlRow.add(newKey);
      newKey.x = keyWidPx * 3 * fk.ordinal();
      newKey.y = startPos;
      newKey._height = keyHeiPx;
      newKey._width = keyWidPx * 3;
    }
    layoutTable.add(controlRow);

    for (int i = 0; i < layoutTable.size() - 1; i++) {
      float top = keyHeiPx * (i+1) + startPos;
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
    stroke(0);
    strokeWeight(1);
    for (Path p : paths) {
      line(p.bp.x, p.bp.y, p.ep.x, p.ep.y);
    }
    for (List<AbstractKey> row : layoutTable) {  
      for (AbstractKey kb : row) {
        kb.draw();
      }
    }
  }

  void addKeyEventListener (KeyEventListener listener) {
    if (this.listeners == null) {
      this.listeners = new ArrayList<KeyEventListener>();
    }
    this.listeners.add(listener);
  }

  void monitor() {
    if (mousePressed) {
      if (curPath != null) { 
        line(curPath.bp.x, curPath.bp.y, curPath.ep.x, curPath.ep.y);
      }
      if (millis() - stayStartTime >= 50 && moveDistance >= 20) {
        paths.add(curPath);
        AbstractKey _key =  getKeyAt(mouseX, mouseY);
        dispatchKeyEvent(new KeyEvent(KeyEventType.KEY_DOWN, _key));
        curPath = new Path(new Point(mouseX, mouseY), new Point(mouseX, mouseY));
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
        row.add(new CharacterKey(s, keyboardFont));
      }
      keyLayout.add(row);
    }
    br.close();
    return keyLayout;
  }

  void mousePressed() {
    curPath = new Path(new Point(mouseX, mouseY), new Point(mouseX, mouseY));
    dispatchKeyEvent(new KeyEvent(KeyEventType.KEY_DOWN, getKeyAt(mouseX, mouseY)));
    stayStartTime = millis();
  }

  void mouseDragged() {
    curPath.ep.x = mouseX;
    curPath.ep.y = mouseY;
    stayStartTime = millis();
    moveDistance++;
    if (!swipeBeginDispatched && moveDistance >= 30) {
      dispatchKeyEvent(new KeyEvent(KeyEventType.SWIPE_BEGIN, null));
      swipeBeginDispatched = true;
    }
  }

  void mouseReleased() {
    paths.clear();
    dispatchKeyEvent(new KeyEvent(KeyEventType.SWIPE_END, getKeyAt(mouseX, mouseY)));
    moveDistance = 0;
    swipeBeginDispatched = false;
  }
}
