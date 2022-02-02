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
	
	private static Map<String, ClientHandler> clients = new Hashtable<>(); // 클라이언트를 저장하는 목록, 정적 필드로 1개만 생성
	private Socket client; // 클라이언트 소켓
	private BufferedReader serverBr; // 클라이언트 출력 스트림과 연결되는 서버의 입력 버퍼
	private BufferedWriter serverBw; // 클라이언트 입력 스트림과 연결되는 서버의 출력 버퍼
	private String clientName; // 클라이언트 아이디
	private static BufferedReader inputBr = new BufferedReader(new InputStreamReader(System.in)); // 표준 입력 스트림과 연결되는 서버의 입력 버퍼

	public ClientHandler(Socket client) {
		try {
			this.client = client;
			serverBr = new BufferedReader(new InputStreamReader(client.getInputStream()));
			serverBw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			clientName = serverBr.readLine();
			clients.put(clientName, this); // 클라이언트 소켓 목록에 저장
			sendMsgToAll("[" + clientName + "] 채팅방 입장", false);
		} catch (IOException e) {
			System.out.println("ClientHandler(Socket client) 입출력 오류: " + e.getMessage());
			closeAll(); 
		}
	}
	
	// 자원 해제
	private void closeAll() {
		removeClient();
		try {
			client.close();
			serverBw.close();
			serverBr.close();
		} catch (IOException e) {
			System.out.println("ClientHandler, closeAll() 입출력 오류: " + e.getMessage());
		}
	}
	
	// 자신을 제외한 모두에게 메세지 전송
	private void sendMsgToAll(String msg, boolean isServer) {
		try {
			Set<Entry<String, ClientHandler>> entrySet = clients.entrySet();
			
			for(Entry<String, ClientHandler> entry : entrySet) {
				ClientHandler clienthandler = entry.getValue();
				
				if(isServer) {
					clienthandler.serverBw.write(msg + "\n"); // 개행문자로 끝표시
					clienthandler.serverBw.flush(); // 버퍼 방출					
				} else if(!isServer && clienthandler.client != this.client) {
					clienthandler.serverBw.write(msg + "\n"); // 개행문자로 끝표시
					clienthandler.serverBw.flush(); // 버퍼 방출
				}				
			}
		} catch (IOException e) {
			System.out.println("sendMsgToAll(String msg) 입출력 오류: " + e.getMessage());
			closeAll();
		}
	}
	
	// 현재 클라이언트 제거
	private void removeClient() {
		clients.remove(clientName);
		sendMsgToAll("[" + clientName + "] 채팅방 퇴장", false);
	}
	
	@Override
	public void run() {
		String clientMsg;

		try {
			while(client.isConnected()) {
				
				// 서버 메시지 쓰기 스레드
				new Thread(() -> {
					try {
						String msg = inputBr.readLine();
						sendMsgToAll("[SERVER]: " + msg, true);
					} catch (IOException e) {
						System.out.println("run() 표준 입력 스트림 입출력 오류: " + e.getMessage());
						closeAll();
					}
				}).start();
				
				// 서버 메시지 읽기 스레드
				clientMsg = serverBr.readLine();
				sendMsgToAll(clientMsg, false);
			}
		}catch (IOException e) {
				System.out.println("run() 클라이언트 스트림 입출력 오류: " + e.getMessage());
				closeAll();
		}
	}
}