package alekso56.TkIrc;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.relauncher.Side;

public class PlayerLoginout {
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent player) {
        if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
            TkIrc.toIrc.joinChannel(Config.cName, Config.cKey);
        }
        if (Config.eJoinMC) {
            TkIrc.toIrc.sendMessage(Config.cName, "* "+ TkEvents.dePing(player.player.getDisplayName()) +" has joined the game");
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent player) {
        if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
            TkIrc.toIrc.joinChannel("0");
        }

        if (Config.eJoinMC) {
            TkIrc.toIrc.sendMessage(Config.cName, "* " + TkEvents.dePing(player.player.getDisplayName()) + " has left the game");
        }
    }
}
