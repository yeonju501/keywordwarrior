

import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


class Client extends Thread{
	Socket s; 
	String ip = "192.168.0.74"; int port = 5555; //접속하는 게임서버 주소
	MWHandler mwh;

	// 서버저장정보컬렉션, 여기도있어야한다. cc >> 속에 닉네임,스코어,색깔
	LinkedHashMap<String,ClientCharacter> clientCList = new LinkedHashMap<String,ClientCharacter>();
	String nickName ="GUEST";int score = 0;
	ClientCharacter cc;
	
	ImageIcon[] color = new ImageIcon[4];
	
	OutputStream os; DataOutputStream dos;	// 소켓에 전달하는 정보
	InputStream is; DataInputStream dis;	// 소켓에서 받는 정보
	
	

	Client(MWHandler mwh){	//MWH에서 로그인버튼 누르면 >>
		this.mwh = mwh;
		this.nickName = mwh.nickN;
		
		color[0] = new ImageIcon("img/redhead1.png");color[1] = new ImageIcon("img/bluehead1.png");
		color[2] = new ImageIcon("img/greenhead1.png");color[3] = new ImageIcon("img/pinkhead1.png");
		connectToS();
	}
	
	void connectToS(){	
		try{
			s = new Socket(ip, port);
			os = s.getOutputStream();
			dos = new DataOutputStream(os);
			is = s.getInputStream();
			dis = new DataInputStream(is);
			sendNickName();
			start();
		}catch(UnknownHostException ue){
		}catch(IOException ie){}
	}
	// 버튼 이벤트처리와 관련된 메소드
	void sendNickName(){	//로그인화면의 로그인버튼클릭 밑 엔터 #002
		try{
			dos.writeUTF("#002"+nickName);	//입장메세지 #002
			dos.flush();
		}catch(IOException ie){}
	}
	void sendMsg(){	//게임 시작전 텍스트필드 #001
		try{
			dos.writeUTF("#001"+nickName+">> "+mwh.msg);	//일반메세지 #001
			dos.flush();
		}catch(IOException ie){
		}
	}
	void readyMsg(){	//게임 화면 레디버튼 #003 레디메세지
		try{
			dos.writeUTF("#003"+1);
			dos.flush();
		}catch(IOException ie){
		}
	}
	void noreadyMsg(){	//레디버튼  레디 풀었을때.#004
      try{
         dos.writeUTF("#004"+-1);
         dos.flush();
      }catch(IOException ie){}
    }
	void changeMode(){	//게임 시작전 서버 모드 변경 #005
		try{
			dos.writeUTF("#005");
			dos.flush();
		}catch(IOException ie){}
	}
	void checkAnswer(String msgCheck){		//게임이 시작했을 때, 텍스트 필드 게임메세지 #006
		try{
			dos.writeUTF("#006"+msgCheck);
			dos.flush();
		}catch(IOException ie){}
		//정답이면 score++, turnOutMsg();
	}
	void sendTopic(int selectedTopic){
		try{
			dos.writeUTF("#007"+selectedTopic);
			dos.flush();
		}catch(IOException ie){}
	}
	void sendExitMsg(){
		try{
			dos.writeUTF("#008");
			dos.flush();
		}catch(IOException ie){}
	}
	void exitMsg(){		//게임 화면 나가기버튼, 이건 ioexception에서 처리할수도 있다. #005 퇴장메세지
		
	}
	//

	//스레드#2 >> 
	public void run(){
		acceptInfo();
	}
	
