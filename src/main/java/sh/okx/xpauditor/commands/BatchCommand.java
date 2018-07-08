package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.xpauditor.XpAuditor;
import sh.okx.xpauditor.xp.Material;
import sh.okx.xpauditor.xp.Nation;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    int total = amounts.values().stream().mapToInt(i -> i).sum();

    amounts.forEach((n, i) -> channel.sendMessage(n + " should get "
        + getPercentage(i, total) + " for this batch (" + i + ").").queue(msg -> msg.pin().queue()));
  }

  private boolean canMakeBatch() {
    Map<Material, Integer> amounts = new HashMap<>();
    Arrays.stream(Material.values())
        .forEach(material -> amounts.put(material, xpAuditor.getCount(material).join()));

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
  private String getPercentage(int n, int total) {
    float proportion = ((float) n) / ((float) total);
    return df.format(proportion);
  }
}
