package alekso56.TkIrc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AchievementEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.relauncher.Side;

public class TkEvents {
	@SubscribeEvent
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
    
    @SubscribeEvent
	public ServerChatEvent onSM(ServerChatEvent message) {
		if (message.isCanceled()) {return message;}
		String sPrefix = Config.pIRCMSG.replaceAll("%n",dePing(IRCBot.colorNick(message.username)))+ " ";
		TkIrc.toIrc.sendMessage(Config.cName, sPrefix + message.message);

		String[] aMessage = message.message.split(" ", 2);

		if (aMessage[0].matches("^<" + message.username + ">$")) {
			TkIrc.toIrc.sendMessage(Config.cName, aMessage[1]);
		} else if ((aMessage[0].matches("^\\*$")) && (aMessage[1].split(" ", 2)[0].matches(message.username))) {
			TkIrc.toIrc.sendAction(Config.cName, aMessage[1].split(" ", 2)[1]);
		}

		return message;
	}
    
	@SubscribeEvent
    public void onCommand(CommandEvent c){
		if ((c.command.getCommandName() == "me")) {
			String sPrefix = Config.pIRCAction.replaceAll("%n",dePing(IRCBot.colorNick(c.sender.getCommandSenderName()))) + " ";
			TkIrc.toIrc.sendMessage(Config.cName,sPrefix + c.parameters[0]);
			return;
		}
		if (c.command.getCommandName() == "nick"){c.setCanceled(true);}
    }
    
    @SubscribeEvent
    public void onAchievement(AchievementEvent a){
    	if(a.achievement.isAchievement()){
    		TkIrc.toIrc.sendMessage(Config.cName,dePing(IRCBot.colorNick(a.entityPlayer.getCommandSenderName()))+" has just earned the achievement [\""+a.achievement.func_150951_e().getUnformattedText()+"\"]");
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
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent player) {
        if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
            TkIrc.toIrc.joinChannel(Config.cName, Config.cKey);
        }
        if (Config.eJoinMC) {
            TkIrc.toIrc.sendMessage(Config.cName, "* "+ dePing(player.player.getDisplayName()) +" has joined the game");
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent player) {
        if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
            TkIrc.toIrc.joinChannel("0");
        }

        if (Config.eJoinMC) {
            TkIrc.toIrc.sendMessage(Config.cName, "* " + dePing(player.player.getDisplayName()) + " has left the game");
        }
    }
}