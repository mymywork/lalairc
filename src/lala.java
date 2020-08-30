/**************************************************
*  0.Full console checks.
*  1.Work current,but slow console.
*  2.Decode font.
*  3.New technology ! fuckingtacking
*
***************************************************/
import java.io.*;
import java.lang.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

public class lala extends MIDlet  {

    	private Display display;

    	public concanvas lcanv;
    	public netsocket sock;
    	public uimod ui;
    	public addon uaddon;
    	public database dbrms;
	public cmdmod cmdm;
	public semaphore sm;
	public semaphore mlock;



	////////////////////////////
    	public int statusid = 0;	//status window id in array
		
	public String mynick;

	public String amodes;
	public String bmodes;
	public String cmodes;
	public String dmodes;

	public String uprefix;		//qaohv
	public String sprefix;		//~&@%+
	public int    topiclen=200;	//

	public String  bannick="";
	public String  banchan="";
	public int     bantype=0;
	public boolean bankick=false;
	public String  banreason="";

	public boolean autoflag=false; //auto command

	public boolean lockmenu=false;

	public String  ircencode;

	public String  exbuffer="";

	public boolean stoptry=false;


        /////////////////////
	// lala constructor
	/////////////////////
	public lala()
	{
        	display = Display.getDisplay( this );

		dbrms = new database();
		sock = new netsocket();
		uaddon = new addon();
		sm = new semaphore();
		mlock = new semaphore();
		lcanv = new concanvas();
		ui = new uimod();
		cmdm = new cmdmod();


		// dbrms
			
		dbrms.ui=ui;

		// ui

		ui.cmd = cmdm;
		ui.display = display;
		ui.lcanv = lcanv;
		ui.dbrms = dbrms;
		ui.uaddon = uaddon;
		ui.main = this;
		ui.sm = sm;

		ui.sock = sock;		//debugggggggggggg!!!

		ui.sclr = new scolor(display,lcanv,ui);
 		lcanv.addCommand(ui.BTN_MENU);
        	lcanv.setCommandListener(ui);
	
		// cmdmod

		cmdm.dbrms = dbrms;
		cmdm.ui = ui;
		cmdm.uaddon = uaddon;
		cmdm.display = display;
		cmdm.sock = sock;
		cmdm.lcanv = lcanv;
		cmdm.main = this;
	
		// concanvas

		lcanv.display = display;
		lcanv.main = this;
		lcanv.sock = sock;
		lcanv.dbrms = dbrms;
		lcanv.cmdm = cmdm;

		// netsocket
		
		
		sock.dbrms = dbrms;
		sock.uaddon = uaddon;
		sock.main = this;

		// end

	}

	protected void destroyApp( boolean unconditional ) throws MIDletStateChangeException 
    	{
		sock.send("QUIT :"+dbrms.quitmsg+"\n");

        	notifyDestroyed(); // уничтожение MIDlet-а
    	}

	protected void pauseApp()
	{
		notifyPaused();
	}
	protected void startApp() throws MIDletStateChangeException
	{
	/*
		//String trrrh = "xaxaxalalala";

		//System.out.println("eee="+trrrh.substring(2,0));

		Vector la = new Vector(2);
		la.addElement("one");
		la.addElement("two");
		la.addElement("three");
		la.addElement("four");
		la.addElement("five");

		System.out.println("1="+la.elementAt(0)+" 2="+la.elementAt(1)+" 3="+la.elementAt(2)+" 4="+la.elementAt(3));
	*/
		
	//	byte buf[] = sock.inet_addr("127.0.0.1");

	//	System.out.println("a="+buf[0]+"b="+buf[1]+"c="+buf[2]+"d="+buf[3]);		


	//	if (true) return;
	/*
		String xui = "qaz";

		System.out.println("0="+xui.charAt(0)+"1="+xui.charAt(1));
	*/

	/*	boolean[] sets = new boolean[10];

		String s = "qaohv";
		String t = "qov";

		Hashtable bubu = new Hashtable(10);
		bubu.put("sets",sets);

		setmods((boolean[])bubu.get("sets"),s,t,true);

		boolean[] zz = (boolean[])bubu.get("sets");

		for (int i=0;i < 10; i++ ) {
			System.out.println("boolean["+i+"]="+zz[i]);
		}
		
		System.out.println("str="+showmods(sets,s));

	*/

	/*	scolor sc = new scolor(display,lcanv);
		display.setCurrent(sc);
		sc.repaint();				

		if ( true ) return;
	*/			
		
		//dbrms.removeStoreAll();	
		//dbrms.removeMenu();

		ui.makeMainMenu();

		do {
			display.setCurrent(ui.mmenu);

			System.out.println("[before paused]");
			
			try {  sm.waitsignal(); }
			catch(InterruptedException exp) { /* exp */ }
		
			System.out.println("[after paused]");
			runirc();	

		} while ( true );
	
	} // startApp


