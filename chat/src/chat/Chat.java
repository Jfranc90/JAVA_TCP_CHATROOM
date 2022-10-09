package chat;

import java.io.*;
import java.net.*;
import java.util.*;

public class Chat
{
	
	
	private int mainPort;
	private InetAddress mainIP;
	private Map<Integer, Destination> dHosts = new TreeMap<>();
	private int numClients = 0;
	private Server mainServer;
	
	//Constructor for the Chat class
	public Chat(int mainPort) {
		this.mainPort = mainPort;
	}
	
	//requirement 1 --> the help commmand
	private void help(){
        System.out.println("COMMAND MANUAL \n myip --> Displays the IP address of the current process \n"
        		+ "myport --> Displays the port on which the current process is listening for incoming connections \n"
        		+ "connect <destination> <port no> --> Establishes a TCP connection to a specified destination and port \n"
        		+ "list --> Display a numbered list of all connections part the current process \n"
        		+ "terminate <connection id> --> Will terminate the connection listed under the specified number in LIST \n"
        		+ "send <connection id> <message> --> Send a message to the host on the connection id \n"
        		+ "exit --> Close all connnections and terminate the current process \n");
    }
	
	
	//requirement 2 and requirement 3
	//getters
	private int getMainPort() {
		return this.mainPort;
	}
	
	private String getMainIP() {
		return mainIP.getHostAddress();
	}
	
	//requirement 4 --> connection
	//uses arguments given by the user in the command line to establish a client connection with the server
	//Checks issues that may arise while creating the connection such as invalid port numbers, ip addresses, or few arguments.
	private void connect(String[] args) {
		if(args != null && args.length == 3) {
			try {
				//Create IP address object to hold the destination
				InetAddress dAddr = InetAddress.getByName(args[1]);
				//Create variable to hold the port number
				int dPort = Integer.parseInt(args[2]);
				
				//host object to create client server connection
				Destination dHost = new Destination(dAddr,dPort);
				
				if(dHost.startConnection()) {
					dHosts.put(numClients, dHost);
					System.out.println("The connection to peer " + dAddr + " is successfully established;");
				}else {
					System.out.println("Connection failed, please try again....");
				}
			}catch(NumberFormatException e) {
				System.err.println("The given destination port is invalid, please try again...");
			}catch(UnknownHostException e) {
				System.err.println("The given destination address is invalid, please try again...");
			}
		}
	}
	
	//requirement 5 --> list the clients/hosts
	//method lists all current clients connected to the server, or none if there are no client connections
	private void listClients() {
		System.out.println("id:\t IP Address \t Port No.");
		if(dHosts.isEmpty()) {
			System.out.println("No clients are currently here...");
		}else {
			for(Integer id: dHosts.keySet()) {
				Destination dHost = dHosts.get(id);
				System.out.println(id + "\t" + dHost.toString());
			}
		}
		System.out.println();
	}
	
	//requirement 6 --> send a message to a desired client/host
	//function uses the arguements given during commmand line to send a messsage to the
	// appropriate client via connection id
	// watches for any errors that might arise during messagee transaction
	public void send(String[] args) {
		if(args.length > 2){
			try {
				int id = Integer.parseInt(args[1]);
				Destination dHost = dHosts.get(id);
				if(dHost != null) {
					StringBuilder msg = new StringBuilder();
					for(int i = 2; i < args.length; i++) {
						msg.append(args[i]);
						msg.append(" " );
					}
					dHost.sendMsg(msg.toString());
				}else {
					System.out.println("No current connection with that Connection id...");
				}
			}catch(NumberFormatException e) {
				System.err.println("Provided Connection id is invalid, plese try again...");
			}
		}else {
			System.out.println("Command missing argument(s): must be --> send: <connection id> <message>");
		}
	}
	
	//requirement 7 --> terminate a client/host
	//method uses the argument of connection id (given during the command line) to find the client with the appropriate
	//connection id and disconnect them from the Server process
	public void terminate(String[] args) {
		if(args != null) {
			try {
				int id = Integer.parseInt(args[1]);
				if(!dHosts.containsKey(id)) {
					System.out.println("System.err.println(\"Provided Connection id is invalid, plese try again...\");");
					return;
				}
				
				Destination dHost = dHosts.get(id);
				if(dHost.closeConnection())
				{
					System.out.println("Peer " + dHost.getdAddr() + " terminates the connection");
					dHosts.remove(id);
				}
			}catch(NumberFormatException e){
				System.err.println("Provided Connection id is invalid, plese try again...");
			}
		}else {
			System.out.println("Command missing argument(s): must be --> terminate <connection id>");
		}
	}
	
