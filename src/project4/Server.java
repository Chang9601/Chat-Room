package project4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static int port = 20000; // ���� ��Ʈ ��ȣ
	private ServerSocket serverSocket; // ���� ����
	private Socket client; // Ŭ���̾�Ʈ ����
	private ClientHandler clientHandler; // Ŭ���̾�Ʈ ó����
	
	public Server(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	// ���� ���� ����
	private void start() {
		try {
			while(!serverSocket.isClosed()) {
				client = serverSocket.accept(); // Ŭ���̾�Ʈ ���� ���� ���
				System.out.println("[CLIENT" + client.getPort() + "] ���� �Ϸ�");
				clientHandler = new ClientHandler(client); 
				Thread thread = new Thread(clientHandler); // Ŭ���̾�Ʈ ������ ����
				thread.start(); // Ŭ���̾�Ʈ ������ ����
			} 
		} catch (IOException e) {
				System.out.println("start() �޼��� ����� ����: " + e.getMessage());
		} 
	}
	
	// ���� ���� ����
	private void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("close() �޼��� ����� ����: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) throws IOException {
			ServerSocket serverSocket = new ServerSocket(port);
			Server server = new Server(serverSocket);

			server.start();
			server.close();
	}
}