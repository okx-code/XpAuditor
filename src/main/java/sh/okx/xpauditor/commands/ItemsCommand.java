package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.xpauditor.XpAuditor;
import sh.okx.xpauditor.xp.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ItemsCommand extends Command {
  public ItemsCommand(XpAuditor xpAuditor) {
    super(xpAuditor, "!items");
  }

  @Override
  public void run(TextChannel channel, Member sender, String[] args) {
    CompletableFuture.runAsync(() -> {
      StringBuilder message = new StringBuilder("**Current items stockpiled:**\n");

      Map<Material, Integer> amounts = new HashMap<>();
      Arrays.stream(Material.values())
          .forEach(material -> amounts.put(material, xpAuditor.getCount(material).join()));

      Material mostDemanded = null;
      int count = Integer.MAX_VALUE;

      for (Map.Entry<Material, Integer> entry : amounts.entrySet()) {
        Material material = entry.getKey();
        message.append("- ").append(entry.getValue() / 64).append("/").append(material.getAmountNeeded())
            .append(" stacks of ")
            .append(material.name().toLowerCase().replace("_", " "))
            .append("\n");

        int canMake = entry.getValue() / material.getAmountNeeded();
        if (canMake < count) {
          mostDemanded = material;
          count = canMake;
        }
      }

      message.append("The most demanded material is ").append(mostDemanded).append(".\n");
      count /= 64;
      message.append("You can make ").append(count).append(" batch").append(count == 1 ? "" : "es")
          .append(" of XP.");
      channel.sendMessage(message).queue();
    });

  }
}
