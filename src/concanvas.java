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

//////////////////////////////////////////////////
// Console CANVAS.
/////////////////////////////////////////////////
public class concanvas extends Canvas {
        
	public concanvas canvas;
        public Display display;
        public lala main;
	public database dbrms;
	public netsocket sock;
	public cmdmod cmdm;


	public int idwin=0;
	public int maxwins=10;			//max may created windows
	public int cntwins=0;			//already created windows // may increase for each new window create
	public winobj wins[];

	int ftsize=Font.SIZE_MEDIUM;
	int fttype=Font.FACE_SYSTEM;

	Font ft_p; 
	Font ft_i; 
	Font ft_u; 
	Font ft_ui;
	Font ft;
	Font sft;

	int defcolorfore=1;
	int defcolorback=0;

	Calendar calendar;			//for timestamp

	int mousey=0;
	int mousex=0;

	/////////////////////////////////////
	// Constructor
	/////////////////////////////////////
        public concanvas (){
		super();
        	canvas=this;

		// Init timezone
				
		calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+4"));

        }

	////////////////////////////////////
	// Canvas last init
	////////////////////////////////////
	public void initcanvas() {

		// Init Font
		
		if ( dbrms.fontindex == 0 ) ftsize=Font.SIZE_SMALL;
		if ( dbrms.fontindex == 1 ) ftsize=Font.SIZE_MEDIUM;
		if ( dbrms.fontindex == 2 ) ftsize=Font.SIZE_LARGE;

		ft_p  = Font.getFont(fttype,Font.STYLE_PLAIN,ftsize);			//font      
		ft_i  = Font.getFont(fttype,Font.STYLE_ITALIC,ftsize);			//italic    
		ft_u  = Font.getFont(fttype,Font.STYLE_UNDERLINED,ftsize);		//underlined
		ft_ui = Font.getFont(fttype,Font.STYLE_ITALIC+Font.STYLE_UNDERLINED,ftsize);   
		ft=ft_p;	
		sft=ft_p;
	}	
	
	////////////////
	// paint
	////////////////
	public void paint(Graphics g) {
//		System.out.println("repaint!");
		resetFont(g);
		g.setFont(ft);
		showwin(g);
	}
	////////////////
	// show canvas
	////////////////
	public void showcanvas() {
		display.setCurrent(this);
		main.lockmenu=false;		//free menulock
		main.mlock.notifysignal();	//free waitlock
	}

	////////////////
	// WinObj Init
	////////////////
	public void initobject(int idx) {

		wins[idx].scrlines=(getHeight()/ft.getHeight())-1;	//screen maximum lines (-1)=title
		wins[idx].setbuflines(wins[idx].scrlines+dbrms.bufsize);

		wins[idx].scrwidth=getWidth();				//may extend screen width	

		wins[idx].color=new int[2];
		wins[idx].color[0]=defcolorfore;			//red
		wins[idx].color[1]=defcolorback;     			//black
	}

