package alekso56.TkIrc;

import java.io.IOException;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;

public class TkHQ extends CommandBase
{
  @Override
public String getCommandName()
  {
    return "tki";
  }
  @Override
public String getCommandUsage(ICommandSender par1iCommandSender)
  {
    return "/" + getCommandName() + " help";
  }
  @Override
public boolean canCommandSenderUseCommand(ICommandSender par1iCommandSender)
  {
    return super.canCommandSenderUseCommand(par1iCommandSender);
  }
@Override
public void processCommand(ICommandSender icommandsender, String[] astring) {
		    if (astring.length < 1) { icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("Commands: reconnect,kick,ban,unban")); return;}
		    if (astring[0].equalsIgnoreCase("reconnect")) {
		      try {
		        TkIrc.toIrc.close();
		      }
		      catch (IOException e) {
		        e.printStackTrace();
		      }
		      TkIrc.toIrc = new IRCBot();
		      TkIrc.toIrc.setUser(TkIrc.proxy.botUser());
		      TkIrc.toIrc.setInfo("TkIRC by alekso56");
		      try {
		        TkIrc.toIrc.connect(Config.sHost, Integer.valueOf(Config.sPort), Config.botName, Config.sKey);
		      } catch (IOException e) {
		        e.printStackTrace();
		      }
		    } else if ((astring[0].equalsIgnoreCase("kick")) && (astring.length > 0)) {
		      TkIrc.toIrc.sendRaw("KICK " + Config.cName + " " + astring[1]);
		    } else if ((astring[0].equalsIgnoreCase("ban")) && (astring.length > 0)) {
		      TkIrc.toIrc.sendRaw("MODE " + Config.cName + " +b " + astring[1] + "!*@*");
		    } else if ((astring[0].equalsIgnoreCase("unban")) && (astring.length > 0)) {
		      TkIrc.toIrc.sendRaw("MODE " + Config.cName + " -b " + astring[1] + "!*@*");
		    }
              else if ((astring[0].equalsIgnoreCase("help")) && (astring.length > 0)) {
            	  icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("Commands: reconnect,kick,ban,unban"));
  }
}
}