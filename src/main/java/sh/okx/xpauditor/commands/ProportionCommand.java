package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.xpauditor.XpAuditor;
import sh.okx.xpauditor.xp.Nation;

import java.text.DecimalFormat;
import java.util.Arrays;

public class ProportionCommand extends Command {
  public ProportionCommand(XpAuditor xpAuditor) {
    super(xpAuditor, "!proportion");
  }

  @Override
  public void run(TextChannel channel, Member sender, String[] args) {
    Arrays.stream(Nation.values()).forEach(nation -> check(channel, nation));
  }

  private void check(TextChannel channel, Nation nation) {
    xpAuditor.getContribution(nation).thenAccept(amount -> {
      if (amount == 0) {
        channel.sendMessage(nation + " has not contributed anything!").queue();
      } else if (amount < 0) {
        channel.sendMessage(nation + " has withdrawn " + -amount + " more items than they put in!").queue();
      } else {
        int total = Arrays.stream(Nation.values())
            .mapToInt(otherNation -> xpAuditor.getContribution(otherNation).join()).sum();
        channel.sendMessage(nation + " has contributed " + amount + " items or "
            + getPercentage(amount, total)).queue();
      }
    });
  }

  private DecimalFormat df = new DecimalFormat("#0.##%");
  private String getPercentage(int n, int total) {
    float proportion = ((float) n) / ((float) total);
    return df.format(proportion);
  }


}
