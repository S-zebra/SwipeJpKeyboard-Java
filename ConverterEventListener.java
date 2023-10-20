interface ConverterEventListener {
  //void converterEvent(ConverterEvent e);
  void onCharEntered(char chr);

  void onStringChanged(int prevLen, String newString);

  void onConvertionCanceled(int len);
}
