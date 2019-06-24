package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.xpauditor.XpAuditor;

import java.awt.*;

public class HelpCommand extends Command {
  public HelpCommand(XpAuditor xpAuditor) {
    super(xpAuditor, "!help");
  }

  @Override
  public void run(TextChannel channel, Member sender, String[] args) {
    channel.sendMessage(new EmbedBuilder()
        .setTitle("Commands")
        .addField("!deposit <amount> <material>", "Deposit resources into the stockpile.", false)
        .addField("!withdraw <amount> <material>", "Withdraw resources from the stockpile.", false)
        .addField("!proportion", "See how many resources each nation has stockpiled.", false)
        .addField("!items", "See what items are in the stockpile and how much XP can be made.", false)
        .addField("!batch [repair]", "Withdraw everything needed for one batch of XP.", false)
        .addField("!nation", "Manage nations (requires Leader)", false)
        .setColor(Color.CYAN)
        .build()).queue();
  }
}
