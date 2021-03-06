package khanbot;

import java.util.ArrayList;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

public class Bot extends PircBot
{
	private ArrayList<String> mods;
	String homeChannel;
	int chatDelay;
	String oAuth;
	
	public Bot(String name)
	{
		this.setName(name);
		mods = new ArrayList<String>();
		homeChannel = null;
		chatDelay = 3000;
	}
	
	public void twitchConnect(String password, String channel) throws Exception
	{
		this.connect("irc.twitch.tv", 6667, password);		
		homeChannel = "#" + channel;
		this.joinChannel(homeChannel);
		oAuth = password;
	}
	
	public void sendMsg(String channel, String message)
	{
		try {
		    Thread.sleep(chatDelay);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		sendMessage(channel, message);
	
	}
	
	public void onMessage(String channel, String sender,
            String login, String hostname, String message)
	{
		System.out.println(channel + ": " + sender + ": " + message);
		String words[] = message.split(" ", 3); 
		
		if(message.startsWith("!"))
			processCommand(channel, sender, message, words);

	}
	
	private void processCommand(String channel, String sender, String message, String[] words)
	{
		if(isAdmin(sender))
		{
			adminCommand(words);
		}
		
		if(isMod(sender))
		{
			modCommand(channel, message);
		}
		
		userCommand(channel, sender, message, words);
		
	}
	
	protected void adminCommand(String[] words)
	{
		if(words[0].equals("!join"))
		{
			try {
				BotManager.joinChannel(words[1]);
				System.out.println("Joining channel: " + words[1]);
			} catch(Exception e) {
				System.out.println(e);
			}
		}	
	}
	
	private void modCommand(String channel, String message)
	{
		if (message.equalsIgnoreCase("!modlist")) 
		{
			if(channel.equalsIgnoreCase(homeChannel))
				sendMsg(channel, mods.toString());
			else
				sendMsg(channel, "Command not available outside home channel");
		}
		else if (message.equalsIgnoreCase("!steal")) 
		{
			sendMsg(channel, "!songs list stealsong");
		}	
	}
	
	private void pyramidCommand(String channel, String sender, String[] words)
	{
		String face = words[1];
		int size = Integer.parseInt(words[words.length - 1]);
		System.out.println(sender + " triggered pyramid of '" + face + "' of size " + size);
		if (size > 8)
			sendMsg(channel, "Wow " + sender + " that's like waaaay to big");
		else
		{
			sendMsg(channel, "Pyramid for " + sender + ":");
			for (int i = 1; i <= size; i++)
			{
				String sendFace = "";
				for (int j = 1; j <= i; j++)
				{
					sendFace = sendFace + face + " ";
				}
				sendMsg(channel, sendFace);
				try {
				    Thread.sleep(1);
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
			}
			for (int i = size - 1; i >= 1; i--)
			{
				String sendFace = "";
				for (int j = 1; j <= i; j++)
				{
					sendFace = sendFace + face + " ";
				}
				sendMsg(channel, sendFace);
			}					
		}
	}
	
	private void userCommand(String channel, String sender, String message, String[] words)
	{
		if (message.equalsIgnoreCase("!time")) 
		{
			String time = new java.util.Date().toString();
			sendMsg(channel, sender + ": The time is now " + time);
		}
		else if (message.equalsIgnoreCase("!userlist")) 
		{
			if(channel.equalsIgnoreCase(homeChannel))
				sendMsg(channel, getUserList());
			else
				sendMsg(channel, "Command not available outside home channel");
		}
		else if (message.substring(0,8).equalsIgnoreCase("!pyramid"))
		{
			pyramidCommand(channel, sender, words);
		}
	}

	public void onUserMode(String targetNick,
            String sourceNick,
            String sourceLogin,
            String sourceHostname,
            String mode)
	{
		if(mode.contains("+o"))
		{
			System.out.println(mode);
			String mod = mode.substring(homeChannel.length() + 4);
			mods.add(mod);
			if(mod.equals(super.getNick()))
			{
				System.out.println("Mod status detected, changing chat delay");
				chatDelay = 1500;
			}
			//sendMsg("#khan___", "Mod detected: " + mode.substring(12));
		}
	}
	
	private boolean isMod(String nick)
	{
		for ( String mod : mods )
		{
			if(nick.equals(mod))
			{
				return true;
			}
		}
		return false;
	}
	
	protected boolean isAdmin(String nick)
	{
		return nick.equalsIgnoreCase("khan___");
		
	}
	
	private String getUserList(){
		
	     String list = "";
	
	     User userList[] = this.getUsers(homeChannel);
	
	     for( User user : userList ){
	
               list = list + user.getNick() + ", ";
	
	     }
	
	     return list;
	}
}
