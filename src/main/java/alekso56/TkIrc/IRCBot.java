package alekso56.TkIrc;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Iterator;

import alekso56.TkIrc.irclib.Base64;
import alekso56.TkIrc.irclib.IRCLib;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.DimensionManager;

public class IRCBot extends IRCLib implements API {
	private static final String bs = Character.toString('\u00A7');
	private double timeFormat(long[] par1ArrayOfLong) {
		long time = 0L;
		long[] var4 = par1ArrayOfLong;
		int var5 = par1ArrayOfLong.length;

		for (int var6 = 0; var6 < var5; var6++) {
			long var7 = var4[var6];

			time += var7;
		}

		return time / par1ArrayOfLong.length;
	}

	@Override
	public boolean isAuthed(String username, String nick) {
		if (TkIrc.ops.contains(username.toLowerCase())) {
			String authnum = "0";
			//check nickserv
			try {
				TkIrc.toIrc.sendRaw("NickServ ACC " + username);
				String response = TkIrc.toIrc.in.readLine();
				String[] parted = response.split(" ");
				authnum = parted[5];

			} catch (IOException e) {
				//e.printStackTrace();
			}
			if (authnum.equals("3")) {
				return true;
			}
		}
		//first check failed, try names
		try {
			TkIrc.toIrc.sendRaw("names " + Config.cName);
			String response = TkIrc.toIrc.in.readLine();
			String[] parted = response.split(" ");
			for (int curr = 0; curr < parted.length; curr++) {
				if (parted[curr].startsWith("@") && parted[curr].contains(username)) {
					return true;
				}
			}

		} catch (IOException e) {
			if (nick != null) {
				TkIrc.toIrc.sendMessage(nick, "Both NS and names failed to work. This is a critical error.");
			}
		}
		if (nick != null) {
			TkIrc.toIrc.sendMessage(nick, "You are unauthorized.");
		}
		return false;
	}

