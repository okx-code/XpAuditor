package sh.okx.xpauditor.xp;

import sh.okx.xpauditor.XpAuditor;

public class NationCount implements Comparable<NationCount> {
  private XpAuditor auditor;
  private Nation nation;
  private Material material;
  private int amount;

  public NationCount(XpAuditor auditor, Nation nation, Material material) {
    this.auditor = auditor;
    this.nation = nation;
    this.material = material;
    amount = auditor.getCount(nation, material).join();
  }

  public int getAmount() {
    return amount;
  }

  public Material getMaterial() {
    return material;
  }

  public Nation getNation() {
    return nation;
  }

  @Override
  public int compareTo(NationCount nationCount) {
    return Integer.compare(amount, nationCount.amount);
  }
}
