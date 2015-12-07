import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class Chat extends JFrame implements Runnable {
	
	Client client;
	Socket socket;
	JTextArea textArea;
	JButton sendBtn, logoutBtn;
	JTextField textField;

	volatile Thread thread;	
	
	DataInputStream din;
	DataOutputStream dout;
	String loginName;
	String userType;
	String chatId;
	
	Chat(Client client, Socket socket,String conv, String loginName, String chatId) throws UnknownHostException, IOException{
		super(conv+" "+loginName);
		this.client = client;
		this.loginName = loginName;
		this.socket =socket;
		this.chatId = chatId;
		textArea = new JTextArea(18,50);
		textArea.setEditable(false);
		textField = new JTextField(50);
		sendBtn = new JButton("Send");
		logoutBtn = new JButton("Logout");
		
		din = new DataInputStream(socket.getInputStream());
		dout = new DataOutputStream(socket.getOutputStream());
		
		setup();
		
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				try {
					dout.writeUTF("LOGOUTCONV");
					dout.writeUTF(chatId);
					client.logoutFromConv(chatId);
					setVisible(false);
					dispose();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		
		textField.addKeyListener(new KeyListener(){
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
			}
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					try {
						dout.writeUTF("DATA");
						dout.writeUTF(chatId);
						dout.writeUTF(textField.getText().toString());
						System.out.println("DATA"+ textField.getText().toString()+ "to "+chatId);
						textField.setText("");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub	
			}
		});
		
		sendBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					dout.writeUTF("DATA");
					dout.writeUTF(chatId);
					dout.writeUTF(textField.getText().toString());
					System.out.println("DATA"+ textField.getText().toString()+ "to "+chatId);
					textField.setText("");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		logoutBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					dout.writeUTF("LOGOUTCONV");
					dout.writeUTF(chatId);
					client.logoutFromConv(chatId);
					setVisible(false);
					dispose();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}
	
	
	private void setup() {
		// TODO Auto-generated method stub
		setSize(600,400);
		JPanel panel = new JPanel();
		panel.add(new JScrollPane(textArea));
		panel.add(textField);
		panel.add(sendBtn);
		panel.add(logoutBtn);
		
		add(panel);
		setVisible(true);
	}
	
	public void setText(String text){
		textArea.append("\n" + text);
	}

	@Override
	public String toString(){
		return this.loginName +": "+this.chatId;
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
