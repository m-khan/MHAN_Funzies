package khanbot.TPP;

public class TPPUser {

	String userName;
	int bets;
	int wins;
	int balance;
	
	public TPPUser(String userName)
	{
		this.userName = userName;
		bets = 0;
		wins = 0;
		balance = -1;
	}
	
	public String toString()
	{
		return userName + ": " + wins + "/" + bets;
	}
	
}
