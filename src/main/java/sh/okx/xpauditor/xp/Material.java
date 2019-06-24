package sh.okx.xpauditor.xp;

public enum Material {
  GLASS_BOTTLE(128, "glass bottle", "glass bottles", "bottle", "bottles", "o"),
  NETHER_WART(64, "nether wart", "nether warts", "wart", "warts", "n"),
  MELON(32, "melon", "melons", "l"),
  SUGAR_CANE(128, "sugar cane", "sugar canes", "cane", "canes", "s"),
  YELLOW_FLOWER(16, "yellow flower", "yellow flowers", "dandelion", "dandelions", "d"),
  ROTTEN_FLESH(128, "rotten flesh", "rotten fleshes", "fleshes", "fleshes", "f"),
  BROWN_MUSHROOM(64, "brown mushroom", "brown mushrooms", "mushroom", "mushrooms", "m"),
  VINE(32, "vine", "vines", "v"),
  BAKED_POTATO(256, "baked potato", "baked potatoes", "potato", "potatoes", "cooked potato", "cooked potatoes", "p"),
  BEETROOT(128, "beetroot", "beetroots", "beet", "b");
  
  private String name;
  private int amount;
  private String[] names;

  Material(int amount, String... names) {
    this.name = names[0];
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
    return name;
  }
}
