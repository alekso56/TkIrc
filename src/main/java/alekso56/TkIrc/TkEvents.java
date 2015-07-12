package alekso56.TkIrc;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AchievementEvent;

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
                              new Object[] { dePing(event.entityLiving.getCommandSenderName()),
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
	public void onSM(ServerChatEvent message) {
		if (message.isCanceled()) {return;}
		String sPrefix = Config.pIRCMSG.replaceAll("%n",dePing(IRCBot.colorNick(message.username)))+ " ";
		TkIrc.toIrc.sendMessage(Config.cName, sPrefix + message.message);

		String[] aMessage = message.message.split(" ", 2);

		if (aMessage[0].matches("^<" + message.username + ">$")) {
			TkIrc.toIrc.sendMessage(Config.cName, aMessage[1]);
		} else if ((aMessage[0].matches("^\\*$")) && (aMessage[1].split(" ", 2)[0].matches(message.username))) {
			TkIrc.toIrc.sendAction(Config.cName, aMessage[1].split(" ", 2)[1]);
		}

		return;
	}
    
	@SubscribeEvent
    public void onCommand(CommandEvent c){
		if ((c.command.getCommandName() == "me") && c.parameters.length >= 1) {
			String sPrefix = Config.pIRCAction.replaceAll("%n",dePing(IRCBot.colorNick(c.sender.getCommandSenderName()))) + " ";
			String msg = c.parameters[0];
			for (int curr = 1;curr < c.parameters.length;curr = curr+1) {msg = msg+" "+c.parameters[curr];}
			TkIrc.toIrc.sendMessage(Config.cName,sPrefix + msg);
			return;
		}
		if (c.command.getCommandName() == "nick"){c.setCanceled(true);}
    }
    
    @SubscribeEvent
    public void onAchievement(AchievementEvent a){
    	if(Config.Achievements){
    	StatisticsFile player = ((EntityPlayerMP) a.entityPlayer).func_147099_x();
    	if(a.achievement.isAchievement() && player.canUnlockAchievement(a.achievement) && !player.hasAchievementUnlocked(a.achievement)){
    		TkIrc.toIrc.sendMessage(Config.cName,dePing(IRCBot.colorNick(a.entityPlayer.getCommandSenderName()))+" has just earned the achievement \""+a.achievement.func_150951_e().getUnformattedText()+"\"");
    	}
    	}
    }

    static String dePing(String sPlayer) {
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