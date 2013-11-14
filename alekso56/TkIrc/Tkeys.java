package alekso56.TkIrc;

import java.util.EnumSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;

public class Tkeys extends KeyBindingRegistry.KeyHandler {
    public Tkeys() {
        super(new KeyBinding[] { ClientProxy.tkbind, ClientProxy.tktoggle }, new boolean[] { false, false });
    }

    public String getLabel() {
        return "Tkeys";
    }

    public void keyDown(EnumSet types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {}

    public void keyUp(EnumSet types, KeyBinding kb, boolean tickEnd) {
        if ((Minecraft.getMinecraft().currentScreen == null) && (kb.keyDescription.equals("TkIrc"))) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiSettings());
        } else if (((Minecraft.getMinecraft().currentScreen instanceof GuiChat))
                   && (kb.keyDescription.equals("TkIrcToggle")) && (Minecraft.getMinecraft().theWorld.isRemote)
                   && (tickEnd)) {
            TkIrc.toggleChat();
        }
    }

    public EnumSet ticks() {
        return EnumSet.of(TickType.CLIENT);
    }
}