	@Override
	public void onMessage(String usr, String u, String h, String nick, String m) {
		if(!m.startsWith(Config.prefixforirccommands)){
			usr = colorNick(usr, u, h);
			if (nick.equals(this.sNick)) {
				mcMessage(usr, m,true);
			} else {
				String sPrefix = Config.pIngameMSG.replaceAll("%c", nick).replaceAll("%n", usr)+ " ";
				mcMessage(sPrefix, m,false);
			}
			return;
		}
		else{m = m.substring(Config.prefixforirccommands.length());}
		m = m.toLowerCase();
		
	    if(!Config.IsUserCommandsEnabled){if(!isAuthed(usr,nick)){return;}}

		if (m.startsWith("players") && (Side.SERVER == FMLCommonHandler.instance().getSide())) {
			String[] aPlayers = MinecraftServer.getServer().getAllUsernames();
			String lPlayers = aPlayers.length == 0 ? "None." : "";
            if(aPlayers.length < 50){
			for (String sPlayer : aPlayers) {
				sPlayer = Scoreboard(sPlayer, false);
				lPlayers = lPlayers + ((lPlayers == "") ? sPlayer : new StringBuilder()
								.append(", ").append(sPlayer).toString());
			}
			 if(lPlayers.length() <= 400){
			     TkIrc.toIrc.sendMessage(nick, lPlayers);}
			 else{
				 int last = 0;
					for (int i = 0; i < lPlayers.length();)
					{
						if (lPlayers.charAt(i) == ',')
						{
							last = i;
							i += 400;
							TkIrc.toIrc.sendNotice(nick, lPlayers.substring(last, i));	
						}
					}
			 }
            }else{
             TkIrc.toIrc.sendMessage(nick, "Too many players present.");
            }
			return;
		}
		if (m.startsWith("c ")&&isAuthed(usr,nick)&&m.length() >=  2 && !m.substring(1).startsWith("me")) {
			TkIrcCommandsender tki = new TkIrcCommandsender();
			tki.resetLog();
			MinecraftServer.getServer().getCommandManager().executeCommand(tki, m.substring(1));
			String out = tki.getLogContents();
			tki.resetLog();
			if(out.isEmpty()){
				TkIrc.toIrc.sendMessage(nick, "Executed succesfully, but got no return.");
			}else{
				TkIrc.toIrc.sendMessage(nick, out);
			}
			return;
		}
		if (m.startsWith("tusercommands") && isAuthed(usr,nick)) {
			if(Config.IsUserCommandsEnabled){Config.IsUserCommandsEnabled = false;}else{Config.IsUserCommandsEnabled = true;}
			TkIrc.toIrc.sendNotice(nick, "Toggled user commands to "+Config.IsUserCommandsEnabled);
			return;
		}
		if (m.startsWith("tachievements") && isAuthed(usr,nick)) {
			if(Config.Achievements){Config.Achievements = false;}else{Config.Achievements = true;}
			TkIrc.toIrc.sendNotice(nick, "Toggled Achievements to "+Config.Achievements);
			return;
		}
		if (m.startsWith("status")) {
			TkIrc.toIrc.sendMessage(nick, TkIrc.toIrc.getrawurle());
			return;
		}
		if (m.startsWith("help") && m.length() == 4) {
	     String msgb = "Prefix: "+Config.prefixforirccommands+" help| players| status| tps <t or worldNum>| base64| moddir| rainbow| ";
		 if (isAuthed(usr, null)){msgb = msgb+"set <command> <reply>| unset <command>| c <mcCommand>| fakecrash| tUserCommands| tAchievements| ";}
		 Iterator<String> commands = TkIrc.commands.keySet().iterator();
	 	 while (commands.hasNext()){
			String current = commands.next();
			if(msgb.length() <= 400){
			  msgb = msgb+current+"| ";
			 }else{
				 TkIrc.toIrc.sendNotice(usr, msgb);
				 msgb = "";
			 }
		    }
		 TkIrc.toIrc.sendNotice(usr, msgb);
		 return;
		}else if(m.startsWith("help") && m.length() >= 5){
			m = m.substring(5).toLowerCase();
			if(m.startsWith("help")){
				TkIrc.toIrc.sendNotice(usr, "help: Display list of commands currently served by this server.");
			}else if(m.startsWith("players")){
				TkIrc.toIrc.sendNotice(usr, "players: List all players currently on server, by username.");
			}else if(m.startsWith("status")){
				TkIrc.toIrc.sendNotice(usr, "status: Parse all of mojangs services and output non working services.");
			}else if(m.startsWith("tps")){
				TkIrc.toIrc.sendNotice(usr, "tps: Show the tick per second on all worlds, or just the number provided");
			}else if(m.startsWith("base64")){
				TkIrc.toIrc.sendNotice(usr, "base64: Command converts input to base64, this module is used in sasl, but can also be used here.");
			}else if(m.startsWith("moddir")){
				TkIrc.toIrc.sendNotice(usr, "moddir: List all currently loaded forgemods, in notice.");
			}else if(m.startsWith("rainbow")){
				TkIrc.toIrc.sendNotice(usr, "rainbow: Command adds random colors to input text.");
			}else if(m.startsWith("set")){
				TkIrc.toIrc.sendNotice(usr, "set: Set a response to a phrase, in the format set <command> <reply>");
			}else if(m.startsWith("unset")){
				TkIrc.toIrc.sendNotice(usr, "unset: Remove a response to a phrase from the database, unset <command>");
			}else if(m.startsWith("c")){
				TkIrc.toIrc.sendNotice(usr, "c: Execute a minecraft command as the server.");
			}else if(m.startsWith("fakecrash")){
				TkIrc.toIrc.sendNotice(usr, "fakecrash: Make a crashfile in the crashreports dir, then print a message.");
			}else if(m.startsWith("tusercommands")){
				TkIrc.toIrc.sendNotice(usr, "tUserCommands: Toggle users ability to use the (irc)server commands.");
			}else if(m.startsWith("tcchievements")){
				TkIrc.toIrc.sendNotice(usr, "tAchievements: Toggle achievements announcements on irc.");
			}
			return;
		}
		if(m.startsWith("base64") && m.length() > 8){
			TkIrc.toIrc.sendMessage(nick, Base64.encode(m.substring(7)));
			return;
		}
		if(m.startsWith("moddir")){
			String rawData = TkIrc.combinedModList();
			if(rawData.length() <= 400){
				TkIrc.toIrc.sendNotice(nick, rawData);
			}else{
				String[] rowData = rawData.split("(?<=\\G.{400})");
				for (String Packet: rowData){
					TkIrc.toIrc.sendNotice(nick, Packet);
				}
			}
			return;
		}
		if(m.startsWith("rainbow") && m.length() > 8){
			TkIrc.toIrc.sendMessage(nick, colorRainbow(m.substring(8)));
			return;
		}
		if (m.startsWith("tps")) {
			StringBuilder out = new StringBuilder();
			NumberFormat percentFormatter = NumberFormat.getPercentInstance();
			boolean equalz = !m.substring(3).trim().isEmpty();
			percentFormatter.setMaximumFractionDigits(1);
			boolean wasInt = false;
			double totalTickTime = 0.0D;
			for (Integer id : DimensionManager.getIDs()) {
				double tickTime = timeFormat(MinecraftServer
						.getServer().worldTickTimes.get(id)) * 1.0E-006D;
				double tps = Math.min(1000.0D / tickTime, 20.0D);
				Boolean equals = false;
				totalTickTime += tickTime;
				try {
					equals = equalz && id.equals(Integer.parseInt(m.substring(3).trim()));
					wasInt = true;
				} catch (NumberFormatException e) {
				}
				String tickPercent = percentFormatter.format(tps / 20.0D);
				String outToPlayer = String.format(
						"%2.2f (%s) %06.02fms %3d %s",
						new Object[] {
								Double.valueOf(tps),
								tickPercent,
								Double.valueOf(tickTime),
								id,
								DimensionManager.getProvider(id.intValue())
										.getDimensionName() });
				if (!m.substring(3).isEmpty()) {
					if (equals) {
						TkIrc.toIrc.sendMessage(nick, outToPlayer);
					}
				} else {
					TkIrc.toIrc.sendMessage(nick, outToPlayer);
				}
			}

			double tps = Math.min(1000.0D / totalTickTime, 20.0D);
			String out1 = String.format(
					"Overall: %2.2f (%s) %06.02fms",
					new Object[] { Double.valueOf(tps),
							percentFormatter.format(tps / 20.0D),
							Double.valueOf(totalTickTime) });
			if (!wasInt || !equalz) {
				TkIrc.toIrc.sendMessage(nick, out1);
			}
			return;
		}
		if (m.startsWith("fakecrash") && isAuthed(usr,nick)){
			TkIrc.FakeCrash(nick);
			return;
		}
		String[] commandsplit = m.split(" ", 3);
		try {
			String mesig = commandsplit[0];
			if (TkIrc.commands.containsKey(mesig)) {
				
				TkIrc.toIrc.sendMessage(nick, TkIrc.commands.get(mesig));
				return;
			}
			if (m.startsWith("unset")
					&& commandsplit[1] != null && isAuthed(usr,nick)) {
				if (TkIrc.commands.get(commandsplit[1]) != null) {
					TkIrc.commands.remove(commandsplit[1]);
					TkIrc.toIrc.sendMessage(nick, "removed " + commandsplit[1]);
					TkIrc.toIrc.savecmd();
				} else {
					TkIrc.toIrc.sendMessage(nick,
							"Command to be removed not found");
				}
				return;
			}
			if (m.startsWith("set") && commandsplit[2] != null && commandsplit[1] != null && isAuthed(usr,nick)) {
				
				TkIrc.commands.put(commandsplit[1].toLowerCase(),commandsplit[2]);
				TkIrc.toIrc.sendNotice(usr, "Set " + commandsplit[1] + " as "+ commandsplit[2]);
				TkIrc.toIrc.savecmd();
				return;
			}
		} catch (IndexOutOfBoundsException e) {
			TkIrc.toIrc.sendMessage(nick, "Invalid command format");
			return;
		}
	}