	/////////////////
	// Console out.
	/////////////////	
	public void consout(int idc,String estr) {
        
	String str;

	if ( dbrms.timestamp ) {
	        String tm = "[" + (calendar.get(calendar.HOUR_OF_DAY) >= 10 ? "" : "0") + calendar.get(calendar.HOUR_OF_DAY) + ":" + (calendar.get(calendar.MINUTE) >= 10 ? "" : "0") + calendar.get(calendar.MINUTE) + "] ";
		str=tm+estr;
	} else { str=estr; }

	int wcur=0;
	int slen=0;
	int nlen=0;
	int plen=0;

	while ( str.length() != slen && ( str.charAt(slen) != 0xD && str.charAt(slen) != 0xA ) ) { slen++; }

	int alen=slen;
	
	//System.out.println("alen="+alen+" mypline="+wins[idc].pline+" wins[idc].bufline[]="+wins[idc].bufline[wins[idc].pline]);

	if ( wins[idc].bufline[wins[idc].pline] != null ) { wcur=ft.stringWidth(getwctrlcode(wins[idc].bufline[wins[idc].pline])); }

	if ( alen == 0 ) return;
		    
	nlen=alen;
				
	while ( alen != 0 ) {

		//System.out.println("[in_pline]="+wins[idc].pline+" alen="+alen+" nlen="+nlen+" plen="+plen+" str.length()="+str.length()+" wcur="+wcur+" scrwidth="+wins[idc].scrwidth);
		
		if ( ((wins[idc].scrwidth-wcur)/sft.charWidth('M')) == 0 ) { wcur=0; wins[idc].pline++; }	//next line if width < pixel size of one char
               	
		if ( wins[idc].pline == wins[idc].maxlines+1 ) { // if pline over(>) maxline ,todo scroll buffer

			//System.out.println("scroll_move one line !!!");

			wins[idc].pline--;			 // back pline to =maxline

			while ( wins[idc].pscrbuf <= (wins[idc].pline-wins[idc].scrlines)+1 && !wins[idc].scroll_lock ) { // because 1-18 2-19 3-20
					
       	        		if ( wins[idwin].objname == wins[idc].objname ) {  repaint(); }	//showscr(g,wins[idc].pscrbuf);
				wins[idc].pscrbuf++;
			}

			if ( !wins[idc].scroll_lock ) wins[idc].pscrbuf--; 			//because cycle while return wins[idc].pscrbuf++;
			
			if ( wins[idc].scroll_lock ) {
				if ( wins[idc].pscrbuf != 1 ) { wins[idc].pscrbuf--; }		//тащить буффер назад если скроллок захвачен. 
				else {								//wins[idc].pscrbuf == 1
				//	if ( ( wins[idc].bufline[1].charAt(0) == 0x07 || wins[idc].bufline[1].charAt(1) == 0x07 ) && wins[idc].selptr > 1 ) { wins[idc].selptr--; System.out.println("####fuck$$$$"+(wins[idc].bufline[1].charAt(0)+0x30)+" and ="+(wins[idc].bufline[1].charAt(1)+0x30)+" str="+wins[idc].bufline[1]); }
				if ( wins[idc].bufline[2].charAt(0) == 0x06 && wins[idc].selptr > 1 ) { wins[idc].selptr--; /* System.out.println("####fuck$$$$"+(wins[idc].bufline[1].charAt(0)+0x30)+" and ="+(wins[idc].bufline[1].charAt(1)+0x30)+" str="+wins[idc].bufline[1]); */ }
					else { /* System.out.println("String don fucking have = "+(wins[idc].bufline[1].charAt(0)+0)+" and ="+(wins[idc].bufline[1].charAt(1)+0)+" str="+wins[idc].bufline[1]); */ }
				}
			}

			for (int mv=1;mv != wins[idc].maxlines;mv++ ) {
				
				wins[idc].bufline[mv]=wins[idc].bufline[mv+1];
			}		
			
			wins[idc].bufline[wins[idc].maxlines]=null;	//clear size of last line in buffer
		}


		int eon;
		
		//System.out.println("symbols count="+(wins[idc].scrwidth)/sft.charWidth('M'));
			
		while ( (eon = (wins[idc].scrwidth-wcur)/sft.charWidth('M')) == 0 ) { wcur=0; wins[idc].pline++;  }

		nlen = showchar2bufchar(eon,nlen,plen,str);


		//System.out.println("debug: eon="+eon+" nlen="+nlen+" alen="+alen+" plen="+plen);
	
		alen=alen-(nlen-plen);							//calc last all size
			
		if ( wins[idc].bufline[wins[idc].pline] == null ) { 

			//System.out.println("new pline ! == null");

/*			if ( plen == 0  && alen == 0 ) {				//one line 

				wins[idc].bufline[wins[idc].pline]="\006\007\004";

			} else if ( alen == 0 && plen != 0 ) {				//end of line + prevcolor 
				
				wins[idc].bufline[wins[idc].pline]="\007"+getprevcolor(idc,wins[idc].pline); 

			} else if ( plen == 0 && alen != 0 ) {				//start of line
				
				wins[idc].bufline[wins[idc].pline]="\006\004";

			} else if ( plen != 0 && alen != 0 ) { 						//middle of line = prevcolor only
				
				wins[idc].bufline[wins[idc].pline]=getprevcolor(idc,wins[idc].pline); 
			} 
*/

			if ( plen == 0 ) { wins[idc].bufline[wins[idc].pline]="\006\004"; }
			else { wins[idc].bufline[wins[idc].pline]=getprevcolor(idc,wins[idc].pline); }

		}

		wins[idc].bufline[wins[idc].pline]=wins[idc].bufline[wins[idc].pline]+str.substring(plen,nlen);	//old string + new string = string line 
		
		//System.out.println("$$$$$$$$$$$$$$:"+str.substring(plen,nlen));

		wcur=wcur+ft.stringWidth(getwctrlcode(str.substring(plen,nlen)));	//calc new pixel width on line
		plen=nlen;								//last position as first "pointer from"
		nlen=slen;								//reset length as last position //slen = full string lenght without crlf
			
			
	} //while (alen != 0)
			
	
	        wins[idc].pline++; 
		wins[idc].bufline[wins[idc].pline]=null;

	///////////////////////////////////////////////////////////////////////////////
	//System.out.println("[show_screen] pline="+wins[idc].pline);

	if ( !wins[idc].scroll_lock ) {

		if ( wins[idc].pline <= wins[idc].scrlines ) {  
			if (wins[idc].objname == wins[idwin].objname ) { wins[idc].selptr=count_sel_ptr(idc); repaint(); } //showscr(g,wins[idc].pscrbuf);
		} else {
			while ( wins[idc].pscrbuf <= (wins[idc].pline-wins[idc].scrlines) ) { 
				wins[idc].selptr=count_sel_ptr(idc);
				if (wins[idc].objname == wins[idwin].objname ) repaint(); //showscr(g,wins[idc].pscrbuf);
				wins[idc].pscrbuf++;

			}
			wins[idc].pscrbuf--;	//because cycle while return wins[idc].pscrbuf++;
		}
	} else if (wins[idc].objname == wins[idwin].objname ) repaint();	//repaint from pscrbuf position.


	//System.out.println("crlf="+crlf+" wcur="+wcur+" out pline="+wins[idc].pline);
	
	
	} //consout


