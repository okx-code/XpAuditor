package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.xpauditor.XpAuditor;
import sh.okx.xpauditor.xp.Material;
import sh.okx.xpauditor.xp.Nation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchCommand extends Command {
  private DecimalFormat df = new DecimalFormat("#0.##%");

  public BatchCommand(XpAuditor xpAuditor) {
    super(xpAuditor, "!batch");
  }

  @Override
  public void run(TextChannel channel, Member sender, String[] args) {
    if(!canMakeBatch()) {
      channel.sendMessage("Not enough resources to make a batch.").queue();
      return;
    }

    Map<Nation, Integer> amounts = new HashMap<>();
    for(Material material : Material.values()) {
      Map<Nation, Integer> withdraw = xpAuditor.withdrawBatch(material);

      withdraw.forEach((n, i) -> amounts.put(n, amounts.getOrDefault(n, 0) + i));
    }

    int totalBottles = 128*9*9;
    if(args.length > 0 && args[0].equalsIgnoreCase("repair")) {
      totalBottles -= 16*9*9;
    }

    double total = amounts.values().stream().reduce(0, Integer::sum);

    long bottlesLeft = totalBottles;
    List<Map.Entry<Nation, Integer>> sorted = amounts.entrySet().stream()
        .sorted(Comparator.comparingInt(Map.Entry::getValue))
        .collect(Collectors.toList());

    StringBuilder builder = new StringBuilder();
    for (int i = 1; i < sorted.size(); i++) {
      Map.Entry<Nation, Integer> entry = sorted.get(i);

      long bottles = Math.round(totalBottles * (entry.getValue() / total));
      builder.append(give(entry.getKey(), bottles, totalBottles)).append("\n");
      bottlesLeft -= bottles;
    }

    Map.Entry<Nation, Integer> entry = sorted.get(0);
    builder.append(give(entry.getKey(), bottlesLeft, totalBottles)).append("\n")
        .append("(total of ").append(totalBottles / (9 * 9)).append(" blocks)");

    channel.sendMessage(builder).queue();
  }

  private String give(Nation nation, long bottles, long totalBottles) {
    return nation + " should get " + formatBottles(bottles) + " for this batch " +
        "(" + getPercentage(bottles, totalBottles) + ")";
  }

  private String formatBottles(long bottles) {
    List<String> items = new ArrayList<>();
    if(bottles > 9*9) {
      long blocks = bottles / (9*9);
      bottles = bottles % (9*9);
      items.add(blocks + " block" + (blocks == 1 ? "" : "s"));
    }
    if(bottles > 9) {
      long emeralds = bottles / 9;
      bottles = bottles % 9;
      items.add(emeralds + " emerald" + (emeralds == 1 ? "" : "s"));
    }
    if(bottles > 0) {
      items.add(bottles + " bottle" + (bottles == 1 ? "" : "s"));
    }
    if(items.size() == 0) {
      items.add("nothing");
    }
    return String.join(", ", items);
  }

  private boolean canMakeBatch() {
    Map<Material, Integer> amounts = xpAuditor.getAmounts();

    int count = Integer.MAX_VALUE;

    for (Map.Entry<Material, Integer> entry : amounts.entrySet()) {
      Material material = entry.getKey();

      int canMake = entry.getValue() / material.getAmountNeeded();
      if (canMake < count) {
        count = canMake;
      }
    }

    count /= 64;
    return count > 0;
  }

  private String getPercentage(double n, double total) {
    double proportion = n / total;
    return df.format(proportion);
  }
}
