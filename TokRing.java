/*------------------------------------------------------------
Class: tokRing

Description:
This Class implements the  interface to the token ring network.
It monitors the network as follows:
1) Wait for reception from the network over the R-pair of the Cable.
2) If a frame destined for the station write it to the rxBuf buffer.
3) Retransmit the frame on the T-pair pipe (standard output fd 1).
4) If the frame received is the token and txBuf empty, write token 
   to the T-pair in the cable.
5) If the frame received is the token and txBuf not empty, write 
   the frame to the T-pair of the Cable.
6) If the received frame source address is the station's address, 
   write the token on the T-pair of the Cable.
7) If the destination address of a received frame is the station's 
   address, the frame is written to rxBuf.
The standard error can be used to write messages to screen.
-------------------------------------------------------------*/

public class TokRing 
{
	// Some definitions for constructing frames
    private final static char STX = '@';    // Start of the frame - start of Xmission
    private final static char ETX = '~';    // End of the frame - end of Xmission
    public final static char SYN = '^';    // Token - allow Hub object to start xmitting token
    private final int DEST_POS = 1;  // Position of the destination identifier
    private final int SRC_POS = 2;   // Position of the source identifier
    private final int MSG_POS = 3;    // Position of the message
    // 
    public enum Status {FINISH, MSG_TOK, MSG_EMPTY, MSG_RECV, MSG_STN };
    // Buffers for exchanging messages with station and network
    String [] buffers = new String[3]; // three buffers
    private final int RxIndex = 0; // Receive buffer
    private final int TxIndex = 1; // Transmit buffer
    private final int AllFramesIx = 2; // Buffering received frames from network.
	// Some identifiers and references
	Cable stnCable;
	char stationId;
	long threadId;  // Thread identifier
	
	public TokRing(Cable cbl, char stnId, long tid)
	{
		stnCable = cbl;		
		stationId = stnId;
		threadId = tid;
		buffers[RxIndex]="";
		buffers[TxIndex]="";
		buffers[AllFramesIx]="";
	}
	
	/*-------------------------------------------------------------
	Method: xmitMessage
	Parameters: char dest - destination of message 
	            String *msg - message string to send
	Returns: nothing
	Description:
	   Creates a frame an appends it to the end of transmit buffer.
	-------------------------------------------------------------*/
	public void xmitMessage(char dest, String msg)
	{
		String frame = ""+STX+stationId+dest+msg+ETX;
		buffers[TxIndex] = buffers[TxIndex]+frame;  // Append frame to buffer
	}
	
	/*-------------------------------------------------------------
	Method: recvMessage
	Parameters: Frame - for returning frame parts, source, dest, msg
	Returns: Return value from extractMsg which can be
	           MSG_EMPTY - buffer is empty.
	           MSG_RECV - message was found.
	Description:
	    Remove a frame from receive buffer if possible.
	-------------------------------------------------------------*/
	public Status recvMessage(Frame frm)
	{
		return(extractMsg(RxIndex,frm));		
	}
	
