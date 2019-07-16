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

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Nation && obj.toString().equalsIgnoreCase(toString());
  }

  @Override
  public int hashCode() {
    return name.toLowerCase().hashCode();
  }
}
