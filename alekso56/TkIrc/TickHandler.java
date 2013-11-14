package alekso56.TkIrc;

//~--- non-JDK imports --------------------------------------------------------

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
//~--- JDK imports ------------------------------------------------------------

public class TickHandler implements ITickHandler {
    public void tickStart(EnumSet type, Object[] tickData) {}

    public void tickEnd(EnumSet type, Object[] tickData) {
        if (type.equals(EnumSet.of(TickType.RENDER))) {
            onRenderTick();
        } else if (type.equals(EnumSet.of(TickType.CLIENT))) {
            GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;

            if (guiscreen != null) {
                onTickInGUI(guiscreen);
            } else {
                onTickInGame();
            }
        }
    }

    public EnumSet ticks() {
        return EnumSet.of(TickType.RENDER, TickType.CLIENT);
    }

    public String getLabel() {
        return null;
    }

    public void onRenderTick() {
        Minecraft        mc   = Minecraft.getMinecraft();
        ScaledResolution var5 = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        Integer          mcH  = Integer.valueOf(var5.getScaledHeight());
        Integer          sX   = Integer.valueOf(4);
        Integer          sY   = Integer.valueOf(mcH.intValue() - mc.fontRenderer.FONT_HEIGHT - 17);

        if ((mc.currentScreen instanceof GuiChat)) {
            String chattingTo = "";

            switch (TkIrc.chatTo) {
            case -1 :
                chattingTo = "Minecraft";

                break;

            case 0 :
                chattingTo = "Minecraft, " + Config.cName;

                break;

            case 1 :
                chattingTo = Config.cName;
            }

            drawGradientRect(sX.intValue() - 2, sY.intValue() - 2,
                             sX.intValue() + mc.fontRenderer.getStringWidth("Chatting To: " + chattingTo) + 2,
                             sY.intValue() + mc.fontRenderer.FONT_HEIGHT + 1, -1072689136, -804253680);
            mc.fontRenderer.drawString("Chatting To: " + chattingTo, sX.intValue(), sY.intValue(), 16777215);
        }
    }

    public void onTickInGUI(GuiScreen guiscreen) {
        if (((guiscreen instanceof GuiChat)) && (TkIrc.chatTo == 1) && (!(guiscreen instanceof GuiIRCChat))) {
            Minecraft.getMinecraft().displayGuiScreen((GuiScreen) null);
            Minecraft.getMinecraft().displayGuiScreen(new GuiIRCChat());
        }
    }

    public void onTickInGame() {}

    protected void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6) {
        float var7  = (par5 >> 24 & 0xFF) / 255.0F;
        float var8  = (par5 >> 16 & 0xFF) / 255.0F;
        float var9  = (par5 >> 8 & 0xFF) / 255.0F;
        float var10 = (par5 & 0xFF) / 255.0F;
        float var11 = (par6 >> 24 & 0xFF) / 255.0F;
        float var12 = (par6 >> 16 & 0xFF) / 255.0F;
        float var13 = (par6 >> 8 & 0xFF) / 255.0F;
        float var14 = (par6 & 0xFF) / 255.0F;

        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glDisable(3008);
        GL11.glBlendFunc(770, 771);
        GL11.glShadeModel(7425);

        Tessellator var15 = Tessellator.instance;

        var15.startDrawingQuads();
        var15.setColorRGBA_F(var8, var9, var10, var7);
        var15.addVertex(par3, par2, 0.0D);
        var15.addVertex(par1, par2, 0.0D);
        var15.setColorRGBA_F(var12, var13, var14, var11);
        var15.addVertex(par1, par4, 0.0D);
        var15.addVertex(par3, par4, 0.0D);
        var15.draw();
        GL11.glShadeModel(7424);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glEnable(3553);
    }
}