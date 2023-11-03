import java.util.List;

public interface ConverterAPI {
  /**
   * ひらがなから構成される文字列を、文節に区切って漢字に変換します。
   *
   * @param from 変換前文字列。ひらがな、カタカナ、句読点等が含まれます。
   * @return 文節(< code > Bunsetsu < / code >)のリスト。
   */
  List<Bunsetsu> convert(String from);
}