	///////////////////////////////////
	// count selection regions
	///////////////////////////////////
	int count_sel_ptr(int idcs) {

		int i=1;
		int p=wins[idcs].pscrbuf;

		//System.out.println("enter ! pscrbuf="+p);

		for (int ln=p+1;ln <= (p+wins[idcs].scrlines-1);ln++ ) {
			
			if ( wins[idcs].bufline[ln] == null ) break;
			if ( wins[idcs].bufline[ln].charAt(0) == 0x06 ) { i++; }
			//System.out.println("line="+wins[idcs].bufline[ln]);
		}
		
		//System.out.println("pscrbuf="+p+"count_sel_ptr="+i);

		return i;
	}


	///////////////////////////////////
	// showchar to buffer char.
	//-------------------------
	// count control chars
	///////////////////////////////////
	int showchar2bufchar(int xeon,int xnlen,int xplen,String xstr) {
		
		int rlpi=0,pi=xplen,a=0;
		int fake[]=new int[2];
			
		//System.out.println("xstr="+xstr.substring(xplen,xnlen));

		while ( xeon != rlpi && pi < xnlen) {

		//System.out.println("eon="+xeon+" pi="+pi+" nlen="+xnlen+" plen="+xplen+" rlpi="+rlpi);


			if ( xstr.charAt(pi) == (char)0x03 ) {		//color
				a=getctrlcodesize(pi,xstr,fake);
				pi=pi+a;
								
			} else if (  xstr.charAt(pi) == (char)0x02 || xstr.charAt(pi) == (char)0x16 || xstr.charAt(pi) == (char)0x1f || xstr.charAt(pi) == (char)0x04 || xstr.charAt(pi) == (char)0x05 ) {
				pi++;
			} else {
				pi++;
				rlpi++;	
			}  
		}
	return pi;		

	}
       	//////////////////////////////////////
	// Get string without ctrl code.
	//////////////////////////////////////
	String getwctrlcode(String s) {
		StringBuffer lala=new StringBuffer();
		int bi=0,ei=0,a=0,slen=s.length();
		int fake[]=new int[2];

		while ( ei != slen ) {
		
			if ( s.charAt(ei) == (char)0x03 ) {		//color
				a=getctrlcodesize(ei,s,fake);
				lala.append(s.substring(bi,ei));
				ei=ei+a;
				bi=ei;
				
			} else if ( s.charAt(ei) == (char)0x02 || s.charAt(ei) == (char)0x16 || s.charAt(ei) == (char)0x1f || s.charAt(ei) == (char)0x04 || s.charAt(ei) == (char)0x05 || s.charAt(ei) == (char)0x06 || s.charAt(ei) == (char)0x07 ) {
				lala.append(s.substring(bi,ei));
				ei=ei+1;
				bi=ei;
			} else {
				ei=ei+1;	
			}  
		}
		lala.append(s.substring(bi,ei));
	return(lala.toString());		
	}
	//////////////////////////////////////
	// Get ctrl code size.
	//////////////////////////////////////
	int getctrlcodesize(int i,String s,int kk[]) {
	//	color[0]=-1;	//foreground
	//	color[1]=-1;  	//background
                int z=s.length()-i-1;
	//	System.out.println("z="+z+"s="+s.substring(i,s.length()));

                int e=1,f=0;
		if ( z != 0 && s.charAt(i+1) >= 0x30 && s.charAt(i+1) <= 0x39 ) { kk[0] = (s.charAt(i+1) & 0x0f); e++; z--; } else { return e; }
		if ( z == 0 ) { return e; }
		if ( s.charAt(i+2) != ',' ) {
			f=1;
			if ( z != 0 && s.charAt(i+2) >= 0x30 && s.charAt(i+2) <= 0x39 ) { kk[0] = (kk[0]*10)+(s.charAt(i+2) & 0x0f); e++; z--; } else { return e; }
		} else { z--; e++; } // == ','
		if ( f == 1 ) if ( z == 0 || s.charAt(i+3) != ',' ) { return e; } else { z--; e++; }
		if ( z != 0 && s.charAt(i+3+f) >= 0x30 && s.charAt(i+3+f) <= 0x39 ) { kk[1]=(s.charAt(i+3+f) & 0xf); e++; z--; } else { return e-1; }
		if ( z != 0 && s.charAt(i+4+f) >= 0x30 && s.charAt(i+4+f) <= 0x39 ) { kk[1] = (kk[1]*10)+(s.charAt(i+4+f) & 0xf ); e++; z--; } else { return e; }
		return e;	
	}


