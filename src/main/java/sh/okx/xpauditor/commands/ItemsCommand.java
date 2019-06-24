package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.xpauditor.XpAuditor;
import sh.okx.xpauditor.xp.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemsCommand extends Command {
  public ItemsCommand(XpAuditor xpAuditor) {
    super(xpAuditor, "!items");
  }

  @Override
  public void run(TextChannel channel, Member sender, String[] args) {
    StringBuilder message = new StringBuilder();
    Map<Material, Integer> amounts = xpAuditor.getAmounts();
    Material mostDemanded = null;
    int count = Integer.MAX_VALUE;

    List<String> complete = new ArrayList<>();
    List<String> incomplete = new ArrayList<>();

    for (Map.Entry<Material, Integer> entry : amounts.entrySet()) {
      Material material = entry.getKey();
      message.append("• ");
      boolean needed = (entry.getValue() / 64) >= material.getAmountNeeded();
      if (needed) {
        message.append("~~");
      }
      message.append(entry.getValue() / 64).append("/").append(material.getAmountNeeded())
          .append(" compacted ")
          .append(material.name().toLowerCase().replace("_", " "));
      if (needed) {
        message.append("~~");
        incomplete.add(message.toString());
      } else {
        complete.add(message.toString());
      }
      message.setLength(0);

      int canMake = entry.getValue() / material.getAmountNeeded();
      if (canMake < count) {
        mostDemanded = material;
        count = canMake;
      }
    }

    message.append("**Current items stockpiled:**\n");
    for (String co : complete) {
      message.append(co).append("\n");
    }
    for (String inco : incomplete) {
      message.append(inco).append("\n");
    }

    message.append("The most demanded material is ").append(mostDemanded).append(".\n");
    count /= 64;
    if (count > 0) {
      message.append("You can make ").append(count).append(" batch").append(count == 1 ? "" : "es")
          .append(" of XP.");
    }
    channel.sendMessage(message).queue();
  }
}
