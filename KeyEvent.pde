enum KeyEventType {
  KEY_DOWN, SWIPE_BEGIN, SWIPE_END;
}

interface KeyEventListener {
  void onKeyEvent(KeyEvent e);
}

class KeyEvent {
  AbstractKey _key;
  KeyEventType type;
  KeyEvent(KeyEventType type, AbstractKey _key) {
    this._key = _key;
    this.type = type;
  }
}