	/*-------------------------------------------------------------
	Method: monitorTokenRing
	Parameters: none
	Returns:  MSG_STN - message has been received for the station.
	Description:
	   Monitors the token ring LAN as described at the beginning
	   of this file.

	   The function returns when a message destined for the station
	   is received, i.e. the receive buffer has been updated.  

	   Note that readMsg() blocks when receive twisted pair in 
	   the cable is empty.
	-------------------------------------------------------------*/	
	public void monitorTokenRing() throws InterruptedException
	{
		   Status flag;               // return flag from readMsg()
		   Frame frame = new Frame(); // for getting frame parts.
		   String frameStr;

		   // loop that monitors network
		   // readMsg blocks when receive TwistedPair is empty.
		   // Throws InterruptedException when Station thread is terminated
		   do
		   {
		      flag = readMsg(frame);
		      // Transmitting message
		      if(flag == Status.MSG_TOK) // token was received - note frame is meaningless
		      {  
		         if(extractMsg(TxIndex,frame) == Status.MSG_EMPTY)  // no frames to Xmit
		            stnCable.stationTransmit(""+SYN);
			     else
			    	stnCable.stationTransmit(""+STX+frame.getSource()+frame.getDest()+frame.getMsg()+ETX);
		      }
		      // Message reception 
		      else if(flag == Status.MSG_RECV) 
		      {     // Received a message - msg contains it, source gives id station that sent it
		         if(frame.getSource() == stationId) // frame sent by this station - need to release token
			         frameStr = ""+SYN;
			     else 
			     {
			    	 frameStr = ""+STX+frame.getDest()+frame.getSource()+frame.getMsg()+ETX;
			         if(frame.getDest() == stationId) 
		             { 
			        	buffers[RxIndex] = buffers[RxIndex]+frameStr; // save copy if for this station
 			            flag = Status.MSG_STN;  // To return so that received message can be processed
		             } 
			     }
		         stnCable.stationTransmit(frameStr);
		      }
		      else // fatal or unknown error
		         System.out.printf("Station %c (%d): unknown value returned by readMsg (%d)\n",
		        		           stationId, threadId, flag);

		   } while( flag != Status.MSG_STN);
	}
	/*-------------------------------------------------------------
	Method: readMsg
	Parameters: 
	    Frame - components to frame received.
	Description:
	    Reads one or more frames from the receive twisted pair in the 
	    Cable and stores them in buffer (AllFramesIx).  

	    If frames have been received, call extractMsg() to extract the
	    first message; it returns MSG_TOK if a token is found or
	    MSG_RECV if a message is found (frame parts are stored
	    in object referenced  by frame).  Frames are removed from allFrames by 
	    extractMsg().  Once allFrames is empty, fill it again from the 
	    receive TwistedPair of the station Cable.

	    Thus this function scans the receive TwistedPair for messages until it
	    finds one destined for station process stationId.

	    See extractMsg() for frame format.
	-------------------------------------------------------------*/
	private Status readMsg(Frame frame) throws InterruptedException
	{
		   Status ret;		    // value returned by method
		   Status retRead;      // to store value returned by read and extractMessage methods
		   
		   while(true) // Loop to find a message
		   {
		      if(buffers[AllFramesIx].equals("")) // buffer empty - need to read from the TwistedPair
		      {
		    	  buffers[AllFramesIx] = stnCable.stationReceive();  // Blocks when TwistedPair empty
		      }
			   // Frames have been received
			   // The following lines can be used for debugging
			   //System.out.println("Station " + stationId+ " (" + "+threadId+
			   //                   "): readMessage >" + buffers[AllFramesIx] + "<");
			   retRead = extractMsg(AllFramesIx,frame);   // extracts from AllFrames buffer
			   if(retRead != Status.MSG_EMPTY) // if MSG_EMPTY, no messages were found in the buffer 
			   {
			          ret = retRead;  // is MSG_ACK or MSG_RECV
				      break;
			   }
			   // if we get here, need to read from cable again
		   }
		   return(ret);	
	}
	
	/*------------------------------------------------
	Method: extractMsg

	Parameters:
	    int index 	- gives index of buffer to process
	    Frame frm	- points to object in which frame components are copied

	Description: 
	     Extracts a frame from the buffer referenced by index.
	     The frame is removed and copied to the object referenced
	     by frm. Frames with improper destination id are skipped.

	     Message format: STX D S <message> ETX
	                     SYN - the token
	     D gives the ident. of the destination station. 
	     S gives the ident. of the station that sent the message 
	     <message> - string of characters
	     If STX is missing, print an error and skip the message.
	------------------------------------------------*/	
	private Status extractMsg(int index, Frame frm)
	{
		   char [] array=buffers[index].toCharArray(); // character array for parsing contents
		   int i=0;  // index to scan the array
		   Status retcd = Status.MSG_EMPTY;  // return value 
		   while(true) // find a message for this station
		   {
		      if(i==array.length) // no messages
		      {
		         retcd = Status.MSG_EMPTY;
			     buffers[index] = "";  // empties the frame buffer - to deal with corruption
		         break; // break the loop
		      }
		      else if(array[i] == SYN) // found the token
		      {
		         retcd = Status.MSG_TOK;
		         i++;  // Skip the SYN
		         // move unread frames to the start of the buffer
		         buffers[index] = new String(array,i,array.length-i);
		         break;
		      }
		      else if(array[i] != STX) // found an error - no STX
		      {
				  System.out.printf("stn(%c,%d): no STX: >%s<\n",stationId,threadId,new String(array,i,array.length-i));
		    	  //System.out.println("stn(" + stationId + "," + this.getId() + 
 			      //       "): no STX: >" + new String(array,i,array.length-i) + "<");
 	              while(i != array.length && array[i] != ETX && array[i] != STX ) i++; // skip until end or beginning
 	              if(i != array.length && array[i] == ETX) i++; // Skip the ETX
		      }
		      else // found a message
		      {
		    	  frm.setSource(array[i+SRC_POS]);
		    	  frm.setDest(array[i+DEST_POS]);
		    	  int j = i+MSG_POS;  // save start of message position
			      while(i != array.length && array[i] != ETX ) i++; // Find end of the message
			      if(array[i] == ETX ) frm.setMsg(new String(array, j, i-j));  // Copies message into msg object
			      else frm.setMsg(new String(array, j, i-j+1));  // must be at the end of the string - should be treated as an error
			      // Update frame buffer
			      if(i != array.length && array[i] == ETX) i++; // Skip the ETX
				  if(i < array.length) buffers[index] = new String(array, i, array.length - i); // move unread frames to the start of the String
				  else buffers[index] = "";  // no frames - string is empty.
			      retcd = Status.MSG_RECV;
                  break;
		      }
		   }
		   return(retcd);
	}
}
