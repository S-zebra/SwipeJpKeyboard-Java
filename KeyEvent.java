public class KeyEvent {
  AbstractKey _key;
  KeyEventType type;
  KeyEvent(KeyEventType type, AbstractKey _key) {
    this._key = _key;
    this.type = type;
  }
}
