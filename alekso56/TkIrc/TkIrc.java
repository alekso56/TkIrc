package alekso56.TkIrc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import alekso56.TkIrc.irclib.IRCLib;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "TKIRC", name = "TK-IRC", version = "2.0", dependencies = "required-after:Forge@[9.11.1.633,]")
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class TkIrc {
	public static final String VERSION = "2.0";
	static int chatTo = 0;
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
		if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
			Config.gameType = Config.Type.SMP;
		}

		config = new Configuration(event.getSuggestedConfigurationFile());
		Config.loadConfig(config);

		if ((toIrc != null) || (!Config.enabled)) {
			return;
		}

		TkEvents eHandler = new TkEvents();
		MinecraftForge.EVENT_BUS.register(eHandler);
		GameRegistry.registerPlayerTracker(eHandler);
		NetworkRegistry.instance().registerChatListener(eHandler);
		NetworkRegistry.instance().registerConnectionHandler(eHandler);

		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
			KeyBindingRegistry.registerKeyBinding(new Tkeys());
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if ((!Config.enabled) || (toIrc != null)) {
			return;
		}

		toIrc = new IRCBot();
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
		commands = toIrc.loadcmd();
	}

	@Mod.EventHandler
	public void registerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(new TkHQ());
	}

	public static String dePing(String sPlayer) {
		if (Config.depinger && sPlayer.length() >= 2) {
			String Player = sPlayer.substring(0, 2)
					+ Character.toString('\u200B')
					+ sPlayer.substring(2, sPlayer.length()-2);
			return Player;
		} else {
			return sPlayer;
		}
	}

	static void toggleChat() {
		if (chatTo == 1) {
			chatTo = -1;
		} else {
			chatTo += 1;
		}
	}
}