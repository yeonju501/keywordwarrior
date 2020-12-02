

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


class ClientCharacter extends JPanel{
	ImageIcon ii; int score; String nickName; int index;
	Color clear = new Color(255, 0, 0, 0);

	ClientCharacter(String nickName,int index, int score, ImageIcon ii){
		this.nickName = nickName;
		this.index = index;
		this.score = score;
		this.ii = ii;
		makeComponent();
		
	}
	
	public void paint(Graphics g){
		g.drawImage(ii.getImage(),0,0,null);
		setOpaque(false);
		super.paint(g);
	}

	JLabel laName;
	void makeComponent(){
		Font maple3 = new Font("메이플스토리", Font.BOLD, 20);
		laName = new JLabel("<html><br>"+nickName+"<br><br>"+score+"</html>"); 
		laName.setFont(maple3);laName.setForeground(Color.WHITE);
		laName.setBackground(clear);laName.setOpaque(false);
		this.add(laName);
		setBackground(clear);
		revalidate();repaint();
	}
}
