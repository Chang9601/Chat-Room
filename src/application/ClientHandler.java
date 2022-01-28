package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

public class ClientHandler implements Runnable {
	
	private static Vector<ClientHandler> clients = new Vector<>();  // Ŭ�󸮾�Ʈ ���� ���, Ŭ���̾�Ʈ ���� ������ ������� 1���� ����
	private Socket client; // Ŭ���̾�Ʈ ����
	private BufferedReader serverBr; // Ŭ���̾�Ʈ ��Ʈ���� ����Ǵ� ������ �Է� ����
	private BufferedWriter serverBw; // Ŭ���̾�Ʈ ��Ʈ���� ����Ǵ� ������ ��� ����
	private static BufferedReader inputBr = new BufferedReader(new InputStreamReader(System.in)); // ǥ�� �Է� ��Ʈ���� ����Ǵ� ������ �Է� ����

	public ClientHandler(Socket client) {
		try {
			this.client = client;
			serverBr = new BufferedReader(new InputStreamReader(client.getInputStream()));
			serverBw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			clients.add(this); // Ŭ���̾�Ʈ ���� ����
			sendMsgToAll("[CLIENT" + client.getPort() + "] ����", false);
		} catch (IOException e) {
			System.out.println("ClientHandler(Socket client) ����� ����: " + e.getMessage());
			closeAll(); 
		}
	}
	
	// �޸� �Ҵ� ���� �� ��� ����
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
	
	// �ڽ��� ������ ��ο��� �޼��� ����(���� �޽���)
	private void sendMsgToAll(String msg, boolean isServer) {
		try {
			for(ClientHandler clienthandler : clients) {
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
		clients.remove(this);
		sendMsgToAll("[CLIENT" + client.getPort() + "] ����", false);
	}
	
	@Override
	public void run() {
		String clientMsg; // Ŭ���̾�Ʈ �޽���

		try {
			while(client.isConnected()) {
				
				new Thread(() -> {
					try {
						String msg = inputBr.readLine(); // ���� �޽���
						sendMsgToAll("[SERVER]: " + msg, true);
					} catch (IOException e) {
						System.out.println("run() ǥ�� �Է� ��Ʈ�� ����� ����: " + e.getMessage());
						closeAll();
					}
				}).start();
				
				clientMsg = serverBr.readLine();
				sendMsgToAll(clientMsg, false);
			}
		}catch (IOException e) {
				System.out.println("run() Ŭ���̾�Ʈ ��Ʈ�� ����� ����: " + e.getMessage());
				closeAll();
		}
	}
}