package alekso56.TkIrc;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;


public class GuiSettings extends GuiScreen {
    protected Integer    wX = Integer.valueOf(230);
    protected Integer    wY = Integer.valueOf(100);
    protected Integer    sI;
    protected Integer    sX;
    protected Integer    sY;
    private GuiTextField cName;
    private GuiTextField botName;
    private GuiTextField aText;

    public void initGui() {
        this.sX    = Integer.valueOf(this.width / 2 - this.wX.intValue() / 2);
        this.sY    = Integer.valueOf(this.height / 2 - this.wY.intValue() / 2);
        this.sI    = Integer.valueOf(0);
        this.cName = new GuiTextField(this.fontRenderer, this.sX.intValue() + this.wX.intValue() - 100,
                                      this.sY.intValue() + (4 + this.fontRenderer.FONT_HEIGHT) * 1, 90,
                                      this.fontRenderer.FONT_HEIGHT + 5);
        this.botName = new GuiTextField(this.fontRenderer, this.sX.intValue() + this.wX.intValue() - 100,
                                        this.sY.intValue() + (4 + this.fontRenderer.FONT_HEIGHT) * 2, 90,
                                        this.fontRenderer.FONT_HEIGHT + 5);
        this.cName.setMaxStringLength(100);
        this.cName.setEnableBackgroundDrawing(false);
        this.cName.setFocused(true);
        this.aText = this.cName;
        this.cName.setText(Config.cName);
        this.botName.setMaxStringLength(100);
        this.botName.setEnableBackgroundDrawing(false);
        this.botName.setText(Config.botName);
    }

    public void drawScreen(int par1, int par2, float par3) {
        this.sI = Integer.valueOf(0);
        drawGradientRect(this.sX.intValue(), this.sY.intValue(), this.sX.intValue() + this.wX.intValue(),
                         this.sY.intValue() + this.wY.intValue(), -1072689136, -804253680);
        drawString(this.fontRenderer, "ForgeIRC", this.width / 2 - this.fontRenderer.getStringWidth("ForgeIRC") / 2,
                   this.height / 2 - this.wY.intValue() / 2 + 12 - this.fontRenderer.FONT_HEIGHT, 16777215);

        Integer localInteger1 = this.sI;
        Integer localInteger2 = this.sI = Integer.valueOf(this.sI.intValue() + 1);

        drawString(this.fontRenderer, "Default Channel", this.sX.intValue() + 6,
                   this.sY.intValue() + 2 + (2 + this.fontRenderer.FONT_HEIGHT) * this.sI.intValue(), 16777215);
        localInteger1 = this.sI;
        localInteger2 = this.sI = Integer.valueOf(this.sI.intValue() + 1);
        drawString(this.fontRenderer, "Bot Nickname", this.sX.intValue() + 6,
                   this.sY.intValue() + 2 + (2 + this.fontRenderer.FONT_HEIGHT) * this.sI.intValue(), 16777215);
        this.cName.drawTextBox();
        this.botName.drawTextBox();

        Integer bColor = Integer.valueOf((par1 > this.sX.intValue() + this.wX.intValue() - 40)
                                         && (par1 < this.sX.intValue() + this.wX.intValue() - 5)
                                         && (par2 > this.sY.intValue() + this.wY.intValue() - 18)
                                         && (par2 < this.sY.intValue() + this.wY.intValue() - 4)
                                         ? -16776961
                                         : -16777216);

        drawRect(this.sX.intValue() + this.wX.intValue() - 40, this.sY.intValue() + this.wY.intValue() - 18,
                 this.sX.intValue() + this.wX.intValue() - 5, this.sY.intValue() + this.wY.intValue() - 4,
                 bColor.intValue());
        drawString(this.fontRenderer, "Save", this.sX.intValue() + this.wX.intValue() - 34,
                   this.sY.intValue() + this.wY.intValue() - 15, 16777215);
        super.drawScreen(par1, par2, par3);
    }

    protected void mouseClicked(int par1, int par2, int par3) {
        if ((par1 > this.sX.intValue() + this.wX.intValue() - 40)
                && (par1 < this.sX.intValue() + this.wX.intValue() - 5)
                && (par2 > this.sY.intValue() + this.wY.intValue() - 18)
                && (par2 < this.sY.intValue() + this.wY.intValue() - 4)) {
            this.mc.displayGuiScreen((GuiScreen) null);

            if ((TkIrc.toIrc.getChannels().size() > 0)
                    && (!this.cName.getText().trim().toLowerCase().equals(
                        ((String) TkIrc.toIrc.getChannels().get(0)).toString().trim().toLowerCase()))) {
                TkIrc.toIrc.partChannel(((String) TkIrc.toIrc.getChannels().get(0)).toString());
            }

            Config.cName = TkIrc.config.get("Channel", "Name", "#ForgeIRC").getString();
            TkIrc.config.get("Channel", "Name", "#ForgeIRC").set(this.cName.getText());
            TkIrc.toIrc.joinChannel(this.cName.getText());

            if (!this.botName.getText().equals(TkIrc.toIrc.getNick())) {
                Config.botName = TkIrc.config.get("general", "Nickname", this.botName.getText()).getString();
                TkIrc.config.get("general", "Nickname", this.botName.getText()).set(this.botName.getText());
                TkIrc.toIrc.setNick(this.botName.getText());
            }

            TkIrc.config.save();
        } else if ((par1 > this.sX.intValue() + this.wX.intValue() - 100)
                   && (par1 < this.sX.intValue() + this.wX.intValue() - 10)
                   && (par2 > this.sY.intValue() + (4 + this.fontRenderer.FONT_HEIGHT) * 2)
                   && (par2 < this.sY.intValue() + (4 + this.fontRenderer.FONT_HEIGHT) * 2 + 90)) {
            this.botName.setFocused(true);
            this.aText = this.botName;
        }

        super.mouseClicked(par1, par2, par3);
    }

    protected void renderSetting(String s, Integer v) {}

    protected void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        this.aText.textboxKeyTyped(c, i);
    }

    public void updateScreen() {
        super.updateScreen();
        this.aText.updateCursorCounter();
    }
}
