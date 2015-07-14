package alekso56.TkIrc;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class TkMcCommands extends CommandBase
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
		return super.canCommandSenderUseCommand(par1ICommandSender);
	}

	@Override
	public List getCommandAliases() {
		return null;
	}
	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		if (astring.length < 1) { icommandsender.addChatMessage(new ChatComponentText("Commands: reconnect")); return;}
		if (astring[0].equalsIgnoreCase("reconnect")) {
			TkIrc.reconnectBot(false);
		}
		else if ((astring[0].equalsIgnoreCase("help")) && (astring.length > 0)) {
			icommandsender.addChatMessage(new ChatComponentText("Commands: reconnect")); }
	}
}