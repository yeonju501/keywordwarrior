import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

class OneClientModule extends Thread{
	Server server;
	Socket s;
	InputStream is; DataInputStream dis;	//소켓에서 받는 정보
	OutputStream os; DataOutputStream dos;	//소켓에 전달하는 정보
	ImageIcon[] color = new ImageIcon[4];
	OneClientModule(Server server){
		this.server = server;
		this.s = server.s;
		color[0] = new ImageIcon("img/redhead1.png");color[1] = new ImageIcon("img/bluehead1.png");
		color[2] = new ImageIcon("img/greenhead1.png");	color[3] = new ImageIcon("img/pinkhead1.png");
		connect();
	}
	void connect(){
		//
		String temp = null;String proto = null;String msg = null;
		try{
			is = s.getInputStream();
			dis = new DataInputStream(is);
			os = s.getOutputStream();
			dos = new DataOutputStream(os);
			//로그인메세지 받는부분	#002
			temp = dis.readUTF();
			proto = temp.substring(0,4);
			msg = temp.substring(4);

			server.nickName = msg;

			//
		}catch(IOException ie){}
	}

	public void run(){
		acceptData();
	}
	
	
	
	
	void acceptData(){	
	/* 정보를 전달받음
	*/	
		String temp = null;String proto = null;String msg = null;
		String nickName = "GUEST";	//지역변수 방송용
		int readyN =0;	// 게임시작 트리거
			
		try{
			//#002 접속할 때.
			nickName = server.nickName;
			int cNumber = server.clientCList.size();	//0
			server.cc = new ClientCharacter(nickName, cNumber, server.score, color[cNumber]);
			server.clientCList.put(nickName, server.cc);
			broadcast("@002"+cNumber);
			Set<String> keys = server.clientCList.keySet();
			for(String key: keys){ 
				broadcast("@003"+server.clientCList.get(key).index+key);
			}
			broadcast("@004"+nickName);
			broadcast("@005"+nickName);
			server.pln(nickName+"님께서 게임서버에 접속하였습니다");
			//
			while(true){
				//server.pln(server.readyN+"  "+readyN);
				if(GameMode.gameMode == GameMode.BEFORE & server.readyN == server.clientMap.size() & server.readyN != 1){
					broadcast("@008"+"3초 후 게임 시작");
					server.pln("게임 시작"); // 
				}
				temp = dis.readUTF();
				proto = temp.substring(0,4);
				msg = temp.substring(4);
				//프로토콜 조건문 분기
				if(proto.equals("#001")){
					broadcast("@001"+msg);	//일반메세지 #001
					server.pln(msg);//서버에 전달하는 메세지
				}
				if(proto.equals("#003")){	
					broadcast("@006"+nickName);
					readyN = Integer.parseInt(msg);
					server.readyN += readyN;
				}
				if(proto.equals("#004")){
					broadcast("@007"+nickName);
					readyN = Integer.parseInt(msg);
					server.readyN += readyN;
				}
				if(proto.equals("#005")){	
					GameMode.gameMode = GameMode.NOTMYTURN;
				}
				
				if(proto.equals("#006")){	//정답체크 > 점수갱신
					if(server.wList.contains(msg)){
						server.wList.remove(msg);
						broadcast("@009"+msg);
						
						ClientCharacter tempCC = server.clientCList.get(nickName);
						tempCC.score += 1;
						broadcast("@012"+nickName+":"+tempCC.score);
						broadcast("@014",nickName);
					}else{
						broadcast("@015",nickName);
					}
				}
				if(proto.equals("#007")){		// 콤보박스
					server.wList.clear();
					String indexS = msg.trim();
					int index = Integer.parseInt(indexS);
					server.wordRead(index);
					String topicname = server.file[index].getName();
					broadcast("@013"+topicname);
					for(String item: server.wList) server.pln(item);
				}
				if(proto.equals("#008")){	//게임종료됐을때.
					
					broadcast("@011");	//모든 클라이언트에게 리셋
					GameMode.gameMode = GameMode.BEFORE;
					server.readyN = 0;
					server.turn = 0;
				}
				
			}
		}catch(IOException ie){	//클라이언트가 접속을 종료했을 때 발생하는 예외
			closeAll();
			server.clientMap.remove(nickName);
			server.clientCList.remove(nickName);
			String msgE = nickName;
			broadcast("@100"+msgE);
			server.pln(msgE);
			/*예외처리 어떻게?*/
		}finally{	
		}
	}

	void broadcast(String temp){	
		/* 모든 클라이언트(자신포함)에게 정보전달하는 방식 
			>> acceptData()에서 받은 프로토콜로 조건문으로 분기 > 
		*/
		Set<String> keys = server.clientMap.keySet();
		Iterator<String> iter = keys.iterator();
		try{
			while( iter.hasNext()){
				String key = iter.next();
				//server.pln(key); 이터레이터순서확인 먼저들어온 순서 맞음.
				OneClientModule ocm = server.clientMap.get(key);
				ocm.dos.writeUTF(temp);
				ocm.dos.flush();
			}
		}catch(IOException io){
		}finally{
		}
	}
	void broadcast(String temp, String nickName){ //이 객체와 연결된 클라이언트에게만.
		try{
			OneClientModule ocm = server.clientMap.get(nickName);
			ocm.dos.writeUTF(temp);
			ocm.dos.flush();
		}catch(IOException io){
		}finally{
		}
	}
	void closeAll(){ //연결객체들 닫기
		try{
			if(dis != null) dis.close();
			if(dos != null) dos.close();
			if(is != null) is.close();
			if(os != null) os.close();
			if(s != null) s.close();
		}catch(IOException ie){
		}
	}
}
