package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.xpauditor.XpAuditor;
import sh.okx.xpauditor.xp.Nation;

import java.text.DecimalFormat;
import java.util.Map;

public class ProportionCommand extends Command {
  public ProportionCommand(XpAuditor xpAuditor) {
    super(xpAuditor, "!proportion");
  }

  @Override
  public void run(TextChannel channel, Member sender, String[] args) {
    Map<Nation, Integer> contributions = xpAuditor.getContributions();
    int sum = contributions.values().stream().reduce(0, Integer::sum);
    StringBuilder message = new StringBuilder();
    for (Map.Entry<Nation, Integer> entry : contributions.entrySet()) {
      int amount = entry.getValue();
      Nation nation = entry.getKey();
      if (amount > 0) {
        message.append(nation).append(" has contributed ").append(amount/64)
            .append(" compacted item")
            .append(amount == 1 ? "" : "s")
            .append(" or ").append(getPercentage(amount, sum))
            .append("\n");
      }
    }
    channel.sendMessage(message).queue();
  }

  private DecimalFormat df = new DecimalFormat("#0.##%");

  private String getPercentage(int n, int total) {
    float proportion = ((float) n) / ((float) total);
    return df.format(proportion);
  }

}
