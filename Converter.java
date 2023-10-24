import processing.core.PApplet;
import processing.core.PFont;
import processing.data.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 変換機能
 */
public class Converter implements KeyEventListener {

  /**
   * 変換APIのURL (Google transliterate API)
   */
  private static final String CONVERT_URL = "http://www.google.com/transliterate?langpair=ja-Hira%7Cja&text=";

  /**
   * 未確定文字列（ひらがな）
   */
  private StringBuilder unconfirmedString;

  /**
   * 変換APIから返ってきたJSON<br>
   * 例:
   * <pre>
   *  [
   *   "も", ["も", "模", "藻", "喪", "裳"]],
   *   "だむ", ["ダム", "打無", "DAM", "dam", "Dam"]]
   *  ]
   * </pre>
   */
  private JSONArray convertResult;

  /**
   * 注目中の文節番号
   */
  private int focusedPos;
  // SKK的な変換をするかどうかのフラグ
  //  private boolean doesConvert;
  private ConverterEventListener listener;
  /**
   * 文字変換中、文字が変化したとき通知先Editable
   */
  private Editable targetEditor;

  /**
   * <code>SWIPE_END</code>イベントが発火したときに変換開始するかどうか
   */
  private boolean isReadyToConvert = false;

  /**
   * 変換中の文節リスト
   */
  private List<Bunsetsu> bunsetsuList;

  /**
   * 描画先のPApplet
   */
  private final PApplet context;


  Converter(PApplet context) {
    unconfirmedString = new StringBuilder();
    bunsetsuList = new ArrayList<>();
    this.context = context;
  }

  void setConvEventListener(ConverterEventListener l) {
    listener = l;
  }

  /**
   * Keyboardから送られてきたキーイベントのハンドラ
   *
   * @param e 送られてきたKeyEvent
   */
  public void onKeyEvent(KeyEvent e) {
    System.out.println(e.type.toString());
    if (e.type == KeyEventType.KEY_DOWN && e._key instanceof NonCharaKey) {
      handleNonCharaKey(((NonCharaKey) e._key).funcKey);
      return;
    }
    if (e._key instanceof CharacterKey && e.type != KeyEventType.SWIPE_END) {
      unconfirmedString.append(((CharacterKey) e._key).getPrintableString());
      listener.onCharEntered(unconfirmedString.charAt(unconfirmedString.length() - 1));
      isReadyToConvert = true;
    }
    if (e.type == KeyEventType.SWIPE_END && isReadyToConvert) {
      beginConvert();
    }
  }

  /**
   * 非文字キー（矢印キーなど）の処理
   *
   * @param fnKey 処理する関数キー
   */
  void handleNonCharaKey(FuncKey fnKey) {
    if (!isConverting()) {
      //targetEditor.sendKey();
      return;
    }
    switch (fnKey) {
      case SHIFT:
        //        Shiftキーオン: 1文字変換
        //        doesConvert = true;
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
      case BACKSPACE:
        break;
    }
  }

  boolean isConverting() {
    return !getCvtString().isEmpty();
  }

  void beginConvert() {
    /**
     *
     */
    int previousStrLength = unconfirmedString.toString().length();
    URL url = null;
    try {
      url = new URL(CONVERT_URL + URLEncoder.encode(unconfirmedString.toString(), "UTF-8"));
    } catch (MalformedURLException | UnsupportedEncodingException mue) {
      mue.printStackTrace(); // bug
    }
    assert url != null;

    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(2000);
      conn.connect();
    } catch (IOException ex) {
      System.err.println("Cannot connect to the conversion server.");
      ex.printStackTrace();
    }
    assert conn != null;

    StringBuilder sb = new StringBuilder();
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    convertResult = JSONArray.parse(sb.toString());
    System.out.println(sb.toString());
    if (convertResult == null) {
      endConvert();
      // doesConvert = false;
      return;
    }

