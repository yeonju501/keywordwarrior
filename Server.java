import java.net.*;
import java.io.*;
import java.util.*;

class Server{
	ServerSocket ss; int port = 5555; //게임서버와 포트정보
	Socket s;
	

	LinkedHashMap<String,OneClientModule> clientMap = new LinkedHashMap<String,OneClientModule>();
	OneClientModule ocm; String nickName;
	
	LinkedHashMap<String,ClientCharacter> clientCList = new LinkedHashMap<String,ClientCharacter>();
	int score; ClientCharacter cc;//키값은 nickName, 게임에서 모든 클라가 공유하는 정보 >> 서버의 멤버로. ocm도 다른객체.
	int readyN = 0;
	int turn =0;	// 턴관리 트리거
	
	TreeSet<String> wList = new TreeSet<String>();	//문제정답지; 파일읽어서 컬렉션에 저장
	File[] file = new File[3];
	
	Server(){
		
		try{
			ss = new ServerSocket(port);
			pln("서버 대기중.....");
			while(true){
				s = ss.accept();
				ocm = new OneClientModule(this);
				ocm.start();
				clientMap.put(nickName,ocm);
			}
		}catch(IOException ie){}		
	}

	void wordRead(int topicIndex){
		file[0] = new File("topic/자바.txt");
		file[1] = new File("topic/롤.txt");
		file[2] = new File("topic/나라이름.txt");
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		
		try{
			fr = new FileReader(file[topicIndex]);
			br = new BufferedReader(fr);
			while((line = br.readLine()) != null){
				if(line != null) line = line.trim();
				if(line.length() != 0){
					wList.add(line);
				}
			}
		}catch(FileNotFoundException fe){
			
		}catch(IOException ie){
		}finally{
			try{
				if(br!=null) br.close();
				if(fr!=null) fr.close();
			}catch(IOException ie){}
		}
	}

	void pln(String str){
		System.out.println(str);
	}
	void p(String str){
		System.out.print(str);
	}
	public static void main(String[] args) 
	{
		new Server();
	}
}
