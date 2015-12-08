import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


public class Login {
	public static void main(String[] args){
		
		final JFrame login = new JFrame("Login");
		JPanel panel = new JPanel();
		JLabel loginNameLabel = new JLabel("Name:");
		final JTextField loginNameTextField = new JTextField(20);
		JLabel ipLabel = new JLabel("   IP:");
		final JTextField ipTextField = new JTextField(20);
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
		radioGroup.add(adminBtn);
	    radioGroup.add(userBtn);
	    userBtn.setSelected(true);
	    panel.add(userBtn);
	    panel.add(adminBtn);
	    panel.add(enterBtn);
		
		login.setSize(300,120);
		login.add(panel);
		login.setVisible(true);
		login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		enterBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {	
						String userType = radioGroup.getSelection().getActionCommand();
						new Client(ipTextField.getText(),loginNameTextField.getText(), userType);
						login.setVisible(false);
						login.dispose();
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
							new Client(ipTextField.getText(),loginNameTextField.getText(), userType);
							login.setVisible(false);
							login.dispose();
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
