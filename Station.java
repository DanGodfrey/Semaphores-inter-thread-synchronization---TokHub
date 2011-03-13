/*
 * Class: Station
 * Description: This class implements the station thread that
 * sends messages to another station thread.  The Constructor sets 
 * the station identifier, the identifier of the station to which messages are 
 * sent, and the reference to the array of messages to be sent.
 * 
 * After each message is sent the station thread waits for an 
 * acknowledgement (Ack message).  All communication is done using
 * calls to the TokRing object (interface to the token ring network).
 * When the station thread receives a messages, it responds by returning 
 * an acknowledgement.
 */
public class Station extends Thread
{
	private char identifier;    // This stations identifier
	private char destination;   // identifier for destination of messages
	private String[] messages;  // Sequence of messages to transmit to destination
    // for messages
    private final String ACKNOWLEDGEMENT="Ack";  // acknowledgement message
    private TokRing tokRingInterface;
	
	public Station(char id, char dest, String[] msgs, Cable cbl)
	{
		identifier = id;
		destination = dest;
		messages = msgs;
		tokRingInterface = new TokRing(cbl, id, this.getId());
	}
	/*-------------------------------------------------------------
	Function: run
	Description:
	   In a loop send the messages found in the array "messages". Between
	   the transmission of each message wait for an acknowledgement (note
	   that ackFlag ensures that an acknowledgement has been received
	   before transmitting the next message).
	   When a message is received, print to the screen
	   the message and send an acknowledgement to the source
	   of the message.
	   The loop is broken when an InterruptedException is received 
	   when the thread is cancelled.
	   Note that run() blocks in calls to the TokRing object
	   method monitorTokRing().
	-------------------------------------------------------------*/
	public void run()
	{
		int i=0;                // index for messages[]
		boolean ackFlag = true; // acknowledgement flag
		TokRing.Status flag;    // return flag from readMessage()
		Frame msgRcv = new Frame(); // for receiving messages

		// loop for transmission and reception
	    do 
		{
		   // Reception of messages 
		   flag = tokRingInterface.recvMessage(msgRcv); 
		   if(flag == TokRing.Status.MSG_EMPTY) /* do nothing */;
		   else if(flag == TokRing.Status.MSG_RECV)  // Message received received
		   {
			  if(msgRcv.getMsg().equals(ACKNOWLEDGEMENT))
			  {
			      if(msgRcv.getSource() == destination) // check out the source
			      { 
				     ackFlag = true; 
					 System.out.println("Station " + identifier + " (" + this.getId() + 
					                    "): Received from station " + msgRcv.getSource() + 
					                    " an acknowldegement");
			      } 
				  else System.out.println("Station " + identifier + " (" + this.getId() + 
				                          "): Received an Ack from " + msgRcv.getSource() + 
				                          "ignored");					  
			  }
			  else 
			  {     // Received a message - msgRcv contains it, msgRcv.getSource gives id station that sent it
				   System.out.println("Station " + identifier + " (" + this.getId() + 
	                                  "): Received from station " + msgRcv.getSource() + 
	                                  " >" + msgRcv.getMsg() + "<");
				   tokRingInterface.xmitMessage(msgRcv.getSource(), ACKNOWLEDGEMENT);
			  }				  
		   }
		   else // fatal or unknown error
			     System.out.printf("Station %c (%d): unknown value returned by recvMessage (%d)\n",
			    		            identifier,getId(),flag);

		   // Transmission of messages 
		   if(ackFlag && (messages[i] != null))
		   {  // Send message
		      tokRingInterface.xmitMessage(destination, messages[i]);
			  System.out.println("Station " + identifier + " (" + this.getId() + 
					             "): Sent to station " + destination + " >" + messages[i] + "<");
			  ackFlag = false; // becomes true at arrival of an ACK
			  i++;             // points to next message for next time
		   }
		   try
		   {
		     tokRingInterface.monitorTokenRing();
		   }
	       catch (InterruptedException ex) { break; }
	       if(this.isInterrupted()) break;  // have been interrupted - break out of loop and terminate
		} while(true);
	    System.out.println("Station " + identifier + " terminated");
	    System.out.flush();
	}
}
