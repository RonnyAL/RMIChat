package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;

import Client.ChatClient;

public interface ChatServer extends Remote {
	boolean addClient(ChatClient c) throws RemoteException;
	void removeClient(ChatClient c) throws RemoteException;
	boolean isReserved(String username) throws RemoteException;
	ArrayList<ChatClient> getClients() throws RemoteException;
	ArrayList<Message> getMessages() throws RemoteException;
	ArrayList<Message> getMessages(int lastReceivedId) throws RemoteException;
	void publishMessage(Message m) throws RemoteException;
}