	protected void pointerPressed(int x, int y) {
		
		System.out.println("pointerPressed x="+x+" y="+y);

		mousex=x;
		mousey=y;

	}
	protected void pointerReleased(int x, int y) {
		
		System.out.println("pointerReleased x="+x+" y="+y);

		if ( ( mousex < x ) && (x-mousex) >= ((wins[idwin].scrwidth)/2)  ) { 	//right
			if (idwin < cntwins-1 ) idwin++;		
			repaint();
			return;
		} else if ( ( mousex > x ) && (mousex-x) >= ((wins[idwin].scrwidth)/2) ) { 	//left
			if (idwin != 0 ) idwin--;		
			repaint();
			return;
		}



		if ( y > mousey ) { //DOWN
			int chkline=0;
			if ( wins[idwin].bufline[wins[idwin].pline] != null ) { chkline = 1; }
			if ( count_sel_ptr(idwin) != wins[idwin].selptr ) { wins[idwin].selptr++; }
			else if ( wins[idwin].pscrbuf < (wins[idwin].pline-wins[idwin].scrlines)+chkline ) { wins[idwin].pscrbuf++; wins[idwin].selptr=count_sel_ptr(idwin); }
			repaint();
		}

		if ( y < mousey ) { //UP
			if ( wins[idwin].selptr == 1 && wins[idwin].pscrbuf != 1 ) { 
				wins[idwin].pscrbuf--;					//selptr остаётся = 1
			} else if ( wins[idwin].selptr > 1 ) { wins[idwin].selptr--; }
			repaint();
		}

	}

