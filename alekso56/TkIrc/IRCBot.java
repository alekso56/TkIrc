package alekso56.TkIrc;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.DimensionManager;
import alekso56.TkIrc.irclib.Base64;
import alekso56.TkIrc.irclib.IRCLib;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class IRCBot extends IRCLib implements API {
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
	public boolean isAuthed(String username,String d) {
		if (TkIrc.ops.contains(username.toLowerCase())) {
			String authnum = "0";
			try {
				TkIrc.toIrc.sendRaw("NickServ ACC " + username);
				String response = TkIrc.toIrc.in.readLine();
				String[] parted = response.split(" ");
				authnum = parted[5];

			} catch (IOException e) {
				e.printStackTrace();
			}
			if (authnum.equals("3")) {
				return true;
			}
		}
		try {
			TkIrc.toIrc.sendRaw("names "+Config.cName);
			String response = TkIrc.toIrc.in.readLine();
			String[] parted = response.split(" ");
			for (int curr = 0;curr<parted.length; curr++) {
				if(parted[curr].startsWith("@") && parted[curr].contains(username)){
					return true;
				}
			}
            
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (d != null){
		 TkIrc.toIrc.sendMessage(d, "ACCESS DENIED!: not authorized");
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
		
		if (m.startsWith("players")
				&& (Side.SERVER == FMLCommonHandler.instance().getSide())) {
			String[] aPlayers = MinecraftServer.getServer().getAllUsernames();
			String lPlayers = aPlayers.length == 0 ? "None." : "";

			for (String sPlayer : aPlayers) {
				sPlayer = Scoreboard(sPlayer, false);
				lPlayers = lPlayers + ((lPlayers == "") ? sPlayer : new StringBuilder()
								.append(", ").append(sPlayer).toString());
			}
			TkIrc.toIrc.sendMessage(nick, lPlayers);
			return;
		}
		if (m.toLowerCase().startsWith("c ")&&isAuthed(usr,nick)&&m.length() >=  2 ) {
					String out = MinecraftServer.getServer().executeCommand(m.substring(1));
					if (out.startsWith(Config.prefixforirccommands)) {
						out = out.substring(Config.prefixforirccommands.length()+1);
					}
					TkIrc.toIrc.sendMessage(nick, out);
			return;
		}

		if (m.startsWith("status")) {
			TkIrc.toIrc.sendMessage(nick, TkIrc.toIrc.getrawurle());
			return;
		}
		if (m.startsWith("help")) {
	     String msgb = "Prefix: "+Config.prefixforirccommands+" help| players| status| tps <t or worldNum>| base64| ";
		 if (isAuthed(usr, null)){msgb = msgb+"set <command> <reply>| unset <command>| c <mcCommand>| fakecrash| ";}
		 Iterator<String> commands = TkIrc.commands.keySet().iterator();
	 	 while (commands.hasNext()){
			String current = commands.next();
			 msgb = msgb+current+"| ";
		    }
		 TkIrc.toIrc.sendNotice(usr, msgb);
		 return;
		}
		if(m.startsWith("base64") && m.length() > 8){
			TkIrc.toIrc.sendMessage(nick, Base64.encode(m.substring(5)));
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
					equals = equalz
							&& id.equals(Integer
									.parseInt(m.substring(3).trim()));
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
		EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(sPlayer);
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
		if (m.split(" ")[0].equals("VERSION")) {
			sendCTCPReply(n, "Personal TKserver 0.3");
		}

		if (m.split(" ")[0].equals("TIME")) {
			sendCTCPReply(n,
					"My internal clock is broken, plz donate ;_;");
		}

		if (m.split(" ")[0].equals("SOURCE")) {
			sendCTCPReply(n,
					"Wait, i got this... a source is some kind of document right?");
		}

		if (m.split(" ")[0].equals("PAGE")) {
			sendCTCPReply(
					n,
					"PAGE i have just thrown your page into the trash bin, was it something important?");
		}

		if (m.split(" ")[0].equals("USERINFO")) {
			sendCTCPReply(
					n,
					"Gender=Female; yeah, i have a gender! who said servers can't have genders!");
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
					MinecraftServer
							.getServer()
							.getConfigurationManager()
							.sendPacketToAllPlayers(new Packet3Chat(ChatMessageComponent.createFromText(p + mPart)));}
					else{MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(m.split(" ")[0]).sendChatToPlayer(ChatMessageComponent.createFromText(p +": "+ mPart));}
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
					MinecraftServer.getServer().getConfigurationManager()
							.sendPacketToAllPlayers(new Packet3Chat(mPart));
				}
			}
		}
	}

	public static String stripColorsForMC(String message) {
		message = message.replaceAll(Character.toString('Â'), "");
		message = message.replaceAll("§([^\\d+r])", "$1");
		message = message.replaceAll("(" + Character.toString('\003')
				+ "\\d{2}),\\d{1,2}", "§1");
		message = message.replaceAll(Character.toString('\003') + "15", "§7");
		message = message.replaceAll(Character.toString('\003') + "14", "§8");
		message = message.replaceAll(Character.toString('\003') + "13", "§d");
		message = message.replaceAll(Character.toString('\003') + "12", "§9");
		message = message.replaceAll(Character.toString('\003') + "11", "§b");
		message = message.replaceAll(Character.toString('\003') + "10", "§3");
		message = message.replaceAll(Character.toString('\003') + "09", "§a");
		message = message.replaceAll(Character.toString('\003') + "08", "§e");
		message = message.replaceAll(Character.toString('\003') + "07", "§6");
		message = message.replaceAll(Character.toString('\003') + "06", "§5");
		message = message.replaceAll(Character.toString('\003') + "05", "§4");
		message = message.replaceAll(Character.toString('\003') + "04", "§c");
		message = message.replaceAll(Character.toString('\003') + "03", "§2");
		message = message.replaceAll(Character.toString('\003') + "02", "§1");
		message = message.replaceAll(Character.toString('\003') + "01", "§0");
		message = message.replaceAll(Character.toString('\003') + "00", "§f");
		message = message.replaceAll(Character.toString('\003') + "9", "§a");
		message = message.replaceAll(Character.toString('\003') + "8", "§e");
		message = message.replaceAll(Character.toString('\003') + "7", "§6");
		message = message.replaceAll(Character.toString('\003') + "6", "§5");
		message = message.replaceAll(Character.toString('\003') + "5", "§4");
		message = message.replaceAll(Character.toString('\003') + "4", "§c");
		message = message.replaceAll(Character.toString('\003') + "3", "§2");
		message = message.replaceAll(Character.toString('\003') + "2", "§1");
		message = message.replaceAll(Character.toString('\003') + "1", "§0");
		message = message.replaceAll(Character.toString('\003') + "0", "§f");
		message = message.replaceAll(Character.toString('\003'), "§r");
		message = message.replaceAll(Character.toString('\002'), "");
		message = message.replaceAll(Character.toString('\017'), "§r");
		message = message.replaceAll(Character.toString('\037'), "");
		message = message.replaceAll(Character.toString('\035'), "");
		message = message.replaceAll(Character.toString('\026'), "");

		return message;
	}
	
	public static String stripColorsForIRC(String message) {
		message = message.replaceAll("§7",Character.toString('\003') + "14");
		message = message.replaceAll("§8",Character.toString('\003') + "14");
		message = message.replaceAll("§d",Character.toString('\003') + "13");
		message = message.replaceAll("§9",Character.toString('\003') + "12");
		message = message.replaceAll("§b",Character.toString('\003') + "11");
		message = message.replaceAll("§3",Character.toString('\003') + "10");
		message = message.replaceAll("§a",Character.toString('\003') + "09");
		message = message.replaceAll("§e",Character.toString('\003') + "08");
		message = message.replaceAll("§6",Character.toString('\003') + "07");
		message = message.replaceAll("§5",Character.toString('\003') + "06");
		message = message.replaceAll("§4",Character.toString('\003') + "05");
		message = message.replaceAll("§c",Character.toString('\003') + "04");
		message = message.replaceAll("§2",Character.toString('\003') + "03");
		message = message.replaceAll("§1",Character.toString('\003') + "02");
		message = message.replaceAll("§0",Character.toString('\003') + "01");
		message = message.replaceAll("§f",Character.toString('\003') + "00");
		message = message.replaceAll("§r",Character.toString('\003'));

		return message;
	}

	static String colorNick(String n) {
		return n;
	}

	static String colorNick(String n, String u, String h) {
		if (TkIrc.ops.contains(n.toLowerCase())) {
			return "§4" + n + "§r";
		} else {
			return n;
		}
	}
	

	static String getNickColor(String n) {
		return n;
	}
}