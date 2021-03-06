package evebot;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Random;

public class MinerBot {

	static final int SCREEN_BUFFER_X = 1920;
	static final int SCREEN_BUFFER_Y = 0;
	static final int BOT_DELAY = 100;
	static final int MENU_HEIGHT = 15;
	static final int TARGET_WIDTH = 110;
	static final double OVERVIEW_HEIGHT = 18.75;
	static final int NUM_BOOKMARKS = 20;
	static final int LAG_DELAY = 3;
	
	static final int MOUSE1 = InputEvent.BUTTON1_DOWN_MASK;
	static final int MOUSE2 = InputEvent.BUTTON3_DOWN_MASK;
	//First target location (491, 110)
	//Second target Location (603, 111)
	//Third 				(711, 111)
	//Fourth				(823, 108)
	//Fifth					(934, 110)
	//System options location (61, 111)
	//Undock Location (1851, 202)
	//Overview Start (1675,212)
	//Orbit Position (1603, 118)
	
	Robot bot;
	Random randomizer;
	
	public MinerBot() throws AWTException
	{
		bot = new Robot();
		randomizer = new Random();
		bot.setAutoDelay(100 + randomizer.nextInt(300));
		
	}
	
	public void clickSystem()
	{
		click(61,111);
	}
	
	public void clickBookmark(int n)
	{
		sop("Warping to bookmark " + n);
		int y = 111 + (n + 6) * MENU_HEIGHT;
		clickSystem();
		move(100, y);
		bot.delay(200);
		click(250, y);
	}
	
	public void lock(int n)
	{
		int y = getOVPos(n);
		bot.keyPress(KeyEvent.VK_CONTROL);
		click(1675, y);
		bot.keyRelease(KeyEvent.VK_CONTROL);
	}
	
	public void unlock(int n)
	{
		int x = 491 + (n - 1) * 110;
		bot.keyPress(KeyEvent.VK_CONTROL);
		bot.keyPress(KeyEvent.VK_SHIFT);
		click(x, 110);
		bot.keyRelease(KeyEvent.VK_CONTROL);
		bot.keyRelease(KeyEvent.VK_SHIFT);

	}
	
	public void selectTarget(int n)
	{
		int x = 491 + (n - 1) * 110;
		click(x, 110);
	}
	
	public void deactivateMods(int target)
	{
		int x = 491 + (target - 1) * 110;
		click(x - 5, 203);
		click(x + 5, 203);
		
	}
	
	public void pressKey(int key)
	{
		bot.keyPress(key);
		bot.keyRelease(key);
	}
	
	public void orbit(int n)
	{
		bot.keyPress(KeyEvent.VK_W);
		selectTarget(n);
		bot.keyRelease(KeyEvent.VK_W);
	}
	
	public void approach(int n)
	{
		bot.keyPress(KeyEvent.VK_Q);
		selectTarget(n);
		bot.keyRelease(KeyEvent.VK_Q);
	}
	
	public void clickDock()
	{	
		click(972, 686);

		pressKey(KeyEvent.VK_F2);			
		pressKey(KeyEvent.VK_F1);

		int y = 111 + 5 * MENU_HEIGHT;
		clickSystem();
		clickSystem();
		bot.delay(300);
		move(100, y);
		bot.delay(300);
		move(250, y);
		bot.delay(300);
		click(600, y + 3 * MENU_HEIGHT);
	}
	
	public void clickUndock()
	{
		click(1851, 202);
	}
	
	public void moveOre()
	{
		click(215, 848, MOUSE2);
		click(236, 855);
		clickAndDrag(258, 894, 587, 414);
	}
	
	public void stackAll()
	{
		click(428, 110, MOUSE2);
		click(468, 161);
	}
	
	public void clickAndDrag(int x1, int y1, int x2, int y2)
	{
		move(x1,y1);
		bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		move(x2,y2);
		bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}
	
	public void click(int x, int y)
	{
		click(x,y,MOUSE1);
	}
	
