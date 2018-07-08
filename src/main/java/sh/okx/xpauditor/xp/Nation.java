package sh.okx.xpauditor.xp;

public enum Nation {
  VINLAND("Vinland"),
  OKASHIMA("Okashima"),
  THREEPTON("Threepton");

  private String string;

  Nation(String string) {
    this.string = string;
  }

  @Override
  public String toString() {
    return string;
  }
}
