
/*
 * Class to simulate twisted pair wires - Version 1
 * Student: 
 * Student Number:
 */
public class TwistedPairVer1
{
	private static int tpNumbers = 1000;
	private int tpId;   // identifier for twisted pair
	private final int maxBufLen = 60;  // Maximum size of string to represent xmission accross twisted pair
	private String buf;   // String to represent a twisted pair xmission, when empty it references an Empty String ""
	/**
	 * Constructor
	 */
	public TwistedPairVer1()
	{
		tpId = TwistedPairVer1.tpNumbers++;  // Unique identifier
		buf = "";
	}

	/*---------------------------------------------
	 * Methods and attributes for recving/xmitting across the twisted pair
	 -----------------------------------------------*/

	/*
	 * Xmitting across twisted pair
	 */
	public synchronized void xmit(String msg) throws InterruptedException
	{
		// Can we xmit?
		if(msg.length() > (maxBufLen - buf.length()) )
		{  // Print error message - should not ever happen.
			logMsg("Exceeding buffer length");
		}

		// ---- Critical Section ------------------------------------
		buf = buf+msg;  // appends new frame to the twisted pair
		//-----------------------------------------------------------


	}
	/*
	 * Recving from twisted pair
	 */
	public synchronized String recv() throws InterruptedException
	{
		String msgs;  // for returning string in twisted pair
		


		// ---- Critical Section ------------------------------------
		msgs = buf;
		buf = "";
		//-----------------------------------------------------------
		


		return(msgs);
	}
	
	// twisted pair identifier getter
	public int getTwistedPairId()  { return tpId; }
	
	// For logging messages
	
	private void logMsg(String msg)
	{
		System.out.println("TwistedPair ("+tpId+", "+Thread.currentThread().getId()+") "+msg);
		System.out.flush();
	}
}
