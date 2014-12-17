package khanbot.TPP;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import khanbot.BotManager;

import org.jibble.pircbot.PircBot;

public class TPPBot extends PircBot {

	private final static Logger LOGGER = Logger.getLogger(BotManager.class.getName());
	static final String TEAM_RED = " red";
	static final String TEAM_BLUE = " blue";
	static final int BET_ALLIN = 0;
	static final int BET_NORMAL = 1;
	static final int BET_MIN = 2;
	final String channel;
	final String fileName;
	boolean isBP;  //is Betting Period
	boolean wfb; //waiting for balance
	int tB; //total blue bets
	int tR; //total red bets
	int tBa; //total blue bets ignoring big bets
	int tRa; //total red bets ignoring big bets
	int balance; //current balance
	ArrayList<Integer> lbR; //Stores large bets for red
	ArrayList<Integer> lbB; //Stores large bets for blue
	HashMap<String, TPPUser> userList;
	Set<TPPUser> redTeam;
	Set<TPPUser> blueTeam;
	
	public TPPBot(String name) throws Exception
	{
		this.setName(name);
		channel = "#twitchplayspokemon";
		fileName = "tppbot1.ser";
		tB = 0;
		tR = 0;
		tBa = 0;
		tRa = 0;
		balance = 100;
		lbR = new ArrayList<Integer>();
		lbB = new ArrayList<Integer>();
		
		redTeam = new HashSet<TPPUser>();
		blueTeam = new HashSet<TPPUser>();
		
		LOGGER.addHandler(new FileHandler("TPPLogs.log"));
		LOGGER.log(Level.INFO, "TPPBOT CREATED");

		loadUserList();
		
	}
	
	public void tppConnect(String password) throws Exception
	{
		this.connect("irc.twitch.tv", 6667, password);		
		this.joinChannel("#twitchplayspokemon");
	}

	public void onMessage(String chan, String sender,
            String login, String hostname, String message)
	{
		String words[] = message.split(" "); 
		processMessage(sender, message, words);
	}
	
	private void sop(String p)
	{
		System.out.println(p);
		//LOGGER.log(Level.INFO, p);
	}
	
	public void infoDump()
	{
		sop("======== INFODUMP ========");
		sop("tRa: " + tRa);
		sop("tBa: " + tBa);
		sop("tR: " + tR);
		sop("tB: " + tB);
		sop("lbR: " + lbR);
		sop("lbB: " + lbB);
		sop("balance: " + balance);
	}
	