    bunsetsuList = new ArrayList<>();
    for (int i = 0; i < convertResult.size(); i++) {
      JSONArray resBunsetsu = convertResult.getJSONArray(i);
      String original = resBunsetsu.getString(0); // i番目の文節の変換前入力文字
      JSONArray candidates = resBunsetsu.getJSONArray(1); // 候補のリスト
      // ArrayListに移し替え
      List<String> candidateList = new ArrayList<>();
      for (int j = 0; j < candidates.size(); j++) {
        candidateList.add(candidates.getString(j));
      }
      bunsetsuList.add(new Bunsetsu(original, candidateList));
    }

    listener.onStringChanged(previousStrLength, getCvtString());
    unconfirmedString = new StringBuilder();
    // 1文字変換フラグOff
    // doesConvert = false
  }

  private String getCvtString() {
    StringBuilder sb = new StringBuilder();
    for (Bunsetsu b : bunsetsuList) {
      sb.append(b.getCurrentString());
    }
    return sb.toString();
  }

  void endConvert() {
    System.out.println("Convert ended");
    StringBuilder confirmedString = new StringBuilder();
    for (Bunsetsu b : bunsetsuList) {
      confirmedString.append(b.getCurrentString());
    }
    bunsetsuList.clear();
    focusedPos = 0;
    isReadyToConvert = false;
  }

  void nextConvPart() {
    if (focusedPos < convertResult.size() - 1) {
      focusedPos++;
    }
  }

  void prevConvPart() {
    if (focusedPos >= 1) {
      focusedPos--;
    }
  }

  void nextCandidate() {
    System.out.println("Next Candidate");
    int lastStringLen = getCvtString().length();
    Bunsetsu fBunsetsu = bunsetsuList.get(focusedPos);
    fBunsetsu.nextCandidate();
    String newString = getCvtString();
    listener.onStringChanged(lastStringLen, newString);
  }

  void prevCandidate() {
    int lastStringLen = getCvtString().length();
    Bunsetsu fBunsetsu = bunsetsuList.get(focusedPos);
    fBunsetsu.prevCandidate();
    String newString = getCvtString();
    listener.onStringChanged(lastStringLen, newString);
  }

  void draw() { //線だけ
    if (!isReadyToConvert) return;
    Point p = targetEditor.getCharPositionBefore(getCvtString().length());
    // 1文字目の左下
    float curX = 0;
    for (int i = 0; i < bunsetsuList.size(); i++) {
      context.strokeWeight(i == focusedPos ? 3 : 1);
      String cBunsetsuString = bunsetsuList.get(i).getCurrentString();
      for (int j = 0; j < cBunsetsuString.length(); j++) {
        PFont.Glyph gl = targetEditor.getFont().getGlyph(cBunsetsuString.charAt(j));
        context.line(p.x + curX, p.y + 5, p.x + curX + gl.width + 5, p.y + 5);
        curX += gl.width + 5;
      }
    }

    // TODO: 候補ウィンドウも描画
    if (bunsetsuList.isEmpty()) return;
    float candWindowStartX = Math.max(0, p.x);
    for (String c : bunsetsuList.get(focusedPos).getAllCandidates()) {
      context.text(c, candWindowStartX, p.y + 25);
      candWindowStartX += (targetEditor.getFont().getSize() * c.length()) + 5;
    }
  }

  void setEditable(Editable e) {
    this.targetEditor = e;
  }
}

/**
 * 文節
 */
class Bunsetsu {
  private int currentIndex = 0;
  private final String original;
  private final List<String> candidates;
  private String currentString;

  public Bunsetsu(String original, List<String> candidates) {
    this.original = original;
    this.candidates = candidates;
    if (original == null)
      throw new IllegalArgumentException("Original should not be null");
    if (candidates == null || candidates.isEmpty())
      throw new IllegalArgumentException("Candidates should not be null or empty");
    currentString = candidates.get(0);
  }

  public void reset() {
    currentString = original;
  }

  public void nextCandidate() {
    currentIndex = Math.min(candidates.size() - 1, currentIndex + 1);
    currentString = candidates.get(currentIndex);
  }

  public void prevCandidate() {
    currentIndex = Math.max(0, currentIndex - 1);
    currentString = candidates.get(currentIndex);
  }

  public String getCurrentString() {
    return currentString;
  }

  public List<String> getAllCandidates() {
    return candidates;
  }
}