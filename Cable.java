/*
 * Class to simulate an Hub cable
 * rxPair represents the transmission twisted pair for station to send to hub
 * txPair represents the reception twisted pair wire for stations to receive from the hub
 */
public class Cable 
{
	private TwistedPairVer1 txPair;   // For xmitting frames to hub
	private TwistedPairVer1 rxPair;   // For recving frames from hub
	private int cableNum;   // identifier for cable - for logging purposes
	/**
	 * Constructor
	 */
	public Cable(int num)
	{
		txPair = new TwistedPairVer1();
		rxPair = new TwistedPairVer1();
		cableNum = num;
	}

	/*---------------------------------------------
	 * Methods and attributes for transmitting across the txPair
	 -----------------------------------------------*/
	/*
	 * The station transmits across the txPair
	 */
	public void stationTransmit(String frame) throws InterruptedException
	{
		txPair.xmit(frame);
	}
	/*
	 * The hub receives across the txPair
	 */
	public String hubReceive() throws InterruptedException
	{
		return(txPair.recv());
	}

	/*---------------------------------------------
	 * Methods and attributes for transmitting across the rxPair
	 -----------------------------------------------*/
	/*
	 * The station receives across the rxPair
	 */	
	public String stationReceive() throws InterruptedException
	{
		return(rxPair.recv());
	}
	/*
	 * The hub thread transmits across the rxPair
	 */		
	public void hubTransmit(String frames) throws InterruptedException
	{
		rxPair.xmit(frames);
	}
}