	public void click(int x, int y, int button)
	{
		move(x,y);
		bot.setAutoDelay(120);
		bot.mousePress(button);
		bot.mouseRelease(button);
	}
	
	public void move(int x, int y)
	{
		bot.setAutoDelay(200 + randomizer.nextInt(300));
		bot.mouseMove(x + SCREEN_BUFFER_X, y + SCREEN_BUFFER_Y);
	}

	public int getOVPos(int n)
	{
		return (int) (212 + (n - 1) * OVERVIEW_HEIGHT);		
	}
	
	public void launchDrones()
	{
		click(1669, 588, MOUSE2);
		click(1700, 598);
	}
	
	public void engageDrones()
	{
		click(1669, 628, MOUSE2);
		click(1700, 715);
	}
	
	public void mineTick() throws InterruptedException
	{
		click(972, 686);
		
		for(int i = 7; i >= 1; i--)
		{
			deactivateMods(i); //turn off all mods without knowing their state.  
			unlock(i);
		}
		
		lock(2);
		lock(3);
		lock(4);
		lock(1);
		
		bot.delay(3000);
		
		selectTarget(1);
		pressKey(KeyEvent.VK_F1);
		
		selectTarget(2);
		pressKey(KeyEvent.VK_F2);

		orbit(3);
		engageDrones();
	}
	
	public final static void sop(String s)
	{
		System.out.println(s);
	}
	
	public static void waitMessage(int n, int r, String s) throws InterruptedException
	{
		Random rand = new Random();
		int add =  + rand.nextInt(r);
		n = n + LAG_DELAY;
		
		Toolkit.getDefaultToolkit().beep();
		
		for(int i = 1; i <= n + add; i++)
		{
			Thread.sleep(1000);
	        System.out.print("("+(MouseInfo.getPointerInfo().getLocation().x - 1920)+", "+MouseInfo.getPointerInfo().getLocation().y+"): ");
			sop(s + "... " + i + "/" + (n+add));
			if(i > (n + add - 3))
				Toolkit.getDefaultToolkit().beep();
		}		
	}
	
	public void fillUp(int numCycles) throws InterruptedException
	{
		launchDrones();
		
		mineTick();
		
		//For checking mouse Position
		for (int i = 1; i < numCycles; i++) {
			
			// Should be ~ 30 cycles to fill, but docking sooner reduces impact of mishaps
			waitMessage(40, 40, "Mining (Cycle " + i + " of " + numCycles +")");
			mineTick();
			
//			Thread.sleep(1000);
	        sop("("+(MouseInfo.getPointerInfo().getLocation().x - 1920)+", "+MouseInfo.getPointerInfo().getLocation().y+")");

		}

		bot.keyPress(KeyEvent.VK_CONTROL);
		pressKey(KeyEvent.VK_Z);
		bot.keyRelease(KeyEvent.VK_CONTROL);
		
		bot.setAutoDelay(200);
		
		waitMessage(50, 20, "Mining (Last Cycle)");

	}
	
	public static void waitMessage(int n, String s) throws InterruptedException
	{
		waitMessage(n, 10, s);
	}
	
	public static void runCycle() throws AWTException, InterruptedException
	{
		MinerBot mBot = new MinerBot();
		Random rand = new Random();
		
		mBot.moveOre();
		
		while(true)
		{
			waitMessage(1, "Restarting macro");
			
			mBot.clickUndock();
			
			waitMessage(15, "Undocking");
			
			mBot.clickBookmark(rand.nextInt(NUM_BOOKMARKS - 1) + 1);
			
			mBot.bot.keyPress(KeyEvent.VK_ALT);
			mBot.pressKey(KeyEvent.VK_F1);
			mBot.bot.keyRelease(KeyEvent.VK_ALT);
			
			waitMessage(70, "Warping to belt");
			
			mBot.fillUp(20);
			
			mBot.clickDock();

			waitMessage(70, 20, "Docking with station");
			
			mBot.moveOre();
			
			waitMessage(1, "Random delay");
		}

	}
	
	public static void main(String[] args) throws Exception
	{
		runCycle();
	}

	
}
