import java.util.List;

/**
 * 文節
 */
class Bunsetsu {
  private int currentIndex = 0;
  private final String original;
  private final List<String> candidates;
  private String currentString;

  public Bunsetsu(String original, List<String> candidates) {
    this.original = original;
    this.candidates = candidates;
    if (original == null)
      throw new IllegalArgumentException("Original should not be null");
    if (candidates == null || candidates.isEmpty())
      throw new IllegalArgumentException("Candidates should not be null or empty");
    currentString = candidates.get(0);
  }

  public void reset() {
    currentString = original;
  }

  public void nextCandidate() {
    currentIndex = Math.min(candidates.size() - 1, currentIndex + 1);
    currentString = candidates.get(currentIndex);
  }

  public void prevCandidate() {
    currentIndex = Math.max(0, currentIndex - 1);
    currentString = candidates.get(currentIndex);
  }

  public String getCurrentString() {
    return currentString;
  }

  public int getCurrentIndex() {
    return currentIndex;
  }

  public List<String> getAllCandidates() {
    return candidates;
  }
}
