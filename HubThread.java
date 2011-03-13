/*-------------------------------------------------------------------
Class: hubThread

Description: 
   This thread listens on cableToMonitor, to a station thread.  
   When data is received from the station, it is send over cableToForward 
   to an adjacent station.
-------------------------------------------------------------------*/

public class HubThread extends Thread
{
	private Cable cableToMonitor;  // Cable to monitor - read
	private Cable cableToForward;  // Cable to forward data received.

	public HubThread(Cable cblMonitor, Cable cblForward)
	{
		cableToMonitor = cblMonitor;
		cableToForward = cblForward;		
	}
	
	public void run()
	{
	   String frames;       // String received
		  
	   while(true)  // working loop
	   {
		  try
		  {
		      // Get frames
			  frames = cableToMonitor.hubReceive();  // Blocks until received
			  // Forward to other cable
			  cableToForward.hubTransmit(frames);
		  }
		  catch (InterruptedException ex) {
			  break; 
		  }
		  if(this.isInterrupted())
		  {
			  break;  // break out of loop and terminate the thread.
		  }
	   }
	   System.out.println("Hub thread" + this.getId() + " terminated");
	   System.out.flush();
	}
}