	/////////////////////////////////////
	// Connect thread and while recving
	/////////////////////////////////////
	public void runirc() {

		mynick=dbrms.nick;
								//must first !
		lcanv.initcanvas();				//initfont

		lcanv.wins=new winobj[lcanv.maxwins];		//arrays for 10 windows

		lcanv.wins[0] = new winobj();			//create objwin "status" in array
		lcanv.cntwins++;				//created window counter
		lcanv.initobject(0);				//initialize objwin buffer and other
		lcanv.wins[0].objname=new String("status");	//set name
		lcanv.wins[0].topic="\0031,10["+mynick+"]"; 	//set topic

		lcanv.showcanvas();				//display ConCanvas

		// init vars

		byte rdbuf[] = new byte[512];
		int size,counttry=1;
		String rprefix = "";
		String rs;


		if (dbrms.autoreconn) { counttry = dbrms.trysnum; }

		do {						//autoreconnect 

			while ( (rs=connect() ) != null ) { 
				lcanv.consout(0,"* Connect error:"+rs); 
				uaddon.sleep(dbrms.timeout*1000); 
			
				if ( lockmenu ) {					//if called menu, freeze reconnect cycle
					try {  mlock.waitsignal(); }			//wait for signal "unfreeze cycle and exit from menu".
					catch(InterruptedException exp) { /* exp */ }
				}
				counttry--;
				if (counttry == 0 || stoptry ) { break; }		//if one try or disconnect event.
			}
			if ( rs != null ) break; 					//if has error.

		//// Server logged

			lcanv.consout(0,"[*] Sending data.");
		
			if ( !dbrms.srvpass.equals("") ) {
				sock.send("PASS "+dbrms.srvpass+"\n");
			}

			sock.send("NICK "+dbrms.nick+"\r\n");
			sock.send("USER "+dbrms.user+" nope nope: "+dbrms.real+"\r\n");

			lcanv.consout(0,"[*] Recving data & polling is "+dbrms.polling);

					
			while (true) {			//work in blocking mode =(
							//error on blocking recv!
			
				if ( dbrms.polling ) {
					//lcanv.consout(0,"* dbg in polling");
			
					while ( (size=sock.avail()) == 0 ) { 
						//lcanv.consout(0,"* dbg avail=0 sleep");
						uaddon.sleep(100); 
						continue; 
					}
					if ( size < 0 ) {
						lcanv.consout(0,"* dbg avail < 0; err="+size);
						break;
					}
				} 

				size = sock.recvnew(rdbuf);
			
				if ( size == 0 ) {
					lcanv.consout(0,"* dbg recv == 0 sleep");
					uaddon.sleep(100); 
					continue; 
				}	
				if ( size < 0 ) { lcanv.consout(0,"* dbg recv < 0; err="+size); break; }


				//String mystr = new String(rdbuf,0,size);
				

				/*mystr = rprefix+mystr;
				rprefix = "";
				int stn=0,ptn=0;
				String cutstr;

				System.out.println("[recv strt]="+mystr+"[recv end]");
			
				while ( stn != mystr.length() ) { 

					if ( (ptn=mystr.indexOf((char)0xD,stn)) != -1 ) {
						
						if ( mystr.charAt(ptn-1) == 0xA ) {
						ptn++;
						} else if ( ptn+1 != mystr.length() ) {
							if ( mystr.charAt(ptn+1) == 0xA ) ptn=ptn+2;
						} else {
							ptn++;
						}	

					} else if ( (ptn=mystr.indexOf((char)0xA,stn)) != -1 ) {
						
						if ( mystr.charAt(ptn-1) == 0xD ) {
							ptn++;
						} else if ( ptn+1 != mystr.length() ) {
							if ( mystr.charAt(ptn+1) == 0xD ) ptn=ptn+2;
						} else {
							ptn++;
						}	

					} else {
						rprefix = mystr.substring(stn);
						break;
					}
	
					
					cutstr = mystr.substring(stn,ptn);
					
					cutstr = uaddon.byteArrayToString(cutstr.getBytes(),ircencode,dbrms.utf8autord);
										
					ircparse(cutstr);
			 		stn=ptn;
				
								                         	
				}*/ //while 1
			

				
				ircparse(uaddon.byteArrayToString(rdbuf,size,ircencode,dbrms.utf8autord));

				System.gc();
				//lcanv.consout(0,"[*] prefix="+rprefix);
			
			} //while 2
			
			//phase II of reconnect
	
			lcanv.consout(0,"* Disconnect from server.");
			lcanv.consout(0,"* dbg err size ="+size);

		
			free_wins();
			free_ui();
			autoflag=false;					//clear flag of send cmds on connect(for new connect.)
			counttry--;					//trys--.
		
		} while (counttry != 0 && !stoptry );
		
								//if connection closed not by user = show message.
		if ( !stoptry ) {
			Alert alt = new Alert("fail","Disconnect from server!",null,AlertType.ERROR);
			alt.setTimeout(5000);
			display.setCurrent(alt);
			uaddon.sleep(5000);
		}

		stoptry=false;
		free_winobj(0);					//free status window.
	}
	
