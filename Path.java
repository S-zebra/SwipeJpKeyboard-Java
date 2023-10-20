public class Path implements Cloneable {
  Point bp, ep;

  Path(Point bp, Point ep) {
    this.bp = bp;
    this.ep = ep;
  }
}