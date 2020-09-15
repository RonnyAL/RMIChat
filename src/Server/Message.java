package Server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Timestamp;

import Client.ChatClient;

public class Message implements Serializable {
	private static final long serialVersionUID = 5349675319346860674L;
	private int id;
	private Timestamp timestamp;
	private String senderName;

	private String content;
	
	
	public Message(ChatClient sender, String content) {
		try {
			this.senderName = sender.getName();
			this.content = content;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Message (String senderName, String content) {
		this.senderName = senderName;
		this.content = content;
	}

	public String getSender() {
		return senderName;
	}

	public String getContent() {
		return content;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
}