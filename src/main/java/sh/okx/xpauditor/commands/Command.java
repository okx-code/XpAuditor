package sh.okx.xpauditor.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import sh.okx.xpauditor.XpAuditor;

public abstract class Command {
  protected XpAuditor xpAuditor;
  protected String name;

  public Command(XpAuditor xpAuditor, String name) {
    this.xpAuditor = xpAuditor;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public abstract void run(TextChannel channel, Member sender, String[] args);
}
