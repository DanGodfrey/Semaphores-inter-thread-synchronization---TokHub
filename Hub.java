/*------------------------------------------------------------
File: hub.java   (CSI3131 Assignment 2)
Description:  This class creates all threads and defines messages
              to be exchanged between stations.
-------------------------------------------------------------*/
public class Hub 
{
	private static Cable [] cables = new Cable[4];
	private static String [] messagesA = 
	{
		"Hello station C",
		"Received your acknowledgement to first message",
		"Blah Blah Blah",
		"Blah Blah Blah again",
		"Have a good day - see you next time",
		null		
	};
	private static String [] messagesB =
	{
		"Hello station D, it's me station B",
		"Thanks for your acknowldgement",
		"How is the weather out your way?",
		"Sunny and fine out here.",
		"Got to go - see you later",
		null		
	};
	private static String [] messagesC =
	{
		"Message alpha",
		"Message beta",
		"Message gamma",
		"Message delta",
		"Message epsilon",
		null
	};
	private static String [] messagesD =
	{
		"Message a",
		"Message b",
		"Message c",
		"Message d",
		"Message e",
		null
	};
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		   int i;
		   Thread [] threadReferences = new Thread[8];
		   
		   // Creating the stations
		   cables[0] = new Cable(0);
		   threadReferences[4] = createStation('A', 'C', messagesA, cables[0]);
		   cables[1] = new Cable(1);
		   threadReferences[5] = createStation('B', 'D', messagesB, cables[1]);
		   cables[2] = new Cable(2);
		   threadReferences[6] = createStation('C', 'A', messagesC, cables[2]);
		   cables[3] = new Cable(3);
		   threadReferences[7] = createStation('D', 'A', messagesD, cables[3]);

		   // creating threads for the hub
		   for(i = 0 ; i < 3 ; i++)
			   threadReferences[i] = createHubThread(cables[i], cables[i+1]);   // for cable i
		   threadReferences[i] = createHubThread(cables[i],cables[0]); // for cable 3; fourth cable
		   // Start transmitting token
		   try {
			   cables[0].hubTransmit(""+TokRing.SYN); // Start token			   
		   }
		   catch (InterruptedException e) { System.out.println("hubTransmit interrupted");}
		   
		   try { Thread.sleep(5000); } 
		   catch (InterruptedException e) { System.out.println("Sleep interrupted");}
		   
		   // Terminate all threads
		   for(i = 0 ; i < 8 ; i++) 
		   {
			   threadReferences[i].interrupt();
		   }
		   // Wait on all threads
		   try { for(i = 0 ; i < 8 ; i++) threadReferences[i].join(); }
		   catch(InterruptedException e) { }
		   System.out.println("All done");
	}

	/*-------------------------------------------------------------
	Method: createStation
	Parameters:
	    stnId - station identifier
	    dest - identifier of station to which messages are sent
	    messages - messages to send
	    
	Description:
	    Creates and starts a station thread (Station) which acts like a station
	    according to given parameters.
	    Receives a reference to a Cable object for communication 
	    between the station thread and hub threads.
	    Returns the thread id for cancellation.
	-------------------------------------------------------------*/
	private static Thread createStation(char stnId, char dest, String [] messages, Cable cbl)
	{
		Station stn = new Station(stnId, dest, messages, cbl);
		stn.start();
		return(stn);
	}

	/*--------------------------------------------------------------
	Function: createHubThreads
	Description:
	   Creates and starts a hub thread to listen on cable cableIx.
	--------------------------------------------------------------*/
	private static Thread createHubThread(Cable cableMonitor, Cable cableFwd)
	{
		HubThread ht = new HubThread(cableMonitor, cableFwd);
		ht.start();
		return(ht);
	}
	
	

}
