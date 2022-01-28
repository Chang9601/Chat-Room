package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private ServerSocket serverSocket; // ���� ����
	private Socket client; // Ŭ���̾�Ʈ ����
	private ClientHandler clientHandler; // Ŭ���̾�Ʈ ó����
	
	public Server(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	public void start() {
		try {
			while(!serverSocket.isClosed()) {
				client = serverSocket.accept(); // Ŭ���̾�Ʈ ���� ���� ���
				System.out.println("[CLIENT" + client.getPort() + "] ���� �Ϸ�");
				clientHandler = new ClientHandler(client); 
				Thread thread = new Thread(clientHandler);
				thread.start(); // Ŭ���̾�Ʈ ������ ����
				
			} 
		} catch (IOException e) {
				System.out.println("start() �޼��� ����� ����: " + e.getMessage());
		} 
	}
	
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("close() �޼��� ����� ����: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) throws IOException {
			ServerSocket serverSocket = new ServerSocket(20000);
			Server server = new Server(serverSocket);
			
			server.start();
			server.close();
	}
}