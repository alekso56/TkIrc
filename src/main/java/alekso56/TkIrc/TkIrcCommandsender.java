package alekso56.TkIrc;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public class TkIrcCommandsender implements ICommandSender,EntityPlayer {
    private StringBuffer buffer = new StringBuffer();
	@Override
	public String getCommandSenderName() {return "TKI";}
	@Override
	public IChatComponent func_145748_c_() {return new ChatComponentText(this.getCommandSenderName());}
	@Override
	public void addChatMessage(IChatComponent p_145747_1_) {this.buffer.append(p_145747_1_.getUnformattedText());}
	@Override
	public boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_) {return true;}
	@Override
	public ChunkCoordinates getPlayerCoordinates() {return new ChunkCoordinates(0, 0, 0);}
	@Override
	public World getEntityWorld() {return MinecraftServer.getServer().getEntityWorld();}
    public void resetLog(){this.buffer.setLength(0);}
    public String getLogContents(){return this.buffer.toString();}
}
