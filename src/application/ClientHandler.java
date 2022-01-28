package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

public class ClientHandler implements Runnable {
	
	private static Vector<ClientHandler> clients = new Vector<>();  // 클라리언트 소켓 목록, 클라이언트 존재 유무와 관계없이 1개만 존개
	private Socket client; // 클라이언트 소켓
	private BufferedReader serverBr; // 클라이언트 스트림과 연결되는 서버의 입력 버퍼
	private BufferedWriter serverBw; // 클라이언트 스트림과 연결되는 서버의 출력 버퍼
	private static BufferedReader inputBr = new BufferedReader(new InputStreamReader(System.in)); // 표준 입력 스트림과 연결되는 서버의 입력 버퍼

	public ClientHandler(Socket client) {
		try {
			this.client = client;
			serverBr = new BufferedReader(new InputStreamReader(client.getInputStream()));
			serverBw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			clients.add(this); // 클라이언트 소켓 저장
			sendMsgToAll("[CLIENT" + client.getPort() + "] 입장", false);
		} catch (IOException e) {
			System.out.println("ClientHandler(Socket client) 입출력 오류: " + e.getMessage());
			closeAll(); 
		}
	}
	
	// 메모리 할당 실패 시 모두 종료
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
	
	// 자신을 제외한 모두에게 메세지 전송(서버 메시지)
	private void sendMsgToAll(String msg, boolean isServer) {
		try {
			for(ClientHandler clienthandler : clients) {
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
		clients.remove(this);
		sendMsgToAll("[CLIENT" + client.getPort() + "] 퇴장", false);
	}
	
	@Override
	public void run() {
		String clientMsg; // 클라이언트 메시지

		try {
			while(client.isConnected()) {
				
				new Thread(() -> {
					try {
						String msg = inputBr.readLine(); // 서버 메시지
						sendMsgToAll("[SERVER]: " + msg, true);
					} catch (IOException e) {
						System.out.println("run() 표준 입력 스트림 입출력 오류: " + e.getMessage());
						closeAll();
					}
				}).start();
				
				clientMsg = serverBr.readLine();
				sendMsgToAll(clientMsg, false);
			}
		}catch (IOException e) {
				System.out.println("run() 클라이언트 스트림 입출력 오류: " + e.getMessage());
				closeAll();
		}
	}
}