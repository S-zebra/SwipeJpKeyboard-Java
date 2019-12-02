import java.net.URLEncoder; //<>//
import java.io.UnsupportedEncodingException;

class Converter implements KeyEventListener {
  private static final String CONVERT_URL = "http://www.google.com/transliterate?langpair=ja-Hira%7Cja&text=";
  private StringBuilder unconfirmedString;
  private List<String> convertingString;
  private JSONArray candidates;
  private int focusedPos;
  private boolean doesConvert;
  private ConverterEventListener listener;
  private Editable targetEditor;
  private int previousStrLength;
  private boolean isReadyToConvert = false;

  Converter() {
    unconfirmedString = new StringBuilder();
    convertingString = new ArrayList<String>();
  }

  void setConvEventListener(ConverterEventListener l) {
    listener = l;
  }

  void onKeyEvent (KeyEvent e) {
    println(e.type.toString());
    if (e.type == KeyEventType.SWIPE_BEGIN) {
      doesConvert = true;
    } else if (e.type == KeyEventType.SWIPE_END) {
      if (doesConvert) {
        beginConvert();
      } else {
        endConvert();
      }
    } else { //KeyDown
      if (e._key instanceof NonCharaKey) {
        handleNonCharaKey(((NonCharaKey)e._key).funcKey);
      } else {
        unconfirmedString.append(((CharacterKey)e._key).getPrintableString());
        listener.onCharEntered(unconfirmedString.charAt(unconfirmedString.length() - 1));
        isReadyToConvert = true;
      }
    }
  }

  void handleNonCharaKey(FuncKey fnKey) {
    if (!isConverting()) {
      //targetEditor.sendKey();
      return;
    }
    switch (fnKey) {
    case SHIFT:
      doesConvert = true;
      break;
    case ARROW_DOWN:
      nextCandidate();
      break;
    case ARROW_UP:
      prevCandidate();
      break;
    case ARROW_LEFT:
      prevConvPart();
      break;
    case ARROW_RIGHT:
      nextConvPart();
      break;
    case RETURN:
      endConvert();
      break;
    }
  }

  boolean isConverting() {
    return !getCvtString().isEmpty();
  }

  void beginConvert() {
    println(unconfirmedString.toString());
    previousStrLength = unconfirmedString.toString().length();
    try {
      String url = CONVERT_URL + URLEncoder.encode(unconfirmedString.toString(), "UTF-8");
      GetRequest req = new GetRequest(url);
      println(url);
      req.send();
      candidates = JSONArray.parse(req.getContent());
      println(req.getContent());
      if (candidates == null) {
        endConvert();
        doesConvert = false;
        return;
      }
      convertingString = new ArrayList<String>();
      for (int i = 0; i < candidates.size(); i++) {
        JSONArray cand = candidates.getJSONArray(i).getJSONArray(1);
        convertingString.add(cand.getString(0));
      }
      listener.onStringChanged(previousStrLength, getCvtString());
      doesConvert = false;
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  private String getCvtString() {
    StringBuilder sb = new StringBuilder();
    for (String s : convertingString) {
      sb.append(s);
    }
    return sb.toString();
  }

  void endConvert() {
    StringBuilder confirmedString = new StringBuilder();
    for (String s : convertingString) {
      confirmedString.append(s);
    }
    unconfirmedString = new StringBuilder();
    isReadyToConvert = false;
  }

  void nextConvPart() {
    if (focusedPos < candidates.size() - 1) {
      focusedPos++;
    }
  }

  void prevConvPart() {
    if (focusedPos > 1) {
      focusedPos--;
    }
  }

  void nextCandidate() {
  }

  void prevCandidate() {
  }

  void draw() { //線だけ
    if (!isReadyToConvert || unconfirmedString.length() == 0) return;
    Point p = targetEditor.getCharPositionBefore(getCvtString().length());
    // 1文字目の左下
    float curX = 0;
    for (int i = 0; i < convertingString.size(); i++) {
      if (i == focusedPos) {
        strokeWeight(3);
      } else {
        strokeWeight(1);
      }
      for (int j = 0; j < convertingString.get(i).length(); j++) {
        PFont.Glyph gl = targetEditor.getFont().getGlyph(convertingString.get(i).charAt(j));
        line(p.x + curX, p.y + 5, p.x + curX + gl.width + 5, p.y + 5);
        curX += gl.width + 5;
      }
    }
    // TODO: 候補ウィンドウも描画
  }

  void setEditable(Editable e) {
    this.targetEditor = e;
  }
}

interface ConverterEventListener {
  //void converterEvent(ConverterEvent e);
  void onCharEntered(char chr);
  void onStringChanged(int prevLen, String newString);
  void onConvertionCanceled(int len);
}

enum ConverterEventType {
  CONV_START, CONV_CHANGED, CONV_END
}

class ConverterEvent {
  List<String> convertingStrings;
  int focusedPosition;
}