	public static String Scoreboard(String sPlayer,boolean isMSG) {
		if(!isMSG || Config.scoreboardColors){
		EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(sPlayer);
		String message = ScorePlayerTeam.formatPlayerName(player.getTeam(), sPlayer);
		sPlayer = stripColorsForIRC(message.substring(0,message.length()));
		}
		return sPlayer;
	}

	@Override
	public void onAction(String n, String u, String h, String d, String m) {
		n = colorNick(n, u, h);

		String sPrefix = Config.pIngameAction.replaceAll("%c",d).replaceAll("%n", n)+" ";

		mcMessage("", sPrefix + m,false);
	}

	@Override
	public void onConnected() {
		TkIrc.toIrc.joinChannel(Config.cName, Config.cKey);
	}

	@Override
	public void onJoin(String n, String u, String h, String c) {
		if (!Config.eJoinIRC) {
			return;
		}

		if (!n.equals(getNick())) {
			mcMessage("[" + Config.cName + "] * " + n + " joined the channel");
		}
	}

	@Override
	public void onNick(String on, String nn) {
		if (!Config.eIRCNick) {
			return;
		}

		if (!nn.equals(getNick())) {
			mcMessage("[" + Config.cName + "] * " + on + " is now known as "
					+ nn);
		}
	}

	@Override
	public void onPart(String n, String u, String h, String c, String r) {
		if (!Config.eJoinIRC) {
			return;
		}

		if (!n.equals(getNick())) {
			mcMessage("[" + Config.cName + "] * " + n + " left the channel");
		}
	}

