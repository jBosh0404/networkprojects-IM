/**
 * 
 */
package jBosh0404.NetworkProjects.InstantMessenger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Scanner;

/**
 * Class to encapsulate the details of a given user.
 * 
 * @author Joshua Messina
 */

@SuppressWarnings("serial")
public class User implements Serializable {

	private String username;
	private String password;
	private String fName;
	private String lName;
	private String bio;
	private int gender;
	
	/**Constant for the file path to the Users directory */
	public static final String WINFILEPATH = System.getProperty("user.home") + "/AppData/Roaming/IMServer/Users/";
	
	private String fileName;
	
	/**Constant used to separate user data included in <code>this.toString()</code> */
	public static final Character X 		= 	'*';
	
	public static final int GENDER_NONE		=	0;
	public static final int GENDER_MALE 	=	1;
	public static final int GENDER_FEMALE	=	2;
	public static final int GENDER_OTHER 	=	3;
	
	/**
	 * Constructor for <code>User</code> with default values for <code>gender</code> and <code>bio</code>.
	 * @param username Unique identifier and display name for the user
	 * @param password Password for the user
	 * @param fName The user's common first name
	 * @param lName The user's common last name
	 */	
	public User(String username, String password, String fName, String lName) {
		
		this.username 	= 	username;
		this.password 	= 	password;
		this.fName 		= 	fName;
		this.lName 		= 	lName;
		this.bio 		= 	"N/A";
		this.gender 	= 	GENDER_NONE;
		this.fileName	=	"USER_" + username;
		
	}
	
	/**
	 * Constructor for <code>User</code> with default value for <code>bio</code>.
	 * @param username Unique identifier and display name for the user
	 * @param password Password for the user
	 * @param fName The user's common first name
	 * @param lName The user's common last name
	 * @param gender The user's gender
	 */
	public User(String username, String password, String fName, String lName, int gender) {
		
		this.username 	= 	username;
		this.password 	= 	password;
		this.fName 		= 	fName;
		this.lName 		= 	lName;
		this.bio 		= 	"N/A";
		this.gender 	= 	gender;
		this.fileName	=	"USER_" + username;
		
	}
	
	/**
	 * Constructor for <code>User</code> with default value for <code>gender</code>.
	 * @param username Unique identifier and display name for the user
	 * @param password Password for the user
	 * @param fName The user's common first name
	 * @param lName The user's common last name
	 * @param bio Biographical information about the user, including interests, personality traits,
	 * and any other details the user would like to provide
	 */
	public User(String username, String password, String fName, String lName, String bio) {
		
		this.username 	= 	username;
		this.password 	= 	password;
		this.fName 		= 	fName;
		this.lName 		= 	lName;
		this.bio 		= 	bio;
		this.gender 	= 	GENDER_NONE;
		this.fileName	=	"USER_" + username;
		
	}
	
	/**
	 * Constructor for <code>User</code> with no default values.
	 * @param username Unique identifier and display name for the user
	 * @param password Password for the user
	 * @param fName The user's common first name
	 * @param lName The user's common last name
	 * @param bio Biographical information about the user, including interests, personality traits,
	 * and any other details the user would like to provide
	 * @param gender The user's gender
	 */
	public User(String username, String password, String fName, String lName, String bio, int gender) {
		
		this.username 	= 	username;
		this.password 	= 	password;
		this.fName 		= 	fName;
		this.lName 		= 	lName;
		this.bio 		= 	bio;
		this.gender 	= 	gender;
		this.fileName	=	"USER_" + username;
		
	}

	public String getUsername() {
		
		return username;
		
	}

	public String getPassword() {
		
		return password;
		
	}

	public String getFName() {
		
		return fName;
		
	}

	public String getLName() {
		
		return lName;
		
	}

	public String getBio() {
		
		return bio;
		
	}
	
	public int getGender() {
		
		return gender;
		
	}
	
	/**
	 * Saves a User object to an external file. Resources are separated by the value of <b>X</b>.
	 * 
	 * @return
	 * <b>True</b> if save successful
	 */
	public boolean save() {
		
		File user = new File(WINFILEPATH + fileName);
		
		if (!user.exists()) {
			
			// Call the helper method to write the data to a file and return the returned boolean value
			// indicating the success of the write operation
			return writeAFile(user);
				
		} else {
			
			// Delete the previous file to allow for a clean save
			user.delete();
			
			// Call the helper method to write the data to a file and return the returned boolean value
			// indicating the success of the write operation
			return writeAFile(user);
			
		}
		
	}
	
	/**
	 * Helper method that writes a User object to a file.
	 * @param file  The file to be written
	 * @return <b>True</b> if write successful
	 */
	private boolean writeAFile(File file) {
		
		// Create a PrintWriter and write the user data to a file
		
		try {
			
		
			PrintWriter writer = new PrintWriter(file);
			writer.print(toString(X));
			writer.close();
			
			// Create a scanner to grab the newly saved user data from the file
			Scanner scanner = new Scanner(file);
			
			String userData = scanner.next();
			scanner.close();
			
			// Compare the String value of the file to the String value of this User object
			if (userData.equals(toString(X))) {
				
				return true;
				
			}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
		// Return false if save operation was not successful. Should be effectively unreachable.
		return false;
		
	}
	
	/**
	 * Converts a User object to a string by concatenating the fields username, password, fName, lName, gender, and bio.
	 * The values of these fields are separated by a passed character value.
	 * @param x The character separating the concatenated strings
	 * @return The String value of the User object
	 */
	public String toString(char x) {
		
		String userString = username + x + password + x + fName + x + lName + x + gender + x + bio;
		
		return userString;
	}
	
}
