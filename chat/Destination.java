package chat;

import java.io.*;
import java.net.*;
import java.util.*;

/***This class will handle client connections to a server***/
public class Destination {
	
	private String dAddr;
	private int dPort;
	private Socket connection;
	private PrintWriter out;
	public boolean isConnected;
	
	//Constructor for class Destination
	public Destination(String  dAddr, int dPort) {
		this.dAddr = dAddr;
		this.dPort = dPort;
	}
	
	//Begin a connection between the client and server
	public boolean startConnection() {
		try {
			this.connection = new Socket(this.dAddr, this.dPort);
			this.out = new PrintWriter(this.connection.getOutputStream(), true);
			this.isConnected = true;
		}catch(IOException e) {
			System.out.println("Error Connecting....");
		}
		return this.isConnected;
	}

	//getters and setters for the destination IP address and port
	public String getdAddr() {
		return dAddr;
	}

	public void setdAddr(String  dAddr) {
		this.dAddr = dAddr;
	}

	public int getdPort() {
		return dPort;
	}

	public void setdPort(int dPort) {
		this.dPort = dPort;
	}
	
	//methodd to send a message
	public void sendMsg(String msg) {
		if(this.connection.isConnected()) {
			this.out.println(msg);
		}
	}
	
	//function that closes conneciton and returns a boolean that checks to see if the connection was successfully closed
	public boolean closeConnection() {
		if(this.out != null) {
			this.out.close();
		}
		if(this.connection != null) {
			try {
				this.connection.close();
			} catch (IOException e) {
				System.err.println("Failed to close the connection.");
			}
		}
		this.isConnected = !this.isConnected;
		return this.isConnected;
	}
	
	@Override
	public String toString() {
		return this.dAddr + "\t" + this.dPort;
	}
}
