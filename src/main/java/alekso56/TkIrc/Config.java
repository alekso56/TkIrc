package alekso56.TkIrc;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config {
	static final String SERVER = "Server";
	static final String CHANNEL = "Channel";
	static final String NICKSERV = "NickServ";
	static final String EVENT = "Events";
	static final String PREFIX = "Prefixes";
	static final String TWITCH = "Twitch";
	static final String GENERAL = "general";
	static boolean enabled;
	static boolean Achievements;
	static boolean depinger;
	static boolean IsUserCommandsEnabled;
	static boolean scoreboardColors;
	static String botName;
	static String opColor;
	static String sHost;
	static String sKey;
	static int sPort;
	static String prefixforirccommands;
	static String cName;
	static String cKey;
	static String nUser;
	static String nKey;
	static String tYourUser;
	static String tTargetUser;
	static String tKey;
	static boolean eDeath;
	static boolean eJoinIRC;
	static boolean eJoinMC;
	static boolean eIRCNick;
	static HashMap mDeathMessages = new HashMap();
	static String pIngameMSG;
	static String pIngameAction;
	static String pIRCMSG;
	static String pIRCAction;

	static void loadConfig(Configuration config) {
		config.load();
		botName = config.get("general", "Nickname", getNick()).getString();
		opColor = config.get("general", "opColor", "e").getString();
		config.get("general", "opColor",
				"e").comment = "Color code for OPs on your server. r = no color. Uses minecraft colors";
		enabled = config.get("general", "ircEnabled", false).getBoolean(false);
		Achievements = config.get("general", "AchievementsEnabled", true).getBoolean(true);
		IsUserCommandsEnabled = config.get("general", "IsUserCommandsEnabled", true).getBoolean(true);
		scoreboardColors = config.get("general", "ScoreboardColorsInIRCMSGS", false).getBoolean(false);

		sHost = config.get("Server", "hostname", "irc.esper.net").getString();
		sPort = config.get("Server", "serverPort", 6667).getInt(6667);
		sKey = config.get("Server", "serverKey", "").getString();
		config.get("Server", "serverKey", "").comment = "Leave serverKey blank unless you know what you're doing";

		cName = config.get("Channel", "Name", "#turtlekingdom").getString();
		cKey = config.get("Channel", "Key", "").getString();
		ConfigCategory cmdcat = config.getCategory("deathmessages");
		Map<String, Property> cmdmap = cmdcat.getValues();

		if (cmdmap.isEmpty()) {
			// System.out.println("cmdmap is empty...");
			config.get("deathmessages", "death.attack.bee.end", "%PLAYER% got stung.");
			config.get("deathmessages", "death.attack.train", "%PLAYER% got hit by a train.");
		}
		for (Map.Entry i : cmdmap.entrySet()) {
			String k = (String) i.getKey();
			Property v = (Property) i.getValue();
			mDeathMessages.put(k, v.getString());
			// System.out.println("Added '" + k + "' with value '" +
			// v.getString() + "'.");
		}
		ConfigCategory cmdcat2 = config.getCategory("operators");
		Map<String, Property> cmdmap2 = cmdcat2.getValues();
		if (config.hasCategory("ops")) {
			if (cmdmap2.isEmpty()) {
				// System.out.println("cmdmap is empty...");
				config.get("operators", config.get("ops","alekso56",1).getName().toLowerCase(), 1).getName().toLowerCase();
				config.get("operators", config.get("ops","Cruor",2).getName().toLowerCase(), 2).getName().toLowerCase();
				config.get("operators", config.get("ops","Cruor_",2).getName().toLowerCase(), 3).getName().toLowerCase();
				config.get("operators", config.get("ops","Odd",3).getName().toLowerCase(), 4).getName().toLowerCase();
				config.get("operators", config.get("ops","SafPlusPlus",4).getName().toLowerCase(), 5).getName().toLowerCase();
				config.get("operators", config.get("ops","Oddstr13",6).getName().toLowerCase(), 6).getName().toLowerCase();
			}
			config.removeCategory(config.getCategory("ops"));
		}

		if (cmdmap2.isEmpty()) {
			// System.out.println("cmdmap is empty...");
			config.get("operators", "alekso56", 1);
		}
		for (Map.Entry i : cmdmap2.entrySet()) {
			TkIrc.ops.add(i.getKey().toString().toLowerCase());
		}
		eDeath = config.get("Events", "Deaths", false).getBoolean(false);
		eJoinMC = config.get("Events", "minecraft_joins", false).getBoolean(true);
		eJoinIRC = config.get("Events", "IRC_Joins", false).getBoolean(false);
		eIRCNick = config.get("Events", "IRC_Nick_Changes", true).getBoolean(true);

		nUser = config.get("NickServ", "username", "").getString();
		config.get("NickServ", "username",
				"").comment = "Type '/ns status' in IRC to get your account username. this is NOT nessicarily your nickname.";
		nKey = config.get("NickServ", "password", "").getString();
		prefixforirccommands = config.get("general", "Prefix_for_irc_Commands", "!").getString();
		depinger = config.get("general", "dePinger_on?", true).getBoolean(true);
		pIngameMSG = config.get("Prefixes", "ingame_MSG_Prefix", "[%c] <%n>").getString().replaceAll("&", "§");
		pIngameAction = config.get("Prefixes", "ingame_Action_Prefix", "[%c] * %n").getString().replaceAll("&", "§");
		pIRCMSG = config.get("Prefixes", "IRC_MSG_Prefix", "<%n>").getString().replaceAll("&",
				Character.toString('\003'));
		pIRCAction = config.get("Prefixes", "IRC_Action_Prefix", "* %n").getString().replaceAll("&",
				Character.toString('\003'));
		config.get("Prefixes", "IRC_Action_Prefix",
				"* %n").comment = "%n = nickname, %c = channel use & symbol for color along with the color code\nIRC Color Guide: http://www.ircbeginner.com/ircinfo/colors.html\nMC Color Guide: http://www.minecraftwiki.net/wiki/Color_Codes";

		tYourUser = config.get("Twitch", "Username", "").getString();
		tKey = config.get("Twitch", "Password", "").getString();

		config.get("Twitch", "Username", "").comment = "Your Twitch username goes here";
		tTargetUser = config.get("Twitch", "Username", "").getString().isEmpty() ? tYourUser
				: config.get("Twitch", "Streamer_Username", "").getString();
		config.get("Twitch", "Streamer_Username",
				"").comment = "Place the name of the streamer whose chat you wish to join here";

		if ((!tYourUser.isEmpty()) && (!tKey.isEmpty())) {
			sHost = "irc.twitch.tv";
			sPort = 6667;
			sKey = tKey;
			cName = "#" + tTargetUser.toLowerCase();
			botName = tYourUser.toLowerCase();
		}
		config.save();
	}

	static String getNick() {
		Random random = new Random();
		return "TK" + Integer.toString(random.nextInt(9999));
	}
}