import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


class User {
	int userId;
	String loginName;
	String password;
	Socket userSocket;
	//String userIP;
	String userType;
	String userStatus;

	public User(String loginName,String password,int userId ,Socket userSocket, String userType){
		this.userId = userId;
		this.loginName = loginName;
		this.password = password;
		this.userSocket = userSocket;
		this.userType = userType;
		this.userStatus = "Online";
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.userId+" "+ this.loginName;
	}
	public void setSocket(Socket socket){
		this.userSocket = socket;
	}
}

class Conversation {
	int convId;
	String convType;
	String convName;
	ArrayList<User> convMembers;

	public Conversation(String convName,String convType ,int convId){
		this.convName = convName;
		this.convType = convType;
		this.convId = convId;
		convMembers= new ArrayList<User>();
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.convId+ " " + this.convName+": "+this.convMembers.toString();
	}
	
	public boolean isMember(int memberId){
		System.out.println("4");
		for(User user : convMembers){
			if(user.userId == memberId){
				System.out.println("5");
				return true;
			}
		}
		System.out.println("6");
		return false;
	}
	
	public boolean removeMember(int memberId){
		System.out.println("Hello i am heere " + convMembers.size());
		int i=0;
		boolean flag=false;
		while(i<this.convMembers.size()){
			if(convMembers.get(i).userId == memberId){
				flag=true;
				break;
			}
			i++;
		}
		if(flag){
			convMembers.remove(i);
			System.out.print(memberId+" member is removed from convMember and " + convMembers.size());
			return true;
		}else{
			System.out.print(memberId+" member is failed removed from convMember.");
			return false;
		}
	}
}

 class ClientHandler extends Thread{
    
	 String loginName;
	 User user;
	 static int userId=0;
	 static int convId=0;
     private Socket clientSocket;
    // private ServerSocket clientServerSocket;
     private Vector<User> userVector = new Vector<User>();
     private Vector<Conversation> convs= new Vector<Conversation>();
     DataInputStream din;
     DataOutputStream dout;
     
    public ClientHandler(Socket clientSocket, Vector<User> userVector, Vector<Conversation> convs) throws IOException{
        this.clientSocket = clientSocket;
        this.userVector = userVector;
        this.convs=convs;
        din= new DataInputStream(clientSocket.getInputStream());
        dout= new DataOutputStream(clientSocket.getOutputStream());
        loginName = clientAuth();        
    }
    
    private String clientAuth(){
        String name="";
        try{           
        	System.out.println("Auth started");
            dout.writeUTF("LOGIN:");
            name=din.readUTF();
            dout.writeUTF("PASS:");
            String password=din.readUTF();
            dout.writeUTF("ROLE:");
            String role=din.readUTF();
            //String clientIP = din.readUTF();
            int i=0;
            while(i<userVector.size()){
            	System.out.println("A");
            	if(userVector.get(i).loginName.equals(name) && userVector.get(i).password.equals(password)){
            		System.out.println("B");
            		userVector.get(i).userStatus = "Online";
            		this.user = userVector.get(i);
            		user.setSocket(clientSocket);
            		this.start();
            		break;
            	}
            	if(userVector.get(i).loginName.equals(name)){
            		if(!userVector.get(i).password.equals(password)){
            			dout.writeUTF("FAILED");
            			break;
            		}
            	}
            	i++;
            }
            if(i==userVector.size()){
            	this.user = new User(name,password,userId ,clientSocket, role);
            	userId++;
            	System.out.println(user.loginName + " has logged in.");
            	userVector.add(user);
            	this.start();
            }
              
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return name;
    }
    
    @Override
    public void run(){
         try {
            while(true){
                DataInputStream din= new DataInputStream(user.userSocket.getInputStream());
                String msg= din.readUTF();
                if(msg.equals("CREATE")){
                	String convName = din.readUTF();
                    Conversation conv= new Conversation(convName,"group" ,convId);
                    convId++;
                    convs.add(conv);
                    System.out.println(convName +" Conversation Created Succesfully");
               
                }else if(msg.equals("ENROLL")){
                	System.out.println("1");
                    String convId = din.readUTF();
                    System.out.println("2");
                    int noI=Integer.parseInt(convId);
                    System.out.println("3");
                    if(convs.get(noI).isMember(user.userId) == false){
                    	convs.get(noI).convMembers.add(user);
                    	System.out.println(user.loginName + " is not a member so is added to "+ convId);
                        dout.writeUTF("DOENROLL");
                        dout.writeUTF(convId);
                    	
                    	ArrayList<User> users= convs.get(noI).convMembers;
                    	for (User user : users) {
                    		DataOutputStream dout= new DataOutputStream(user.userSocket.getOutputStream());
                    		dout.writeUTF("LOGINCONV");
                    		dout.writeUTF(convId);
                    		dout.writeUTF(loginName + " has logged in.");
                    	}
                    	System.out.println(user.loginName +" has Enrolled in "+ convs.get(noI).convName);
                    }
        
                    
                }
                else if(msg.equals("CHAT")){
                	
                	String peerIdString = din.readUTF();
                    int peerId = Integer.parseInt(peerIdString);
                	String newConvName = user.loginName +"-"+userVector.get(peerId).loginName;
                	Conversation conv= new Conversation(newConvName, "peer", convId);
                	int peerConvId = convId; 
                	String convIdString = Integer.toString(convId);
                	convId++;
                    convs.add(conv);
                    conv.convMembers.add(user);
                    if(!userVector.get(peerId).userStatus.equals("Offline")){
                    	conv.convMembers.add(userVector.get(peerId));
                        System.out.println("Conversation Created Succesfully" + "ID: "+ convIdString);
                        dout.writeUTF("DOCHAT");
                        dout.writeUTF(convIdString);
                        //dout.writeUTF(userVector.get(peerId).userIP);
                        System.out.println("Chat:" + convIdString);
                        
                        Socket chattedUserSocket = userVector.get(peerId).userSocket;
                        DataOutputStream doutChat = new DataOutputStream(chattedUserSocket.getOutputStream());
                        doutChat.writeUTF("PEERCHAT");
                        doutChat.writeUTF(convIdString);
                        doutChat.writeUTF(userVector.get(peerId).loginName);
                        System.out.println("PEERCHAT:" + convIdString);
                        
                        ArrayList<User> users= convs.get(peerConvId).convMembers;
                        for (User user : users) {
                            DataOutputStream dout= new DataOutputStream(user.userSocket.getOutputStream());
                            dout.writeUTF("LOGINCONV");
                            dout.writeUTF(convIdString);
                            dout.writeUTF(loginName + " opened chat with "+ userVector.get(peerId).loginName);
                       }
                    }

                }
                else if(msg.equals("DATA")){
                	String convID = din.readUTF();
                    int noI = Integer.parseInt(convID);
                    String txt = din.readUTF();
                    System.out.println("DATA " +user.loginName +txt+ "to"+ convID);
                    ArrayList<User> users= convs.get(noI).convMembers;
                    for (User user : users) {
                         DataOutputStream dout= new DataOutputStream(user.userSocket.getOutputStream());
                         dout.writeUTF("DATA");
                         dout.writeUTF(convID);
                         dout.writeUTF(loginName + ": " + txt);
                    }   
                }
                else if(msg.equals("GETCLIENTS")){
                	DataOutputStream dout= new DataOutputStream(user.userSocket.getOutputStream());
                	String users="";
                	for(User user : userVector){
                		if(user.userId != this.user.userId && !user.userType.equals("Admin")){
                			users += user.userId+" "+user.loginName +" "+user.userStatus +",";
                		}
                	}
                	dout.writeUTF("CLIENTS");
                	dout.writeUTF(users);
                }
                else if(msg.equals("GETGROUPS")){
                	DataOutputStream dout= new DataOutputStream(user.userSocket.getOutputStream());
                	dout.writeUTF("GROUPS");
                	String groups="";
                	for(Conversation conv : convs){
                		if(conv.convType.equals("group")){
                			groups += conv.convId+" "+conv.convName +","; 
                		}
                		else if(conv.convType.equals("peer") && conv.isMember(userId)){
                			groups += conv.convId+" "+conv.convName +",";
                		}
                	}
                	//System.out.println(groups);
                	dout.writeUTF(groups);
                }
                else if(msg.equals("LOGOUTCONV")){
                	String convId = din.readUTF();
                    int noI = Integer.parseInt(convId);
                    System.out.println("LOGOUTCONV is reseved" + noI);
                    convs.get(noI).removeMember(this.user.userId);

                    if(convs.get(noI).convType.equals("peer")){
                    	ArrayList<User> usersInConv= convs.get(noI).convMembers;
                    	for (User user : usersInConv) {
                    		DataOutputStream dout= new DataOutputStream(user.userSocket.getOutputStream());
                    		dout.writeUTF("CLOSECHAT");
                    		dout.writeUTF(convId);
                    	}
                    }else{
                    	ArrayList<User> usersInConv= convs.get(noI).convMembers;
                    	for (User user : usersInConv) {
                    		DataOutputStream dout= new DataOutputStream(user.userSocket.getOutputStream());
                    		dout.writeUTF("LOGOUTCONV");
                    		dout.writeUTF(convId);
                    		dout.writeUTF(loginName + " has logged out.");
                    	}
                    }
                    System.out.println(user.loginName +" has logged out "+ convs.get(noI).convName);
                }
                else if(msg.equals("LOGOUT")){
                	System.out.println("Ana 3yz alog out");
                	dout.writeUTF("LOGOUT");
                	Server.userMakeOffline(this.user.userId);
                	for(Conversation conv: convs){
                		conv.removeMember(this.user.userId);
                	}
                	this.din.close();
                	this.dout.close();
                	this.clientSocket.close();
                	break;
                }
                else if(msg.equals("REMOVE")){
                	String userIdString = din.readUTF();
                	int userId = Integer.parseInt(userIdString);
                	User removedUser = Server.userVector.get(userId);
                	DataOutputStream dout = new DataOutputStream(removedUser.userSocket.getOutputStream());
                	dout.writeUTF("LOGOUT");
                	Server.removeUser(userId);
                	for(Conversation conv: convs){
                		conv.removeMember(userId);
                	}
                }
                else if(msg.equals("CHANGESTATUS")){
                	String status = din.readUTF();
                	this.user.userStatus = status;
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

 public class Server extends Thread {

	 ServerSocket serverSocket;
	 Socket socket;
	 static Vector<User> userVector;
	 static Vector<Conversation> activeConvs; 

	 JFrame ChatServerFrame;
	 JPanel panel;
	 JButton startServerBtn;
	 JButton stopServerBtn;

	 public Server() throws IOException{
		 serverSocket = new ServerSocket(8888);
		 userVector = new Vector<User>();
		 activeConvs= new Vector<Conversation>();

		 final JFrame ChatServerFrame = new JFrame("ChatServer");
		 JPanel panel = new JPanel();
		 JButton startServerBtn = new JButton("Start");
		 JButton stopServerBtn = new JButton("Stop");
		 ChatServerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		 panel.add(startServerBtn);
		 panel.add(stopServerBtn);
		 ChatServerFrame.setSize(200,100);
		 ChatServerFrame.add(panel);
		 ChatServerFrame.setVisible(true);

		 startServerBtn.addActionListener(new ButtonListener());
		 stopServerBtn.addActionListener(new ButtonListener());
	 }
	 
	 static void userMakeOffline(int userId){
		 int i=0;
		 while(i<userVector.size()){
			 if(userVector.get(i).userId == userId){
				 userVector.get(i).userStatus="Offline";
			 }
			 i++;
		 }
	 }

	 static boolean removeUser(int userId){
		 int i=0;
		 boolean flag=false;
		 while(i<userVector.size()){
			 if(userVector.get(i).userId == userId){
				 flag=true;
				 break;
			 }
			 i++;
		 }
		 if(flag){
			 userVector.remove(i);
			 System.out.print(userId+" member is removed from userVector and " + userVector.size());
			 return true;
		 }else{
			 System.out.print(userId+" member is failed removed from userVector.");
			 return false;
		 }

	 }

	 static boolean removeUserFromConv(int userId){
		 for(Conversation con : activeConvs){
			con.removeMember(userId);
		 }
		 return true;
	 }

	 public static void main(String[] args) throws IOException{
		 Server server = new Server();
	 }

	 @Override
	 public void run() {
		 // TODO Auto-generated method stub
		 super.run();
		 while(true){
			 try {
				 socket = serverSocket.accept();
				 ClientHandler clientHandler = new ClientHandler(socket,userVector,activeConvs);
				 //clientHandler.start();
			 } catch (IOException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 }
		 }
	 }

	 class ButtonListener implements ActionListener {
		 ButtonListener() {};
		 public void actionPerformed(ActionEvent e) {
			 if (e.getActionCommand().equals("Start")) {
				 System.out.println("Server Started ..");
				 Enumeration e1=null;
				try {
					e1 = NetworkInterface.getNetworkInterfaces();
				} catch (SocketException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				 while(e1.hasMoreElements())
				 {
				     NetworkInterface n = (NetworkInterface) e1.nextElement();
				     Enumeration ee = n.getInetAddresses();
				     while (ee.hasMoreElements())
				     {
				         InetAddress i = (InetAddress) ee.nextElement();
				         if(i.isSiteLocalAddress()){
				        	 System.out.println("IP: "+i.getHostAddress());
				         }
				         
				     }
				 }
				 start();
			 }else if(e.getActionCommand().equals("Stop")){
				 System.out.println("Server Stopped ..");

				 try {
					 System.exit(1);
				 } catch (Throwable e1) {
					 // TODO Auto-generated catch block
					 e1.printStackTrace();
				 }
			 }
		 }
	 }
 }