	@Override
	public void onQuit(String n, String u, String h, String r) {
		if (!Config.eJoinIRC) {
			return;
		}

		mcMessage("[" + Config.cName + "] * " + n + " quit IRC (" + r + ")");
	}

	@Override
	public void onKick(String n, String kn, String u, String h, String c,
			String r) {
		if (!Config.eJoinIRC) {
			return;
		}

		mcMessage("[" + Config.cName + "] * " + kn
				+ "was kicked from the channel by " + n + " (" + r + ")");
	}

	@Override
	public void onKick(String s, String kn, String c, String r) {
		if (!Config.eJoinIRC) {
			return;
		}

		mcMessage("[" + Config.cName + "] * " + kn
				+ "was kicked from the channel by " + s + " (" + r + ")");
	}

	@Override
	public void onCTCP(String n, String u, String h, String d, String m) {
		if (m.split(" ")[0].equals("VERSION") || m.split(" ")[0].equals("CLIENTINFO")) {
			sendCTCPReply(n, "VERSION Personal TKserver 0.4");
		}

		if (m.split(" ")[0].equals("TIME")) {
			sendCTCPReply(n,
					"TIME "+new Date().toString());
		}

		if (m.split(" ")[0].equals("SOURCE")) {
			sendCTCPReply(n,
					"SOURCE Wait, i got this... a source is some kind of document right?");
		}

		if (m.split(" ")[0].equals("PAGE")) {
			sendCTCPReply(
					n,
					"PAGE i have just thrown your page into the trash bin, was it something important?");
		}
		
		if (m.split(" ")[0].equals("FINGER")) {
			sendCTCPReply(
					n,
					"FINGER kinky!");
		}

		if (m.split(" ")[0].equals("USERINFO")) {
			sendCTCPReply(
					n,
					"USERINFO Gender=Female; yeah, i have a gender! who said servers can't have genders!");
		}
		
		if (m.split(" ")[0].equals("PING")) {
			sendCTCPReply(
					n,
					"PING "+m.split(" ")[1]);
		}
	}