	//////////////////////////////////////
	// Key event handler
	//////////////////////////////////////
	protected void keyPressed(int keyCode) {

//	System.out.println("[keyPressed]="+keyCode);

	if (keyCode == KEY_NUM1) {
		if (idwin != 0 ) idwin--;		
		repaint();

	}
	if (keyCode == KEY_NUM7) {
		//String s3 = "JOIN #kkk\n";
		//sock.send(s3.getBytes(),s3.length());

		if ( wins[idwin].scroll_lock ) wins[idwin].scroll_lock=false;
		if ( !wins[idwin].scroll_lock ) wins[idwin].scroll_lock=true;
	}

	if (keyCode == KEY_NUM3) {
		if (idwin < cntwins-1 ) idwin++;		
		repaint();
	}

	if (keyCode == KEY_NUM4) {	// <<LEFT
		if ( (wins[idwin].scrpoint+ft.charWidth('a')) >= 0 ) { wins[idwin].scrpoint=0; }
		else { wins[idwin].scrpoint=wins[idwin].scrpoint+ft.charWidth('a'); }
//		showscr(gg,wins[idwin].pscrbuf);
        }
	if (keyCode == KEY_NUM6) {	// RIGHT>>
		if ( (wins[idwin].scrpoint-ft.charWidth('a')) < -(wins[idwin].scrwidth-getWidth()) ) { wins[idwin].scrpoint=-(wins[idwin].scrwidth-getWidth()); }
		else { wins[idwin].scrpoint=wins[idwin].scrpoint-ft.charWidth('a'); }
//		showscr(gg,wins[idwin].pscrbuf);
	}
	if (keyCode == KEY_NUM8 ) { //DOWN
		int chkline=0;
		if ( wins[idwin].bufline[wins[idwin].pline] != null ) { chkline = 1; }
		if ( count_sel_ptr(idwin) != wins[idwin].selptr ) { wins[idwin].selptr++; }
		else if ( wins[idwin].pscrbuf < (wins[idwin].pline-wins[idwin].scrlines)+chkline ) { wins[idwin].pscrbuf++; wins[idwin].selptr=count_sel_ptr(idwin); }
		repaint();
		}

	if (keyCode == KEY_NUM2 ) { //UP
		if ( wins[idwin].selptr == 1 && wins[idwin].pscrbuf != 1 ) { 
			wins[idwin].pscrbuf--;					//selptr остаётся = 1
		} else if ( wins[idwin].selptr > 1 ) { wins[idwin].selptr--; }
		repaint();
	}

	if (keyCode == KEY_NUM9 ) { //BUFFER
		//System.out.println("mouse="+hasPointerEvents());

		/*int cnt=1;
		while ( wins[idwin].bufline[cnt] != null  ) { 
		System.out.println("["+cnt+"]["+(wins[idwin].bufline[cnt].charAt(0)+0)+"]["+(wins[idwin].bufline[cnt].charAt(1)+0)+"]:"+wins[idwin].bufline[cnt]);
		cnt++;
		}	
		
		System.out.println("objname="+wins[idwin].objname);
		for (Enumeration e = wins[idwin].nicks.keys() ; e.hasMoreElements() ;) {
			Object key = e.nextElement();
			System.out.println("key="+key+" val="+main.showmods((boolean[])wins[idwin].nicks.get(key),main.sprefix));
		}*/

		if ( wins[idwin].objname.equals("status") ){ cmdm.handleinit("msg $txt $txt"); }
		else { cmdm.handleinit("msg "+wins[idwin].objname+" $txt"); }

	}


	if (keyCode == KEY_NUM5 ) {

	consout(0,"sttopic="+wins[0].topic);

	//main.cmdm.handleinit("msg $chan xuito $boxaa(zaza)");
	//consout(idwin,"123456789012345678901234567890123456789\003\003\003\003");
	//System.out.println("selected region="+get_selected_region());
//	repaint();

/*        String bla = "k"+(char)0x3+"1,4"+"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaz";
	String kk  = "fbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"+(char)0x3+"3,7"+"bbbbbbbz";
	String tt  = "kccccccc"+(char)0x3+"4,6"+"cccccccccccc"+(char)0x3+"1,4"+"cccccccccccccccccccz";
	String nn  = "kpppppppppppppp"+(char)0x3+"1,4"+"ppppppppppppppppppppppppz";

        clrscr(gg);

//	consout(gg,"");
//	consout(gg,getwctrlcode("ppppvvvv"));
	
//	System.out.println(""+getctrlcodesize(0,""+(char)0x3+"3,1"+"vvvv",color));
//	showstr("lala"+(char)0x3+"1,4xui",10,10);

//	consout(gg,"k"+(char)0x3+"1,4"+"aaaaaaaaaaa"+(char)0x3+"1,0"+"aaaaaaaaaaaaaaaaaaaaaaaaaaaz");
//	consout(gg,""+(char)0xD+(char)0xA+"trrrrrh");

	consout(bla); //0	
	consout(kk);	//1
	consout(tt);	//2
	consout(tt);	//3
	consout(tt);	//4
	consout(tt);	//5
	consout(tt);	//6
	consout(tt);	//7
	consout(tt);	//8
	consout(tt);	//9
	consout(tt);	//10
	consout(tt);	//11
	consout(tt);	//12
	consout(tt);	//13
	consout(tt);	//14
	consout(tt);	//15
	consout(tt);	//16

//	consout(tt);	//16
  
	consout(tt);	//17
	consout(tt);	//18
	consout(nn); //19

//	consout(bla);	*/
                }
	}
	/////////////////////////////////////
	// get prev color
	/////////////////////////////////////
	public String getprevcolor(int idcl,int sline){
		int gpcolor[]= new int[2];
		int inx=0,inxd,retstat;
		char tl=0,ti=0,tc=0;
		String stlcode=""+(char)0x5;
		

		gpcolor[0]=defcolorfore; gpcolor[1]=defcolorback;
		
		if ( sline != 1 ) {
			
			if ( (inxd=wins[idcl].bufline[sline-1].indexOf(0x4)) == -1 ) { inxd=0; }

			inx=inxd;

			while ( (inx=wins[idcl].bufline[sline-1].indexOf(0x3,inx)) != -1 ) {
				retstat = getctrlcodesize(inx ,wins[idcl].bufline[sline-1],gpcolor);
				if ( retstat == 1 ) { gpcolor[0]=defcolorfore; gpcolor[1]=defcolorback; }
				//if ( retstat < 4 ) { gpcolor[0]=defcolorfore; }			//if size of color code < 4 we are dont have background code.
				inx=inx+1;
			}

			inx=inxd;
			
			while ( (inx=wins[idcl].bufline[sline-1].indexOf(0x2,inx)) != -1 ) {  //italic
				if ( tc == 0x2 ) { tc = 0; } else { tc=0x2; }
				inx=inx+1;
			}
			
			inx=inxd;
			
			while ( (inx=wins[idcl].bufline[sline-1].indexOf(0x16,inx)) != -1 ) {  //invert
				if ( ti == 0x16 ) { ti = 0; } else { ti=0x16; }
				inx=inx+1;
			}
			
			inx=inxd;
			
			while ( (inx=wins[idcl].bufline[sline-1].indexOf(0x1f,inx)) != -1 ) {  //lined
				if ( tl == 0x1f ) { tl = 0; } else { tl=0x1f; }
				inx=inx+1;
			} 

			if ( tc != 0 ) { stlcode=stlcode+(char)tc; }
			if ( ti != 0 ) { stlcode=stlcode+(char)ti; }
			if ( tl != 0 ) { stlcode=stlcode+(char)tl; }


		}
		if ( gpcolor[1] > 9 ) return stlcode+(char)0x3+gpcolor[0]+","+gpcolor[1]+(char)0x07; 
		return stlcode+(char)0x3+gpcolor[0]+",0"+gpcolor[1]+(char)0x07;
	}

