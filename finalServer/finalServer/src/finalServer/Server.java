package finalServer;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class Server {

	class DoctorInfo{
		String account;
		String name;
		String department;
		int waitCount;
		ArrayList<String> patientQueue=new ArrayList<String>();	
	}
	public static class medicineInfo{
		medicineInfo(String name,int count,int price){
			this.name=name;
			this.count=count;
			this.price=price;
		}
		String name;
		int count;
		int price;
	}
	
	
	ServerSocket server;
	static Socket charge;
	static Socket store;
	static Socket client;
	
	
	static int internalCount;//�ڿ��Ŷ�����
	static int surgeryCount; //����Ŷ�����
	static public HashMap<String,Socket> doctorSocket =new HashMap<String,Socket>();
	static public ArrayList<DoctorInfo> onlineDoctor=new ArrayList<DoctorInfo>();
    
	private String command;
	static public ResultSet rs=null;
	static public Statement st = null;
	static public Connection con;
	
	
	
	
	
	
	
	
	
	Server(){                      //���캯��
		//�������ݿ���������
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			                   //����ʧ��
			e.printStackTrace();
		}	
		//�������ݿ�����
		String str="jdbc:sqlserver://localhost:1433;databaseName=medical";
		try {
			con = DriverManager.getConnection(str,"sa","199511");
		} catch (SQLException e) {
			                              //����ʧ��
			e.printStackTrace();
		}
		if(con!=null){
			System.out.println("���ݿ����ӳɹ�");
		}
		
		
		
		
		
		try {  
		    server=new ServerSocket(12000); //�򿪶˿�
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}	
	
	public void openServer(){
		
		while(true){  //��ѭ����һֱ�ȴ�socket����
			try {
				client=server.accept();  
				System.out.println("��������");
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						addSoket(Server.client);				
					}
				}).start();
					
		   
									
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
		

	private void addSoket(Socket c) {
		try {
			Socket client=c;
			//info=���ͣ��˺ţ����룬
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
	    	while(true){
				String info=br.readLine();
				if(analysis(info,client)){//������¼����Ϣ
					break;
				}  
			}
			System.out.println("��½�ɹ����߳��˳�");
			
		} catch (IOException e) {
			        //��¼���ɹ�������ʧ��
			e.printStackTrace();
		}
		
	}
	
	public boolean analysis(String info, Socket client){
		
		String[] string=info.split(",");
		String account=string[1];
		String password=string[2];
		PrintWriter pw;
		if(!login(account,password)){// ��¼������˺�������ȷ���������һ����ʼ�߳�
			
			
			try {
				pw = new PrintWriter(client.getOutputStream());
				pw.println("NO");
		    	pw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return false;
		}
		
		try {
			pw = new PrintWriter(client.getOutputStream());
			pw.println("OK");
	    	pw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(string[0].equals("charge")){
			charge=client;
			System.out.println("�����߳�");
			new ChargeThread(client).start();
			
		}		
		else if(string[0].equals("doctor")){
			doctorSocket.put(string[1], client); 
			 addOnlineDoctorInfo(string[1]);                                     //update����ҽ����Ϣ
			new DoctorThread(string[1],client).start();  //����ҽ���߳�
		}
		else if(string[0].equals("store")){
			store=client;
			new StoreThread(client).start();
		}
		return true;
	}
	
	private void addOnlineDoctorInfo(String string) {
		                            //�����ݿ� ҽ����Ϣ��
		if(string.equals("123")){
			DoctorInfo di=new DoctorInfo();
			di.name="����";
			di.waitCount=0;
			di.department="surgery";
			di.account=string;
			onlineDoctor.add(di);
		}else if(string.equals("456")){
			DoctorInfo di=new DoctorInfo();
			di.name="����";
			di.waitCount=0;
			di.department="surgery";
			di.account=string;
			onlineDoctor.add(di);
		}
		
	}

	private boolean login(String account, String password) { //��¼����
		if(account.equals("123")&&password.equals("abc"))   //�����ݿ� �˺���Ϣ��
			return true;
		else if(account.equals("456")&&password.equals("abc"))
			return true;
		System.out.println("�˺��������");
		     return false;
		
	}
	
	public static void main(String[] args) {
		Server server=new Server();
		server.openServer();  //�򿪷�����

	}

}
