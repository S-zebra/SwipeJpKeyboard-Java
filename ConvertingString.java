import processing.data.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ConvertingString {
  private final List<Bunsetsu> bunsetsuList;
  private int focusedBunsetsu;

  /**
   * 変換APIから返ってきたJSONから、文節に区切られたConvertingStringを生成します。<br>
   * 例:
   * <pre>
   *  [
   *    "も", ["も", "模", "藻", "喪", "裳"]],
   *    "だむ", ["ダム", "打無", "DAM", "dam", "Dam"]]
   *  ]
   * </pre>
   *
   * @param convertResult 変換APIから返ってきたJSON
   */
  public ConvertingString(JSONArray convertResult) {
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
    focusedBunsetsu = 0;
  }

  void nextConvPart() {
    if (focusedBunsetsu < bunsetsuList.size() - 1) {
      focusedBunsetsu++;
    }
  }

  void prevConvPart() {
    if (focusedBunsetsu >= 1) {
      focusedBunsetsu--;
    }
  }

  void nextCandidate(ConverterEventListener listener) {
    System.out.println("Next Candidate");
    int lastStringLen = toString().length();
    Bunsetsu fBunsetsu = bunsetsuList.get(focusedBunsetsu);
    fBunsetsu.nextCandidate();
    String newString = toString();
    listener.onStringChanged(lastStringLen, newString);
  }

  void prevCandidate(ConverterEventListener listener) {
    int lastStringLen = toString().length();
    Bunsetsu fBunsetsu = bunsetsuList.get(focusedBunsetsu);
    fBunsetsu.prevCandidate();
    String newString = toString();
    listener.onStringChanged(lastStringLen, newString);
  }

  public List<Bunsetsu> getBunsetsuList() {
    return bunsetsuList;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Bunsetsu b : bunsetsuList) {
      sb.append(b.getCurrentString());
    }
    return sb.toString();
  }
}