	public void mcMessage(String p, String m,boolean isPM) {
		if (m == null) {
			return;
		}

		if (p == null) {
			p = "";
		}

		m = stripColorsForMC(m);
        String x = m;
        try{
		if(isPM && m.length() > 3){x  = m.split(" ")[1];}
		if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
			TkIrc.proxy.mcMessage(p, x);
		} else {
			String[] mParts = x.split("(?<=\\G.{"
					+ Integer.toString(118 - p.length()) + "})");
			for (String mPart : mParts) {
				if (MinecraftServer.getServer() != null && MinecraftServer.getServer().getConfigurationManager() != null) {
					if(!isPM){
					MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(p+mPart));}
					else{MinecraftServer.getServer().getConfigurationManager().func_152612_a(m.split(" ")[0]).addChatComponentMessage(new ChatComponentText(p +": "+ mPart));}
				}
			}
		}
        }catch(Exception err){/*TkIrc.toIrc.sendMessage(p, "yo, you fakd up shit.");*/}
	}

	public void mcMessage(String m) {
		if (m == null) {
			return;
		}

		if (Side.CLIENT == FMLCommonHandler.instance().getSide()) {
			TkIrc.proxy.mcMessage(m);
		} else {
			String[] mParts = m.split("(?<=\\G.{" + Integer.toString(118) + "})");

			for (String mPart : mParts) {
				if (MinecraftServer.getServer() != null
						&& MinecraftServer.getServer()
								.getConfigurationManager() != null) {
					MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(mPart));
				}
			}
		}
	}

	public static String stripColorsForMC(String message) {
		message = message.replaceAll(bs+"([^\\d+r])", "$1");
		message = message.replaceAll("(" + Character.toString('\003')+ "\\d{2}),\\d{1,2}", bs+"1");
		message = message.replaceAll(Character.toString('\003') + "15", bs+"7");
		message = message.replaceAll(Character.toString('\003') + "14", bs+"8");
		message = message.replaceAll(Character.toString('\003') + "13", bs+"d");
		message = message.replaceAll(Character.toString('\003') + "12", bs+"9");
		message = message.replaceAll(Character.toString('\003') + "11", bs+"b");
		message = message.replaceAll(Character.toString('\003') + "10", bs+"3");
		message = message.replaceAll(Character.toString('\003') + "09", bs+"a");
		message = message.replaceAll(Character.toString('\003') + "08", bs+"e");
		message = message.replaceAll(Character.toString('\003') + "07", bs+"6");
		message = message.replaceAll(Character.toString('\003') + "06", bs+"5");
		message = message.replaceAll(Character.toString('\003') + "05", bs+"4");
		message = message.replaceAll(Character.toString('\003') + "04", bs+"c");
		message = message.replaceAll(Character.toString('\003') + "03", bs+"2");
		message = message.replaceAll(Character.toString('\003') + "02", bs+"1");
		message = message.replaceAll(Character.toString('\003') + "01", bs+"0");
		message = message.replaceAll(Character.toString('\003') + "00", bs+"f");
		message = message.replaceAll(Character.toString('\003') + "9", bs+"a");
		message = message.replaceAll(Character.toString('\003') + "8", bs+"e");
		message = message.replaceAll(Character.toString('\003') + "7", bs+"6");
		message = message.replaceAll(Character.toString('\003') + "6", bs+"5");
		message = message.replaceAll(Character.toString('\003') + "5", bs+"4");
		message = message.replaceAll(Character.toString('\003') + "4", bs+"c");
		message = message.replaceAll(Character.toString('\003') + "3", bs+"2");
		message = message.replaceAll(Character.toString('\003') + "2", bs+"1");
		message = message.replaceAll(Character.toString('\003') + "1", bs+"0");
		message = message.replaceAll(Character.toString('\003') + "0", bs+"f");
		message = message.replaceAll(Character.toString('\003'), bs+"r");
		message = message.replaceAll(Character.toString('\002'), "");
		message = message.replaceAll(Character.toString('\017'), bs+"r");
		message = message.replaceAll(Character.toString('\037'), "");
		message = message.replaceAll(Character.toString('\035'), "");
		message = message.replaceAll(Character.toString('\026'), "");

		return message;
	}
	
	public static String stripColorsForIRC(String message) {
		message = message.replaceAll(bs+"7",Character.toString('\003') + "14");
		message = message.replaceAll(bs+"8",Character.toString('\003') + "14");
		message = message.replaceAll(bs+"d",Character.toString('\003') + "13");
		message = message.replaceAll(bs+"9",Character.toString('\003') + "12");
		message = message.replaceAll(bs+"b",Character.toString('\003') + "11");
		message = message.replaceAll(bs+"3",Character.toString('\003') + "10");
		message = message.replaceAll(bs+"a",Character.toString('\003') + "09");
		message = message.replaceAll(bs+"e",Character.toString('\003') + "08");
		message = message.replaceAll(bs+"6",Character.toString('\003') + "07");
		message = message.replaceAll(bs+"5",Character.toString('\003') + "06");
		message = message.replaceAll(bs+"4",Character.toString('\003') + "05");
		message = message.replaceAll(bs+"c",Character.toString('\003') + "04");
		message = message.replaceAll(bs+"2",Character.toString('\003') + "03");
		message = message.replaceAll(bs+"1",Character.toString('\003') + "02");
		message = message.replaceAll(bs+"0",Character.toString('\003') + "01");
		message = message.replaceAll(bs+"f",Character.toString('\003') + "00");
		message = message.replaceAll(bs+"r",Character.toString('\003'));

		return message;
	}
	
	static int randomWithRange(int min, int max)
	{
	   int range = (max - min) + 1;     
	   return (int)(Math.random() * range) + min;
	}
	
	static String colorRainbow(String msg){
		String[] colors = new String[14];
		colors[13] = Character.toString('\003') + "14";
		colors[12] = Character.toString('\003') + "13";
		colors[11] = Character.toString('\003') + "12";
		colors[10] = Character.toString('\003') + "11";
		colors[9] = Character.toString('\003') + "10";
		colors[8] = Character.toString('\003') + "09";
		colors[7] = Character.toString('\003') + "08";
		colors[6] = Character.toString('\003') + "07";
		colors[5] = Character.toString('\003') + "06";
		colors[4] = Character.toString('\003') + "05";
		colors[3] = Character.toString('\003') + "04";
		colors[2] = Character.toString('\003') + "03";
		colors[1] = Character.toString('\003') + "02";
		colors[0] = Character.toString('\003') + "01";
		StringBuilder buildstring = new StringBuilder();
		for (int i = 0;i < msg.length(); i++){
		    buildstring.append(colors[randomWithRange(0,13)]+Character.toString(msg.charAt(i)));
		}
		return buildstring.toString();
	}

	static String colorNick(String n) {
		return n;
	}

	static String colorNick(String n, String u, String h) {
		if (TkIrc.ops.contains(n.toLowerCase())) {
			return bs+"4" + n + bs+"r";
		} else {
			return n;
		}
	}
	

	static String getNickColor(String n) {
		return n;
	}
}