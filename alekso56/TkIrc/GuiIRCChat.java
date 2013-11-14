package alekso56.TkIrc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;

public class GuiIRCChat extends GuiChat {
    protected void keyTyped(char par1, int key) {
        if (key == 1) {
            this.mc.displayGuiScreen((GuiScreen) null);
        } else if (key == 28) {
            String message = this.inputField.getText().trim();

            if (message.length() > 0) {
                Minecraft.getMinecraft().thePlayer.addChatMessage("[i]<" + Minecraft.getMinecraft().thePlayer.username
                        + "> " + message);
                TkIrc.toIrc.sendMessage(Config.cName, message);
            }

            this.mc.displayGuiScreen((GuiScreen) null);
        } else {
            this.inputField.textboxKeyTyped(par1, key);
        }
    }

    public void updateScreen() {
        this.inputField.updateCursorCounter();
    }
}