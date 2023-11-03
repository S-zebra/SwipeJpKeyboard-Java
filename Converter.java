import processing.core.PApplet;
import processing.core.PFont;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 変換機能
 */
public class Converter implements KeyEventListener {
  /**
   * 未確定文字列（ひらがな）
   */
  private StringBuilder unconfirmedString;

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

  /**
   * 変換候補リスト
   */
  private ConversionStrip strip;

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

  /**
   * 変換を開始する
   */
  void beginConvert() {
    int previousStrLength = unconfirmedString.toString().length();
    bunsetsuList = new GoogleConvertAPI().convert(unconfirmedString.toString());
    listener.onStringChanged(previousStrLength, getCvtString());
    unconfirmedString = new StringBuilder();
    strip = new ConversionStrip(bunsetsuList.get(0).getAllCandidates(), context);
    // 1文字変換フラグOff
    // doesConvert = false
  }

  /**
   * 現在編集中の変換中文字列を返す
   *
   * @return 現在編集中の変換中文字列
   */
  private String getCvtString() {
    StringBuilder sb = new StringBuilder();
    for (Bunsetsu b : bunsetsuList) {
      sb.append(b.getCurrentString());
    }
    return sb.toString();
  }

  /**
   * 変換を終了し、変換中文節をクリアする
   */
  void endConvert() {
    System.out.println("Convert ended");
    bunsetsuList.clear();
    strip.setCandidates(Collections.emptyList(), 0);
    focusedPos = 0;
    isReadyToConvert = false;
  }

  /**
   * 1つ次の文節を注目する
   */
  void nextConvPart() {
    focusedPos = Math.min(bunsetsuList.size() - 1, focusedPos + 1);
    Bunsetsu b = bunsetsuList.get(focusedPos);
    strip.setCandidates(b.getAllCandidates(), b.getCurrentIndex());
  }

  /**
   * 1つ前の文節を注目する
   */
  void prevConvPart() {
    focusedPos = Math.max(0, focusedPos - 1);
    Bunsetsu b = bunsetsuList.get(focusedPos);
    strip.setCandidates(b.getAllCandidates(), b.getCurrentIndex());
  }

  void nextCandidate() {
    System.out.println("Next Candidate");
    int lastStringLen = getCvtString().length();
    Bunsetsu fBunsetsu = bunsetsuList.get(focusedPos);
    fBunsetsu.nextCandidate();
    String newString = getCvtString();
    listener.onStringChanged(lastStringLen, newString);
    strip.focusNext();
  }

  void prevCandidate() {
    int lastStringLen = getCvtString().length();
    Bunsetsu fBunsetsu = bunsetsuList.get(focusedPos);
    fBunsetsu.prevCandidate();
    String newString = getCvtString();
    listener.onStringChanged(lastStringLen, newString);
    strip.focusPrev();
  }

  /**
   * 変換中文字列と変換候補リストをカーソル下部に描画する
   */
  void draw() {
    if (!isReadyToConvert) return;

    // 現在注目中の文節の左下を取る
    Point p = targetEditor.getCharPositionBefore(getCvtString().length());

    // Editorが使用中のものと同じフォント／サイズで変換中文字列を描画する
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

    if (!isConverting()) return;
    strip.draw(new Point(Math.max(0, p.x), p.y + 32));
  }

  void setEditable(Editable e) {
    this.targetEditor = e;
  }
}
