import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

public class ConversionStrip {
  private final List<Item> items;
  private final PApplet context;
  private Dimension dimension;

  private final PFont stripFont;
  private final PFont stripSubFont;

  private float xOffset = 0;

  private static class Item {
    private final String text;
    private final int number;
    private boolean highlighted;
    private final PApplet context;
    private Dimension dimension;

    public Item(String text, int number, boolean highlighted, PApplet context) {
      this.text = String.format("%s: %s", number, text);
      dimension = new Dimension();
      dimension.width = context.textWidth(this.text);
      dimension.height = context.textAscent() + context.textDescent();
      this.number = number;
      this.highlighted = highlighted;
      this.context = context;
    }

    public boolean isHighlighted() {
      return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
      this.highlighted = highlighted;
    }

    public Dimension getDimension() {
      return dimension;
    }

    public void draw(Point point, PFont font) {
      context.textAlign(PGraphics.LEFT);
      context.fill(0);
      if (highlighted) {
        context.rect(point.x, point.y - font.getSize(), dimension.width, dimension.height - 2);
        context.fill(255);
      }
      context.text(text, point.x, point.y);
    }
  }

  private int focusedItem;

  public ConversionStrip(List<String> candidates, PApplet context) {
    this.context = context;
    this.items = new ArrayList<>();
    this.stripFont = context.createFont("SansSerif", 16, true);
    this.stripSubFont = context.createFont("SansSerif", 12, true);
    setCandidates(candidates);
  }

  public void focusNext() {
    items.get(focusedItem).setHighlighted(false);
    focusedItem = Math.min(items.size() - 1, focusedItem + 1);
    items.get(focusedItem).setHighlighted(true);
  }

  public void focusPrev() {
    items.get(focusedItem).setHighlighted(false);
    focusedItem = Math.max(0, focusedItem - 1);
    items.get(focusedItem).setHighlighted(true);
  }

  public void setCandidates(List<String> candidates) {
    items.clear();
    context.textFont(stripFont);
    float totalWidth = 0;
    for (int i = 0; i < candidates.size(); i++) {
      Item ni = new Item(candidates.get(i), i + 1, false, context);
      items.add(ni);
      totalWidth += ni.getDimension().width + 10; // 各要素間の余白px
    }
    this.dimension = new Dimension(totalWidth, context.textAscent() + context.textDescent());
    if (!items.isEmpty())
      items.get(0).setHighlighted(true);
  }

  public void draw(Point point) {
    float curWidth = 5; // padding-left

    Point offsetPt = new Point(point.x, point.y); // make point mutable
    if (point.x + dimension.width >= context.width) {
      offsetPt.x -= (point.x + dimension.width) - context.width;
      offsetPt.x = Math.max(0, offsetPt.x);
    }

    context.textFont(stripFont);
    for (int i = 0; i < items.size(); i++) {
      Point newPt = new Point(offsetPt.x + curWidth, offsetPt.y);
      items.get(i).draw(newPt, stripFont);
      curWidth += items.get(i).getDimension().width + stripFont.getSize() / 2;
    }
    boolean drawSubText = false; // just for fun

    context.noFill();
    context.strokeWeight(1);

    if (drawSubText) {
      context.rect(offsetPt.x, offsetPt.y - (dimension.height / 2) - 7, curWidth, dimension.height / 2 + 25);
      context.textFont(stripSubFont);
      context.fill(0.5f);
      context.text("Powered by Google Transliterate API", point.x + 5, point.y + (dimension.height / 2) + 3);
    } else {
      context.rect(offsetPt.x, offsetPt.y - (dimension.height / 2) - 10, curWidth, dimension.height / 2 + 15);
    }
  }
}
