package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.xpauditor.XpAuditor;
import sh.okx.xpauditor.xp.Material;
import sh.okx.xpauditor.xp.Nation;

public class DepositCommand extends Command {
  public DepositCommand(XpAuditor xpAuditor) {
    super(xpAuditor, "!deposit");
  }

  @Override
  public void run(TextChannel channel, Member sender, String[] args) {
    if (args.length < 2) {
      channel.sendMessage("Usage: **" + name + " <amount> [compacted] <material>**").queue();
      return;
    }

    boolean compacted = args[1].equalsIgnoreCase("compacted");
    int amount;
    try {
      amount = Integer.parseInt(args[0]) * (compacted ? 64 : 1);
      if (amount < 1) {
        throw new IllegalArgumentException();
      }
    } catch (IllegalArgumentException | AssertionError ex) {
      channel.sendMessage("Invalid amount.").queue();
      return;
    }

    args = String.join(" ", args).split(" ", compacted ? 3 : 2);

    Material material = Material.fromName(args[args.length - 1]);
    if (material == null) {
      channel.sendMessage("Invalid material.").queue();
      return;
    }

    Nation nation = xpAuditor.getNation(sender);
    if (nation == null) {
      channel.sendMessage("You are not in a nation!").queue();
      return;
    }

    xpAuditor.deposit(amount, material, nation);
    channel.sendMessage("Deposited " + amount + " of "
        + material
        + " for " + nation).queue();
  }
}
