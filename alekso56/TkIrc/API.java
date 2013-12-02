package alekso56.TkIrc;

/**
 * @author alekso56
 *
 */
public interface API {
	public boolean isAuthed(String username,String channel);
	
	public void onConnected();
	
	public void onNick(String oldNick, String newNick);
	
	public void onJoin(String nick, String username, String hostmask, String c);
	public void onJoin(String s, String c);
	
	public void onPart(String nick, String username, String hostmask, String c, String r);
	public void onPart(String s, String c, String r);
	
	public void onKick(String nick, String kn, String username, String hostmask, String c,String r);
	public void onKick(String s, String kn, String c, String r);
	
	public void onQuit(String nick, String username, String hostmask, String r);
	
	public void onCTCP(String nick, String username, String hostmask, String channel, String message);
	public void onCTCP(String s, String channel, String message);
	
	public void onAction(String s, String d, String m);
	public void onAction(String n, String u, String h, String d, String m); 
	
	public void onMessage(String nickname, String u, String h, String channel, String message);
	public void onMessage(String s, String d, String m);
	
	public void onNotice(String n, String u, String h, String d, String m);
	public void onNotice(String s, String d, String m);
}
