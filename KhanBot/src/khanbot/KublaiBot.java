package khanbot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

public class KublaiBot extends PircBot
{
	private ArrayList<String> mods;
	String homeChannel;
	int chatDelay;
	String oAuth;
	boolean canSpeak;
	HashMap<String, ArrayList<String>> tempLogs;
	HashMap<String, ArrayList<String>> fullLogs;
	String filename;
	int messageCount;
	static final String STRING_TERMINATE = "<<TERM>>";
	static final String STRING_STARTER = "<<START>>";
	
	@SuppressWarnings("unchecked")
	public KublaiBot(String name)
	{
		this.setName(name);
		mods = new ArrayList<String>();
		homeChannel = null;
		chatDelay = 3000;
		filename = "defaulfile.ser";
		messageCount = 0;
		canSpeak = false;
		tempLogs = new HashMap<String, ArrayList<String>>();
		fullLogs = readLogs();
		if (fullLogs == null)
		{
			fullLogs = new HashMap<String, ArrayList<String>>();
		}
		
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
		String words[] = message.split(" "); 
		
		if(message.startsWith("!"))
		{
			processCommand(channel, sender, message, words);
		}
		else if(!message.contains("http://"))
		{
			//System.out.println(sender + ": " + message);
			logMessage(words);
		}
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
	
	@SuppressWarnings("unchecked")
	protected void adminCommand(String[] words)
	{
		if(words[0].equals("!join"))
		{
			try {
				BotManager.activateSmartBot(words[1]);
				System.out.println("Joining channel: " + words[1]);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		else if(words[0].equalsIgnoreCase("!savechatlogs"))
		{
			saveLogs();
		}
		else if(words[0].equalsIgnoreCase("!dumplog"))
		{	
			printLogs(readLogs());
		}
		else if(words[0].equalsIgnoreCase("!speak"))
		{
			canSpeak = true;
			sendMsg(homeChannel, "Hi!");
		}
		else if(words[0].equalsIgnoreCase("!quiet"))
		{
			canSpeak = false;
			sendMsg(homeChannel, "/me enters lurk mode");
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
		else if (words[0].equalsIgnoreCase("!pyramid"))
		{
			pyramidCommand(channel, sender, words);
		}
	}

	private void logMessage(String[] words)
	{
		messageCount++;
		if(words.length > 50 || words.length < 1)
		{
//			Handle copy past-uh message
			System.out.println("Message too long or too short.");
			return;
		}
		
		if(!tempLogs.containsKey(STRING_STARTER))
			tempLogs.put(STRING_STARTER, new ArrayList<String>());
		tempLogs.get(STRING_STARTER).add(words[0]);
		
		
		
		for(int i = 0; i < words.length - 1; i++)
		{
			if(tempLogs.containsKey(words[i]))
			{
				tempLogs.get(words[i]).add(words[i+1]);
			}
			else
			{
				ArrayList<String> list = new ArrayList<String>();
				list.add(words[i+1]);
				tempLogs.put(words[i], list);
			}
		}
		
		if(tempLogs.containsKey(words[words.length - 1]))
		{
			ArrayList<String> list = tempLogs.get(words[words.length - 1]);
			list.add(STRING_TERMINATE);
		}
		else
		{
			ArrayList<String> list = new ArrayList<String>();
			list.add(STRING_TERMINATE);
			tempLogs.put(words[words.length - 1], list);
		}
		
		if(messageCount > 20)
		{
			saveLogs();
			messageCount = 0;
		}
		
	}
	
	public void printLogs(HashMap<String, ArrayList<String>> log)
	{
		System.out.println("Printing logs: " + log);
		for(String key : log.keySet())
		{
			System.out.println(key + ": " + log.get(key));
		}
	}
	
	@SuppressWarnings("unchecked")
	public void saveLogs()
    {
		System.out.println("Saving logs....");
		
		fullLogs = readLogs();
		if (fullLogs == null)
		{
			fullLogs = new HashMap<String, ArrayList<String>>();
			fullLogs.put(STRING_STARTER, new ArrayList<String>());			
		}
		
		for(String key : tempLogs.keySet())
		{
			if(fullLogs.containsKey(key))
			{
				//System.out.println(key);
				fullLogs.get(key).addAll(tempLogs.get(key));
			}
			else
			{
				fullLogs.put(key, tempLogs.get(key));
			}
		}
		try
		{
	           FileOutputStream fos =
	              new FileOutputStream(filename, false);
	           ObjectOutputStream oos = new ObjectOutputStream(fos);
	           oos.writeObject(fullLogs);
	           oos.close();
	           fos.close();
	           System.out.println("Chat data from '" + homeChannel + "' has been saved in " + filename);
	    }
		catch(IOException ioe)
	    {
			ioe.printStackTrace();
	    }
		tempLogs.clear();
		
    }	
	
	@SuppressWarnings({ "rawtypes" })
	public HashMap readLogs()
	{
		System.out.println("Attempting to read " + filename);
		HashMap temp = null;
		try
		{
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			temp = (HashMap) ois.readObject();
			ois.close();
			fis.close();
		}
		catch(IOException ioe)
		{
			//ioe.printStackTrace();
			System.out.println("No log file found; A new one will be created.");
			
			return null;
		}
		catch(ClassNotFoundException c)
		{
			System.out.println("Class not found");
			c.printStackTrace();
			return null;
		}
		return(temp);
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
