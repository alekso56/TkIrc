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
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.IChatListener;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

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
                dmsg = dmsg.replaceAll("%PLAYER%", event.entityLiving.getEntityName());
                dmsg = dmsg.replaceAll("%SOURCE%", s);
            }

            TkIrc.toIrc.sendMessage(Config.cName, "* " + dmsg);
        }
    }

    @ForgeSubscribe
    public void onSM(ServerChatEvent event) {
        String username = IRCBot.colorNick(event.username, null, null);

        if (MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(event.username)) {
            username = username.equals(event.username)
                       ? "§" + Config.opColor + event.username + "§r"
                       : username;
        }
    }
    public Packet3Chat serverChat(NetHandler handler, Packet3Chat message) {
        if ((message.message.startsWith("/")) && (!message.message.startsWith("/me"))
                && (!message.message.startsWith("$$"))) {
            return message;
        }

        if ((message.message.startsWith("/me")) && (message.message.length() >= 4)) {
                String username = IRCBot.colorNick(handler.getPlayer().username);
                username = TkIrc.dePing(username);
                String sPrefix  = Config.pIRCAction.replaceAll("%n", username) + " ";

                if (TkIrc.chatTo >= 0) {
                    TkIrc.toIrc.sendMessage(Config.cName, sPrefix+ message.message.substring(4));
                }
        } else if (!message.message.startsWith("$$")) {
            String username = IRCBot.colorNick(handler.getPlayer().username);
            username = TkIrc.dePing(username);
            String sPrefix  = Config.pIRCMSG.replaceAll("%n", username) + " ";

            if (TkIrc.chatTo >= 0) {
                TkIrc.toIrc.sendMessage(Config.cName, sPrefix + message.message);
            }
        }

        return message;
    }

    public Packet3Chat clientChat(NetHandler handler, Packet3Chat message)
    {
      if (Config.gameType != Config.Type.SMPREMOTE) {
        return message;
      }

      String[] aMessage = message.message.split(" ", 2);

      if (aMessage[0].matches("^<" + handler.getPlayer().username + ">$"))
      {
        if (TkIrc.chatTo >= 0) {
          TkIrc.toIrc.sendMessage(Config.cName, aMessage[1]);
        }
      }
      else if ((aMessage[0].matches("^\\*$")) && (aMessage[1].split(" ", 2)[0].matches(handler.getPlayer().username)))
      {
        if (TkIrc.chatTo >= 0) {
          TkIrc.toIrc.sendAction(Config.cName, aMessage[1].split(" ", 2)[1]);
        }
      }

      return message;
    }

    public void onPlayerLogin(EntityPlayer player) {
        if ((Config.gameType != Config.Type.SMP) && (Config.gameType != Config.Type.SMPLAN)) {
            Config.gameType = Config.Type.SSP;
            TkIrc.toIrc.joinChannel(Config.cName, Config.cKey);
        }

        if (Config.eJoinMC) {
            TkIrc.toIrc.sendMessage(Config.cName, "* "+ TkIrc.dePing(player.username) +" has joined the game");
        }
    }

    public void onPlayerLogout(EntityPlayer player) {
        if (Config.gameType == Config.Type.SSP || Config.gameType == Config.Type.SMPREMOTE) {
            TkIrc.toIrc.joinChannel("0");
        } else if (Config.gameType == Config.Type.SMPLAN
                   &&MinecraftServer.getServer().getAllUsernames().length > 1) {
            Config.gameType = Config.Type.SSP;
        }

        if (Config.eJoinMC) {
            TkIrc.toIrc.sendMessage(Config.cName, "* " + TkIrc.dePing(player.username) + " has left the game");
        }
    }

    public void onPlayerChangedDimension(EntityPlayer player) {}

    public void onPlayerRespawn(EntityPlayer player) {}

    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {}

    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) {
        if (Config.gameType == Config.Type.SSP) {
            Config.gameType = Config.Type.SMPLAN;
        }

        return null;
    }

    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) {
        Config.gameType = Config.Type.SMPREMOTE;
        TkIrc.toIrc.joinChannel(Config.cName, Config.cKey);
    }

    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) {
        Config.gameType = Config.Type.SSP;
        TkIrc.toIrc.joinChannel(Config.cName, Config.cKey);
    }

    public void connectionClosed(INetworkManager manager) {
        if (Config.gameType == Config.Type.SMPREMOTE) {
            TkIrc.toIrc.joinChannel("0");
        }
    }

    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {}
}