package project4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	private Socket socket; // ����
	private BufferedReader clientBr; // ���� ��� ��Ʈ���� ����Ǵ� Ŭ���̾�Ʈ�� �Է� ����
	private BufferedWriter clientBw; // ���� �Է� ��Ʈ���� ����Ǵ� Ŭ���̾�Ʈ�� ��� ����
	private BufferedReader inputBr; // ǥ�� �Է� ��Ʈ���� ����Ǵ� Ŭ���̾�Ʈ�� �Է� ����
	private String username; // Ŭ���̾�Ʈ �̸�
	
	public Client(Socket socket, String username) {
			try {
				this.socket = socket;
				this.username = username;
				clientBr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				clientBw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				inputBr = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("���̵�: " + username + "(CLIENT" + socket.getLocalPort() + ")");
			} catch (IOException e) {
				System.out.println("Client(Socket socket) ����� ����: " + e.getMessage());
				closeAll();
			}
	}
	
	// Ŭ���̾�Ʈ �޽��� ���� ������
	public void writeMsg() {
		try {
			clientBw.write(username + "\n"); // ���๮�ڷ� �� ǥ��
			clientBw.flush();
			
			while(socket.isConnected()) {
				String msg = inputBr.readLine(); // Ŭ���̾�Ʈ �޽���
				clientBw.write("[" + username +"]: " +msg + "\n"); // ���๮�ڷ� �� ǥ��
				clientBw.flush(); // ���� ����
			}
		} catch (IOException e) {
			System.out.println("writeMsg() ����� ����: " + e.getMessage());	
			closeAll();		
		}
	}
	
	// Ŭ���̾�Ʈ �޽��� �б� ������
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
	
	// �ڿ� ����
	private void closeAll() {
			try {
				inputBr.close();
				socket.close();
				clientBr.close();
				clientBw.close();
			} catch (IOException e) {
			}
	}

	// ��ȿ�� ��Ʈ ���� �Ǻ�
	private static boolean isPortInBound(String strPort) {
		int port = Integer.valueOf(strPort);
		return port >= 1024 && port <= 65535;
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		Scanner scanner = new Scanner(System.in);
		int port = -1;
		String username;
		
		while(true) {
			System.out.print("���� ��Ʈ�� �Է��ϼ���(1024 - 65535): ");
			String strPort = scanner.nextLine();
			
			if(strPort.matches("^\\d{4,5}$") && isPortInBound(strPort)) {
				port = Integer.valueOf(strPort);
				break;
			}				
		}
		
		Socket socket = new Socket("localhost", port);
		
		System.out.print("���̵� �Է��ϼ���: ");
		username = scanner.nextLine();
		
		Client client = new Client(socket, username);
		client.readMsg();
		client.writeMsg();
		
		client.closeAll();
		scanner.close();
	}
}