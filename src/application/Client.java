package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	private Socket socket; // 소켓
	private BufferedReader clientBr; // 서버 스트림과 연결되는 클라이언트의 입력 버퍼
	private BufferedWriter clientBw; // 서버 스트림과 연결되는 클라이언트의 출력 버퍼
	private BufferedReader inputBr; // 표준 입력 스트림과 연결되는 클라이언트의 입력 버퍼
	
	public Client(Socket socket) {
			try {
				this.socket = socket;
				clientBr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				clientBw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				inputBr = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("아이디: CLIENT" + socket.getLocalPort());
			} catch (IOException e) {
				System.out.println("Client(Socket socket) 입출력 오류: " + e.getMessage());
				closeAll();
			}
	}
	
	// 메시지 쓰기 스레드
	public void writeMsg() {
		try {
			while(socket.isConnected()) {
				String msg = inputBr.readLine(); // 클라이언트 메시지
				clientBw.write("[CLIENT" + socket.getLocalPort() + "]: " +msg + "\n"); // 개행문자로 끝 표시
				clientBw.flush(); // 버퍼 방출
			}
		} catch (IOException e) {
			System.out.println("writeMsg() 입출력 오류: " + e.getMessage());	
			closeAll();		
		}
	}
	
	// 메시지  읽기 스레드
	public void readMsg() {
		new Thread(() -> {
			try {
				while(socket.isConnected()) {
					String serverMsg = clientBr.readLine(); // 서버 메시지
					System.out.println(serverMsg);
				} 
			} catch (IOException e) {
				System.out.println("readMsg() 입출력 오류: " + e.getMessage());
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