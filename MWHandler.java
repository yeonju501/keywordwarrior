import java.awt.event.*;
import javax.swing.*;

class MWHandler implements ActionListener{
	MainWindow mw;
	Client client;	//MWH에서 만들어준 클라객체를 이용하기위해서 멤버로 만듦.
	MWHandler(MainWindow mw){
		this.mw = mw;
	}
	String nickN, msg, msgCheck;	//msg는 textfield에 입력되는 메세지
	int readymode=0;
	int selectedTopic =0;
	public void actionPerformed(ActionEvent e){
		Object obj = e.getSource();
		//로그인창에서의 이벤트처리
		if(obj == mw.bStart||obj == mw.tfName){
			
			//문제점: 닉네임의 중복을 회피해야함
			nickN = mw.tfName.getText();
			//System.out.println(nickN);
			mw.removeComp();
			mw.gameComp();
			client = new Client(this);//서버에접속(); >> 소켓을 연결한다, nickName 을 전달한다 >> 1.Client.java >> MainWindow.java >> MWHandler.java >> Client.java (정보의 흐름)
			mw.tInputChat.grabFocus();
			mw.makeMusic();
		}else if(obj == mw.bOut){
			//옵션페인을 출력하여 나가는것을 확인한다
			System.exit(-1);
		}

		//게임창에서의 이벤트처리
		if(obj == mw.tInputChat){
			if(GameMode.gameMode == GameMode.BEFORE ||GameMode.gameMode == GameMode.FIRST_CLIENT){
				msg = mw.tInputChat.getText();
				client.sendMsg();
				mw.tInputChat.setText("");
			}else if(GameMode.gameMode == GameMode.MYTURN){
				msgCheck = mw.tInputChat.getText();
				client.checkAnswer(msgCheck);
				mw.tInputChat.setText("");
			}
		}else if(obj==mw.bReady){
			if(GameMode.gameMode == GameMode.BEFORE || GameMode.gameMode == GameMode.FIRST_CLIENT){
				if(readymode == 0){
					client.readyMsg();
					readymode = 1;//레디
				}else if(readymode==1){
					client.noreadyMsg();
					readymode = 0;
				}
			}else{/*닫아놓기*/}
		}else if(obj==mw.bExit){
			if(GameMode.gameMode == GameMode.BEFORE || GameMode.gameMode == GameMode.FIRST_CLIENT){
				System.exit(-1);
			}else{/*우선 닫아놓기, 나중에 접속종료할 때 겜중단되는거 하면 열자*/}
		}else if(obj==mw.combo){
			if(GameMode.gameMode == GameMode.FIRST_CLIENT){
				selectedTopic = mw.combo.getSelectedIndex();
				client.sendTopic(selectedTopic);
			}else{
				mw.tShowChat.append("방장만 제시어를 선택할 수 있습니다\n");
			}
		}
		
	}

}
