package finalServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import finalServer.Server.DoctorInfo;

public class ChargeThread extends Thread {
	
	class patientInfo{
		String id;
		String name;
		String sex;
		String age;		
	}
	static public HashMap<String,patientInfo> allPatient=new HashMap<String,patientInfo>();
	static int id=0;
	
	Socket client;
	BufferedReader br;
	
	PrintWriter pw;
	ChargeThread(Socket s){
		this.client=s;
		try {
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			//bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
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
	//info=true,id
	//info=department,name,sex,age.....
	//info=look  ->message=chooseDoctor,...,id||message=noDoctor
	private void analysis(String info) {
			
		
		newPatient(info);   //新来的病人信息处理
		charge(info);     //已经挂了号的病人的收费信息处理
					
	}

	

	private void charge(String info) {
		String string[]=info.split(",");
		if(string[0].equals("true")||string[0].equals("false")){
			String message=info;
			try {
				PrintWriter pw= new PrintWriter(Server.store.getOutputStream());
				//bw = new BufferedWriter(new OutputStreamWriter(Server.store.getOutputStream()));
				pw.println(message);
				pw.flush();
				System.out.println("发送信息给指定药师，付钱情况"+message);
			} catch (IOException e) {
				         //与选择到的医生连接失败
				e.printStackTrace();
			}
			if(string[0].equals("false")){
				allPatient.remove(string[1]);
			}
			return ;
		}
		
	}
	
	private void newPatient(String info) {
		String string[]=info.split(",");
		if(string[0].equals("true")||string[0].equals("false"))
			return;
		
		DoctorInfo doctor=chooseDoctor(info);
		if(doctor==null){
			
			sendNoDoctorInfo();
			System.out.println("目前没有医生");
			return;
		}
		patientInfo p=new patientInfo();
		p.id=(id+"");
		p.name=string[1];
		p.sex=string[2];
		p.age=string[3];
		allPatient.put(p.id,p);
		id++;
		//message= name(patient),sex,age//,id   发给医生的信息
		doctor.patientQueue.add(string[1]);
		doctor.waitCount++;
		String message=string[1]+","+string[2]+","+string[3]+","+id;
		Socket sendSocket=Server.doctorSocket.get(doctor.account);
		try {
			//bw = new BufferedWriter(new OutputStreamWriter(sendSocket.getOutputStream()));
			PrintWriter pw= new PrintWriter(sendSocket.getOutputStream());
			pw.println(message);
			pw.flush();
			sendChooseDoctorInfo(doctor.name+","+string[0]);
			System.out.println("发送信息给指定医生："+message);
		} catch (IOException e) {
			 //与选择到的医生连接失败
			e.printStackTrace();
		}
		
	}

	

	

	private DoctorInfo chooseDoctor(String info) {
		DoctorInfo doctor = null;
        String string[]=info.split(",");  
		if(string[0].equals("internal")){
			Server.internalCount++;		
		}
		else if(string[0].equals("surgery")){
			Server.surgeryCount++;
		}
	
		int min=100;
		String doctorAccount;
		
		for(DoctorInfo d:Server.onlineDoctor){
			if(d.department.equals(string[0])){
				if(d.waitCount<min){
					min=d.waitCount;
					doctor=d;
				}
			}		
		}
	
		return doctor;
			
	}
	
	private void sendNoDoctorInfo() {
		try {
			PrintWriter pw= new PrintWriter(client.getOutputStream());
			pw.println("noDoctor");
			pw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void sendChooseDoctorInfo(String str) {
		try {
			PrintWriter pw= new PrintWriter(client.getOutputStream());
			pw.println("chooseDoctor,"+str);   //message=     ...,name,department 发给收费员
			pw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
