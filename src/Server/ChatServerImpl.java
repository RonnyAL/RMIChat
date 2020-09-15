package Server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;

import Client.ChatClient;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {
	private static final long serialVersionUID = 1L;
	private static final String SERVERUSERNAME = "ChatServer";
	
	private ArrayList<ChatClient> clients;
	private ArrayList<Message> messages;
	
	protected ChatServerImpl() throws RemoteException {
		super();
		clients = new ArrayList<ChatClient>();
		messages = new ArrayList<Message>();
	}

	public static void main(String[] args) {
		try {
			LocateRegistry.createRegistry(1099);
			Naming.rebind("ChatServer", new ChatServerImpl());
			System.out.println("Server bound to RMI...\n");
			
		} catch (RemoteException re) {
			System.err.println("Failed to bind server to RMI. Port in use?");
		} catch (MalformedURLException e) {
			System.err.println("Invalid URL!");
		}
	}

	@Override
	public boolean addClient(ChatClient c) throws RemoteException {
		if (isReserved(c.getName())) return false;
		clients.add(c);
		broadcastMessage(c.getName() + " has joined the server.");
		for (ChatClient client : clients) {
			client.requestClients();
		}
		return true;
	}

	@Override
	public void removeClient(ChatClient c) throws RemoteException {
		clients.remove(c);
		broadcastMessage(c.getName() + " has left the server.");
		for (ChatClient client : clients) {
			client.requestClients();
		}
	}

	@Override
	public ArrayList<ChatClient> getClients() throws RemoteException {
		return clients;
	}

	@Override
	public void publishMessage(Message m) throws RemoteException {
		m.setTimestamp(new Timestamp(System.currentTimeMillis()));
		m.setId(messages.size());
		messages.add(m);
		
		for (ChatClient c : clients) {
			c.displayMessage(m);
		}
	}
	
	private void broadcastMessage(String message) {
		Message m = new Message(SERVERUSERNAME, message);
		try {
			publishMessage(m);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<Message> getMessages() throws RemoteException {
		return messages;
	}

	@Override
	public ArrayList<Message> getMessages(int lastReceivedId) throws RemoteException {
		ArrayList<Message> filter = (ArrayList<Message>) messages.clone();
		filter.removeIf(m -> m.getId() < lastReceivedId);
		return filter;
	}

	@Override
	public boolean isReserved(String username) throws RemoteException {
		if (username == SERVERUSERNAME) return true;
		for (ChatClient c : clients) {
			if (c.getName() == username) return true;
		}
		return false;
	}
}
