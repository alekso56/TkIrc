package alekso56.TkIrc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerStatusServer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;

public class TkEvents {
	
    public LivingDeathEvent onPlayerDead(LivingDeathEvent event) {
        if (!Config.eDeath) {
            return event;
        }

        String s = (event.source.getSourceOfDamage() != null)
                   ? event.source.getSourceOfDamage().getCommandSenderName()
                   : "generic";

        if ((event.entityLiving instanceof EntityPlayer)) {
            String dmsg = StatCollector.translateToLocalFormatted("death.attack." + event.source.damageType,
                              new Object[] { event.entityLiving.getCommandSenderName(),
                                             s });
            
            String cdmsg = (String) Config.mDeathMessages.get("death." + event.source.damageType);

           // System.out.println(event.source.damageType);

            if (cdmsg != null) {
                dmsg = cdmsg;
                dmsg = dmsg.replaceAll("%PLAYER%", dePing(event.entityLiving.getCommandSenderName()));
                dmsg = dmsg.replaceAll("%SOURCE%", s);
            }

            TkIrc.toIrc.sendMessage(Config.cName, "* " + dmsg);
        }
		return event;
    }

    public ServerChatEvent onSM(ServerChatEvent message) {
        if(message.isCanceled()){return message;}
            String sPrefix  = Config.pIRCMSG.replaceAll("%n", dePing(IRCBot.colorNick(message.username))) + " ";
            TkIrc.toIrc.sendMessage(Config.cName, sPrefix + message.message);

      String[] aMessage = message.message.split(" ", 2);

      if (aMessage[0].matches("^<" + message.username + ">$"))
      {
          TkIrc.toIrc.sendMessage(Config.cName, aMessage[1]);
      }
      else if ((aMessage[0].matches("^\\*$")) && (aMessage[1].split(" ", 2)[0].matches(message.username)))
      {
          TkIrc.toIrc.sendAction(Config.cName, aMessage[1].split(" ", 2)[1]);
      }

      return message;
    }

    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent player) {
        if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
            TkIrc.toIrc.joinChannel(Config.cName, Config.cKey);
        }
        if (Config.eJoinMC) {
            TkIrc.toIrc.sendMessage(Config.cName, "* "+ dePing(player.player.getDisplayName()) +" has joined the game");
        }
    }

    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent player) {
        if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
            TkIrc.toIrc.joinChannel("0");
        }

        if (Config.eJoinMC) {
            TkIrc.toIrc.sendMessage(Config.cName, "* " + dePing(player.player.getDisplayName()) + " has left the game");
        }
    }

    private static String dePing(String sPlayer) {
    	sPlayer = IRCBot.Scoreboard(sPlayer, true);
		if (Config.depinger && sPlayer.length() >= 2) {
			String Player = sPlayer.substring(0,sPlayer.length()/2)
					+ Character.toString('\u200B')
					+ sPlayer.substring(sPlayer.length()/2);
			return Player;
		} else {
			return sPlayer;
		}
	}
}