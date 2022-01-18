package org.swingChat;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ServerEx extends JFrame{
	static HashMap hm = new HashMap();	// userID와 BufferdWriter를 key-value 쌍으로 저장하여 상황에 따라 메세지를 보내줄 수 있도록 함.
	private JTextArea chat = null; 		// 서버의 상황과 메세지를 입력받았을 때 상황을 보여주기 위한 TextArea
	private ConnectClient cc = null;	// 서버가 멈추지않고 동작하기위해 Thread로 만들어줌.

	public ServerEx() {
		setTitle("서버");
		setSize(300, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		Container c = getContentPane();
		chat = new JTextArea();
		c.setLayout(new BorderLayout());
		c.add(new JScrollPane(chat), BorderLayout.CENTER);
		setVisible(true);

		// 서버 쓰레드 동작
		cc = new ConnectClient();
		cc.start();

	}

	public static void main(String[] args) {
		new ServerEx();
	}
	
	// 클라이언트 접속을 대기하는 쓰레드
	class ConnectClient extends Thread {
		private ServerSocket listener = null;
		private Socket socket = null;

		@Override
		public void run() {
			try {
				// 서버는 한번만 생성
				listener = new ServerSocket(9999);
				chat.append("접속대기중...\n");
				while (true) {
					// 접속은 계속 유지해야되므로 wile문 안에서 무한반복
					socket = listener.accept();
					chat.append("클라이언트 접속 완료...\n");
					
					// 클라이언트가 서버에 접속 시 해당 클라이언트와 메세지를 주고 받을 쓰레드 생성
					ServerThread sth = new ServerThread(socket);
					sth.start();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class ServerThread extends Thread {

		Socket socket = null;
		private BufferedReader br = null;
		private BufferedWriter bw = null;
		private String userID;
		
		// 생성자에 socket을 받아 서버에 접속한 클라이언트의 socket정보를 받아옴.
		public ServerThread(Socket socket) {
			this.socket = socket;
			try {
				// 클라이언트와 메세지를 주고받기위해 입출력 스트림 생성
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				
				// 클라이언트의 첫번째 입력은 userID로 정의했기 때문에 첫번째 입력을 userID 변수에 저장.
				userID = br.readLine();
				String msg = userID + " 접속하였습니다!";
				// 클라이언트로부터 입력받은 메세지를 서버의 TextArea에 출력
				chat.append(msg + "\n");
				// 다대다 통신을 위해 HashMap에 userID와 BufferedWriter를 저장해준다.
				hm.put(userID, bw);
				// 클라이언트 접속 정보를 다른 클라이언트에게 전송하기위한 broadCast()메서드 호출
				broadCast(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		// 쓰레드가 시작되면 반복할 코드
		@Override
		public void run() {
			while (true) {
				try {
					// 클라이언트로부터 메세지를 받을때까지 대기하면 받으면 msg 변수에 저장
					String msg = br.readLine();
					
					// 메세지에 /to라는 문구가 있고 그 문구가 메세지의 제일 처음에 있다면 directMessage를 보냄.
					if ((msg.indexOf("/to") != -1) && (msg.indexOf("/to") == 0)) {
						directMessage(msg);
					} else {
						// 어떤 클라이언트가 보낸 메세지인지 알 수 있도록 메세지앞에 userID를 붙여준다.
						msg = userID + " : " + msg;
						// 서버창에도 메세지를 보이게해줌
						chat.append(msg + "\n");
						// DM이 아니라면 전체 클라이언트에게 메세지를 보냄
						broadCast(msg);

					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		
		// 전체 클라이언트에게 메세지 보내는 메서드
		public void broadCast(String msg) {
			// hashmap의 value를 collection에 저장(Iterator를 사용하기 위함)
			Collection collection = hm.values();
			// Iterator로 만들어서 모든 클라이언트의 BufferedWriter를 가져온다.
			Iterator it = collection.iterator();
			while (it.hasNext()) {
				// Iterator의 제네릭에 타입을 안넣어주었으므로 Iterator가 갖고있는 객체(Object type)를 BW타입으로 강제 형변환 해준다.
				BufferedWriter print = (BufferedWriter) it.next();
				try {
					// print는 반복이 되면서 각 클라이언트의 Socket에 메세지를 보낸다.
					print.write(msg + "\n");
					print.flush();
				} catch (IOException e) {
				}
			}
		}

		public void directMessage(String msg) {
			int start, end;
			// 클라이언트가 DM을 발송하면 /to userName 메세지 형식이므로 첫번째 공백을 찾는다.
			start = msg.indexOf(" ");
			// 첫번째 공백 이후로 검사하여 두번째 공백을 찾는다.
			end = msg.indexOf(" ", start + 1);
			
			// 공백을 통해 전달해야할 클라이언트의 userID를 찾음.
			String user = msg.substring(start + 1, end);
			
			// 메세지 내용만 추출
			String message = msg.substring(end + 1);
			// 누가 누구한테 보내는지 명확하게 하기위해 format을 지정하고 서버 TextArea에 띄움.
			chat.append(userID + " -> " + user + " : " + message + "\n");
			
			
			if (hm.get(user) != null) {
				// 누가 보낸 메세지인지 알려주기위해 보낸 클라이언트의 메세지에 userID 추가
				message = userID + " : " + message;
				// 모두가 아닌 특정 클라이언트에게 보내기위해 HashMap.get()사용해서
				// 전달해줄 userID를 갖는 클라이언트의 BufferedWriter를 찾음.
				BufferedWriter print = (BufferedWriter) hm.get(user);
				try {
					// 메세지를 보낸 클라이언트도 메세지를 확인할 수 있도록 보낸 클라이언트에게도 메세지 전송
					// 메세지를 보낸 클라이언트의 BW는 현재 갖고있으므로 찾을 필요가 없다.
					bw.write(message + "\n");
					bw.flush();
					// 메세지를 받을 클라이언트에게만 메세지를 전달.
					print.write(message + "\n");
					print.flush();
				} catch (IOException e) {

				}

			// userID가 일치하는 클라이언트가 없다면 메세지를 보낸 클라이언트에게 해당 클라이언트를 찾을 수 없다는 메세지 전송
			}else {
				message = "not found "+user;
				chat.append(message + "\n");
				try {
					bw.write(message + "\n");
					bw.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