	//method to begin the terminal menu --> will ask the user for their commands (i.e. connect, terminate, list, etc.)
	//method will begin the server (and server thread) as well as the menu for the user to choose their action(s)
	public void beginChat() {
		Scanner scanner = new Scanner(System.in);
		try {

			this.mainIP = InetAddress.getLocalHost();
			this.mainServer = new Server();
			Thread serverThread = new Thread(mainServer);
			serverThread.start();

			while (true) {
				System.out.print("Enter the command :");
				String command = scanner.nextLine();
				if (command != null && command.trim().length() > 0) {
					command = command.trim();
					// common help args..
					if (command.equalsIgnoreCase("help") || command.equalsIgnoreCase("/h")
							|| command.equalsIgnoreCase("-h")) {
						this.help();
					} else if (command.equalsIgnoreCase("myip")) {
						System.out.println(this.getMainIP());
					} else if (command.equalsIgnoreCase("myport")) {
						System.out.println(this.getMainPort());
					} else if (command.startsWith("connect")) {
						String[] commandArg = command.split("\\s+");
						this.connect(commandArg);
					} else if (command.equalsIgnoreCase("list")) {
						this.listClients();
					} else if (command.startsWith("terminate")) {
						String[] args = command.split("\\s+");
						this.terminate(args);
					} else if (command.startsWith("send")) {
						String[] commandArg = command.split("\\s+");
						this.send(commandArg);
					} else if (command.startsWith("exit")) {

						System.out.println("Closing connections...");
						System.out.println("Chat Exited!");
						this.closeAll();
						System.exit(0);
					} else {
						System.out.println("Invalid command, try again!!!");
						System.out.println();
					}
				} else {
					System.out.println("Invalid command, try again!!!");
					System.out.println();
				}

			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} finally {
			if (scanner != null)
				scanner.close();
			this.closeAll();
		}
	}
	
	//Method to completely end the program, server.
	private void closeAll() {
		 for(Integer id : dHosts.keySet()){
	            Destination destinationHost = dHosts.get(id);
	            destinationHost.closeConnection();
	        }
	        dHosts.clear();
	        mainServer.stopServer();;
		
	}

	//Server Class
	public class Server implements Runnable
	{
		BufferedReader in;
	    Socket socket;
	    boolean isStopped = false;
	    List<Client> clientList = new ArrayList<Client>();	    
	    
	    @Override
	    public void run() {
	    	try {
	    		//Create serverSocket object.
	    		ServerSocket serverSocket = new ServerSocket(getMainPort());
	    		System.out.println("Server is up"
	    				+ "\n currently waiting for Client(s)...");
	    		
	    		//While the Server is active, we will accept Client Connections,
	    		while(!this.isStopped) {
	    			try {
	    				this.socket = serverSocket.accept();
	    				this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
	    				System.out.println(this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort() + " :client has connected.");
	    				Client clients = new Client(this.in, this.socket);
	    				//Create a thread for the clients to run concurrently
	    				Thread cThread = new Thread(clients);
	    				cThread.start();
	    				clientList.add(clients);
	    			}catch(IOException e) {
	    				System.err.println(e.getStackTrace());
	    			}
	    		}
	    	}catch(IOException e){
	    		System.err.println(e.getStackTrace());
	    	}
	    }
	    
	    //function to stop the server
	    public void stopServer() {
	    	this.isStopped = !this.isStopped;
	    	for(Client clients: clientList) {
	    		clients.stop();
	    	}
	    	Thread.currentThread().interrupt();
	    }
	}

	//Client class
	public class Client implements Runnable{

		private BufferedReader in;
		private Socket clientSocket;
		private boolean isStopped = false;
		
		private Client(BufferedReader in, Socket clientSocket) {
			this.in = in;
			this.clientSocket = clientSocket;
		}
		
		@Override
		public void run() {
			//While the client conneciton is active, the function will receive messages from other clients
			while(!this.clientSocket.isClosed() && !this.isStopped) {
				String str;
				
				try {
					//Check for an empty message
					str = in.readLine();
					if(str == null) {
						this.stop();
						System.out.println("The Connection was terminated by " + this.clientSocket.getInetAddress().getHostAddress() + ":"
						+ this.clientSocket.getPort());
						return;
					}
					
					System.out.println("Message recieved from " + this.clientSocket.getInetAddress().getHostAddress() + ": " + str);
				}catch(IOException e) {
					System.err.println(e.getStackTrace());
				}
			}
		}
		
		//Function to terminate a client conneciton
		private void stop() {
			if(this.in != null)
                try {
                	this.in.close();
                } catch (IOException e) {
                }

            if(this.clientSocket != null)
                try {
                    this.clientSocket.close();
                } catch (IOException e) {
                }
            this.isStopped = !this.isStopped;
            Thread.currentThread().interrupt();
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException{
		if(args != null && args.length > 0)
		{
			int lPort = Integer.parseInt(args[0]);
			try {
				int port = Integer.parseInt(args[0]);
				Chat chat = new Chat(lPort);
				chat.beginChat();
			}catch(NumberFormatException e) {
				System.err.print("Port Number provided is invalid...");
			}
		}else {
			System.out.println("Missing a port number...");
		}
	}
}
