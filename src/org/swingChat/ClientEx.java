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
	private Socket socket = null;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	private ChatArea chat = null;
	private JTextField reader = null;
	private boolean initflag = false;
	private String userID = null;
	private String message = null;

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
		connectServer();

		Thread th = new Thread(chat);
		th.start();
	}

	public void connectServer() {
		try {
			Socket socket = new Socket(InetAddress.getLocalHost(), 9999);

			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

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

		if (e.getSource() == reader) {
			if (!initflag) {
				userID = reader.getText();
				chat.setText("");
				reader.setText("");
				try {
					bw.write(userID + "\n");
					bw.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				initflag = true;
			} else {
				String msg = reader.getText();
				reader.setText("");
				try {
					bw.write(msg + "\n");
					bw.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	class ChatArea extends JTextArea implements Runnable {
		@Override
		public void run() {
			while(true) {
				
			try {
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
