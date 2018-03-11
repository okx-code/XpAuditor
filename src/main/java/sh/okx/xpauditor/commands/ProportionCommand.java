package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import sh.okx.xpauditor.XpAuditor;
import sh.okx.xpauditor.xp.Nation;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;

public class ProportionCommand extends Command {
  public ProportionCommand(XpAuditor xpAuditor) {
    super(xpAuditor, "!proportion");
  }

  @Override
  public void run(TextChannel channel, Member sender, String[] args) {
    if(args.length != 1) {
      channel.sendMessage("**Usage:** !proportion <users/nations/all>").queue();
    }

    boolean all = args[0].equalsIgnoreCase("all");
    boolean users = all || args[0].equalsIgnoreCase("users");
    boolean nations = all || args[0].equalsIgnoreCase("nations");

    if(nations) {
      Arrays.stream(Nation.values()).forEach(nation -> check(channel, nation));
    }
    if(users) {
      sendUserContributions(channel);
    }
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

  private void sendUserContributions(TextChannel channel) {
    xpAuditor.getUserContributions().thenAccept(map -> {
      int total = xpAuditor.getTotalUserContributions().join();
      for(Map.Entry<User, Integer> entry : map.entrySet()) {
        String user = entry.getKey().getName() + "#" + entry.getKey().getDiscriminator();
        int amount = entry.getValue();

        if (amount < 0) {
          channel.sendMessage(user + " has withdrawn " + -amount + " more items than they put in!").queue();
        } else if(amount > 0) {
          channel.sendMessage(user + " has contributed " + amount + " items or "
              + getPercentage(amount, total)).queue();
        }
      }
    });
  }

  private DecimalFormat df = new DecimalFormat("#0.##%");

  private String getPercentage(int n, int total) {
    float proportion = ((float) n) / ((float) total);
    return df.format(proportion);
  }


}
