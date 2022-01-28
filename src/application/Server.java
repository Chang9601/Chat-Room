package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private ServerSocket serverSocket; // 서버 소켓
	private Socket client; // 클라이언트 소켓
	private ClientHandler clientHandler; // 클라이언트 처리기
	
	public Server(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	public void start() {
		try {
			while(!serverSocket.isClosed()) {
				client = serverSocket.accept(); // 클라이언트 소켓 연결 대기
				System.out.println("[CLIENT" + client.getPort() + "] 연결 완료");
				clientHandler = new ClientHandler(client); 
				Thread thread = new Thread(clientHandler);
				thread.start(); // 클라이언트 스레드 생성
				
			} 
		} catch (IOException e) {
				System.out.println("start() 메서드 입출력 오류: " + e.getMessage());
		} 
	}
	
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("close() 메서드 입출력 오류: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) throws IOException {
			ServerSocket serverSocket = new ServerSocket(20000);
			Server server = new Server(serverSocket);
			
			server.start();
			server.close();
	}
}