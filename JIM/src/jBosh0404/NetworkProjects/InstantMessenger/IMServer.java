/**
 * 
 */
package jBosh0404.NetworkProjects.InstantMessenger;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * An instant messenger server. Accepts incoming connections from instances of the <b>IMClient</b> class and
 * routes <code>Message</code> object traffic between the various clients.
 * @author Joshua
 * 
 *
 */
@SuppressWarnings("serial")
public class IMServer extends JFrame {	
	/*
	 * ***NOTE***
	 * I need to set up a single synchronized method that handles all client/server communications.
	 * It should accept as parameters an object of type Object, which is the data to be sent to the
	 * client, and a String, which tells the method what to cast the Object to. It would have a
	 * decision structure that determines what kind of data is being sent, then alerts the client
	 * to the type of data it should expect to receive by sending it a string that would trigger a
	 * decision structure on the client side to prepare for the appropriate data type. Originally
	 * I planned to do this with chained try/catch blocks, but it is simpler to use the passed string
	 * to determine how to cast the object. I may also have to have a reversed client/server connection
	 * from the server to each client for the purpose of pushing activeUser updates, since I intend for
	 * these updates to happen once a second. I worry that trying to use the synchronized methods for
	 * this purpose will tie up those methods more often than not.
	 * 
	 * Another thing I may need to account for is a possible deadlock situation between the server and
	 * client. If both are attempting to send something to each other, or if both are attempting to read
	 * something from each other, they will just sit there waiting indefinitely, as niether will move on
	 * to performing the opposite function until the current one has resolved. I need to figure out a way
	 * of handling this.
	 * **UPDATE**
	 * After further consideration, I don't think the deadlock will be a problem. I believe that having the
	 * send message and receive message functionalities on separate, concurrent threads will prevent the
	 * deadlock from happening, as each is running independently of the other, so a message should be able
	 * to be sent to and received from the client simultaneously. We'll see what happens when I am finally
	 * able to test this.
	 */
	
	private final String WINDOWS_PATH;
	
	/**JTextArea to display server status messages, including new client connections.  */
	private JTextArea jta;
	/**ServerSocket to accept incoming connection requests from clients. */
	private ServerSocket serverSocket;
	/**String LinkedList containing a list of all users currently registered with the server. */
	private LinkedList<String> registeredUsers;
	/**String LinkedList containing a list of users who are currently connected to the server. */
	private LinkedList<String> activeUsers;
	/**Message LinkedList to hold a list of Message objects pending delivery to their respective client recipients. */
	private LinkedList<Message> outMessages;
	/**String constant for the path to the RegisteredUsers.txt file, which is the source for the <code>registeredUsers</code> linked list.  */
	private final String REG_USERS_PATH;
	/**String constant for the path to the Users directory. */
	private final String USER_FILE_PATH;
	
	/**String for the operating system to account for file system differences. */
	private String OS;
	
	/**Boolean value to indicate OS is Windows. */
	private boolean isWindows;
	//private boolean isMac;
	//private boolean isUnix;
	
