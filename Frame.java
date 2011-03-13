// Class: Frame
// Used to exchange frame information between Station and TokRing interface
public class Frame 
{
	private char source;
	private char destination;
	private String message;
	
	public char getSource() {return(source);}
	public char getDest() { return(destination); }
	public String getMsg() { return(message); }
	public void setSource(char src) {source=src;}
	public void setDest(char dest) { destination=dest; }
	public void setMsg(String msg) { message = msg; }
}
