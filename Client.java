

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
	String ip = "192.168.0.74"; int port = 5555; //�����ϴ� ���Ӽ��� �ּ�
	MWHandler mwh;

	// �������������÷���, ���⵵�־���Ѵ�. cc >> �ӿ� �г���,���ھ�,����
	LinkedHashMap<String,ClientCharacter> clientCList = new LinkedHashMap<String,ClientCharacter>();
	String nickName ="GUEST";int score = 0;
	ClientCharacter cc;
	
	ImageIcon[] color = new ImageIcon[4];
	
	OutputStream os; DataOutputStream dos;	// ���Ͽ� �����ϴ� ����
	InputStream is; DataInputStream dis;	// ���Ͽ��� �޴� ����
	
	

	Client(MWHandler mwh){	//MWH���� �α��ι�ư ������ >>
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
	// ��ư �̺�Ʈó���� ���õ� �޼ҵ�
	void sendNickName(){	//�α���ȭ���� �α��ι�ưŬ�� �� ���� #002
		try{
			dos.writeUTF("#002"+nickName);	//����޼��� #002
			dos.flush();
		}catch(IOException ie){}
	}
	void sendMsg(){	//���� ������ �ؽ�Ʈ�ʵ� #001
		try{
			dos.writeUTF("#001"+nickName+">> "+mwh.msg);	//�Ϲݸ޼��� #001
			dos.flush();
		}catch(IOException ie){
		}
	}
	void readyMsg(){	//���� ȭ�� �����ư #003 ����޼���
		try{
			dos.writeUTF("#003"+1);
			dos.flush();
		}catch(IOException ie){
		}
	}
	void noreadyMsg(){	//�����ư  ���� Ǯ������.#004
      try{
         dos.writeUTF("#004"+-1);
         dos.flush();
      }catch(IOException ie){}
    }
	void changeMode(){	//���� ������ ���� ��� ���� #005
		try{
			dos.writeUTF("#005");
			dos.flush();
		}catch(IOException ie){}
	}
	void checkAnswer(String msgCheck){		//������ �������� ��, �ؽ�Ʈ �ʵ� ���Ӹ޼��� #006
		try{
			dos.writeUTF("#006"+msgCheck);
			dos.flush();
		}catch(IOException ie){}
		//�����̸� score++, turnOutMsg();
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
	void exitMsg(){		//���� ȭ�� �������ư, �̰� ioexception���� ó���Ҽ��� �ִ�. #005 ����޼���
		
	}
	//

	//������#2 >> 
	public void run(){
		acceptInfo();
	}
	
	int turn = 0;		//�� ����.
	void acceptInfo(){	//OCM���� �������޹޴� �޼ҵ� >> ���� ������ ȭ�鿡 ���.
		String temp = null;String proto = null;String msgfromS = null;
		MainWindow mw = mwh.mw; //Ŭ������ �� �� �����ؾ���.
		int cNumber = 0;	//������ ��==Ŭ���̾�Ʈ������ 
		 
		try{
			while(true){
				temp = dis.readUTF();
				proto = temp.substring(0,4);
				msgfromS = temp.substring(4);
				//���⼭���� �������� ó�� 
				if(proto.equals("@002")){	//@002 ������ ��
					cNumber = Integer.parseInt(msgfromS);
					if(cNumber == 0){
						GameMode.gameMode = GameMode.FIRST_CLIENT;
					}
					mw.laPers.setText("<html>"+"Player X "+(cNumber+1)+"</html>");
				}
				
				if(proto.equals("@003")){	//@003 �������� ��, Ŭ�� �÷��ǿ� ����
					String i = msgfromS.substring(0,1);
					int index = Integer.parseInt(i);
					String n = msgfromS.substring(1);
					pln(i+" "+n);
					if(!clientCList.containsKey(n)){
						clientCList.put(n, new ClientCharacter(n,index,score,color[index]));
					}
					
					//System.out.println(clientCList.size());
				}
				if(proto.equals("@004")){	//@004 ������ ĳ���� ǥ��
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
				if(proto.equals("@005")){	//@005 �����ϴ� ĳ���� ǥ��
					mw.tShowChat.append(msgfromS+"�Բ��� ���Ӽ����� �����ϼ̽��ϴ�.\n");
				}
				
				if(proto.equals("@006")){	//@006 ����޼���
					mw.tShowChat.append(msgfromS+"���� ����\n");
				}
				if(proto.equals("@007")){	//@007 ����޼���
					mw.tShowChat.append(msgfromS+"���� ���� ���\n");
				}
				if(proto.equals("@008")){	//@008 ���ӽ��۸޼���
					mw.tShowChat.append(msgfromS+"\n");
					try{
						mw.tShowChat.append("3\n");
						Thread.sleep(1000);
						mw.tShowChat.append("2\n");
						Thread.sleep(1000);
						mw.tShowChat.append("1\n");
						Thread.sleep(1000);
						mw.tShowChat.append("����\n");
						changeMode();
						mw.clip.start();
						this.new GameTimer().start();	//#3 ������
					}catch(InterruptedException ite){}
				}
				if(proto.equals("@009")){	
					mw.laAnswer.setText(msgfromS);
				}
				if(proto.equals("@011")){		//���� ��
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
				if(proto.equals("@013")){	// �޺��ڽ�
					
					String[] temp1 = msgfromS.split("\\.");
					mw.laTopic.setText("TOPIC:    "+ temp1[0]);
					mw.revalidate();mw.repaint();
				}
				if(proto.equals("@014")){	// ä��â�� �����̶�� �˸�
					mw.tShowChat.append("����!\n");
				}
				if(proto.equals("@015")){	// ä��â�� Ʋ�ȴٰ� �˸�
					mw.tShowChat.append("����!\n");
				}
				if(proto.equals("@001")){	//@001 ǥ���� �޼���
					pln(msgfromS);
					mw.tShowChat.append(msgfromS+"\n");	
				}
				if(proto.equals("@100")){
					mw.tShowChat.append(msgfromS+"���� �����Ͽ����ϴ�.\n");
					mw.pC.remove(clientCList.get(msgfromS));
					clientCList.remove(msgfromS);
					mw.revalidate();mw.repaint();
				}
				//�������
			}
		}catch(IOException ie){
		}finally{
			closeAll();
		}
	}
	class GameTimer extends Thread{	//Ŭ���� �޼ҵ带 �̿��ϱ����� ����Ŭ��
		public void run(){
			cc = clientCList.get(nickName);
			int index = cc.index;	//������ �ڱ� �ε���
			pln(index+"�ڱ��ε���"+turn+"������");
			int timeInit = 60; 		//�ѽð�, �Ͻð�
			GameMode.gameMode = GameMode.MYTURN;
			
			while(true){
				try{
					mwh.mw.revalidate();mwh.mw.repaint();
					mwh.mw.laTime.setText("<html>"+GameMode.BLANK+timeInit+GameMode.BLANK+"</html>"); //�󺧿� ǥ��
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
	void closeAll(){ //���ᰴü�� �ݱ�
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
