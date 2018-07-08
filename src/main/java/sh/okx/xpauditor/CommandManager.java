package sh.okx.xpauditor;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sh.okx.xpauditor.commands.BatchCommand;
import sh.okx.xpauditor.commands.Command;
import sh.okx.xpauditor.commands.DepositCommand;
import sh.okx.xpauditor.commands.HelpCommand;
import sh.okx.xpauditor.commands.ItemsCommand;
import sh.okx.xpauditor.commands.ProportionCommand;
import sh.okx.xpauditor.commands.WithdrawCommand;

import java.util.HashSet;
import java.util.Set;

public class CommandManager extends ListenerAdapter {
  private Set<Command> commands = new HashSet<>();

  public CommandManager(XpAuditor xpAuditor) {
    commands.add(new DepositCommand(xpAuditor));
    commands.add(new WithdrawCommand(xpAuditor));
    commands.add(new ProportionCommand(xpAuditor));
    commands.add(new ItemsCommand(xpAuditor));
    commands.add(new HelpCommand(xpAuditor));
    commands.add(new BatchCommand(xpAuditor));
  }

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    if (event.isWebhookMessage() || event.getAuthor().isBot()) {
      return;
    }

    String[] parts = event.getMessage().getContentRaw().split(" ", 2);
    String cmd = parts[0];
    String[] args = parts.length > 1 ? parts[1].split(" ") : new String[0];

    for (Command command : commands) {
      if (command.getName().equalsIgnoreCase(cmd)) {
        command.run(event.getChannel(), event.getMember(), args);
        break;
      }
    }
  }
}
