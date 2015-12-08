import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


public class Client implements Runnable {
	
	//ServerSocket serverSocket;
	//Vector<Socekt> socketVector = new Vector<Socket>();
	//String myIP;
	//InetAddress inetAddress;
	Socket socket;
	static Thread mainThread;
	static String loginName;
	String password;
	String userType;
	static DataInputStream din;
	static DataOutputStream dout;
	static String msg;
	private Vector<Chat> chatVector = new Vector<Chat>();
	JFrame mainFrame;
	JFrame failedFrame;
	JPanel panel;
	JLabel headerLabel,statusLabel,groupLabel,clientLabel;
	static DefaultListModel<String> groupsListModel, clientsListModel;
	JList<String> groupList,clientList;
	JButton createBtn,chatBtn, enrollBtn, refreshBtn, logoutBtn, removeBtn;
	JTextField convName;
	JComboBox<String> userStatusDropdown;
	String[] userStatusArray = new String[] {"Online", "Busy", "Away"};
	
	static String peerChat;
	static String userId;
	static String groupChat;
		
	public Client(String ip, String loginName,String password ,String userType) throws UnknownHostException, IOException{
			
		this.loginName = loginName;
		this.userType = userType;
		this.password = password;
		socket = new Socket(ip,8888);
//		serverSocket = new ServerSocket(8888);
//		 Enumeration e1=null;
//		try {
//			e1 = NetworkInterface.getNetworkInterfaces();
//		} catch (SocketException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		 while(e1.hasMoreElements())
//		 {
//		     NetworkInterface n = (NetworkInterface) e1.nextElement();
//		     Enumeration ee = n.getInetAddresses();
//		     while (ee.hasMoreElements())
//		     {
//		         inetAddress = (InetAddress) ee.nextElement();
//		         if(inetAddress.isSiteLocalAddress()){
//		        	System.out.println("IP: "+inetAddress.getHostAddress());
//					myIP =inetAddress.toString()
//					break;
//		         }
//		         
//		     }
//		 }
		setupUI(loginName);
		din = new DataInputStream(socket.getInputStream());
		dout = new DataOutputStream(socket.getOutputStream());
		
		if(din.readUTF().equals("LOGIN:"))
			dout.writeUTF(loginName);
		if(din.readUTF().equals("PASS:")){
			dout.writeUTF(password);
			System.out.println(password);
		}
			
		
		if(din.readUTF().equals("ROLE:"))
			dout.writeUTF(userType);

		mainThread = new Thread(this);
		mainThread.start();
		System.out.println("Thread Started");

		Thread refresherThread = new Thread(new Refresher());
		refresherThread.start();
		System.out.println("Refresher Thread Started");
		refresherThread=null;

//		if(din.readUTF().equals("IP:")){
//			dout.writeUTF(myIP);
//		}

	}
	

