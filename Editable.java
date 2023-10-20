import processing.core.PFont;

public interface Editable {
  Point getCaretPosition();

  PFont getFont();

  Point getCharPositionBefore(int i);
}