	/////////////////////////////////
	// show screen
	/////////////////////////////////
	public void showscr(Graphics g,int scroff) {
	
	clrscr(g);
	int lhcur=ft.getHeight(),yy=0;
	int pscr=scroff; 			//offset in wins[idwin].conbuf by line

	resetFont(g);

	int rsl=0;			//selected regions

	while ( yy <= (wins[idwin].scrlines-1) && (scroff+yy) <= wins[idwin].pline ) {

		
		rsl=showstr(g,wins[idwin].bufline[scroff+yy],wins[idwin].scrpoint,lhcur,rsl);		
	
		lhcur=lhcur+ft.getHeight();
		yy++;

		}
	
	//System.out.println("screen debug:yy="+yy+" scroff="+scroff+" pline="+wins[idwin].pline);

	}
       	//////////////////////////////////////
	// ifselected
	//////////////////////////////////////
	int ifselected(int rsl) {
		if (  rsl == 0 && wins[idwin].selptr == 1 ) { return 1; }
		else if (  wins[idwin].selptr == rsl ) { return 1; }
		else { return 0; }
	}

       	//////////////////////////////////////
	// show line with colors
	//////////////////////////////////////
	int showstr(Graphics g,String s,int x,int y,int rsl) {
		if ( s == null) return rsl;
		int wlen=0,bi=0,ei=0,a=0,slen=s.length();
		g.setColor(getcolormap(wins[idwin].color[0]));

	/////////////////

		int sl=rsl,slcd=0;
		
		//if ( slen >= 2 && s.charAt(0) == 0x06 && s.charAt(1) == 0x07 ) { ei=ei+2; bi=ei; sl++; slcd=ifselected(sl); }
		//else 
		if ( slen >= 1 && s.charAt(0) == 0x06 ) { ei++; bi++; sl++; slcd=ifselected(sl);

			if ( slcd == 1 ) { 
				g.setColor(getcolormap(10)); 
	        		g.fillRect(x,y,wins[idwin].scrwidth,ft.getHeight());
			}

		}
		//else if ( slen >= 1 && s.charAt(0) == 0x07 ) { ei++; bi++; slcd=ifselected(sl); if (sl == 0 ) sl++; }
		else { slcd=ifselected(sl); if (sl == 0 ) sl++;

			if ( slcd == 1 ) { 
				g.setColor(getcolormap(10)); 
	        		g.fillRect(x,y,wins[idwin].scrwidth,ft.getHeight());
			}
		}

		while ( ei != slen ) {
		

			if ( s.charAt(ei) == (char)0x03 ) {	//color

				wlen=drawstr(g,x,y,wlen,bi,ei,s,slcd);
				a=getctrlcodesize(ei,s,wins[idwin].color);
				
				if ( a == 1 ) { wins[idwin].color[0]=defcolorfore; wins[idwin].color[1]=defcolorback; }
				if ( dbrms.colorsoff ) { wins[idwin].color[0]=defcolorfore; wins[idwin].color[1]=defcolorback; }
				
				ei=ei+a;
				bi=ei;
			} else if ( s.charAt(ei) == (char)0x07 ) { //nope for mark position
				ei++;
				bi=ei;	
			} else if ( s.charAt(ei) == (char)0x04 ) { //reset Font&Color.
				wlen=drawstr(g,x,y,wlen,bi,ei,s,slcd);
				ei++;
				bi=ei;
				resetFont(g);
				wins[idwin].color[0]=defcolorfore; wins[idwin].color[1]=defcolorback;
			
			} else if ( s.charAt(ei) == (char)0x05 ) { //reset Font
				wlen=drawstr(g,x,y,wlen,bi,ei,s,slcd);
				ei++;
				bi=ei;
				resetFont(g);
			} else if ( s.charAt(ei) == (char)0x02 ) { //italic
				wlen=drawstr(g,x,y,wlen,bi,ei,s,slcd);
				ei++;
				bi=ei;
				reselectFont(g,1,0);
			} else if ( s.charAt(ei) == (char)0x16 ) { //invert base colors
				wlen=drawstr(g,x,y,wlen,bi,ei,s,slcd);
				ei++;
				bi=ei;
			} else if ( s.charAt(ei) == (char)0x1f ) { //lined
				wlen=drawstr(g,x,y,wlen,bi,ei,s,slcd);
				ei++;
				bi=ei;
				reselectFont(g,0,1);
			} else if ( s.charAt(ei) == (char)0x0d ||  s.charAt(ei) == (char)0x0a ) {
				wlen=drawstr(g,x,y,wlen,bi,ei,s,slcd);
				ei++;
				bi=ei;
			} else {
				ei++;	
			}  
		}
		drawstr(g,x,y,wlen,bi,ei,s,slcd);
		
		return sl;
	/////////////////
	}
	///////////////////////////
	// draw string out = wlen
	///////////////////////////
	int drawstr(Graphics g,int x,int y,int wlen,int bi,int ei,String s,int slcd) {
			
		if ( slcd == 1 ) { g.setColor(getcolormap(10)); }
		else { g.setColor(getcolormap(wins[idwin].color[1])); }	
											//background
	        g.fillRect(x+wlen,y,ft.stringWidth(s.substring(bi,ei)),ft.getHeight());

		g.setColor(getcolormap(wins[idwin].color[0]));				//foreground
		g.drawString(s.substring(bi,ei),x+wlen,y,Graphics.TOP | Graphics.LEFT);

		return wlen+ft.stringWidth(s.substring(bi,ei));
	}

