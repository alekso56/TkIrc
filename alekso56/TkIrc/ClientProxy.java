package alekso56.TkIrc;

//~--- non-JDK imports --------------------------------------------------------

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class ClientProxy extends CommonProxy {
    public static KeyBinding tkbind = new KeyBinding("TkIrc", 50);
    public static KeyBinding tktoggle  = new KeyBinding("TkIrctoggle", 61);

    public String botUser() {
        return "tkclient";
    }

    public void mcMessage(String p, String m) {
        if ((Minecraft.getMinecraft().theWorld != null) && (Minecraft.getMinecraft().theWorld.isRemote)
                && (Minecraft.getMinecraft().thePlayer != null) && (m != null)) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(p + m);
        }
    }

    public void mcMessage(String m) {
        if ((Minecraft.getMinecraft().theWorld != null) && (Minecraft.getMinecraft().theWorld.isRemote)
        		&& (Minecraft.getMinecraft().thePlayer != null) && (m != null)) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(m);
        }
    }
}