package bmir.radx.metadata.evaluator.sharedComponents;

public class URLCount {
  private int totalURL;
  private int resolvableURL;
  private int unresolvableURL;

  public URLCount(int totalURL, int resolvableURL, int unresolvableURL) {
    this.totalURL = totalURL;
    this.resolvableURL = resolvableURL;
    this.unresolvableURL = unresolvableURL;
  }

  public void incrementTotalURL() {
    totalURL++;
  }

  public void incrementResolvableURL() {
    resolvableURL++;
  }

  public void incrementUnresolvableURL() {
    unresolvableURL++;
  }

  public int getTotalURL() {
    return totalURL;
  }

  public int getResolvableURL() {
    return resolvableURL;
  }

  public int getUnresolvableURL() {
    return unresolvableURL;
  }
}