	public void resetFont(Graphics g) {
		ft = ft_p;
		g.setFont(ft);
		wins[idwin].style_i=0;
		wins[idwin].style_l=0;
	}

	public void reselectFont(Graphics g,int i,int l) {

		if ( wins[idwin].style_i == 1 ) { wins[idwin].style_i = wins[idwin].style_i - i; } else { wins[idwin].style_i = wins[idwin].style_i + i; }
		if ( wins[idwin].style_l == 1 ) { wins[idwin].style_l = wins[idwin].style_l - l; } else { wins[idwin].style_l = wins[idwin].style_l + l; }
		
		if ( wins[idwin].style_l == 1 && wins[idwin].style_i == 1 ) { ft = ft_ui; g.setFont(ft); } 
		if ( wins[idwin].style_l == 1 && wins[idwin].style_i == 0 ) { ft = ft_u;  g.setFont(ft); } 
		if ( wins[idwin].style_l == 0 && wins[idwin].style_i == 1 ) { ft = ft_i;  g.setFont(ft); } 
		if ( wins[idwin].style_l == 0 && wins[idwin].style_i == 0 ) { ft = ft_p;  g.setFont(ft); } 
	}
	//////////////////////////////////
	// show window
	//////////////////////////////////
	void showwin(Graphics g){
//		synchronized(wins[idwin]){
			showtitle(g);
			showscr(g,wins[idwin].pscrbuf);
//		}
	}
	//////////////////////////////////
	// show title
	//////////////////////////////////
	void showtitle(Graphics g){ 

		resetFont(g);
		clrtitle(g);
		
		int x=0,y=0;
				
		wins[idwin].color[0]=defcolorfore; 
		wins[idwin].color[1]=defcolorback;

		g.setColor(getcolormap(defcolorfore));

//		System.out.println("object=["+getwctrlcode(wins[idwin].objname)+"]");

		x=ft.stringWidth("["+getwctrlcode(wins[idwin].objname)+"]");
		g.drawString("["+wins[idwin].objname+"] ",0,0,Graphics.TOP | Graphics.LEFT);		//objname = channel

//		System.out.println("wins[idwin].topic="+wins[idwin].topic);

		showstr(g,wins[idwin].topic,x,y,-1);

		wins[idwin].color[0]=defcolorfore; 
		wins[idwin].color[1]=defcolorback;
				
	}
	//////////////////////////////////
	// clr screen
	//////////////////////////////////
	public void clrscr (Graphics g) {
		g.setColor(getcolormap(0)); //black
	        g.fillRect(0,ft.getHeight(),getWidth(),getHeight());
	//	g.setColor(43690); //bluer
	//      g.fillRect(0,0,getWidth(),ft.getHeight());
		g.setColor(0xaa0000); //red
	}
	//////////////////////////////////
	// clr title
	//////////////////////////////////
	public void clrtitle (Graphics g) {
		g.setColor(43690);				//blue
	        g.fillRect(0,0,getWidth(),ft.getHeight());	//
		g.setColor(0xaa0000);				//red
	}


