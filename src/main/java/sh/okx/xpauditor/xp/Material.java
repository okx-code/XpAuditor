package sh.okx.xpauditor.xp;

public enum Material {
  GLASS_BOTTLE(128, "glass", "glass bottle", "glass bottles"),
  NETHER_WART(64, "nether wart", "nether warts", "wart", "warts"),
  MELON(32, "melon", "melons"),
  SUGAR_CANE(128, "sugar cane", "sugar canes", "cane", "canes"),
  YELLOW_FLOWER(16, "yellow flower", "yellow flowers", "dandelion", "dandelions"),
  ROTTEN_FLESH(128, "rotten flesh"),
  BROWN_MUSHROOM(64, "brown mushroom", "brown mushrooms"),
  VINE(32, "vine", "vines"),
  BAKED_POTATO(256, "baked potato", "baked potatoes", "potato", "potatoes", "cooked potato", "cooked potatoes"),
  BEETROOT(128, "beetroot", "beetroots");

  private int amount;
  private String[] names;

  Material(int amount, String... names) {
    this.amount = amount;
    this.names = names;
  }

  public int getAmountNeeded() {
    return amount;
  }

  public String[] getNames() {
    return names;
  }

  public static Material fromName(String name) {
    for(Material value : values()) {
      for(String materialName : value.names) {
        if(materialName.equalsIgnoreCase(name)) {
          return value;
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return name().toLowerCase().replace("_", " ");
  }
}
