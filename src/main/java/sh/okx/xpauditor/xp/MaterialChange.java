package sh.okx.xpauditor.xp;

public class MaterialChange {
  private Material material;
  private int amount;

  private MaterialChange(Material material, int amount) {
    this.material = material;
    this.amount = amount;
  }

  public Material getMaterial() {
    return material;
  }

  public int getAmount() {
    return amount;
  }

  public static MaterialChange fromArgs(String[] args) throws IllegalArgumentException {
    int amount;
    try {
      amount = Integer.parseInt(args[0]) * 64;
      if (amount < 1) {
        throw new IllegalArgumentException("Amount must be greater than 0.");
      }
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid number.");
    }

    args = String.join(" ", args).split(" ", 2);

    Material material = Material.fromName(args[1]);
    if (material == null) {
      throw new IllegalArgumentException("Invalid material.");
    }

    return new MaterialChange(material, amount);
  }
}
