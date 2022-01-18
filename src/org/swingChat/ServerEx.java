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
	static HashMap hm = new HashMap();
	private JTextArea chat = null;
	private JTextField writer = null;
	private ConnectClient cc = null;

	public ServerEx() {
		setTitle("서버");
		setSize(300, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		Container c = getContentPane();
		chat = new JTextArea();
		writer = new JTextField();
		c.setLayout(new BorderLayout());
		c.add(new JScrollPane(chat), BorderLayout.CENTER);
		c.add(writer, BorderLayout.SOUTH);
		setVisible(true);

		// 서버 쓰레드 동작
		cc = new ConnectClient();
		cc.start();

	}

	public static void main(String[] args) {
		new ServerEx();
	}

	class ConnectClient extends Thread {
		private ServerSocket listener = null;
		private Socket socket = null;

		@Override
		public void run() {
			try {
				listener = new ServerSocket(9999);
				chat.append("접속대기중...\n");
				while (true) {
					socket = listener.accept();
					chat.append("클라이언트 접속 완료...\n");

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

		public ServerThread(Socket socket) {
			this.socket = socket;
			try {
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				userID = br.readLine();
				String msg = userID + " 접속하였습니다!";
				chat.append(msg + "\n");
				hm.put(userID, bw);
				broadCast(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			while (true) {
				try {
					String msg = br.readLine();

					if ((msg.indexOf("/to") != -1) && (msg.indexOf("/to") == 0)) {
						directMessage(msg);
					} else {
						msg = userID + " : " + msg;
						chat.append(msg + "\n");
						broadCast(msg);

					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		public void broadCast(String msg) {
			Collection collection = hm.values();
			Iterator it = collection.iterator();
			while (it.hasNext()) {
				BufferedWriter print = (BufferedWriter) it.next();
				try {
					print.write(msg + "\n");
					print.flush();
				} catch (IOException e) {
				}
			}
		}

		public void directMessage(String msg) {
			int start, end;
			start = msg.indexOf(" ");
			end = msg.indexOf(" ", start + 1);

			String user = msg.substring(start + 1, end);

			String message = msg.substring(end + 1);
			chat.append(userID + " -> " + user + " : " + message + "\n");
			
			if (hm.get(user) != null) {
				message = userID + " : " + message;
				BufferedWriter print = (BufferedWriter) hm.get(user);
				try {
					bw.write(message + "\n");
					bw.flush();
					print.write(message + "\n");
					print.flush();
				} catch (IOException e) {

				}

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
