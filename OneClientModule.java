import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

class OneClientModule extends Thread{
	Server server;
	Socket s;
	InputStream is; DataInputStream dis;	//���Ͽ��� �޴� ����
	OutputStream os; DataOutputStream dos;	//���Ͽ� �����ϴ� ����
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
			//�α��θ޼��� �޴ºκ�	#002
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
	/* ������ ���޹���
	*/	
		String temp = null;String proto = null;String msg = null;
		String nickName = "GUEST";	//�������� ��ۿ�
		int readyN =0;	// ���ӽ��� Ʈ����
			
		try{
			//#002 ������ ��.
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
			server.pln(nickName+"�Բ��� ���Ӽ����� �����Ͽ����ϴ�");
			//
			while(true){
				//server.pln(server.readyN+"  "+readyN);
				if(GameMode.gameMode == GameMode.BEFORE & server.readyN == server.clientMap.size() & server.readyN != 1){
					broadcast("@008"+"3�� �� ���� ����");
					server.pln("���� ����"); // 
				}
				temp = dis.readUTF();
				proto = temp.substring(0,4);
				msg = temp.substring(4);
				//�������� ���ǹ� �б�
				if(proto.equals("#001")){
					broadcast("@001"+msg);	//�Ϲݸ޼��� #001
					server.pln(msg);//������ �����ϴ� �޼���
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
				
				if(proto.equals("#006")){	//����üũ > ��������
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
				if(proto.equals("#007")){		// �޺��ڽ�
					server.wList.clear();
					String indexS = msg.trim();
					int index = Integer.parseInt(indexS);
					server.wordRead(index);
					String topicname = server.file[index].getName();
					broadcast("@013"+topicname);
					for(String item: server.wList) server.pln(item);
				}
				if(proto.equals("#008")){	//�������������.
					
					broadcast("@011");	//��� Ŭ���̾�Ʈ���� ����
					GameMode.gameMode = GameMode.BEFORE;
					server.readyN = 0;
					server.turn = 0;
				}
				
			}
		}catch(IOException ie){	//Ŭ���̾�Ʈ�� ������ �������� �� �߻��ϴ� ����
			closeAll();
			server.clientMap.remove(nickName);
			server.clientCList.remove(nickName);
			String msgE = nickName;
			broadcast("@100"+msgE);
			server.pln(msgE);
			/*����ó�� ���?*/
		}finally{	
		}
	}

	void broadcast(String temp){	
		/* ��� Ŭ���̾�Ʈ(�ڽ�����)���� ���������ϴ� ��� 
			>> acceptData()���� ���� �������ݷ� ���ǹ����� �б� > 
		*/
		Set<String> keys = server.clientMap.keySet();
		Iterator<String> iter = keys.iterator();
		try{
			while( iter.hasNext()){
				String key = iter.next();
				//server.pln(key); ���ͷ����ͼ���Ȯ�� �������� ���� ����.
				OneClientModule ocm = server.clientMap.get(key);
				ocm.dos.writeUTF(temp);
				ocm.dos.flush();
			}
		}catch(IOException io){
		}finally{
		}
	}
	void broadcast(String temp, String nickName){ //�� ��ü�� ����� Ŭ���̾�Ʈ���Ը�.
		try{
			OneClientModule ocm = server.clientMap.get(nickName);
			ocm.dos.writeUTF(temp);
			ocm.dos.flush();
		}catch(IOException io){
		}finally{
		}
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
}
