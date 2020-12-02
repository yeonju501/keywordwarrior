import java.net.*;
import java.io.*;
import java.util.*;

class Server{
	ServerSocket ss; int port = 5555; //���Ӽ����� ��Ʈ����
	Socket s;
	

	LinkedHashMap<String,OneClientModule> clientMap = new LinkedHashMap<String,OneClientModule>();
	OneClientModule ocm; String nickName;
	
	LinkedHashMap<String,ClientCharacter> clientCList = new LinkedHashMap<String,ClientCharacter>();
	int score; ClientCharacter cc;//Ű���� nickName, ���ӿ��� ��� Ŭ�� �����ϴ� ���� >> ������ �����. ocm�� �ٸ���ü.
	int readyN = 0;
	int turn =0;	// �ϰ��� Ʈ����
	
	TreeSet<String> wList = new TreeSet<String>();	//����������; �����о �÷��ǿ� ����
	File[] file = new File[3];
	
	Server(){
		
		try{
			ss = new ServerSocket(port);
			pln("���� �����.....");
			while(true){
				s = ss.accept();
				ocm = new OneClientModule(this);
				ocm.start();
				clientMap.put(nickName,ocm);
			}
		}catch(IOException ie){}		
	}

	void wordRead(int topicIndex){
		file[0] = new File("topic/�ڹ�.txt");
		file[1] = new File("topic/��.txt");
		file[2] = new File("topic/�����̸�.txt");
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
