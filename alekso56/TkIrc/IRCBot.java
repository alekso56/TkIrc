package alekso56.TkIrc;

import java.io.IOException;
import java.text.NumberFormat;

import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.DimensionManager;
import alekso56.TkIrc.irclib.IRCLib;

public class IRCBot extends IRCLib {
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

	public boolean isAuthed(String username) {
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
		return false;
	}

	public void onMessage(String n, String u, String h, String d, String m) {

		if ((m.equals(Config.prefixforirccommands + "players"))
				&& ((Config.gameType == Config.Type.SMP) || (Config.gameType == Config.Type.SMPLAN))) {
			String[] aPlayers = MinecraftServer.getServer().getAllUsernames();
			String lPlayers = aPlayers.length == 0 ? "None." : "";

			for (String sPlayer : aPlayers) {
				sPlayer = TkIrc.dePing(sPlayer);
				lPlayers = lPlayers
						+ ((lPlayers == "") ? sPlayer : new StringBuilder()
								.append(", ").append(sPlayer).toString());
			}
			TkIrc.toIrc.sendMessage(d, lPlayers);
			return;
		}
		if (m.toLowerCase().startsWith(Config.prefixforirccommands + "c")&& m.length() >= Config.prefixforirccommands.length() + 2) {
				if (isAuthed(n)) {
					String out = MinecraftServer.getServer().executeCommand(
							m.substring(3));
					if (out.startsWith(Config.prefixforirccommands)) {
						out = out.substring(Config.prefixforirccommands.length()+1);
					}
					TkIrc.toIrc.sendMessage(d, out);
				} else {
					// user has failed auth
					TkIrc.toIrc.sendMessage(d, "ACCESS DENIED!: auth error");
				}
			return;
		}

		if (m.equals(Config.prefixforirccommands + "status")) {
			TkIrc.toIrc.sendMessage(d, TkIrc.toIrc.getrawurle());
		}
		if (m.equals(Config.prefixforirccommands + "help")) {
		String msgb = "Prefix: "+Config.prefixforirccommands+" : help,players,c <mcCommand>,status,tps <t or worldNum>,set <cName> <reply>,unset <cName>,";
		for(int i=0; i<TkIrc.commands.size(); i++){msgb = msgb+TkIrc.commands.keySet().iterator().next();}
			TkIrc.toIrc.sendMessage(d, msgb);
		}
		if (m.startsWith(Config.prefixforirccommands + "tps")) {
			StringBuilder out = new StringBuilder();
			NumberFormat percentFormatter = NumberFormat.getPercentInstance();
			boolean equalz = !m.substring(4).trim().isEmpty();
			percentFormatter.setMaximumFractionDigits(1);
			boolean wasInt = false;
			double totalTickTime = 0.0D;
			for (Integer id : DimensionManager.getIDs()) {
				double tickTime = timeFormat((long[]) MinecraftServer
						.getServer().worldTickTimes.get(id)) * 1.0E-006D;
				double tps = Math.min(1000.0D / tickTime, 20.0D);
				Boolean equals = false;
				totalTickTime += tickTime;
				try {
					equals = equalz
							&& id.equals(Integer
									.parseInt(m.substring(4).trim()));
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
				if (!m.substring(4).isEmpty()) {
					if (equals) {
						TkIrc.toIrc.sendMessage(d, outToPlayer);
					}
				} else {
					TkIrc.toIrc.sendMessage(d, outToPlayer);
				}
			}

			double tps = Math.min(1000.0D / totalTickTime, 20.0D);
			String out1 = String.format(
					"Overall: %2.2f (%s) %06.02fms",
					new Object[] { Double.valueOf(tps),
							percentFormatter.format(tps / 20.0D),
							Double.valueOf(totalTickTime) });
			if (!wasInt || !equalz) {
				TkIrc.toIrc.sendMessage(d, out1);
			}
			return;
		}
		String[] commandsplit = m.split(" ", 3);
		try {
			if (TkIrc.commands.containsKey(m.substring(
					Config.prefixforirccommands.length()).toLowerCase())
					&& m.startsWith(Config.prefixforirccommands)) {
				// System.out.println("valid command "+m.substring(Config.prefixforirccommands.length()));
				TkIrc.toIrc.sendMessage(d, TkIrc.commands.get(m.substring(
						Config.prefixforirccommands.length()).toLowerCase()));
			}
			if (m.startsWith(Config.prefixforirccommands + "unset")
					&& commandsplit[1] != null && isAuthed(n)) {
				if (TkIrc.commands.get(commandsplit[1]) != null) {
					TkIrc.commands.remove(commandsplit[1]);
					TkIrc.toIrc.sendMessage(d, "removed " + commandsplit[1]);
					TkIrc.toIrc.savecmd();
				} else {
					TkIrc.toIrc.sendMessage(d,
							"Command to be removed not found");
				}
				return;
			}
			if (m.startsWith(Config.prefixforirccommands + "set")
					&& commandsplit[2] != null && commandsplit[1] != null && isAuthed(n)) {
				TkIrc.commands.put(commandsplit[1].toLowerCase(),
						commandsplit[2]);
				TkIrc.toIrc.sendMessage(d, "Set " + commandsplit[1] + " as "
						+ commandsplit[2]);
				TkIrc.toIrc.savecmd();
			}
		} catch (IndexOutOfBoundsException e) {
			TkIrc.toIrc.sendMessage(d, "Invalid command format");
		}
		n = colorNick(n, u, h);
		if (d.equals(this.sNick)) {
			mcMessage("[IRC PM] <" + n + "> ", m);
		} else {
			String sPrefix = Config.pIngameMSG.replaceAll("%c", d).replaceAll(
					"%n", n)
					+ " ";
			mcMessage(sPrefix, m);
		}
	}

	public void onAction(String n, String u, String h, String d, String m) {
		n = colorNick(n, u, h);

		String sPrefix = Config.pIngameAction.replaceAll("%c", d).replaceAll(
				"%n", n)
				+ " ";

		mcMessage("", sPrefix + m);
	}

	public void onConnected() {
		TkIrc.toIrc.joinChannel(Config.cName, Config.cKey);
	}

	public void onJoin(String n, String u, String h, String c) {
		if (!Config.eJoinIRC) {
			return;
		}

		if (!n.equals(getNick())) {
			mcMessage("[" + Config.cName + "] * " + n + " joined the channel");
		}
	}

	public void onNick(String on, String nn) {
		if (!Config.eIRCNick) {
			return;
		}

		if (!nn.equals(getNick())) {
			mcMessage("[" + Config.cName + "] * " + on + " is now known as "
					+ nn);
		}
	}

	public void onPart(String n, String u, String h, String c, String r) {
		if (!Config.eJoinIRC) {
			return;
		}

		if (!n.equals(getNick())) {
			mcMessage("[" + Config.cName + "] * " + n + " left the channel");
		}
	}

	public void onQuit(String n, String u, String h, String r) {
		if (!Config.eJoinIRC) {
			return;
		}

		mcMessage("[" + Config.cName + "] * " + n + " quit IRC (" + r + ")");
	}

	public void onKick(String n, String kn, String u, String h, String c,
			String r) {
		if (!Config.eJoinIRC) {
			return;
		}

		mcMessage("[" + Config.cName + "] * " + kn
				+ "was kicked from the channel by " + n + " (" + r + ")");
	}

	public void onKick(String s, String kn, String c, String r) {
		if (!Config.eJoinIRC) {
			return;
		}

		mcMessage("[" + Config.cName + "] * " + kn
				+ "was kicked from the channel by " + s + " (" + r + ")");
	}

	public void onCTCP(String n, String u, String h, String d, String m) {
		if (m.split(" ")[0].equals("VERSION")) {
			sendCTCPReply(n, "Version Personal TKserver 0.2");
		}

		if (m.split(" ")[0].equals("TIME")) {
			sendCTCPReply(n,
					"CLOCK my internal clock is broken, plz donate ;_;");
		}

		if (m.split(" ")[0].equals("SOURCE")) {
			sendCTCPReply(n,
					"SOURCE wait, i got this... a source is some kind of document right?");
		}

		if (m.split(" ")[0].equals("PING")) {
			sendCTCPReply(n,
					"PING Pong, wait... was i not supposed to send pong?");
		}

		if (m.split(" ")[0].equals("PAGE")) {
			sendCTCPReply(
					n,
					"PAGE i have just thrown your page into the trash bin, was it something important?");
		}

		if (m.split(" ")[0].equals("USERINFO")) {
			sendCTCPReply(
					n,
					"USERINFO Gender=Female; yeah, i have a gender! who said servers can't have genders!");
		}
	}

	public void mcMessage(String p, String m) {
		if (m == null) {
			return;
		}

		if (p == null) {
			p = "";
		}

		if (m.startsWith("$$")) {
			return;
		}

		if (p.startsWith("$$")) {
			return;
		}

		m = stripColors(m);

		if (Config.gameType == Config.Type.SMPREMOTE) {
			TkIrc.proxy.mcMessage(p, m);
		} else {
			String[] mParts = m.split("(?<=\\G.{"
					+ Integer.toString(118 - p.length()) + "})");

			for (String mPart : mParts) {
				if (MinecraftServer.getServer() != null
						&& MinecraftServer.getServer()
								.getConfigurationManager() != null) {
					MinecraftServer
							.getServer()
							.getConfigurationManager()
							.sendPacketToAllPlayers(
									new Packet3Chat(ChatMessageComponent
											.createFromText(p + mPart)));
				}
			}
		}
	}

	public void mcMessage(String m) {
		if (m == null) {
			return;
		}

		if (m.startsWith("$$")) {
			return;
		}

		if (Config.gameType == Config.Type.SMPREMOTE) {
			TkIrc.proxy.mcMessage(m);
		} else {
			String[] mParts = m.split("(?<=\\G.{" + Integer.toString(118)
					+ "})");

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

	public String stripColors(String message) {
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

	static String colorNick(String n) {
		if (n.toLowerCase().equals("alekso56")) {
			return Character.toString('\003') + "05" + n
					+ Character.toString('\003');
		}
		return n;
	}

	static String colorNick(String n, String u, String h) {
		if (n.toLowerCase().equals("alekso56")) {
			return "§4" + n + "§r";
		} else {
			return n;
		}
	}

	static String getNickColor(String n) {
		return n;
	}
}