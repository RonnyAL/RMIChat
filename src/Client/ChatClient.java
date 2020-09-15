package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Server.Message;

public interface ChatClient extends Remote {
	void requestMessages() throws RemoteException;
	void requestClients() throws RemoteException;
	void displayMessage(Message m) throws RemoteException;
	String getName() throws RemoteException;
	void connect() throws RemoteException;
	void disconnect() throws RemoteException;
	void submitMessage(String text) throws RemoteException;
}
