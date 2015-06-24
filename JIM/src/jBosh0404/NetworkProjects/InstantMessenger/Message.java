/**
 * 
 */
package jBosh0404.NetworkProjects.InstantMessenger;

import java.io.Serializable;
import java.util.Random;

/**
 * Class to encapsulate the contents and metadata for a message, including the message text, the username of the sender and recipient
 * and a unique identifier.
 * @author Joshua
 *
 */
@SuppressWarnings("serial")
public class Message implements Serializable {
	
	/**The username of the sender of the message */
	private String sender;
	/**The username of the recipient of the message */
	private String receiver;
	/**The text contents of the message */
	private String message;
	/**A unique identifier for the message object */
	private final int ID;
	
	
	/**
	 * The default constructor.
	 * @param sender The username of the sender of the message
	 * @param receiver The username of the recipient of the message
	 * @param message The text contents of the message
	 */
	public Message (String sender, String receiver, String message) {
		
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
		this.ID = new Random().nextInt(1000000);
		
	}
	
	public String getSender() {
		
		return sender;
		
	}
	
	public String getReceiver() {
		
		return receiver;
		
	}
	
	public String getMessage() {
		
		return message;
		
	}
	
	public int getID() {
		
		return ID;
		
	}
	
	/**
	 * Compares a passed message object with the current instance
	 * @param message The message to be compared with the current instance
	 * @return <b>True</b> if the messages are identical, not including the unique identifier 
	 */
	public boolean equals(Message message) {
		
		if (message.sender.equals(this.sender) && message.receiver.equals(this.receiver)
				&& message.message.equals(this.message)) {
			
			return true;
			
		} else {
		
			return false;
			
		}
	}
	
	/**
	 * Compares a passed message object with the current instance
	 * @param message The message to be compared with the current instance
	 * @return <b>True</b> if the messages are identical including the ID
	 */
	public boolean equalsWithID(Message message) {
		
		if (message.sender.equals(this.sender) && message.receiver.equals(this.receiver)
				&& message.message.equals(this.message) && message.ID == this.ID) {
			
			return true;
			
		} else {
		
			return false;
		}
		
	}
	
	/**
	 * 
	 * @return <b>True</b> if the message contains any string characters.
	 */
	public boolean containsText() {
		
		char[] alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()-_=+{}[]|\\:;\"'<,>.?/`~".toCharArray();
		
		for (int i = 0; i < alpha.length; i++) {
			
			if (message.contains("" + alpha[i])) {
				
				return true;
				
			}
			
		}
		
		return false;
		
	}
	
}