	private void processMessage(String sender, String message, String[] words)
	{
		if(sender.equalsIgnoreCase("tppinfobot"))
		{
			sop(sender + ": " + message);
			if(message.equalsIgnoreCase("A new match is about to begin!"))
			{
//				Signals the beginning of the betting period

				sop("========= NEW MATCH STARTED =========");
				newMatch();
			}
			else if(message.equalsIgnoreCase("Betting closes in 10 seconds!"))
			{
//				Signals the end of the betting period, this is where we bet.
				Timer timer = new Timer();
				timer.schedule(new FinishTimer(this), 8000);
			}
			else if("Team Blue won the match!".equalsIgnoreCase(message))
			{
				for(TPPUser user : blueTeam)
				{
					user.bets++;
					user.wins++;
				}
				for(TPPUser user : redTeam)
				{
					user.bets++;
				}
				
			}
			else if("Team Blue won the match!".equalsIgnoreCase(message))
			{
				for(TPPUser user : blueTeam)
				{
					user.bets++;
				}
				for(TPPUser user : redTeam)
				{
					user.bets++;
					user.wins++;
				}
			}
		}
		else if(sender.equalsIgnoreCase("tppbankbot"))
		{
			sop(sender + ": " + message);
			if(message.startsWith("@" + this.getName()))
			{
				isBP = true;
				balance = Integer.parseInt(words[words.length - 1]);
				sop("========= BALANCE DETECTED: " + balance + " =========");
			}
			else if(message.startsWith("@"))
			{
				String userName = words[0].substring(1);
				int userBalance = Integer.parseInt(words[words.length - 1]);
				if(userList.containsKey(userName))
				{
					userList.get(userName).balance = userBalance;
				}
				else
				{
					TPPUser newUser = new TPPUser(userName);
					newUser.balance = userBalance;
					userList.put(userName, newUser);
				}
			}
		}
		else if(isBP)
		{
			if(words.length == 3 && words[0].equalsIgnoreCase("!bet"))
			{
				sop(sender + ": " + message);

				if(userList.containsKey(sender));
				{
					TPPUser better = userList.get(sender);
					int bet = Integer.parseInt(words[1]);
					String team = words[2];
					
					if(bet <= better.balance && !blueTeam.contains(better) && !redTeam.contains(better))
					{
						processBet(better, bet, team);
					}
				
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadUserList()
	{
		try{
			FileInputStream fileIn = new FileInputStream(fileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			userList = (HashMap<String, TPPUser>) in.readObject();
			in.close();
			fileIn.close();
		}catch(IOException e){
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	private void saveUserList()
	{
        try{
        	FileOutputStream fout = new FileOutputStream(fileName);
        	ObjectOutputStream out = new ObjectOutputStream(fout);
        	out.writeObject(userList);
        	out.close();
        	fout.close();
        	System.out.println("User List saved in " + fileName);
        }catch(IOException e){
        	e.printStackTrace();
        }

	}
	
	private void newMatch()
	{
		
		blueTeam.clear();
		redTeam.clear();

		saveUserList();
		
		isBP = true;
		tB = 0;
		tR = 0;
		tBa = 0;
		tRa = 0;
		lbR.clear();
		lbB.clear();
		
		try {
		    Thread.sleep((long)((Math.random() * 5000) + 20000));
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		wfb = true;
		sendMessage(channel, "!balance");
	}
	
	public void startBet()
	{
		isBP = false;
		processOdds();
		infoDump();
	}
	
	private void sendMsg(String message)
	{
		sendMessage(channel, message);
		sop("MESSAGE SENT: " + message);
	}
	
	private void processBet(TPPUser better, int bet, String team)
	{
		if(team.equalsIgnoreCase("red"))
		{
			redTeam.add(better);
			tR += bet;
			if(bet <= 1000 && bet > 0) 
			{
				tRa += bet;
			}
			else if (bet > 1000)
			{
				tRa += 1000;
				lbR.add(new Integer(bet));
			}					
		}
		if(team.equalsIgnoreCase("blue"))
		{
			blueTeam.add(better);
			tB += bet;
			if(bet <= 1000 && bet > 0) 
			{
				tBa += bet;
			}
			else if (bet > 1000)
			{
				tBa += 1000;
				lbB.add(new Integer(bet));
			}					
		}
	}
	
	private void processOdds()
	{
		double rOdds = (tRa * 1.0) / (tBa * 1.0);
		double bOdds = (tBa * 1.0) / (tRa * 1.0);
		
		if(!processOddsBasic(rOdds, bOdds))
		{
			processCloseOdds();
		}
	}
	
	private boolean processOddsBasic(double rOdds, double bOdds)
	{
		sop("Odds:");
		sop("RED: " + rOdds);
		sop("BLUE: " + bOdds);
		boolean betMade = false;
		if(rOdds > 2)
		{
			betMade = true;
			if(rOdds > 10)
				makeBet(TEAM_RED, BET_ALLIN);
			else
				makeBet(TEAM_RED, BET_NORMAL);
		}
		else if(bOdds > 2)
		{
			betMade = true;
			if(bOdds > 10)
				makeBet(TEAM_BLUE, BET_ALLIN);
			else
				makeBet(TEAM_BLUE, BET_NORMAL);
		}
		return betMade;
	}
	
	private void processCloseOdds()
	{
		boolean outR = false;
		for(Integer i : lbR)
		{
			if(i.intValue() > tRa)
				outR = true;
		}
		
		boolean outB = false;
		for(Integer i : lbB)
		{
			if(i.intValue() > tBa)
				outB = true;
		}
		
		if(outR && outB)
		{
//			Both sides have been stacked, probably should stay out
			sop("========= ODDS UNPREDICTABLE, NO BET MADE =========");
		}
		else if(outR)
		{
			makeBet(TEAM_BLUE, BET_MIN);
		}
		else if(outB)
		{
			makeBet(TEAM_RED, BET_MIN);			
		}
		else
		{
			balance = 500;
			double rOdds = (tR * 1.5) / (tB * 1.0);
			double bOdds = (tB * 1.5) / (tR * 1.0);
			if(!processOddsBasic(rOdds, bOdds))
			{
				sop("========= ODDS TOO CLOSE TO CALL =========");
			}
		}
	}
	
	private void makeBet(String team, int betFlag)
	{
		if (balance > 1000)
		{
			balance = 1000;
		}
		
		if(betFlag == BET_MIN)
		{
			sop("========= MINIMUM BET PLACED:" + team + " =========");
			if(balance < 200)
				this.sendMsg("!bet " + balance + team);
			else
				this.sendMsg("!bet " + 100 + team);
				
		}
		else if(betFlag == BET_ALLIN || balance < 400)
		{
			sop("========= ALLIN BET PLACED:" + team + " =========");
			this.sendMsg("!bet " + balance + team);
		}
		else
		{
			sop("========= BET PLACED:" + team + " =========");
			this.sendMsg("!bet " + balance / 2 + team);
		}
	}

	public static void main(String[] args) throws Exception
	{
		joinTPP();
	}
	
	public static void joinTPP() throws Exception
	{
		TPPBot bot = new TPPBot("Khan___");
		bot.tppConnect("oauth:8xqea4a2u4x4zzcfhrrpgh86q80l7ot");
		System.out.println("TPPBot Activated.");
	}


}

class FinishTimer extends TimerTask
{
	TPPBot master;
	
	public FinishTimer(TPPBot bot)
	{
		master = bot;
	}
	
	public void run()
	{
		master.startBet();
	}
}



