package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	private Socket socket; // ����
	private BufferedReader clientBr; // ���� ��Ʈ���� ����Ǵ� Ŭ���̾�Ʈ�� �Է� ����
	private BufferedWriter clientBw; // ���� ��Ʈ���� ����Ǵ� Ŭ���̾�Ʈ�� ��� ����
	private BufferedReader inputBr; // ǥ�� �Է� ��Ʈ���� ����Ǵ� Ŭ���̾�Ʈ�� �Է� ����
	
	public Client(Socket socket) {
			try {
				this.socket = socket;
				clientBr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				clientBw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				inputBr = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("���̵�: CLIENT" + socket.getLocalPort());
			} catch (IOException e) {
				System.out.println("Client(Socket socket) ����� ����: " + e.getMessage());
				closeAll();
			}
	}
	
	// �޽��� ���� ������
	public void writeMsg() {
		try {
			while(socket.isConnected()) {
				String msg = inputBr.readLine(); // Ŭ���̾�Ʈ �޽���
				clientBw.write("[CLIENT" + socket.getLocalPort() + "]: " +msg + "\n"); // ���๮�ڷ� �� ǥ��
				clientBw.flush(); // ���� ����
			}
		} catch (IOException e) {
			System.out.println("writeMsg() ����� ����: " + e.getMessage());	
			closeAll();		
		}
	}
	
	// �޽���  �б� ������
	public void readMsg() {
		new Thread(() -> {
			try {
				while(socket.isConnected()) {
					String serverMsg = clientBr.readLine(); // ���� �޽���
					System.out.println(serverMsg);
				} 
			} catch (IOException e) {
				System.out.println("readMsg() ����� ����: " + e.getMessage());
				closeAll();
			}
		}).start();
	}
	
	private void closeAll() {
			try {
				inputBr.close();
				socket.close();
				clientBr.close();
				clientBw.close();
			} catch (IOException e) {
			}
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket socket = new Socket("localhost", 20000);
		Client client = new Client(socket);
		client.readMsg();
		client.writeMsg();
	}
}