/**************************************************
*
* 		ConCanvas irc module.
*
***************************************************/
import java.io.*;
import java.lang.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

////////////////////////////////////////////////////
// Window Object Class
////////////////////////////////////////////////////
public class winobj {

	int maxlines;  			  		//console lines maximum for scroll

	String bufline[];			 	//console StringLine buffer

	int scrlines=0;					//screen maximum lines (-1)=title
	int scrwidth=0;		 		        //may extend screen width
	int scrpoint=0;				        //screen pointer (need for extend)

	boolean	scroll_lock=false;

	int selptr=1;					//selector ptr
	int pscrbuf=1;  				//screen buffer pointer (need for scroll)
	int pline=1;					//scroll buffer pointer

 	int color[];					//global current color

	int style_i=0;
	int style_l=0;

	String mode;
	String objname;
	String topic;
	Hashtable nicks;
	
	public void setbuflines(int i){
		maxlines=i;
		bufline = new String[i+2]; 		//lines array (console buffer)
							// i+2 is padding =)
	}
} // class winobj

