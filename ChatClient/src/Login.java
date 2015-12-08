import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


public class Login {
	public static void main(String[] args){
		
		final JFrame login = new JFrame("Login");
		JPanel panel = new JPanel();
		JLabel loginNameLabel = new JLabel("Username:");
		final JTextField loginNameTextField = new JTextField(15);
		JLabel passwordLabel = new JLabel("Password:");
		JPasswordField passwordField = new JPasswordField(15);
		JLabel ipLabel = new JLabel("   IP:");
		final JTextField ipTextField = new JTextField(15);
		JButton enterBtn = new JButton("Login");
		JRadioButton adminBtn = new JRadioButton("Admin");
		adminBtn.setActionCommand("Admin");
	    JRadioButton userBtn = new JRadioButton("User");
	    userBtn.setActionCommand("User");
	    ButtonGroup radioGroup = new ButtonGroup();
		
	    panel.add(ipLabel);
	    panel.add(ipTextField);
	    panel.add(loginNameLabel);
		panel.add(loginNameTextField);
		panel.add(passwordLabel);
		panel.add(passwordField);
		radioGroup.add(adminBtn);
	    radioGroup.add(userBtn);
	    userBtn.setSelected(true);
	    panel.add(userBtn);
	    panel.add(adminBtn);
	    panel.add(enterBtn);
		
		login.setSize(280,150);
		login.add(panel);
		login.setVisible(true);
		login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		enterBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {	
						String userType = radioGroup.getSelection().getActionCommand();
						char[] pass = passwordField.getPassword();
						//System.out.print(pass);
						String passwordString="";
						for(int i=0; i<pass.length;i++){
							passwordString += pass[i];
						}
						System.out.println(passwordString);
						String username = loginNameTextField.getText();
						if(username.equals("") || pass.length==0){
							JFrame failedFrame = new JFrame("Alert");
							JOptionPane.showMessageDialog(failedFrame, "Username and Password can't be NULL");
						}else{
							new Client(ipTextField.getText(),loginNameTextField.getText(),passwordString,userType);
							login.setVisible(false);
							login.dispose();
						}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		loginNameTextField.addKeyListener(new KeyListener(){
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					try {
						String userType = radioGroup.getSelection().getActionCommand();
						char[] pass = passwordField.getPassword();
						//System.out.print(pass);
						String passwordString=null;
						for(int i=0; i<pass.length;i++){
							passwordString += pass[i];
						}
						System.out.println(passwordString);
						String username = loginNameTextField.getText();
						if(username.equals("") || pass.length==0){
							JFrame failedFrame = new JFrame("Alert");
							JOptionPane.showMessageDialog(failedFrame, "Username and Password can't be NULL");
						}else{
							new Client(ipTextField.getText(),loginNameTextField.getText(),passwordString,userType);
							login.setVisible(false);
							login.dispose();
						}
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
	}
}
