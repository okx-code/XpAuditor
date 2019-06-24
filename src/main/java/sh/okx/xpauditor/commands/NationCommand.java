package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.xpauditor.XpAuditor;
import sh.okx.xpauditor.xp.Nation;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

public class NationCommand extends Command {
  public NationCommand(XpAuditor xpAuditor) {
    super(xpAuditor, "!nation");
  }

  @Override
  public void run(TextChannel channel, Member sender, String[] args) {
    if (!hasLeaderRole(sender)) {
      return;
    }

    if (args.length > 0) {
      if (args[0].equalsIgnoreCase("list")) {
        List<String> nations = xpAuditor.getNations().stream().map(Nation::toString).collect(Collectors.toList());
        channel.sendMessage("Nations: " + String.join(", ", nations)).queue();
        return;
      } else if (args[0].equalsIgnoreCase("add")) {
        if (args.length < 2) {
          channel.sendMessage("**Usage:** !nation add <nation>").queue();
        } else if (xpAuditor.addNation(args[1])) {
          channel.sendMessage("Successfully added nation: " + args[1]).queue();
        } else {
          channel.sendMessage("Could not add nation.").queue();
        }
        return;
      } else if (args[0].equalsIgnoreCase("delete")) {
        if (args.length < 2) {
          channel.sendMessage("**Usage:** !nation delete <nation>").queue();
        } else if (sender.getUser().getIdLong() != 115090410849828865L) {
          channel.sendMessage("no.").queue();
        } else if (xpAuditor.deleteNation(args[1])) {
          channel.sendMessage("Successfully deleted nation: " + args[1]).queue();
        } else {
          channel.sendMessage("Could not delete nation.").queue();
        }
        return;
      } else if (args[0].equalsIgnoreCase("merge")) {
        if (args.length < 3) {
          channel.sendMessage("**Usage:** !nation merge <nation> <nation>").queue();
        } else if (args[1].equalsIgnoreCase(args[2])) {
          channel.sendMessage("Cannot merge a nation into itself").queue();
        } else if (xpAuditor.mergeNation(args[1], args[2])) {
          channel.sendMessage("Merged items of " + args[1] + " into " + args[2]).queue();
        } else {
          channel.sendMessage("Could not merge items").queue();
        }
        return;
      }
    }

    channel.sendMessage(new EmbedBuilder()
        .setTitle("Commands")
        .addField("!nation list", "List the nations", false)
        .addField("!nation delete <nation>", "Delete a nation **use with care**", false)
        .addField("!nation add <nation>", "Add a new nation", false)
        .addField("!nation merge <nation> <nation>", "Merge the items of the first nation into the second, and delete the first nation.", false)
        .setColor(Color.BLUE)
        .build()).queue();
  }

  private boolean hasLeaderRole(Member member) {
    for (Role role : member.getRoles()) {
      if ("Leader".equalsIgnoreCase(role.getName())) {
        return true;
      }
    }
    return false;
  }
}
