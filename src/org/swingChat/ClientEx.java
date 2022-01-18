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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClientEx extends JFrame implements ActionListener {
	private Socket socket = null;		// 서버에 연결할 Socket
	private BufferedReader br = null;	// 서버가 보낸 메세지를 읽을 입력 스트림
	private BufferedWriter bw = null;	// 서버로 메세지를 전달할 출력 스트림
	private ChatArea chat = null;		// 채팅방 (TextArea를 상속받고 쓰레드로 만들기위해 Runnable 인터페이스를 상속받음
	private JTextField reader = null;	// 메세지를 입력할 곳
	private boolean initflag = false;	// 클라이언트가 서버에 접속하고 userID를 전달했음을 판단할 boolean 변수
	private String userID = null;		// userID

	public ClientEx() {
		setTitle("클라이언트");
		setSize(300, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Container c = getContentPane();
		chat = new ChatArea();
		reader = new JTextField(30);
		reader.addActionListener(this);
		c.setLayout(new BorderLayout());
		c.add(chat, BorderLayout.CENTER);
		c.add(reader, BorderLayout.SOUTH);
		setVisible(true);
		// 서버에 연결하는 메서드 
		connectServer();
		
		// 서버에 연결 후 쓰레드 실행
		Thread th = new Thread(chat);
		th.start();
	}

	public void connectServer() {
		try {
			// localHost ip, 9999 port에 접속
			socket = new Socket(InetAddress.getLocalHost(), 9999);
			
			// 서버에 접속하고 입출력 스트림 생성
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			// 서버에 연결하고 userID를 정하기위해 클라이언트에게 알려준다.
			chat.append("TextField에 userID를 입력해주세요!");
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		// TextFeild에서 Enter를 누르면 메세지를 전달
		if (e.getSource() == reader) {
			// userID를 전달하지 않았다면 첫번째 메세지는 반드시 userID로 서버에 전달하기위한 조건문
			if (!initflag) {
				userID = reader.getText();
				// 이제 채팅을 시작하므로 TextArea 클리어
				chat.setText("");
				// TextField에서 메세지 전달받고 클리어
				reader.setText("");
				try {
					bw.write(userID + "\n");
					bw.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// 초기 설정이 완료됨.
				initflag = true;
			} else {
				// TextField에 입력한 메세지를 받아서
				String msg = reader.getText();
				reader.setText("");
				try {
					// 서버에 메세지 전달 "\"이 빠지면 서버의 InputStream이 읽을 수 없음.
					bw.write(msg + "\n");
					bw.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	// 쓰레드로 동작하는 TextArea
	class ChatArea extends JTextArea implements Runnable {
		@Override
		public void run() {
			while(true) {
				
			try {
				// 서버로부터 메세지를 전달받으면 TextArea에 보여줌.
				String msg = br.readLine();
				chat.append(msg +"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
	}

	public static void main(String[] args) {
		new ClientEx();
	}

}
