package alekso56.TkIrc.irclib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import alekso56.TkIrc.TkIrc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class IRCLib extends Thread {
	protected boolean bConnected;
	protected List lChannel = new ArrayList();
	protected String sServer;
	protected String sNick;
	protected String sName = "sdIRC";
	protected String sUser = "sdIRC";
	protected String sKey = "";
	protected String SASLUser = "";
	protected String SASLPass = "";
	protected Integer iPort;
	protected Socket socket;
	public BufferedWriter out;
	public BufferedReader in;

	public boolean connect(String s, Integer p) throws IOException {
		if (this.sNick == null)
			return false;
		if (this.bConnected)
			return false;
		this.socket = new Socket(s, p.intValue());
		this.bConnected = true;
		this.out = new BufferedWriter(new OutputStreamWriter(
				this.socket.getOutputStream(), "UTF-8"));
		this.in = new BufferedReader(new InputStreamReader(
				this.socket.getInputStream(), "UTF-8"));
		start();
		if ((!this.SASLUser.isEmpty()) && (!this.SASLPass.isEmpty()))
			this.out.write("CAP REQ :sasl\n");
		if (!this.sKey.isEmpty())
			this.out.write("PASS " + this.sKey + "\n");
		this.out.write("USER " + this.sUser + " * * :" + this.sName + "\n");
		this.out.write("NICK " + this.sNick + "\n");
		this.out.flush();
		return true;
	}

	public boolean connect(String s, Integer p, String n) throws IOException {
		this.sNick = n;
		return connect(s, p);
	}

	public boolean connect(String s, Integer p, String n, String k)
			throws IOException {
		this.sNick = n;
		this.sKey = k;
		return connect(s, p);
	}

	public void close() throws IOException {
		this.bConnected = false;
		this.socket.close();
	}

	public String getServer() {
		return this.bConnected ? this.sServer : null;
	}

	public String getNick() {
		return this.sNick;
	}

	public boolean setNick(String n) {
		if (this.bConnected) {
			this.sNick = n;
			sendRaw("NICK :" + n);
			return true;
		}
		this.sNick = n;
		return true;
	}

	public String getUser() {
		return this.sUser;
	}

	public boolean setUser(String u) {
		if (this.bConnected) {
			return false;
		}
		this.sUser = u;
		return true;
	}

	public boolean setInfo(String i) {
		if (this.bConnected) {
			return false;
		}
		this.sName = i;
		return true;
	}

	public void setSASLUser(String s) {
		this.SASLUser = s;
	}

	public void setSASLPass(String s) {
		this.SASLPass = s;
	}

	public List getChannels() {
		return this.lChannel;
	}

	public boolean sendRaw(String s) {
		if (this.bConnected) {
			try {
				this.out.write(s + "\n");
				this.out.flush();
				return true;
			} catch (IOException e) {
				this.bConnected = false;
				return false;
			}
		}
		return false;
	}

	protected Matcher parseMask(String m) {
		Matcher iUser = Pattern.compile(":(.*)!(.*)@(.*)").matcher(m);
		return iUser.matches() ? iUser : null;
	}

	protected void process(String line) {
		if (line.toUpperCase().startsWith("PING ")) {
			sendRaw("PONG " + line.substring(5));
		} else if (line.toUpperCase().startsWith("AUTHENTICATE +")) {
			sendRaw("AUTHENTICATE " + getSASL(this.SASLUser, this.SASLPass));
		} else {
			String[] aLine = line.split(" ", 3);
			processCommand(aLine[1], line);
		}
	}

	protected void processCommand(String sCommand, String sLine) {
		String[] sParsed = sLine.split(" ");

		if (sCommand.toUpperCase().trim().equals("004"))
			onConnected();
		else if (sCommand.toUpperCase().trim().equals("433")) {
			setNick(this.sNick + "_");
		} else if (sCommand.toUpperCase().equals("CAP")) {
			if (sParsed[3].equals("ACK"))
				sendRaw("AUTHENTICATE PLAIN");
		} else if ((sCommand.toUpperCase().trim().equals("903"))
				|| (sCommand.toUpperCase().trim().equals("904"))
				|| (sCommand.toUpperCase().trim().equals("905"))
				|| (sCommand.toUpperCase().trim().equals("906"))
				|| (sCommand.toUpperCase().trim().equals("907"))) {
			sendRaw("CAP END");
		} else if (!sCommand.toUpperCase().trim().equals("432")) {
			if (sCommand.toUpperCase().trim().equals("NICK")) {
				this.sNick = sParsed[2].replaceFirst(":", "");
				String snNick = sParsed[0].split("!")[0].replaceFirst(":", "");
				onNick(snNick, this.sNick);
			} else if (sCommand.toUpperCase().trim().equals("JOIN")) {
				Matcher iUser = parseMask(sParsed[0]);
				String sOrigin = iUser == null ? sParsed[0].replaceFirst(":",
						"") : iUser.group(1);
				if (iUser == null) {
					onJoin(sOrigin, sParsed[2]);
				} else {
					if (iUser.group(1).equals(this.sNick))
						this.lChannel.add(sParsed[2]);
					onJoin(iUser.group(1), iUser.group(2), iUser.group(3),
							sParsed[2]);
				}
			} else if (sCommand.toUpperCase().trim().equals("PART")) {
				Matcher iUser = parseMask(sParsed[0]);
				if (iUser.group(1).equals(this.sNick))
					this.lChannel.remove(sParsed[2]);
				String sPart = sLine.split(" ", 4).length > 3 ? sLine.split(
						" ", 4)[3].replaceFirst(":", "") : null;
				onPart(iUser.group(1), iUser.group(2), iUser.group(3),
						sParsed[2], sPart);
			} else if (sCommand.toUpperCase().trim().equals("KICK")) {
				Matcher iUser = parseMask(sParsed[0]);
				String sOrigin = iUser == null ? sParsed[0].replaceFirst(":",
						"") : iUser.group(1);
				if (iUser == null) {
					onKick(sOrigin, sParsed[3], sParsed[2], sParsed[4]);
				} else {
					if (iUser.group(1).equals(this.sNick))
						this.lChannel.remove(sParsed[2]);
					onKick(iUser.group(1), iUser.group(2), iUser.group(3),
							sParsed[3], sParsed[2], sParsed[4]);
				}
			} else if (sCommand.toUpperCase().trim().equals("QUIT")) {
				Matcher iUser = parseMask(sParsed[0]);
				onQuit(iUser.group(1), iUser.group(2), iUser.group(3),
						sLine.split(" ", 3)[2].replaceFirst(":", ""));
			} else if (sCommand.toUpperCase().trim().equals("PRIVMSG")) {
				Matcher iUser = parseMask(sParsed[0]);
				String sOrigin = iUser == null ? sParsed[0].replaceFirst(":",
						"") : iUser.group(1);
				if (sLine.split(" ", 4)[3].replaceFirst(":", "").startsWith(
						Character.toString('\001'))) {
					if (iUser == null)
						processCTCP(
								sOrigin,
								sParsed[2],
								sLine.split(" ", 4)[3].replaceFirst(":", "")
										.substring(
												1,
												sLine.split(" ", 4)[3]
														.replaceFirst(":", "")
														.length() - 1));
					else {
						processCTCP(
								iUser.group(1),
								iUser.group(2),
								iUser.group(3),
								sParsed[2],
								sLine.split(" ", 4)[3].replaceFirst(":", "")
										.substring(
												1,
												sLine.split(" ", 4)[3]
														.replaceFirst(":", "")
														.length() - 1));
					}
				} else if (iUser == null)
					onMessage(sOrigin, sParsed[2],
							sLine.split(" ", 4)[3].replaceFirst(":", ""));
				else {
					onMessage(iUser.group(1), iUser.group(2), iUser.group(3),
							sParsed[2],
							sLine.split(" ", 4)[3].replaceFirst(":", ""));
				}

			} else if (sCommand.toUpperCase().trim().equals("NOTICE")) {
				Matcher iUser = parseMask(sParsed[0]);
				String sOrigin = iUser == null ? sParsed[0].replaceFirst(":",
						"") : iUser.group(1);
				if (iUser == null)
					onNotice(sOrigin, sParsed[2],
							sLine.split(" ", 4)[3].replaceFirst(":", ""));
				else
					onNotice(iUser.group(1), iUser.group(2), iUser.group(3),
							sParsed[2],
							sLine.split(" ", 4)[3].replaceFirst(":", ""));
			}
		}
	}

	public void processCTCP(String n, String u, String h, String d, String m) {
		String[] mParts = m.split(" ", 2);
		if (mParts[0].equals("ACTION"))
			onAction(n, u, h, d, mParts[1]);
		else
			onCTCP(n, u, h, d, m);
	}

	public void processCTCP(String s, String d, String m) {
		String[] mParts = m.split(" ", 2);
		if (mParts[0].equals("ACTION"))
			onAction(s, d, mParts[1]);
		else
			onCTCP(s, d, m);
	}

	public String getSASL(String u, String p) {
		return Base64.encode(u + Character.toString('\000') + u
				+ Character.toString('\000') + p);
	}

	public void run() {
		try {
			String line = null;
			while ((this.in != null) && ((line = this.in.readLine()) != null))
				process(line);
		} catch (IOException e) {
		}
	}

	public void joinChannel(String c) {
		sendRaw("JOIN " + c);
	}

	public void joinChannel(String c, String k) {
		sendRaw("JOIN " + c + " " + k);
	}

	public void partChannel(String c) {
		sendRaw("PART " + c);
	}

	public void sendMessage(String d, String m) {
		sendRaw("PRIVMSG " + d + " :" + m);
	}

	public void sendAction(String d, String m) {
		sendCTCP(d, "ACTION " + m);
	}

	public void sendCTCP(String d, String m) {
		sendRaw("PRIVMSG " + d + " :" + Character.toString('\001') + m
				+ Character.toString('\001'));
	}

	public void sendCTCPReply(String d, String m) {
		sendRaw("NOTICE " + d + " :" + Character.toString('\001') + m
				+ Character.toString('\001'));
	}

	public void onConnected() {
	}

	public void onNick(String oldNick, String newNick) {
	}

	public void onJoin(String n, String u, String h, String c) {
	}

	public void onJoin(String s, String c) {
	}

	public void onPart(String n, String u, String h, String c, String r) {
	}

	public void onPart(String s, String c, String r) {
	}

	public void onKick(String n, String kn, String u, String h, String c,
			String r) {
	}

	public void onKick(String s, String kn, String c, String r) {
	}

	public void onQuit(String n, String u, String h, String r) {
	}

	public void onCTCP(String n, String u, String h, String d, String m) {
	}

	public void onCTCP(String s, String d, String m) {
	}

	public void onAction(String s, String d, String m) {
	}

	public void onAction(String n, String u, String h, String d, String m) {
	}

	public void onMessage(String n, String u, String h, String d, String m) {
	}

	public void onMessage(String s, String d, String m) {
	}

	public void onNotice(String n, String u, String h, String d, String m) {
	}

	public void onNotice(String s, String d, String m) {
	}

	public String getrawurle() {
		String json = "";
		try {
			URL stat = new URL("http://status.mojang.com/check");
			URLConnection status = stat.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					status.getInputStream(), "UTF-8"));
			String inputLine;
			StringBuilder a = new StringBuilder();
			while ((inputLine = in.readLine()) != null)
				a.append(inputLine);
			in.close();
			json = a.toString();
		} catch (IOException ex) {
			System.err
					.println("Error when trying to retrieve Mojang server status data.");
		}
		Gson gson;
		ArrayList<HashMap<String, String>> jsonObject;
		gson = new Gson();

		Type listType = new TypeToken<ArrayList<HashMap<String, String>>>() {
		}.getType();
		jsonObject = gson.fromJson(json, listType);
		StringBuilder s = new StringBuilder();
		if (jsonObject.get(0).get("minecraft.net").equalsIgnoreCase("green")) {
			// System.out.println("minecraft.net is online.");
		} else {
			// mc webpage is borked
			s.append("mcWeb,");
		}
		if (jsonObject.get(1).get("login.minecraft.net")
				.equalsIgnoreCase("green")) {
			// System.out.println("login.minecraft.net is online.");
		} else {
			// login is borked
			s.append("login,");
		}

		if (jsonObject.get(2).get("session.minecraft.net")
				.equalsIgnoreCase("green")) {
			// System.out.println("session.minecraft.net is online.");
		} else {
			// sessionmc is brok
			s.append("session,");
		}

		if (jsonObject.get(5).get("skins.minecraft.net")
				.equalsIgnoreCase("green")) {
			// System.out.println("skins.minecraft.net is online.");
		} else {
			// skins are borked
			s.append("skins");
		}
		if (s.length() != 0) {
			return "These services are brok; "+s.toString();
		} else {
			return "All services are OK!";
		}
	}

	public void savecmd() {
		try {
			FileOutputStream fos = new FileOutputStream("config/tkCommands.txt");
			ObjectOutputStream ouz = new ObjectOutputStream(fos);
			ouz.writeObject(TkIrc.commands);
			ouz.flush();
			ouz.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public HashMap<String, String> loadcmd() {
		try {
			FileInputStream fis = new FileInputStream("config/tkCommands.txt");
			ObjectInputStream in = new ObjectInputStream(fis);
			HashMap<String, String> input_array = (HashMap<String, String>) in.readObject();
			in.close();
			return input_array;
		} catch (ClassNotFoundException e) {}
		catch(IOException e){
			savecmd();
		}
		return TkIrc.commands;
	}
}