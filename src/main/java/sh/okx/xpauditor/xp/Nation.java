package sh.okx.xpauditor.xp;

public class Nation {
  /*VINLAND("Vinland"),
  OKASHIMA("Okashima"),
//  THREEPTON("Threepton"),
  MALTOVIA("Maltovia"),
  ODRESH("Odresh"),
  FALVYU("Falvyu");

  private String string;

  Nation(String string) {
    this.string = string;
  }

  @Override
  public String toString() {
    return string;
  }*/

  private final String name;

  public Nation(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
