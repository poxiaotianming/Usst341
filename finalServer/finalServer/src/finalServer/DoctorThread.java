package finalServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Set;

public class DoctorThread extends Thread {
	
	String myAccount;
	Socket client;
	BufferedReader br;

	PrintWriter pw;
	HashMap<String,Server.medicineInfo> medicineData=new HashMap<String,Server.medicineInfo>();
	
	DoctorThread(String account,Socket s){
		this.client=s;	
		this.myAccount=account;
		sendInitMedicineInfo();
		try {
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	public void run(){
		String info;
		try {
			while(true){
			info=br.readLine();
			analysis(info);  //分析数据
			}
		} catch (IOException e) {
			          //与客户端连接失败
			e.printStackTrace(); 
			//break;  
		}
	}
	       //(doctorName)
	//info=name(patient),sex,age,id#medicine,count-medicine,count....
	private void analysis(String info) {
		int price=0;
		String string[]=info.split("#");
		String patientInfo[]=string[0].split(",");
		String medicineInfo[]=string[1].split("-");
		readData();
		for(String s:medicineInfo){
			String str[]=s.split(",");
			price+=medicineData.get(str[0]).price*Integer.parseInt(str[1]);
			
		}

		//message=boolean,price,name(patient),sex,age,id#medicine,count-medicine,count....
		String i=price+"";
		String message="false,"+i+","+info;
		try {
			PrintWriter pw= new PrintWriter(Server.charge.getOutputStream());
			//bw = new BufferedWriter(new OutputStreamWriter(Server.charge.getOutputStream()));
			pw.println(message);
			pw.flush();
			System.out.println("发送信息给指定收费："+message);
		} catch (IOException e) {
			//与收费员连接失败
			e.printStackTrace();
		}
//		try {
//			PrintWriter pw= new PrintWriter(Server.store.getOutputStream());
//			//bw = new BufferedWriter(new OutputStreamWriter(Server.store.getOutputStream()));
//			pw.println(message);
//			pw.flush();
//			System.out.println("发送信息给指定药师："+message);
//		} catch (IOException e) {
//			    //与药师连接失败
//			e.printStackTrace();
//		}
		for(Server.DoctorInfo di:Server.onlineDoctor){
			if(di.account.equals(myAccount)){
				di.waitCount--;
				System.out.println("减了这个账号："+myAccount+"hao"+di.waitCount);
			}
		}
	    //把病人从医生队列里移除
	}

	private void readData() {//从文件读取数据，更新到medicineData
		try {
		    
			Statement st =Server.con.createStatement();
			ResultSet rs=st.executeQuery("select * from medicine");
			while(rs.next()){
//				System.out.print(rs.getInt(1)+" ");
//				System.out.print(rs.getString(2)+" ");
//				System.out.print(rs.getString(3)+" ");
//				System.out.print(rs.getString(4)+" ");
//				System.out.print(rs.getString(5)+" ");
//				System.out.print(rs.getInt(6)+" ");
//				System.out.println();
				Server.medicineInfo mi=new Server.medicineInfo(rs.getString(1).trim(),rs.getInt(2),rs.getInt(3));
				medicineData.put(rs.getString(1).trim(), mi);
			}
			rs.close();
			st.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//message=medicine,name-price#name-price....
	public void sendInitMedicineInfo(){
		readData();
		String message="medicine,";
		try {
			pw=new PrintWriter(client.getOutputStream());
			Set<String> set = medicineData.keySet();   
			for (String s:set) { 
				message+=(s+"-"+medicineData.get(s).price+"#");	 
			}  
			System.out.println(message);
			pw.println(message);  
			pw.flush();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
