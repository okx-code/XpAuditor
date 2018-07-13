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
    boolean compacted = args[1].equalsIgnoreCase("compacted");
    int amount;
    try {
      amount = Integer.parseInt(args[0]) * (compacted ? 64 : 1);
      if (amount < 1) {
        throw new IllegalArgumentException("Amount must be greater than 0.");
      }
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid number.");
    }

    args = String.join(" ", args).split(" ", compacted ? 3 : 2);

    Material material = Material.fromName(args[args.length - 1]);
    if (material == null) {
      throw new IllegalArgumentException("Invalid material.");
    }

    return new MaterialChange(material, amount);
  }
}
