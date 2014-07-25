package alekso56.TkIrc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.IChatListener;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class TkEvents implements IChatListener, IPlayerTracker, IConnectionHandler {
    @ForgeSubscribe
    public void onPlayerDead(LivingDeathEvent event) {
        if (!Config.eDeath) {
            return;
        }

        String s = (event.source.getSourceOfDamage() != null)
                   ? event.source.getSourceOfDamage().getEntityName()
                   : "generic";

        if ((event.entityLiving instanceof EntityPlayer)) {
            String dmsg = StatCollector.translateToLocalFormatted("death.attack." + event.source.damageType,
                              new Object[] { event.entityLiving.getEntityName(),
                                             s });
            
            String cdmsg = (String) Config.mDeathMessages.get("death." + event.source.damageType);

           // System.out.println(event.source.damageType);

            if (cdmsg != null) {
                dmsg = cdmsg;
                dmsg = dmsg.replaceAll("%PLAYER%", dePing(event.entityLiving.getEntityName()));
                dmsg = dmsg.replaceAll("%SOURCE%", s);
            }

            TkIrc.toIrc.sendMessage(Config.cName, "* " + dmsg);
        }
    }

    @ForgeSubscribe
    public void onSM(ServerChatEvent message) {
        if(message.isCanceled()){return;}
            String sPrefix  = Config.pIRCMSG.replaceAll("%n", dePing(IRCBot.colorNick(message.username))) + " ";
            TkIrc.toIrc.sendMessage(Config.cName, sPrefix + message.message);
    }
    @Override
	public Packet3Chat clientChat(NetHandler handler, Packet3Chat message)
    {
      if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
        return message;
      }

      String[] aMessage = message.message.split(" ", 2);

      if (aMessage[0].matches("^<" + handler.getPlayer().username + ">$"))
      {
          TkIrc.toIrc.sendMessage(Config.cName, aMessage[1]);
      }
      else if ((aMessage[0].matches("^\\*$")) && (aMessage[1].split(" ", 2)[0].matches(handler.getPlayer().username)))
      {
          TkIrc.toIrc.sendAction(Config.cName, aMessage[1].split(" ", 2)[1]);
      }

      return message;
    }

    @Override
	public void onPlayerLogin(EntityPlayer player) {
        if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
            TkIrc.toIrc.joinChannel(Config.cName, Config.cKey);
        }
        if (Config.eJoinMC) {
            TkIrc.toIrc.sendMessage(Config.cName, "* "+ dePing(player.username) +" has joined the game");
        }
    }

    @Override
	public void onPlayerLogout(EntityPlayer player) {
        if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
            TkIrc.toIrc.joinChannel("0");
        }

        if (Config.eJoinMC) {
            TkIrc.toIrc.sendMessage(Config.cName, "* " + dePing(player.username) + " has left the game");
        }
    }
    @Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) {
        TkIrc.toIrc.joinChannel(Config.cName, Config.cKey);
    }

    @Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) {
        TkIrc.toIrc.joinChannel(Config.cName, Config.cKey);
    }

    @Override
	public void connectionClosed(INetworkManager manager) {
        if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
            TkIrc.toIrc.joinChannel("0");
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

	public void playerLoggedIn(Player player, NetHandler netHandler,INetworkManager manager) {}
	public String connectionReceived(NetLoginHandler netHandler,INetworkManager manager) {return null;}
	public void clientLoggedIn(NetHandler clientHandler,INetworkManager manager, Packet1Login login) {}
	public void onPlayerChangedDimension(EntityPlayer player) {}
	public void onPlayerRespawn(EntityPlayer player) {}
	public Packet3Chat serverChat(NetHandler handler, Packet3Chat message) {
		if ((message.message.startsWith("/me")) && (message.message.length() >= 4)) {
            String sPrefix  = Config.pIRCAction.replaceAll("%n", dePing(IRCBot.colorNick(handler.getPlayer().username))) + " ";
            TkIrc.toIrc.sendMessage(Config.cName, sPrefix+ message.message.substring(4));
		}
		return message;}
}