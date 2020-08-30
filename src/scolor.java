/**************************************************
*
* 	  Color Select Canvas module.
*
***************************************************/
import java.io.*;
import java.lang.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

//////////////////////////////////////////////////
// Color select CANVAS.
/////////////////////////////////////////////////
public class scolor extends Canvas implements CommandListener {

	Display 	display;
	concanvas 	lcanv;
	uimod		ui;
	
	int	subw,subh;
	int	sqx,sqy;

	Font	sft;

	int	cp=0;

	int	fc;
	int	bc;

	String	ctmsg;


	private Command SBTN_OK      = new Command("Ok", Command.SCREEN, 1);
	private Command SBTN_CANCEL  = new Command("Cancel", Command.CANCEL, 1);

	////////////////////////////////////
	// Constructor
	////////////////////////////////////
	public scolor (Display d,concanvas c,uimod u) {
		display=d;
		lcanv=c;
		ui=u;

		sft = Font.getFont(lcanv.fttype,Font.STYLE_PLAIN,Font.SIZE_MEDIUM);

		subw=(getWidth()-10)/4;
		subh=(getHeight()-sft.getHeight()-10)/4;

		resetSquareCord();
		
		this.addCommand(SBTN_OK);
		this.addCommand(SBTN_CANCEL);
		this.setCommandListener(this);
		reset();
	
	}
	/////////////////////////////////////
	// reset
	////////////////////////////////////
	public void reset() {
		fc=-1;
		bc=-1;
	}

	/////////////////////////////////////
	// reset
	////////////////////////////////////
	public void resetSquareCord() {
		sqx=0;
		sqy=sft.getHeight();
	}
		
	////////////////////////////////////
	// paint
	////////////////////////////////////
	public void paint(Graphics g) {
		showcolors(g);
	}

	////////////////////////////////////
	// Show colors
	////////////////////////////////////
	public void showcolors(Graphics g) {
		int px=2,py=2+sft.getHeight(),c=0;

		if ( fc == -1 ) { ctmsg = "foreground"; } else { ctmsg="background"; }
 
		g.setColor(lcanv.getcolormap(0)); //white
	        g.fillRect(0,0,getWidth(),getHeight());
		g.setColor(43690);				//blue
	        g.fillRect(0,0,getWidth(),sft.getHeight());	
		g.setColor(lcanv.getcolormap(1));
		g.drawString("["+ctmsg+"] code:"+cp,0,0,Graphics.TOP | Graphics.LEFT);

	

		for (int a=0; a < 4 ; a++ ) {
			for (int b=0; b < 4 ; b++ ) {
				g.setColor(lcanv.getcolormap(c));
				g.fillRect(px,py,subw,subh);
				px=px+subw+2;
				c++;
			}
			py=py+subh+2;
			px=2;
		}
		showsquare(g);
	}

	////////////////////////////////////
	// Square selector.
	////////////////////////////////////
	public void showsquare(Graphics g) {
		g.setStrokeStyle(Graphics.DOTTED);
		g.setColor(lcanv.getcolormap(1));
		g.drawRect(sqx+1, sqy+1, subw+1, subh+1);
	} 

	//////////////////////////////////////
	// Key event handler
	//////////////////////////////////////
	protected void keyPressed(int keyCode) {

		System.out.println("[keyPressed]="+keyCode);

		if (keyCode == KEY_NUM2) {					//up
			if ( (sqy-subh-2) < sft.getHeight() ) return;
			sqy=sqy-subh-2;
			cp=cp-4;
			repaint();
		}
		if (keyCode == KEY_NUM8) {					//down
			if ( (sqy+subh+2+subh+2) > getHeight() ) return;
			sqy=sqy+subh+2;
			cp=cp+4;
			repaint();
		}
		
		if (keyCode == KEY_NUM4) {					//left
			if ( (sqx-subw-2) < 0 ) return;
			sqx=sqx-subw-2;
			cp--;
			repaint();
		}

		if (keyCode == KEY_NUM6) {					//right
			if ( (sqx+subw+2+subw+2) > getWidth() ) return;
			sqx=sqx+subw+2;
			cp++;
			repaint();
		}
	}
	/////////////////////////////////////////////////////
	// Command Actions
	/////////////////////////////////////////////////////
	public void commandAction(Command c, Displayable d) {
		if ( c == SBTN_OK ) {
			if ( fc == -1 ) { 
				fc=cp; cp=0; repaint(); 
				resetSquareCord();

			} 
			else { 
				bc=cp;  
				ui.editbox.setString(ui.editbox.getString()+"%c%"+fc+","+bc);
				display.setCurrent(ui.editbox);
				reset();
				resetSquareCord();
			}
			
		}

		if ( c == SBTN_CANCEL ) {
			if ( fc == -1 ) {
				cp=0;
				resetSquareCord();
				display.setCurrent(ui.editbox);
			} else { 
				fc=-1;
				resetSquareCord();
				repaint();
			}
		}
	
	}

} //canvas