package project4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ClientHandler implements Runnable {
	
	private static Map<String, ClientHandler> clients = new Hashtable<>(); // Ŭ���̾�Ʈ�� �����ϴ� ���, ���� �ʵ�� 1���� ����
	private Socket client; // Ŭ���̾�Ʈ ����
	private BufferedReader serverBr; // Ŭ���̾�Ʈ ��� ��Ʈ���� ����Ǵ� ������ �Է� ����
	private BufferedWriter serverBw; // Ŭ���̾�Ʈ �Է� ��Ʈ���� ����Ǵ� ������ ��� ����
	private String clientName; // Ŭ���̾�Ʈ ���̵�
	private static BufferedReader inputBr = new BufferedReader(new InputStreamReader(System.in)); // ǥ�� �Է� ��Ʈ���� ����Ǵ� ������ �Է� ����

	public ClientHandler(Socket client) {
		try {
			this.client = client;
			serverBr = new BufferedReader(new InputStreamReader(client.getInputStream()));
			serverBw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			clientName = serverBr.readLine();
			clients.put(clientName, this); // Ŭ���̾�Ʈ ���� ��Ͽ� ����
			sendMsgToAll("[" + clientName + "] ä�ù� ����", false);
		} catch (IOException e) {
			System.out.println("ClientHandler(Socket client) ����� ����: " + e.getMessage());
			closeAll(); 
		}
	}
	
	// �ڿ� ����
	private void closeAll() {
		removeClient();
		try {
			client.close();
			serverBw.close();
			serverBr.close();
		} catch (IOException e) {
			System.out.println("ClientHandler, closeAll() ����� ����: " + e.getMessage());
		}
	}
	
	// �ڽ��� ������ ��ο��� �޼��� ����
	private void sendMsgToAll(String msg, boolean isServer) {
		try {
			Set<Entry<String, ClientHandler>> entrySet = clients.entrySet();
			
			for(Entry<String, ClientHandler> entry : entrySet) {
				ClientHandler clienthandler = entry.getValue();
				
				if(isServer) {
					clienthandler.serverBw.write(msg + "\n"); // ���๮�ڷ� ��ǥ��
					clienthandler.serverBw.flush(); // ���� ����					
				} else if(!isServer && clienthandler.client != this.client) {
					clienthandler.serverBw.write(msg + "\n"); // ���๮�ڷ� ��ǥ��
					clienthandler.serverBw.flush(); // ���� ����
				}				
			}
		} catch (IOException e) {
			System.out.println("sendMsgToAll(String msg) ����� ����: " + e.getMessage());
			closeAll();
		}
	}
	
	// ���� Ŭ���̾�Ʈ ����
	private void removeClient() {
		clients.remove(clientName);
		sendMsgToAll("[" + clientName + "] ä�ù� ����", false);
	}
	
	@Override
	public void run() {
		String clientMsg;

		try {
			while(client.isConnected()) {
				
				// ���� �޽��� ���� ������
				new Thread(() -> {
					try {
						String msg = inputBr.readLine();
						sendMsgToAll("[SERVER]: " + msg, true);
					} catch (IOException e) {
						System.out.println("run() ǥ�� �Է� ��Ʈ�� ����� ����: " + e.getMessage());
						closeAll();
					}
				}).start();
				
				// ���� �޽��� �б� ������
				clientMsg = serverBr.readLine();
				sendMsgToAll(clientMsg, false);
			}
		}catch (IOException e) {
				System.out.println("run() Ŭ���̾�Ʈ ��Ʈ�� ����� ����: " + e.getMessage());
				closeAll();
		}
	}
}