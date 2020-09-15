package Client;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Vector;

import Server.ChatServer;
import Server.Message;
import javafx.application.Platform;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {
	private static final long serialVersionUID = -9072757850215772326L;
	private static final String HOSTNAME = "ChatServer";
	protected ChatServer obj = null;
	private String name;
	private String clientServiceName;
	private Vector<Integer> knownMessages;
	private ChatClientGUI gui;
	

	protected ChatClientImpl(String name, ChatClientGUI gui) throws RemoteException {
		super();
		this.gui = gui;
		this.name = name;
		try {
			this.clientServiceName = "ClientService_" + URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		knownMessages = new Vector<Integer>();
		this.gui = gui;
	}

	@Override
	public void connect() {
		try {
			Registry r = LocateRegistry.getRegistry("localhost");
			obj = (ChatServer) Naming.lookup(HOSTNAME);
			//TODO: Håndter opptatte brukernavn.
			Naming.rebind(HOSTNAME + "/" + clientServiceName, this);
			requestMessages();
			obj.addClient(this);
		} catch (RemoteException re) {
			re.printStackTrace();
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (NotBoundException nbe) {
			nbe.printStackTrace();
		}
	}

	@Override
	public void disconnect() {
		try {
			obj.removeClient(this);
			Naming.unbind(HOSTNAME + "/" + clientServiceName);
		} catch (RemoteException e) {
			System.err.println("Unable to reach rmiregistry.");
		} catch (MalformedURLException e) {
			System.err.println("Invalid URL!");
		} catch (NotBoundException e) {
			System.err.printf("'%s' is not bound.", clientServiceName);
		}
	}
	
	@Override
	public void requestMessages() {
		try {
			for (Message m : obj.getMessages()) {
				displayMessage(m);
			}
		} catch (RemoteException re) {
			System.err.println("Failed to retreive messages from server!");
		}
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void displayMessage(Message m) throws RemoteException {
		if (knownMessages.contains(m.getId())) return;
		knownMessages.add(m.getId());
		Platform.runLater(new Runnable() {
            @Override public void run() {
            	gui.addMessage(m);
            }
        });		
	}
	
	@Override
	public void submitMessage(String text) {
		Message m = new Message(this, text);
		try {
			obj.publishMessage(m);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void requestClients() throws RemoteException {
		ArrayList<ChatClient> clients = obj.getClients();
		Platform.runLater(new Runnable() {
            @Override public void run() {
            	gui.updateUserList(clients);
            }
        });
	}
}