	///////////////////////////////////
	// connect selector.
	///////////////////////////////////
	public String connect() {

		if ( dbrms.contype == 1 ) { 
			lcanv.consout(0,"* Connecting to "+dbrms.ircsrv+":"+dbrms.port+" via proxy "+dbrms.conhost+":"+dbrms.conport+"\n");
			return sock.proxyconnect(dbrms.ircsrv,dbrms.port,dbrms.conhost,dbrms.conport); 
		}
		else if ( dbrms.contype == 2 ) { 
			lcanv.consout(0,"* Connecting to "+dbrms.ircsrv+":"+dbrms.port+" via socks4 "+dbrms.conhost+":"+dbrms.conport+"\n");
			return sock.socks4connect(dbrms.ircsrv,dbrms.port,dbrms.conhost,dbrms.conport); }
		else if ( dbrms.contype == 3 ) {
			lcanv.consout(0,"* Connecting to "+dbrms.ircsrv+":"+dbrms.port+" via socks5 "+dbrms.conhost+":"+dbrms.conport+"\n");
			return sock.socks5connect(dbrms.ircsrv,dbrms.port,dbrms.conhost,dbrms.conport); }
		else { 	lcanv.consout(0,"* Connecting to "+dbrms.ircsrv+":"+dbrms.port+"\n");
			return sock.connect(dbrms.ircsrv,dbrms.port); 
		}
	}


	///////////////////////////////////
	// free ui resources.
	///////////////////////////////////
	public void free_ui() {
		ui.pstack=0;
		ui.ptlist=0;
		ui.hashmod=null;
		System.gc();
	}

	///////////////////////////////////
	// clear all wins, but no status.
	///////////////////////////////////
	public void free_wins() {
		lcanv.idwin=0;
		for ( int i=1,m=lcanv.cntwins;i != m;i++) { free_winobj(i); }		
	}
	/////////////////////
	// clear one winobj
	/////////////////////
	public void free_winobj(int n) {
		lcanv.wins[n].topic=null;
		lcanv.wins[n].objname=null;
		lcanv.wins[n].mode=null;
		lcanv.wins[n].nicks=null;
		lcanv.wins[n].bufline=null;
		System.gc();
		lcanv.wins[n]=null;
		System.gc();
		lcanv.cntwins--;
	}

	///////////////////
	// getstring
	////////////////
	public String getstring(int i,String s) {
		return uaddon.getstringbychar(i,s," ");
	}
	////////////////
	// getchanid
	///////////////
	public int getchanid(String chan) {
		int i=0;
		while ( i != lcanv.cntwins && !lcanv.wins[i].objname.equals(chan) ) { i++; }
		if ( i >= lcanv.cntwins ) { return -1; }
		return i;
	}
	///////////////////////////////////////
	// getparam (param=value)
	///////////////////////////////////////
	public String getparam(String getparm,String strparam) {
		int a;
		if ( (a=strparam.indexOf(getparm)) == -1 ) return "";
		return strparam.substring(strparam.indexOf("=",a+1)+1,strparam.indexOf(" ",a+1));
	}

