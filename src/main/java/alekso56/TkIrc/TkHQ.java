package alekso56.TkIrc;

import java.io.IOException;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class TkHQ extends CommandBase
{

	@Override
	public int compareTo(Object arg0) {
		return this.getCommandName().compareTo(((ICommand) arg0).getCommandName());
	}

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
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
		return true;
	}

	@Override
	public List getCommandAliases() {
		return null;
	}
	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		if (astring.length < 1) { icommandsender.addChatMessage(new ChatComponentText("Commands: reconnect")); return;}
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
		}
		else if ((astring[0].equalsIgnoreCase("help")) && (astring.length > 0)) {
			icommandsender.addChatMessage(new ChatComponentText("Commands: reconnect")); }
	}
}