	int turn = 0;		//턴 순서.
	void acceptInfo(){	//OCM에서 정보전달받는 메소드 >> 받은 정보를 화면에 출력.
		String temp = null;String proto = null;String msgfromS = null;
		MainWindow mw = mwh.mw; //클래스로 뺄 때 조심해야함.
		int cNumber = 0;	//접속자 수==클라이언트사이즈 
		 
		try{
			while(true){
				temp = dis.readUTF();
				proto = temp.substring(0,4);
				msgfromS = temp.substring(4);
				//여기서부터 프로토콜 처리 
				if(proto.equals("@002")){	//@002 접속자 수
					cNumber = Integer.parseInt(msgfromS);
					if(cNumber == 0){
						GameMode.gameMode = GameMode.FIRST_CLIENT;
					}
					mw.laPers.setText("<html>"+"Player X "+(cNumber+1)+"</html>");
				}
				
				if(proto.equals("@003")){	//@003 입장했을 때, 클라 컬렉션에 저장
					String i = msgfromS.substring(0,1);
					int index = Integer.parseInt(i);
					String n = msgfromS.substring(1);
					pln(i+" "+n);
					if(!clientCList.containsKey(n)){
						clientCList.put(n, new ClientCharacter(n,index,score,color[index]));
					}
					
					//System.out.println(clientCList.size());
				}
				if(proto.equals("@004")){	//@004 접속한 캐릭터 표시
					//System.out.println(clientCList.get(nickName).index+" "+cNumber);
					if(clientCList.get(nickName).index == cNumber){
						Set<String> keys = clientCList.keySet();
						for(String key: keys) mw.pC.add(clientCList.get(key));
					}else if(clientCList.get(nickName).index < cNumber){
						mw.pC.add(clientCList.get(msgfromS));
					}
					clientCList.get(nickName).laName.setForeground(Color.RED);
					
					mw.revalidate(); mw.repaint();
				}
				if(proto.equals("@005")){	//@005 접속하는 캐릭터 표시
					mw.tShowChat.append(msgfromS+"님께서 게임서버에 접속하셨습니다.\n");
				}
				
				if(proto.equals("@006")){	//@006 레디메세지
					mw.tShowChat.append(msgfromS+"님이 레디\n");
				}
				if(proto.equals("@007")){	//@007 레디메세지
					mw.tShowChat.append(msgfromS+"님이 레디 취소\n");
				}
				if(proto.equals("@008")){	//@008 게임시작메세지
					mw.tShowChat.append(msgfromS+"\n");
					try{
						mw.tShowChat.append("3\n");
						Thread.sleep(1000);
						mw.tShowChat.append("2\n");
						Thread.sleep(1000);
						mw.tShowChat.append("1\n");
						Thread.sleep(1000);
						mw.tShowChat.append("시작\n");
						changeMode();
						mw.clip.start();
						this.new GameTimer().start();	//#3 스레드
					}catch(InterruptedException ite){}
				}
				if(proto.equals("@009")){	
					mw.laAnswer.setText(msgfromS);
				}
				if(proto.equals("@011")){		//게임 끝
					mwh.readymode = 0;
					if(clientCList.get(nickName).index == 0){
						GameMode.gameMode = GameMode.FIRST_CLIENT;
					}
					GameMode.gameMode = GameMode.BEFORE;
					
					Iterator<String> keys = clientCList.keySet().iterator();
					int a = 0;
					String topName = null;
					while(keys.hasNext()){
						String tempName = keys.next();
						int tempScore = clientCList.get(tempName).score;
						if( tempScore > a){
							a = tempScore;
							topName = tempName;
						}
					}
					mw.laTopic.setText("WINNER: "+ topName);
				}
				if(proto.equals("@012")){
					String[] temp1 = msgfromS.split(":");
					String nicktemp = temp1[0];							
					int scoretemp = Integer.parseInt(temp1[1].trim());
					ClientCharacter tempCC = clientCList.get(nicktemp);
					tempCC.score = scoretemp;
					tempCC.laName.setText("<html><br>"+nicktemp+"<br><br>"+tempCC.score+"</html>");
					mw.revalidate();mw.repaint();
				}
				if(proto.equals("@013")){	// 콤보박스
					
					String[] temp1 = msgfromS.split("\\.");
					mw.laTopic.setText("TOPIC:    "+ temp1[0]);
					mw.revalidate();mw.repaint();
				}
				if(proto.equals("@014")){	// 채팅창에 정답이라고 알림
					mw.tShowChat.append("정답!\n");
				}
				if(proto.equals("@015")){	// 채팅창에 틀렸다고 알림
					mw.tShowChat.append("오답!\n");
				}
				if(proto.equals("@001")){	//@001 표시할 메세지
					pln(msgfromS);
					mw.tShowChat.append(msgfromS+"\n");	
				}
				if(proto.equals("@100")){
					mw.tShowChat.append(msgfromS+"님이 퇴장하였습니다.\n");
					mw.pC.remove(clientCList.get(msgfromS));
					clientCList.remove(msgfromS);
					mw.revalidate();mw.repaint();
				}
				//여기까지
			}
		}catch(IOException ie){
		}finally{
			closeAll();
		}
	}
	class GameTimer extends Thread{	//클라의 메소드를 이용하기위해 내부클로
		public void run(){
			cc = clientCList.get(nickName);
			int index = cc.index;	//무조건 자기 인덱스
			pln(index+"자기인덱스"+turn+"현재턴");
			int timeInit = 60; 		//총시간, 턴시간
			GameMode.gameMode = GameMode.MYTURN;
			
			while(true){
				try{
					mwh.mw.revalidate();mwh.mw.repaint();
					mwh.mw.laTime.setText("<html>"+GameMode.BLANK+timeInit+GameMode.BLANK+"</html>"); //라벨에 표시
					timeInit -= 1;
					Thread.sleep(1000);
								
					if(timeInit == 0){
						mwh.mw.laTime.setText("<html>"+GameMode.BLANK+"Time Over"+GameMode.BLANK+"</html>");
						Thread.sleep(1000);
						break;
					}
				}catch(InterruptedException ite){}
			}
			sendExitMsg();	//
			timeInit = 60;
			mwh.mw.clip.stop();
		}
	}

	void pln(String str){
		System.out.println(str);
	}
	void p(String str){
		System.out.print(str);
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
	public static void main(String[] args){
		//new Client();
	}
}