	///////////////////////////////////////
	// setmods
	///////////////////////////////////////
	public void setmods(boolean[] bmods,String src,String trg,boolean op) {

		for (int p=0; p < trg.length() ; p++) {
			for (int i=0; i < src.length() ; i++) {
				if ( src.charAt(i) == trg.charAt(p) ) {
					bmods[i]=op;
					//p++;
					//i=0;
					break;
				}
			}
		}
	}

	///////////////////////////////////////
	// showmods
	///////////////////////////////////////
	public String showmods(boolean[] bmods,String src){
		String smods="";
		for (int i=0; i < src.length(); i++) {
			if ( bmods[i] == true) { smods=smods+src.charAt(i); }
		}

		return smods;
	}

	///////////////
	// irc parser
	///////////////
	public void ircparse(String cmd) { 
		String nick,ident,address,scmd,sparam;

		//System.out.println("[handle cmd]="+cmd);
	
		int of1=cmd.indexOf(" ");
		if ( of1 == -1 ) { 
			lcanv.consout(statusid,";err no server command;"); 
			//	System.out.println("[FAILED PARSE CMD]="+cmd); 
			return; 
		}
		String from = cmd.substring(0,of1);

		if ( from != "" && from.charAt(0) == ':' ) {
			if ( from.indexOf("!") != -1 && from.indexOf("@") != -1 ) { 
				nick=from.substring(0, from.indexOf("!"));
				ident=from.substring(from.indexOf("!")+1, from.indexOf("@"));
				address=from.substring(from.indexOf("@")+1);
			} 
			else { nick=from; ident=""; address=""; }
		} else {  nick=""; ident=""; address=""; }

		int of2 = cmd.indexOf(" ",of1+1);

		if ( from != "" && from.charAt(0) == ':' ) {
			if ( of2 == -1 ) { scmd=cmd.substring(of1+1); sparam = ""; }
			else { scmd=cmd.substring(of1+1,of2); sparam = cmd.substring(of2+1); }
		} else {  scmd=from; sparam=cmd.substring(of1+1); }
		////////////
		// correct
		////////////
		if ( nick != "" && nick.charAt(0) == ':' ) { nick = nick.substring(1); }
		if ( sparam != "" && sparam.charAt(0) == ':' ) { sparam = sparam.substring(1); }

		//System.out.println("[debug] nick="+nick+" ident="+ident+" addr="+address+" cmd="+scmd+" sparam="+sparam);
		irchandle(nick,ident,address,scmd,sparam);	
		lcanv.repaint();
	}
	public void nickservIdentifyCheck(String nick,String param) {

		if ( nick.toLowerCase().equals("nickserv") & ( param.toLowerCase().indexOf("identify") != -1 ) & !dbrms.nspass.equals("") ) {
			sock.send("PRIVMSG NickServ :identify "+dbrms.nspass+"\n");
		}

	}

