
/*
 * Class to simulate twisted pair wires - Version 1
 * Student: 
 * Student Number:
 */
public class TwistedPairVer1
{
	private static int tpNumbers = 1000;
	private int tpId;   // identifier for twisted pair
	//private final int maxBufLen = 60;  // Maximum size of string to represent xmission accross twisted pair
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
		
		//For this version, length is not limited. Therefore the check is omitted 
		
		/*if(msg.length() > (maxBufLen - buf.length()) )
		{  // Print error message - should not ever happen.
			logMsg("Exceeding buffer length");
		}*/
		
		// ---- Critical Section ------------------------------------
		buf = buf+msg;  // appends new frame to the twisted pair
		//-----------------------------------------------------------
		this.notifyAll(); //finished adding frame to buffer. Notify all waiting consumers so that they can proceed. 
	}
	/*
	 * Recving from twisted pair
	 */
	public synchronized String recv() throws InterruptedException
	{
		String msgs;  // for returning string in twisted pair
		
		/*
		  	While the buffer contains the empty string, there is nothing to be received ("consumed")
		  	therefore, the recv method waits (releasing the monitor) until the xmit method ("producer")
		  	sends a notification that something has been added to the buffer (in other words, something 
		  	has been "produced") 
		 */
		
		while (buf == "")
		{	
			try
			{
				wait();
			}
			catch (InterruptedException ex) 
			{
				 this.logMsg("terminated"); //log that an interrupt occurred at this level
				 throw ex; //This needs to be handled further up (in the worker method) so we re-throw the error
			}
		}

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
