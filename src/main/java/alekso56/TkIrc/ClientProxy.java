package alekso56.TkIrc;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class ClientProxy extends CommonProxy {

    @Override
	public String botUser() {
        return "tkclient";
    }

    @Override
	public void mcMessage(String p, String m) {
        if ((Minecraft.getMinecraft().theWorld != null) && (Minecraft.getMinecraft().theWorld.isRemote)
                && (Minecraft.getMinecraft().thePlayer != null) && (m != null)) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(p + m));
        }
    }

    @Override
	public void mcMessage(String m) {
        if ((Minecraft.getMinecraft().theWorld != null) && (Minecraft.getMinecraft().theWorld.isRemote)
        		&& (Minecraft.getMinecraft().thePlayer != null) && (m != null)) {
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(m));
        }
    }
}