	////////////////////////
	// Handle irc commands
	////////////////////////
	public void irchandle(String nick,String ident,String address,String cmd,String param) {
		
		int id;
		if ( cmd.equals("005") ) {
			
			if ( !autoflag ) {

				sock.send("PROTOCTL NAMESX\n");
								
				if ( !dbrms.chans.equals("") ) {
					sock.send("JOIN "+dbrms.chans+"\n");
				}
				autoflag=true;
			}			


			String modes;

			if ( !(modes = getparam("CHANMODES",param)).equals("") ) {
				amodes = uaddon.getstringbychar(1,modes,",");	//list add/del - always has parameter
				bmodes = uaddon.getstringbychar(2,modes,",");   //set/unset - always has parameter
				cmodes = uaddon.getstringbychar(3,modes,",");	//set - always has parameter, unset without parameter
				dmodes = uaddon.getstringbychar(4,modes,",");	//set/unset - no has parameter
				ui.createChanmodeForm();
			}

			if ( !(modes = getparam("PREFIX",param)).equals("") ) {
				uprefix = modes.substring(1,modes.indexOf(")"));
				sprefix = modes.substring(modes.indexOf(")")+1);
				//System.out.println("u="+uprefix+" s="+sprefix);
			}

			if ( !(modes = getparam("TOPICLEN",param)).equals("") ) {
				topiclen=Integer.valueOf(modes).intValue();
			}
		}		

		if ( cmd.equals("433") ) {	//nick name already use
			if ( mynick == dbrms.altnick ) { 
				cmdm.handleinit("nick $txt connect");
			} else {
				sock.send("NICK "+dbrms.altnick+"\n");
				mynick=dbrms.altnick;
			}
		}

		if ( cmd.equals("PING") ) {
			//System.out.println("PONG !!!!");
			String ping = "PONG :"+param+"\n";
			if ( dbrms.pingpong ) lcanv.consout(0,ping); 
			sock.send(ping);
		}
		if ( cmd.equals("PRIVMSG") ) {

			nickservIdentifyCheck(nick,param);			

			String cwin;

			if ( getstring(1,param).startsWith("#") ) { cwin = getstring(1,param); }	//channel
			else { cwin=nick; }								//query

			
			if ( (id = getchanid(cwin.toLowerCase())) == -1 ) {				//if chan ?
				if ( cwin.startsWith("#") ) { id = 0; nick=nick+":"+cwin; } 		//if (chan and notfound) chan:nick -> status
			}

			if ( param.substring(param.indexOf(":")+1).startsWith("\001ACTION") ) {
				if ( id == -1 ) id=0;
				lcanv.consout(id,"\00313 * "+nick+" "+param.substring( param.indexOf((char)0x01+"ACTION")+8 , param.lastIndexOf((char)0x01)));
			
			} else if ( param.substring(param.indexOf(":")+1).startsWith((char)0x01+"VERSION"+(char)0x01) ) {
				if ( id == -1 || !cwin.startsWith("#") ) { id=lcanv.idwin; }
				else if ( id != -1 || cwin.startsWith("#") ) { nick=nick+":"+cwin; }

				lcanv.consout(id,"["+nick+" VERSION]\n");
				int hwp;			
	
				if ( (hwp=dbrms.ctcpver.indexOf("%hw%")) < 0 ) {
		
					sock.send("NOTICE "+nick+" :"+(char)0x01+"VERSION "+dbrms.ctcpver+(char)0x01+"\n");
											
				} else { 
					sock.send("NOTICE "+nick+" :"+(char)0x01+"VERSION "+dbrms.ctcpver.substring(0,hwp)+System.getProperty("microedition.platform")+dbrms.ctcpver.substring(hwp+4)+(char)0x01+"\n");
					
				}
			} else if ( cwin.startsWith("#") ) {
				lcanv.consout(id,"<"+nick+"> "+param.substring(param.indexOf(":")+1));
			} else { 
				if ( id != -1 ) { lcanv.consout(id,"<"+nick+"> "+param.substring(param.indexOf(":")+1)); }
				else { lcanv.consout(lcanv.idwin,"*"+nick+"* "+param.substring(param.indexOf(":")+1)); }
				
			}
		}	
		if ( cmd.equals("NOTICE") ) {

			nickservIdentifyCheck(nick,param);
			
			id = getchanid(getstring(1,param).toLowerCase());		//chan
			if ( id == -1 ) { lcanv.consout(lcanv.idwin,"-"+nick+"- "+param.substring(param.indexOf(":")+1)); }	//private or no window
			else { 	lcanv.consout(id,"-"+nick+"- "+param.substring(param.indexOf(":")+1)); } //channel
		}
		if ( cmd.equals("NICK") ) {

			if ( nick.equals(mynick) ) { 
				mynick=getstring(1,param);
				lcanv.wins[0].topic="\0031,10["+mynick+"]"; 
			}
						
			for (id=1; id < lcanv.cntwins;id++) {
				if ( lcanv.wins[id].nicks.containsKey(nick) ) {
					
					lcanv.wins[id].nicks.put(getstring(1,param),lcanv.wins[id].nicks.get(nick));		
					lcanv.wins[id].nicks.remove(nick);

					if ( dbrms.jpqn ) lcanv.consout(id,"* "+nick+" is now known as "+getstring(1,param)+"\n");
				}
			}
		}

		if ( cmd.equals("375") || cmd.equals("372") || cmd.equals("376") ) {	//MOTD
			if (!dbrms.skpmotd) lcanv.consout(0,param.substring(param.indexOf(":")+1));	//status window
		}

		if ( cmd.equals("353") ) {	//channel names
			
			//System.out.println("lalalallal="+getstring(3,param).toLowerCase());
			int lichan = getchanid(getstring(3,param).toLowerCase());
			String lnicks = param.substring(param.indexOf(":")+1);	// only nicks
			String lnick;
			String lmode;
			int n=1,p=0;
			//System.out.println("event 353 lnicks="+lnicks);

			while ( !(lnick=getstring(n,lnicks)).equals("") ) {
				//System.out.println("*** event lnick="+lnick);
				lmode="";
				for ( int u=0; u < sprefix.length(); u++ ) {
					if ( lnick.charAt(p) == sprefix.charAt(u) ) {
						p++;
						u=0;
					}
				}
				if ( p != 0 ) { 
					lmode=lnick.substring(0,p); 
					lnick=lnick.substring(p);
				}
				boolean[] bmods = new boolean[sprefix.length()+1];
				setmods(bmods,sprefix,lmode,true);
				//System.out.println("event 353 after setmod");

				//System.out.println("lnick="+lnick+" lmode="+lmode+" lichan="+lichan+" n="+n);
				lcanv.wins[lichan].nicks.put(lnick,bmods);	
				bmods=null;	
		
				//System.out.println("event 353 after nicks.put");				

				n++;
				p=0;
			}	

			//System.out.println("event 353 end="+lnicks);
				
		}

		if ( cmd.equals("JOIN") ) {

			//System.out.println("[join cmd]="+nick+" cmd="+cmd);
			if ( nick.equals(mynick) && lcanv.cntwins != lcanv.maxwins ) {

				System.out.println("[create channel]="+getstring(1,param).toLowerCase());

				lcanv.wins[lcanv.cntwins] = new winobj();
				lcanv.initobject(lcanv.cntwins);
				lcanv.wins[lcanv.cntwins].objname=getstring(1,param).toLowerCase();	
				lcanv.wins[lcanv.cntwins].topic="";
				lcanv.wins[lcanv.cntwins].nicks=new Hashtable(10);
				lcanv.idwin=lcanv.cntwins;
				lcanv.cntwins++;						
			}
			if ( !nick.equals(mynick) ) { 
				if ( (id = getchanid(getstring(1,param).toLowerCase())) == -1 ) { lcanv.consout(statusid,";chan not in hash;"); return; }
					
				lcanv.wins[id].nicks.put(nick,new boolean[sprefix.length()]);
				if ( !dbrms.jpqn ) return;
				if ( dbrms.showhost ) { lcanv.consout(id,"* "+nick+"("+ident+"@"+address+")"+" joined "+getstring(1,param)+"\n"); }
				else { lcanv.consout(id,"* "+nick+" joined "+getstring(1,param)+"\n"); }
			}
		}
		if ( cmd.equals("PART") ) {
			if ( nick.equals(mynick) ) {
				id = getchanid(getstring(1,param).toLowerCase());
				lcanv.wins[id].nicks=null;
				int did = id+1;
				while ( did != lcanv.cntwins ) {
					lcanv.wins[id]=lcanv.wins[did];
					did++;
					id++;
				}
				lcanv.cntwins--;	
				lcanv.idwin=lcanv.cntwins-1;
				System.gc();
			}
			if ( !nick.equals(mynick) ) {
				if ( (id = getchanid(getstring(1,param).toLowerCase())) == -1 ) { lcanv.consout(statusid,";chan not in hash;"); return; }
				
				lcanv.wins[id].nicks.remove(nick);
				if ( !dbrms.jpqn ) return;
				if ( dbrms.showhost ) { lcanv.consout(id,"* "+nick+"("+ident+"@"+address+")"+" part "+getstring(1,param)+"\n"); }
				else { lcanv.consout(id,"* "+nick+" part "+getstring(1,param)+"\n"); }
			}
		}

		if ( cmd.equals("KICK") ) {
			if ( getstring(2,param).equals(mynick) ) {
				if ( (id = getchanid(getstring(1,param).toLowerCase())) == -1 ) { lcanv.consout(statusid,";chan not in hash;"); return; }
				int did = id+1;
				while ( did != lcanv.cntwins ) {
					lcanv.wins[id]=lcanv.wins[did];
					did++;
					id++;
				}
				lcanv.cntwins--;	
				lcanv.consout(statusid,"* You are kicked from "+getstring(1,param)+" by "+nick+" ("+param.substring(param.indexOf(":")+1)+")\n");
				lcanv.idwin=statusid;		//status
				if ( dbrms.autorejoin ) {
					sock.send("JOIN "+getstring(1,param)+"\n");
				}
			} else { 
				if ( (id = getchanid(getstring(1,param).toLowerCase())) == -1 ) { lcanv.consout(statusid,";chan not in hash;"); return; }
				lcanv.wins[id].nicks.remove(getstring(2,param));
				lcanv.consout(id,"* "+getstring(2,param)+" has been kick by "+nick+" ("+param.substring(param.indexOf(":")+1)+")\n");  
			}
		}

		if ( cmd.equals("QUIT") ) {
				
			for (id=1; id < lcanv.cntwins;id++) {
				if ( lcanv.wins[id].nicks.containsKey(nick) ) {
					lcanv.wins[id].nicks.remove(nick);
					
					if ( !dbrms.jpqn ) return;
					if ( dbrms.showhost ) { lcanv.consout(id,"* "+nick+"("+ident+"@"+address+")"+" Quit:("+param.substring(param.indexOf(":")+1)+")\n"); }
					else { lcanv.consout(id,"* "+nick+" Quit:("+param.substring(param.indexOf(":")+1)+")\n"); }
				}
			}
		}


		if ( cmd.equals("MODE") ) {
			if ( getstring(1,param).equals(mynick) ) { 
				lcanv.consout(0,"* "+nick+" sets mode "+param.substring(param.indexOf(" ")+1)+"\n");
			}
			else if ( (id = getchanid(getstring(1,param).toLowerCase())) == -1 ) { lcanv.consout(statusid,"error:chan not in hash"); return; } 
			else {		
			
			String mods = getstring(2,param);
			
			
				for (int k=0,p=3,op=0; k < mods.length(); k++ ) {
				
					if ( mods.charAt(k) == (char)':' ) continue;
					if ( mods.charAt(k) == (char)'+' ) { op=1; continue; }			
					if ( mods.charAt(k) == (char)'-' ) { op=2; continue; }
				
					if ( op != 0 && uprefix.indexOf(mods.charAt(k)) != -1 ) {	//usermodes
						
						if ( op == 1 ) { setmods((boolean[])lcanv.wins[id].nicks.get((String)getstring(p,param)),uprefix,""+mods.charAt(k),true); }
						if ( op == 2 ) { setmods((boolean[])lcanv.wins[id].nicks.get((String)getstring(p,param)),uprefix,""+mods.charAt(k),false); }			
						p++;
					}				

				}
			
				lcanv.consout(id,"* "+nick+" sets mode "+param.substring(param.indexOf(" ")+1)+"\n");
			}
		}

		if ( cmd.equals("TOPIC") ) {				//changed topic
			if ( (id = getchanid(getstring(1,param).toLowerCase())) == -1 ) { lcanv.consout(statusid,";chan not in hash;"); return; }		
			lcanv.wins[id].topic=param.substring(param.indexOf(":")+1);
			lcanv.consout(id,"* "+nick+" changes topic to '"+param.substring(param.indexOf(":")+1)+(char)0x4+"'\n");
		}
		if ( cmd.equals("332") ) {				//setted topic
			if ( (id = getchanid(getstring(2,param).toLowerCase())) == -1 ) { lcanv.consout(statusid,";chan not in hash;"); return; }		
			lcanv.wins[id].topic=param.substring(param.indexOf(":")+1);
			lcanv.consout(id,"topic is '"+param.substring(param.indexOf(":")+1)+(char)0x4+"'\n");
		}
		if ( cmd.equals("333") ) {				//setted by
			if ( (id = getchanid(getstring(2,param).toLowerCase())) == -1 ) { lcanv.consout(statusid,";chan not in hash;"); return; }
			lcanv.consout(id,"Set by "+getstring(3,param)+"\n");
		}

		if ( cmd.equals("366") ) { return; }
		

		if ( cmd.equals("311") || cmd.equals("378") || cmd.equals("319") || cmd.equals("312") || cmd.equals("703") || cmd.equals("318") || cmd.equals("671") || cmd.equals("307") || cmd.equals("317") ) { 
			if ( cmd.equals("311") ) {
				if ( bannick.equals(getstring(2,param)) ) {
					String sndcmd="";
					if ( bantype == 0 ) sndcmd = "MODE "+banchan+" +b "+bannick+"!*@*\n";
					if ( bantype == 1 ) sndcmd = "MODE "+banchan+" +b *!"+getstring(3,param)+"@*\n";
					if ( bantype == 2 ) sndcmd = "MODE "+banchan+" +b *!*@"+getstring(4,param)+"\n";
					if ( bantype == 3 ) sndcmd = "MODE "+banchan+" +b "+bannick+"!"+getstring(3,param)+"@*\n";
					if ( bantype == 4 ) sndcmd = "MODE "+banchan+" +b *!"+getstring(3,param)+"@"+getstring(4,param)+"\n";
					if ( bantype == 5 ) sndcmd = "MODE "+banchan+" +b "+bannick+"!*@"+getstring(4,param)+"\n";
					if ( bantype == 6 ) sndcmd = "MODE "+banchan+" +b "+bannick+"!"+getstring(3,param)+"@"+getstring(4,param)+"\n";
					//System.out.println("doban="+sndcmd);
					sock.send(sndcmd);
					if ( bankick == true ) {
						sock.send("KICK "+banchan+" "+bannick+" :"+banreason+"\n");
					}
					bannick="";
					bankick=false;
				}
			}
			lcanv.consout(0,param.substring(param.indexOf(" ")+1)+"\n");
		}

		if ( cmd.equals("367") || cmd.equals("348") || cmd.equals("346") ) { // ban, exp, inv
			
			//System.out.println("ban="+getstring(3,param)+"Create by "+getstring(4,param)+" at "+uaddon.millsToDate(Integer.valueOf(getstring(5,param)).longValue()) );
			
			String cinfo = "Create by "+getstring(4,param)+" at "+uaddon.millsToDate(Integer.valueOf(getstring(5,param)).longValue());
			
			ui.hashmod.put(getstring(3,param),cinfo);		
		}
		
		if ( cmd.equals("368") ) { 					//bans end
			ui.showListitem(ui.hashmod.keys(),"submenu menu_banlist");
		}

		if ( cmd.equals("348") ) { 					//exps end
			ui.showListitem(ui.hashmod.keys(),"submenu menu_explist");
		}

		if ( cmd.equals("347") ) { 					//invs end
			ui.showListitem(ui.hashmod.keys(),"submenu menu_invlist");
		}

		
		if ( cmd.equals("324") ) { 					//channel mods.
			ui.setChanmodeForm(param);
		}

		if ( cmd.equals("473") || cmd.equals("474") || cmd.equals("475") ) {				//setted topic
			lcanv.consout(0," * "+getstring(2,param)+" "+param.substring(param.indexOf(":")+1));
		}

		if ( cmd.equals("321") ) { 					//list start
			lcanv.consout(0," * Recving list.");
			ui.hashmod = new Hashtable(20);
		}

		if ( cmd.equals("322") ) { 					//list recv
			String lstchan = getstring(2,param);
			
			if ( lstchan.length() > 1 ) {
				String chrchan = lstchan.substring(0,2);
				System.out.println("id chan ["+chrchan+"]");

				if ( !ui.hashmod.containsKey((String)chrchan) ) {
					ui.hashmod.put(chrchan,(Hashtable)new Hashtable(4));
				} 	
				
				

				((Hashtable)ui.hashmod.get(chrchan)).put(lstchan,uaddon.getlaststring(3,param));
			}
		}
		

		if ( cmd.equals("323") ) { 					//list end
			ui.showListitem(ui.hashmod.keys(),"sublist");
		}

		//lcanv.consout(0,"*dbg: "+nick+"!"+cmd+"!"+param);
	}



} //class lala