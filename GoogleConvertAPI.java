import processing.data.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <a href="https://www.google.co.jp/ime/cgiapi.html">Google CGI API for Japanese Input</a>
 */
public final class GoogleConvertAPI implements ConverterAPI {
  /**
   * 変換APIのURL (Google transliterate API)
   */
  private static final String API_URL = "http://www.google.com/transliterate?langpair=ja-Hira%7Cja&text=";

  @Override
  public List<Bunsetsu> convert(String from) {
    URL url = null;
    try {
      url = new URL(API_URL + URLEncoder.encode(from, "UTF-8"));
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

    /*
     * 例:
     *  [
     *   "も", ["も", "模", "藻", "喪", "裳"]],
     *   "だむ", ["ダム", "打無", "DAM", "dam", "Dam"]]
     *  ]
     */
    JSONArray convertResult = JSONArray.parse(sb.toString());
    System.out.println(sb.toString());
    if (convertResult == null) {
      // doesConvert = false;
      return null;
    }

    List<Bunsetsu> bunsetsuList = new ArrayList<>();
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
    return bunsetsuList;
  }
}