	@Override
	public synchronized void run() {
		// TODO Auto-generated method stub
		while(true){
			try {
				String msg = din.readUTF();
				if(msg.equals("DATA")){
					String chatId = din.readUTF();
					String text = din.readUTF();
					for(Chat chat : chatVector){
						if(chat.chatId.equals(chatId))
							chat.setText(text);
					}
				}else if(msg.equals("FAILED")){
					System.out.println("Auth Failed");
					mainFrame.setVisible(false);
					mainFrame.dispose();
					failedFrame = new JFrame("Alert");
					JOptionPane.showMessageDialog(failedFrame, "Wrong Password");
				}
				else if(msg.equals("LOGOUTCONV")){
					String chatId = din.readUTF();
					String loginMsg = din.readUTF();
					for(Chat chat : chatVector){
						if(chat.chatId.equals(chatId))
							chat.setText(loginMsg);
					}
				}
				else if(msg.equals("LOGINCONV")){
					String chatId = din.readUTF();
					String loginMsg = din.readUTF();
					for(Chat chat : chatVector){
						if(chat.chatId.equals(chatId))
							chat.setText(loginMsg);
					}
				}else if(msg.equals("DOENROLL")){
					String chatId = din.readUTF();
					Chat chat = new Chat(this,socket,groupChat,loginName,chatId);
					chatVector.add(chat);
					System.out.println("chatVector: "+ chatVector.size());
					Thread chatThread = new Thread(chat);
					chatThread.start();
				}
				else if(msg.equals("DOCHAT")){
					//String peerIP = din.readUTF();
					//Socket socket = new Socket(peerIP,8888); 
					//socketVector.add(socket);
					
					String chatId = din.readUTF();
					Chat chat = new Chat(this,socket,loginName+"-"+peerChat,loginName,chatId);
					System.out.println(loginName+"-"+peerChat+" "+ loginName);
					chatVector.add(chat);
					System.out.println("chatVector: "+ chatVector.size());
					Thread chatThread = new Thread(chat);
					chatThread.start();
				}
				else if(msg.equals("PEERCHAT")){
					//String peerIP = din.readUTF();
					//Socket socket = new Socket(peerIP,8888); 
					//socketVector.add(socket);
					String chatId = din.readUTF();
					String peerName = din.readUTF();
					Chat chat = new Chat(this,socket,peerName+"-"+loginName,loginName,chatId);
					chatVector.add(chat);
					System.out.println("chatVector: "+ chatVector.size());
					Thread chatThread = new Thread(chat);
					chatThread.start();
				}
				else if(msg.equals("CLOSECHAT")){
					String chatId = din.readUTF();
					logoutFromConv(chatId);
				}
				else if(msg.equals("LOGOUT")){
					din.close();
					dout.close();
					socket.close();
					mainFrame.setVisible(false);
					mainFrame.dispose();
					System.exit(1);
				}
				else if(msg.equals("LOGIN")){
					din.close();
					dout.close();
					socket.close();
					mainFrame.setVisible(false);
					mainFrame.dispose();
					System.exit(1);
				}
				else if(msg.equals("CLIENTS")){
					viewClientList();
					
				}else if(msg.equals("GROUPS")){
					viewGroupList();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean logoutFromConv(String chatId){
		int i=0;
		boolean flag=false;
		while(i<this.chatVector.size()){
			if(chatVector.get(i).chatId.equals(chatId)){
				chatVector.get(i).setVisible(false);
				chatVector.get(i).dispose();
				flag=true;
				break;
			}
			i++;
		}
		if(flag){
			chatVector.remove(i);
			System.out.print(chatId+" chat is removed from chatVector.");
			return true;
		}
		return false;
		
	}
	
	private void setupUI(String loginName){
		mainFrame = new JFrame(loginName);
		mainFrame.setSize(500,350);

		panel = new JPanel();
		panel.setSize(480,480);

		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent){
				System.exit(0);
			}        
		});
		
		
		headerLabel = new JLabel("Hello " + loginName ,JLabel.CENTER);
		//statusLabel = new JLabel("Online",JLabel.CENTER); 
		userStatusDropdown = new JComboBox<>(userStatusArray);
		userStatusDropdown.setActionCommand("ChangeStatus");
		userStatusDropdown.addActionListener(new ButtonListener(this));
		
		clientLabel = new JLabel("Online Users", JLabel.CENTER);
		groupLabel = new JLabel("Online Groups", JLabel.CENTER);

		createBtn = new JButton("Create");
		chatBtn = new JButton("Chat");
		enrollBtn = new JButton("Enroll");
		refreshBtn = new JButton("Refresh");
		logoutBtn = new JButton("Logout");
		removeBtn = new JButton("Remove");
		
		
		createBtn.addActionListener(new ButtonListener(this));
		chatBtn.addActionListener(new ButtonListener(this));
		enrollBtn.addActionListener(new ButtonListener(this));
		refreshBtn.addActionListener(new ButtonListener(this));
		logoutBtn.addActionListener(new ButtonListener(this));
		removeBtn.addActionListener(new ButtonListener(this));
		
		convName = new JTextField(20);

		clientList = new JList<String>();
		groupList = new JList<String>();
		clientList.setBounds(new Rectangle(50,100));
		groupList.setBounds(new Rectangle(50,100));
		JScrollPane clientListScrollPane = new JScrollPane(clientList);
		JScrollPane groupListScrollPane = new JScrollPane(groupList);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(headerLabel)
						.addComponent(clientLabel)
						.addComponent(clientListScrollPane)
						.addComponent(chatBtn)
						.addComponent(convName)
						.addComponent(refreshBtn))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(userStatusDropdown)
						.addComponent(groupLabel)
						.addComponent(groupListScrollPane)
						.addComponent(enrollBtn)
						.addComponent(createBtn)
						.addComponent(logoutBtn)
						.addComponent(removeBtn)));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(headerLabel)
						.addComponent(userStatusDropdown))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(clientLabel)
						.addComponent(groupLabel))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(clientListScrollPane)
						.addComponent(groupListScrollPane))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(chatBtn)
						.addComponent(enrollBtn))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(convName)
						.addComponent(createBtn))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(refreshBtn)
						.addComponent(logoutBtn))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(removeBtn)));
		
		if(userType.equals("User")){
			removeBtn.setVisible(false);
		}	
		
		panel.setLayout(layout);
		mainFrame.add(panel);
		mainFrame.setVisible(true);  
	}
		
	
	void viewGroupList(){
		groupsListModel = new DefaultListModel<String>();
		String groupsString = null;
		try {
			groupsString = din.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("groupsString is here");
		String[] groups = groupsString.split(",");
		for(String group : groups){
			groupsListModel.addElement(group);
			//System.out.println(group+" ");
		}
		groupList.setModel(groupsListModel);
	}

	void viewClientList(){
		clientsListModel = new DefaultListModel<String>();
		String clientsString=null;
		try {
			clientsString = din.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("clientsString is here");
		String[] clients = clientsString.split(",");
		for(String client : clients){
			clientsListModel.addElement(client);
			//System.out.println(client+" ");
		}
		clientList.setModel(clientsListModel);
	}



	class ButtonListener implements ActionListener {
		Client client;
		
		ButtonListener(Client client) {
			this.client = client;
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Create")) {
				System.out.println("createBtn has been clicked");
				try {
					Client.dout.writeUTF("CREATE");
					Client.dout.writeUTF(convName.getText());
					convName.setText("");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			}else if(e.getActionCommand().equals("Refresh")){
				System.out.println("refreshBtn has been clicked");
				Thread refreshThread = new Thread(new Refresher());
				refreshThread.start();
				refreshThread=null;
			}
			
			else if(e.getActionCommand().equals("Enroll")){
				System.out.println("enrollBtn has been clicked");
				if(!groupList.isSelectionEmpty()){
					try {
						dout.writeUTF("ENROLL");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					groupChat = groupList.getSelectedValue();
					String chatID=null;
					Pattern p = Pattern.compile("\\d+");
					Matcher m = p.matcher(groupChat); 
					if (m.find()) {
						chatID =  m.group();
					}
					try {
						dout.writeUTF(chatID);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			else if(e.getActionCommand().equals("Chat")){	
				System.out.println("chatBtn has been clicked");
				if(!clientList.isSelectionEmpty()){
					try {
						dout.writeUTF("CHAT");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					String SelectedValue = clientList.getSelectedValue();
					String[] splitedArray = SelectedValue.split("\\s+");
					peerChat = splitedArray[1];
					String userId=null;
					Pattern p = Pattern.compile("\\d+");
					Matcher m = p.matcher(SelectedValue); 
					if (m.find()) {
						userId =  m.group();
					}
					try {
						dout.writeUTF(userId);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			else if(e.getActionCommand().equals("Logout")){
				System.out.println("logoutBtn has been clicked");
				try {
					dout.writeUTF("LOGOUT");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				mainFrame.setVisible(false);
				mainFrame.dispose();
				System.exit(1);
			}
			else if(e.getActionCommand().equals("Remove")){
				System.out.println("removeBtn has been clicked");
				try {
					dout.writeUTF("REMOVE");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String clientToRemove = clientList.getSelectedValue();
				String userId=null;
				Pattern p = Pattern.compile("\\d+");
				Matcher m = p.matcher(clientToRemove); 
				if (m.find()) {
					userId =  m.group();
				}
				try {
					dout.writeUTF(userId);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if(e.getActionCommand().equals("ChangeStatus")){
				String userStatus = (String) userStatusDropdown.getSelectedItem();
				try {
					dout.writeUTF("CHANGESTATUS");
					dout.writeUTF(userStatus);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	class Refresher extends Thread{
		@Override
		public void run() {
				// TODO Auto-generated method stub
				try {
					dout.writeUTF("GETGROUPS");
					dout.writeUTF("GETCLIENTS");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
		}
	}
}