	/*METHOD COMPLETE*/
	/**
	 * The default constructor.<br>
	 * Handles the acceptance of all new client connections and generates <code>HandleAClient</code> objects to
	 * handle the send/receive message and active user update push operations for each one.
	 */
	public IMServer() {
		
		OS = System.getProperty("os.name").toLowerCase();
		isWindows = OS.indexOf("win") >= 0;
		//isMac = OS.indexOf("mac") >= 0;
		//isUnix = OS.indexOf("nix") >= 0;
		WINDOWS_PATH = User.WINFILEPATH;
		jta = new JTextArea();
		registeredUsers = new LinkedList<String>();
		activeUsers = new LinkedList<String>();
		outMessages = new LinkedList<Message>();
		
		if (isWindows) {
			
			REG_USERS_PATH = WINDOWS_PATH + "RegisteredUsers.txt";
			USER_FILE_PATH = WINDOWS_PATH + "USER_";
			
		} else { // Modify to allow for various operating systems
			
			REG_USERS_PATH = WINDOWS_PATH + "RegisteredUsers.txt";
			USER_FILE_PATH = WINDOWS_PATH + "USER_";
			
		}
		
		setLayout(new BorderLayout());
		add(new JScrollPane(jta), BorderLayout.CENTER);
		
		setTitle("JIMServer");
		setSize(500,300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
		try {
			populateRegisteredUsers();
			serverSocket = new ServerSocket(8000);
			jta.append("IMServer started at " + new Date() + '\n');
			
			// initialize first client
			int clientNo = 1;
			
			while (true) {
				
				Socket socket = serverSocket.accept();
				jta.append("Starting thread for client " + clientNo + " at " + new Date() + "\n");
				
				// Create object to handle IM communication and start a thread for it
				new Thread(new HandleAClient(socket)).start();
				
				// initialize next client
				clientNo++;
				
			}
			
		} catch(IOException e) {
			System.err.println(e);
		}
		
		try {
			
			serverSocket.close();
			
		} catch(IOException e) {
			System.err.println(e);
		}
		
	}

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------//
	
	/*METHOD COMPLETE*/
	/**
	 * Stores a message object in the global list of outgoing message objects.
	 * @param message The message object to be stored
	 */
	private synchronized void messageReceived(Message message) {
	 
		
		outMessages.add(message); // add the message to the list of outgoing messages
		
	}
	
	/*METHOD COMPLETE*/
	/**
	 * Finds a specific message from the list of messages.
	 * @param id The name of the recipient for the message (<code>Message.receiver</code>)
	 * @return The searched message
	 */
	private Message getOutgoingMessage(String id) {
		
		for (int i = 0; i < outMessages.size(); i++) {
			
			if (outMessages.get(i).getReceiver().equals(id)) {
				
				return outMessages.get(i);
				
			} 
		}
		
		return null; // return an empty message object to satisfy the return requirements of the method, should never be reached
		
	}
	
	/*METHOD COMPLETE*/
	/**
	 * Removes a passed message from the global list of messages.
	 * @param message The message to be removed from the global list
	 */
	private synchronized void clearOldMessage(Message message) {
		// I may want to consider unsynchronizing this method
		if (!outMessages.isEmpty()) {
		
			for (int i = 0; i < outMessages.size(); i++) {
				
				if (outMessages.get(i).getID() == message.getID()) {
					
					outMessages.remove(i);
					return;
					
				}				
			}			
		}		
	}
	
	/*METHOD COMPLETE*/
	/**
	 * Registers a user with the server. Checks to see if the user attempting
	 * to register already exists. If it does not exist, the username is added to
	 * the registered users list and the User object is saved to a file.
	 * @param user The User object to be registered with the server
	 * @return <b>True</b> if user registration is successful<br><b>False</b> if a
	 * user with a matching username already exists
	 */
	private boolean registerUser(User user) {
		
		// Check to see if the user already exists.
		User cUser = loadAUser(user.getUsername());
		
		if (cUser == null) {
		
			registeredUsers.add(user.getUsername());		
			saveRegisteredUsers();
		
			return user.save();
			
		} else {
			
			return false;
			
		}
		
	}
	
	/*METHOD COMPLETE*/
	/**
	 * Populates the global resource <code>usernames</code> with the list of usernames from the "usernames.txt" external file.
	 * Creates the "usernames.txt" file and seeds it with a default value if the file does not already exist.
	 */
	private void populateRegisteredUsers() {
		
		File users;
		
		users = new File(REG_USERS_PATH);
		if (users.exists()) {
			
			Scanner unRetriever; // "un-" represents the usernames list, not the prefix "un-" like in "undo" or "untie"
			
			try {
				
				unRetriever = new Scanner(users); 
				while (unRetriever.hasNext()) {
	
					registeredUsers.add(unRetriever.nextLine());
	
				}
	
				unRetriever.close();
				
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			} 
	
			
		} else {
			
			try {
					
				PrintWriter writer = new PrintWriter(REG_USERS_PATH, "UTF-8");
				writer.println("default");
				writer.close();
				registeredUsers.add("default");
						
				
			} catch (FileNotFoundException e1) {
				
				e1.printStackTrace();
				
			} catch (IOException ex) {
				
				ex.printStackTrace();
				
			}
			
		}
		
	}
	
	/*METHOD COMPLETE*/
	/**
	 * Saves the list of registered users to an external file
	 * @return <b>True</b> if save operation is successful
	 */
	private boolean saveRegisteredUsers() {
		
		try {
			
			File regUsers = new File(REG_USERS_PATH);
			
			// Check to see if the file already exists. If so, delete it to allow for a clean save of the file.
			if (regUsers.exists()) {
				
				regUsers.delete();
				
			}
			
			PrintWriter writer = new PrintWriter(regUsers);
			
			for (int i = 0; i < registeredUsers.size(); i++) {
				
				writer.println(registeredUsers.get(i));
				
			}
			
			writer.close();
			
			// Create a scanner to grab the newly saved user data from the file
			Scanner scanner = new Scanner(regUsers);
			int counter = 0;
			
			while (scanner.hasNext()) {
				
				if (registeredUsers.get(counter).equals(scanner.nextLine())) {
					
					counter++;
					
				}
				
			}
			
			scanner.close();
			
			if (counter == registeredUsers.size()) {
				
				return true;
				
			} else {
				
				return false;
				
			}
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			return false;
			
		}
		
	}
	
	/*METHOD COMPLETE*/
	/**
	 * Verifies the password of the user attempting to log in
	 * @param login The <code>LoginID</code> object received from the client
	 * @return <b>True</b> if the user exists and the login password matches the stored password<br><b>False</b> if the user does
	 * not exist or the login password does not match the stored password
	 */
	public boolean verifyUser(LoginID login) {
		
		User user = loadAUser(login.getUsername());
		
		if (user != null) {
			
			if (user.getPassword().equals(login.getPassword())) {
				
				return true;
				
			} else
				return false;
			
		} else
			return false;
		
	}
	
	/*METHOD COMPLETE*/
	/**
	 * Loads a User object from a file. Helper method for verifying user credentials
	 * @param username The username of the user to be loaded
	 * @return The loaded User object
	 */
	private User loadAUser(String username) {
		
		File userFile = new File(USER_FILE_PATH + username);
		
		if (userFile.exists()) {
			
			try {
				
				Scanner scanner = new Scanner(userFile);
				String[] userData = scanner.next().split(User.X.toString());
				scanner.close();
				
				return new User(userData[0], userData[1], userData[2], userData[3], userData[4], Integer.parseInt(userData[5]));
				
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
				
			}
			
		}
		
		return null;
		
	}

	/*METHOD COMPLETE*/
	/**
	 * The main method. Creates a new IMServer.
	 * @param args
	 */
	public static void main(String[] args) {
		
		new IMServer();

	}
	
//-----------------------------------------------------------------------------------------------------------------------------------------------------------------//
	
	/**	
	 * Inner Class <b>HandleAClient</b> <br> Handles login and user registration requests from each client
	 * and creates the objects <b>ReadAMessage</b>, <b>WriteAMessage</b>, and <b>UpdateActiveUsers</b> to handle incoming messages,
	 * outgoing messages, and to push active user updates to the client, respectively.
	 */
	class HandleAClient implements Runnable {
		
		private Socket socket;
		private User user;
		private LoginID login;
		
		/**
		 * The default constructor.
		 * @param socket The <code>Socket</code> object used to generate the <code>ObjectInputStream</code> and 
		 * <code>ObjectOutputStream</code> objects needed to handle client/server communications
		 */
		public HandleAClient(Socket socket) {
			
			this.socket = socket;
			
		}
		
		/**
		 * Override the Runnable.run() method. Creates the object streams for client/server communication, handles 
		 * user registration and login requests from the client, and starts the processes for receiving/sending messages
		 * and sending active user updates to the client.
		 */
		public void run() {
			
			try {
				// Create the object streams to handle communication between the server and the client
				ObjectInputStream inputFromClient = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream outputToClient = new ObjectOutputStream(socket.getOutputStream());
				
				// Check to see if the client is sending registration information, if so register the user, if not verify login creds
				// send success of either operation to the client
				boolean isRegistering = inputFromClient.readBoolean();
				
				if (isRegistering) {
					
					user = (User) inputFromClient.readObject();
					
					boolean registrationComplete = registerUser(user);
					
					outputToClient.writeBoolean(registrationComplete);
					
					if (registrationComplete) {
					
						activeUsers.add(user.getUsername());
					
						outputToClient.writeObject(activeUsers);
					}
					
				} else {
					
					login = (LoginID) inputFromClient.readObject();
					boolean isRegistered = verifyUser(login);
					
					if (isRegistered) {
						
						activeUsers.add(login.getUsername());
						outputToClient.writeBoolean(isRegistered);
						outputToClient.writeObject(activeUsers);
						startReadWriteUpdateTasks(inputFromClient, outputToClient, socket);
						
						
					} else {
					
						outputToClient.writeBoolean(isRegistered); // isRegistered = false
						socket.close(); // user verification failed, close the socket
						
					}
				}
				
				
				
				
			} catch (SocketException e) {
				
				if (user != null) {
					
					activeUsers.remove(user.getUsername());
					
				}
				
			} catch(IOException e) {
				
				e.printStackTrace();
				
			} catch (ClassNotFoundException e) {
				
				e.printStackTrace();
				
			}
		}
		
		/**
		 * Creates the ReadAMessage, WriteAMessage, and UpdateActiveUsers objects and their respective threads to handle
		 * incoming/outgoing messages and to push active user updates to the client.
		 * @param inputFromClient <code>ObjectInputStream</code> to handle incoming communications from the client
		 * @param outputToClient <code>ObjectOutputStream</code> to handle outgoing communications to the client
		 * @param socket socket object required to create the secondary connection for active user updates
		 */
		private void startReadWriteUpdateTasks(ObjectInputStream inputFromClient, ObjectOutputStream outputToClient, Socket socket) {
			
			ReadAMessage readTask = new ReadAMessage(inputFromClient, outputToClient);
			
			Thread readThread = new Thread(readTask);
			readThread.start();
				
			WriteAMessage writeTask = new WriteAMessage(outputToClient, inputFromClient, login);
				
			Thread writeThread = new Thread(writeTask);
			writeThread.start();
			
			
			
		}
		
	}
	
	/**
	 * Inner Class <b>UpdateActiveUsers</b> <br> Pushes updates to the <code>activeUsers</code> list to the client
	 * via a secondary client/server connection where IMServer is the client and IMClient is the server. 
	 */
	class UpdateActiveUsers implements Runnable {
		
		Socket secondarySocket;
		ObjectOutputStream updater;		
		
		/**
		 * The default constructor.
		 * @param socket The <code>Socket</code> object used to retrieve the IP address of the client to allow
		 * a secondary connection to be established between server and client
		 */
		public UpdateActiveUsers(Socket socket) {
			
			try {
				
				secondarySocket = new Socket(socket.getInetAddress(), 7999);
				
				updater = new ObjectOutputStream(secondarySocket.getOutputStream());
				
			} catch (IOException e) {
				
				
				e.printStackTrace();
				
			}
			
		}
		
		/**
		 * Overrides the Runnable.run() method. Pushes the activeUsers object to the client every 1 second.
		 */
		public void run() {
			
			try {
				
				while (true) {
					
					updater.writeObject(activeUsers);
					Thread.sleep(1000);
					
				}
				
			} catch (IOException e) {
				
				try {
					secondarySocket.close();
				} catch (IOException e1) {
					
					e1.printStackTrace();
				}
				e.printStackTrace();
					
			} catch (InterruptedException e) {
				
				try {
					secondarySocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				e.printStackTrace();
				
			}
				
		}			
		
	}
	
	/**
	 * Inner Class <b>ReadAMessage</b> <br> Handles reading <code>Message</code> objects from the client.
	 */
	class ReadAMessage implements Runnable {
		
		private ObjectInputStream inputFromClient;
		private ObjectOutputStream outputToClient;
		
		/**
		 * The default constructor.
		 * @param ois The <code>ObjectInputStream</code> object to handle incoming messages from the client
		 * @param oos The <code>ObjectOutputStream</code> object to handle boolean responses to the client alerting
		 * it to the (un)successful receipt of a message
		 */
		public ReadAMessage(ObjectInputStream ois, ObjectOutputStream oos) {
			
			inputFromClient = ois;
			outputToClient = oos;
		}
		
		/**
		 * Overrides the Runnable.run() method. Handles the receipt of <code>Message</code> objects from the client and
		 * sends a boolean response indicating the success of the operation.
		 */
		public void run() {
			
			
			try {
				
				while (true) {
				
					Message inMessage = (Message) inputFromClient.readObject();
					messageReceived(inMessage);
					outputToClient.writeBoolean(inMessage.containsText());
				
				}
				
			} catch (ClassNotFoundException e) {
				
				e.printStackTrace();
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			
		}
		
		
		
	}
	
	/**
	 * Inner Class <b>WriteAMessage</b> <br>Handles writing <code>Message</code> objects to the client.
	 */
	class WriteAMessage implements Runnable {
		
		private ObjectOutputStream outputToClient;
		private ObjectInputStream inputFromClient;
		private LoginID login;

		/**
		 * The default constructor.
		 * @param oos The <code>ObjectOutputStream</code> object to handle outgoing messages to the client
		 * @param ois The <code>ObjectInputStream</code> object to receive a boolean value from the client indicating the
		 * (un)successful receipt of a message
		 * @param lid The <code>LoginID</code> object used to identify messages intended for the client
		 */
		public WriteAMessage(ObjectOutputStream oos, ObjectInputStream ois, LoginID lid) {
			
			outputToClient = oos;
			inputFromClient = ois;
			login = lid;
			
		}
		
		/**
		 * Overrides the Runnable.run() method. Handles all outgoing <code>Message</code> object traffic to the client. Receives a
		 * boolean response from the client indicating the success of the operation.
		 */
		public void run() {
			
			try {
				
				while (true) {
				
					Message outMessage = getOutgoingMessage(login.getUsername());
					
					outputToClient.writeObject(outMessage);
					
					boolean sendSuccessful = inputFromClient.readBoolean();
					
					if (sendSuccessful) {
						
						clearOldMessage(outMessage);
						
					}
				
				}
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			
			
			
		}
		
		
	}
	
}