	//////////////////////////////////
	// get_selected_region
	//////////////////////////////////
	public 	String get_selected_region() {
		int	rsl=0,scrptr=wins[idwin].pscrbuf;
		StringBuffer	marked=new StringBuffer();


		//System.out.println("get_selected_region:  selptr="+wins[idwin].selptr+" scrptr="+scrptr+" 1st_char="+(wins[idwin].bufline[scrptr].charAt(0)+0)+" str="+wins[idwin].bufline[scrptr]);

		if ( wins[idwin].selptr == 1 && wins[idwin].bufline[scrptr].charAt(0) != 0x06 ) {		//UP SEARCH START OF REGION
			while ( scrptr != 1 && wins[idwin].bufline[scrptr].charAt(0) != 0x06  ) {
				scrptr--;
			}
		
		} else {
			if ( wins[idwin].selptr != 1 && wins[idwin].bufline[scrptr].charAt(0) != 0x06 ) { rsl++; } 
	   		scrptr--;
			do {
				scrptr++;
				//System.out.println("get_selected_region: [3] selptr="+wins[idwin].selptr+" scrptr="+scrptr+" 1st_char="+(wins[idwin].bufline[scrptr].charAt(0)+0)+" str="+wins[idwin].bufline[scrptr]);
				
				if ( wins[idwin].bufline[scrptr].charAt(0) == 0x06 ) { rsl++; } 
				
			} while ( scrptr != wins[idwin].pline && wins[idwin].selptr != rsl );
		}
		do {
			//System.out.println("get_selected_region: [4] psym="+wins[idwin].bufline[scrptr].indexOf(0x07));
			int psym;
			if ( (psym=wins[idwin].bufline[scrptr].indexOf(0x07)) < 0 ) { marked.append(wins[idwin].bufline[scrptr].substring(2)); }
			else { marked.append(wins[idwin].bufline[scrptr].substring(psym+1)); }
			scrptr++;		
		} while ( scrptr != wins[idwin].pline && wins[idwin].bufline[scrptr].charAt(0) != 0x06 );

		return marked.toString();
	}

	//////////////////////////////////
	// remap mirc colors
	//////////////////////////////////	
/*	public int getcolormap(int i)
    	{
        switch(i)
        {
        case 0: // '\0'
            return 0xffffff;

        case 1: // '\001'
            return 0;

        case 2: // '\002'
            return 128;

        case 3: // '\003'
            return 65280;

        case 4: // '\004'
            return 0xff0000;

        case 5: // '\005'
            return 0xa52a2a;

        case 6: // '\006'
            return 0xa020f0;

        case 7: // '\007'
            return 0xffa500;

        case 8: // '\b'
            return 0xffff00;

        case 9: // '\t'
            return 0x32cd32;

        case 10: // '\n'
            return 65535;

        case 11: // '\013'
            return 0xe0ffff;

        case 12: // '\f'
            return 0x0000fc;

        case 13: // '\r'
            return 0xffb5c5;

        case 14: // '\016'
            return 0xbebebe;

        case 15: // '\017'
            return 0xd3d3d3;
        }
        return i;
    } */

     public int getcolormap(int numb) {
 		numb &= 0x0f;
 		switch (numb) {
			case 0:  return 0x00ffffff;
			case 1:  return 0x00000000;
			case 2:  return 0x0000007f;
			case 3:  return 0x00009300;
			case 4:  return 0x00ff0000;
			case 5:  return 0x007f0000;
			case 6:  return 0x009c009c;			
			case 7:  return 0x00fc7f00;
			case 8:  return 0x00ffff00;
			case 9:  return 0x0000fc00;
			case 10: return 0x00009393;			
			case 11: return 0x0000ffff;
			case 12: return 0x000000fc;
			case 13: return 0x00ff00ff;
			case 14: return 0x007f7f7f;
			case 15: return 0x00d4d0c8;			
 		}
 		return 0x00FFFFFF;
 	}



} //class canvas
