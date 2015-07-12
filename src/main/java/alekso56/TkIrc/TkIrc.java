package alekso56.TkIrc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import alekso56.TkIrc.irclib.IRCLib;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = "TKIRC", name = "TK-IRC", version = "2.6.6", dependencies = "required-after:Forge@[10.12.0.967,]",acceptableRemoteVersions = "*")
public class TkIrc {
	protected static Configuration config;
	public static IRCLib toIrc;
	public static HashMap<String, String> commands = new HashMap<String, String>();
	public static ArrayList<String> ops = new ArrayList<String>();
	@Instance
	public static TkIrc instance;
	@SidedProxy(clientSide = "alekso56.TkIrc.ClientProxy", serverSide = "alekso56.TkIrc.CommonProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		Config.loadConfig(config);

		if ((toIrc != null) || (!Config.enabled)) {
			return;
		}
	}
	
	@Mod.EventHandler
	public void serverStart(FMLServerStartingEvent event) 
	{
		if (!Config.enabled) {
			return;
		}
		 event.registerServerCommand(new TkHQ());
		 MinecraftForge.EVENT_BUS.register(new TkEvents());
		 FMLCommonHandler.instance().bus().register(new playerlogs());
		 toIrc.joinChannel(Config.cName, Config.cKey);
	}


	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if ((!Config.enabled) || (toIrc != null)) {
			return;
		}
        
		reconnectBot(true);
		commands = toIrc.loadcmd();
	}
	
	public static void reconnectBot(boolean init){
		if(!init){
		try {
			TkIrc.toIrc.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		}
		toIrc = new IRCBot();
		toIrc.initialTimer();
		toIrc.setUser(proxy.botUser());
		if ((!Config.nUser.isEmpty()) && (!Config.nKey.isEmpty())) {
			toIrc.setSASLUser(Config.nUser);
			toIrc.setSASLPass(Config.nKey);
		}
 
		toIrc.setInfo("TkIRc");

		try {
			if (Config.sKey.equals("")) {
				toIrc.connect(Config.sHost, Integer.valueOf(Config.sPort),
						Config.botName);
			} else {
				toIrc.connect(Config.sHost, Integer.valueOf(Config.sPort),
						Config.botName, Config.sKey);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String combinedModList() {
		ListIterator data = Loader.instance().getModList().listIterator();
		String parsedData = "";
		while (data.hasNext()) {
			String tempData = data.next().toString();
			if (tempData.startsWith("FMLMod:")) {
				tempData = tempData.substring(7);
				if (data.hasNext()) {
					parsedData = parsedData + tempData + ",";
				} else {
					parsedData = parsedData + tempData + ".";
				}
			}
		}
		return parsedData;
	}

	public static void FakeCrash(String d){
	        String[] astring = new String[] {"Who set us up the TNT?", "Everything\'s going to plan. No, really, that was supposed to happen.", "Uh... Did I do that?", "Oops.", "Why did you do that?", "I feel sad now :(", "My bad.", "I\'m sorry, Dave.", "I let you down. Sorry :(", "On the bright side, I bought you a teddy bear!", "Daisy, daisy...", "Oh - I know what I did wrong!", "Hey, that tickles! Hehehe!", "I blame Dinnerbone.", "You should try our sister game, Minceraft!", "Don\'t be sad. I\'ll do better next time, I promise!", "Don\'t be sad, have a hug! <3", "I just don\'t know what went wrong :(", "Shall we play a game?", "Quite honestly, I wouldn\'t worry myself about that.", "I bet Cylons wouldn\'t have this problem.", "Sorry :(", "Surprise! Haha. Well, this is awkward.", "Would you like a cupcake?", "Hi. I\'m Minecraft, and I\'m a crashaholic.", "Ooh. Shiny.", "This doesn\'t make any sense!", "Why is it breaking :(", "Don\'t do that.", "Ouch. That hurt :(", "You\'re mean.", "This is a token for 1 free hug. Redeem at your nearest Mojangsta: [~~HUG~~]", "There are four lights!"};

	        String comment;
			try
	        {
	            comment = astring[(int)(System.nanoTime() % astring.length)];
	        }
	        catch (Throwable throwable)
	        {
	            comment = "Witty comment unavailable :(";
	        }
			//FMLLog.log("Minecraft-Server", Level.SEVERE, "%s", "This crash report has been saved to' in ForgeModLoader-server-0.log");
			File file1 = new File(new File(".", "crash-reports"), "crash-Fake-TKI-Crash-server.txt");
			try {
				if(file1.exists()){file1.delete();}
				file1.createNewFile();
				TkIrc.toIrc.sendMessage(d, comment);
			} catch (IOException e) {
				TkIrc.toIrc.sendMessage(d, "uhh, the fake crash crashed. D:");
			}

	}
}