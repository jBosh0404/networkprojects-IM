/**
 * 
 */
package jBosh0404.NetworkProjects.InstantMessenger;

import java.io.Serializable;
/**
 * Class to encapsulate login information including username and password. Methods and fields are self-explanantory.
 * @author Joshua
 *
 */
@SuppressWarnings("serial")
public class LoginID implements Serializable {
	
	private String username;
	private String password;
	
	public LoginID(String username, String password) {
		
		this.username = username;
		this.password = password;
		
	}
	
	public String getUsername() {
		
		return username;
		
	}
	
	public String getPassword() {
		
		return password;
		
	}

}
