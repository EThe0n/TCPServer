package TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

class Server {
	static final int port = 4321; // 상수값으로 port 번호 설정
	
	/*
	 * Thread를 통해 서버가 클라이언트가 죽어도 계속 동작하도록 함
	 * **건드리지 말것.**
	 */
	public static void main(String[] args) {
		try(ServerSocket server = new ServerSocket(port)){
			while(true){
				try{
					Socket client = server.accept();
					Thread task = new DaytimeThread(client);
					task.start();
				}catch(IOException e){}
			}
		}catch(IOException e){
			System.err.println("error : " + e.getMessage());
		}
	}

	Server() {}
	
	private static class DaytimeThread extends Thread{
		private Socket client;		// 클라이언트 소켓
		private PrintWriter out;	// 클라이언트에 데이터를 씀
		private BufferedReader in;	// 클라이언트로부터 데이터를 읽음
		
		DaytimeThread(Socket client){
			this.client = client;
		}
		
		public void run(){
			try{
				System.out.println("thread runs");
				out = new PrintWriter(client.getOutputStream(), true); 
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));

				// receive data from client
				String message = readData();
				
				// modify data
				String response = queryData(message);
				
				// send data to client
				sendData(response);
				
				out.close();
				in.close();
			}catch(Exception e){
				System.err.println(e);
			}finally{
				try{
					client.close();
				}catch(IOException e){}
			}
		}
		
		/*
		 * 데이터를 클라이언트로 부터 읽어들이는 함수
		 * 읽을 때는 무조건 String Type으로 읽어야함 
		 */
		public String readData() throws IOException
		{
			String rcvMessage = in.readLine();
			System.out.println("Receive : " + rcvMessage);
			
			return rcvMessage;
		}
		
		/*
		 * 데이터를 클라이언트에 전송
		 * 전송할때는 무조건 String Type이어야 함
		 */
		public void sendData(String message)
		{
			out.println(message);
			System.out.println("Send : " + message);
		}
		
		/*
		 * 데이터를 변경하는 함수
		 * Docs : http://juliusdavies.ca/json-simple-1.1.1-javadocs/org/json/simple/JSONObject.html
		 */
		@SuppressWarnings("unchecked")
		public String queryData(String message) throws Exception
		{
			// 1. Parse String
			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(message);
			JSONObject result = new JSONObject();
			JSONArray list = new JSONArray();
			
			// 2. Get data 
			String searchFor = (String)obj.get("searchFor");
			System.out.println("search for what? " + searchFor);
			
			// 3. Modify data
			JSONObject temp = new JSONObject();
			if (searchFor.equals("Theme")) {
				System.out.println("Theme Search");
				temp.put("id", "1");
				temp.put("name", "Theme search");
				temp.put("year", 2015);
				list.add(temp);
				list.add(temp);
				list.add(temp);
			}
			else if (searchFor.equals("Region")) {
				System.out.println("Region Search");
				temp.put("id", 2);
				temp.put("name", "Region search");
				temp.put("year", 2017);
				list.add(temp);
				list.add(temp);
				list.add(temp);
			}
			else {
				System.out.println("Custom Search");
				temp.put("id", 3);
				temp.put("name", "Custom search");
				temp.put("year", 2020);
				list.add(temp);
				list.add(temp);
				list.add(temp);
			}
			result.put("results", list);
			
			// Table heads
			String[] headString = {
					"id", "name", "year"
			};
			
			JSONArray tableHeads = new JSONArray();
			for (String a : headString) {
				JSONObject tableHead = new JSONObject();
				tableHead.put("name", a);
				tableHeads.add(tableHead);
			}
			if (searchFor.equals("Region")) {
				JSONObject tableHead = new JSONObject();
				tableHead.put("name", "for test");
				tableHeads.add(tableHead);
			}
			result.put("tablehead", tableHeads);
			
			JSONObject link = new JSONObject();
			result.put("link_name", "https://www.google.co.kr/");
			
			// 4. return JSON String
			return result.toJSONString();
		}
		
	}
}