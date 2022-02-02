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
	private Socket socket; // 소켓
	private BufferedReader clientBr; // 서버 출력 스트림과 연결되는 클라이언트의 입력 버퍼
	private BufferedWriter clientBw; // 서버 입력 스트림과 연결되는 클라이언트의 출력 버퍼
	private BufferedReader inputBr; // 표준 입력 스트림과 연결되는 클라이언트의 입력 버퍼
	private String username; // 클라이언트 이름
	
	public Client(Socket socket, String username) {
			try {
				this.socket = socket;
				this.username = username;
				clientBr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				clientBw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				inputBr = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("아이디: " + username + "(CLIENT" + socket.getLocalPort() + ")");
			} catch (IOException e) {
				System.out.println("Client(Socket socket) 입출력 오류: " + e.getMessage());
				closeAll();
			}
	}
	
	// 클라이언트 메시지 쓰기 스레드
	public void writeMsg() {
		try {
			clientBw.write(username + "\n"); // 개행문자로 끝 표시
			clientBw.flush();
			
			while(socket.isConnected()) {
				String msg = inputBr.readLine(); // 클라이언트 메시지
				clientBw.write("[" + username +"]: " +msg + "\n"); // 개행문자로 끝 표시
				clientBw.flush(); // 버퍼 방출
			}
		} catch (IOException e) {
			System.out.println("writeMsg() 입출력 오류: " + e.getMessage());	
			closeAll();		
		}
	}
	
	// 클라이언트 메시지 읽기 스레드
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
	
	// 자원 해제
	private void closeAll() {
			try {
				inputBr.close();
				socket.close();
				clientBr.close();
				clientBw.close();
			} catch (IOException e) {
			}
	}

	// 유효한 포트 범위 판별
	private static boolean isPortInBound(String strPort) {
		int port = Integer.valueOf(strPort);
		return port >= 1024 && port <= 65535;
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		Scanner scanner = new Scanner(System.in);
		int port = -1;
		String username;
		
		while(true) {
			System.out.print("서버 포트를 입력하세요(1024 - 65535): ");
			String strPort = scanner.nextLine();
			
			if(strPort.matches("^\\d{4,5}$") && isPortInBound(strPort)) {
				port = Integer.valueOf(strPort);
				break;
			}				
		}
		
		Socket socket = new Socket("localhost", port);
		
		System.out.print("아이디를 입력하세요: ");
		username = scanner.nextLine();
		
		Client client = new Client(socket, username);
		client.readMsg();
		client.writeMsg();
		
		client.closeAll();
		scanner.close();
	}
}