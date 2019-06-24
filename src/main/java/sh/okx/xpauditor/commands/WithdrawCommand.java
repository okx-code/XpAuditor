package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.xpauditor.XpAuditor;
import sh.okx.xpauditor.xp.Material;
import sh.okx.xpauditor.xp.MaterialChange;
import sh.okx.xpauditor.xp.Nation;

public class WithdrawCommand extends Command {
  public WithdrawCommand(XpAuditor xpAuditor) {
    super(xpAuditor, "!withdraw");
  }

  @Override
  public void run(TextChannel channel, Member sender, String[] args) {
    if (args.length < 2) {
      channel.sendMessage("Usage: **" + name + " <amount> <material>**").queue();
      return;
    }

    Nation nation = xpAuditor.getNation(sender);
    if (nation == null) {
      channel.sendMessage("You are not in a nation!").queue();
      return;
    }

    try {
      MaterialChange change = MaterialChange.fromArgs(args);
      if (xpAuditor.withdraw(change.getAmount(), change.getMaterial(), nation)) {
        channel.sendMessage("Withdrew " + change.getAmount()/64 + " compacted "
            + change.getMaterial() + " for " + nation).queue();
      } else {
        channel.sendMessage("You do not have enough resources to withdraw that much.").queue();
      }
    } catch(IllegalArgumentException ex) {
      channel.sendMessage(ex.getMessage()).queue();
    }
  }
}
