/**************************************************
*
* 	    Commands funtions module.
*
***************************************************/
import java.io.*;
import java.lang.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

public class cmdmod {
	
	database	dbrms;
	uimod		ui;
	addon		uaddon;	
	Display 	display;
	netsocket	sock;
	concanvas	lcanv;
	lala		main;

	String  fnick;
	String  item;

	String  ccmd;
	String  fcmd;
	String  pcmd[]=new String[5];

	/////////////////////////////

	int	n;		
	String	mcmd;

	/////////////////////////////
	

	//////////////////////////////////////
	// init handling new command string
	//////////////////////////////////////
	public void handleinit (String cstr) {

		n=0;
		mcmd="";
		ccmd=uaddon.getstring(1,cstr);				//cmd
		fcmd = uaddon.getlaststring(2,cstr);
		//System.out.println("ccmd="+ccmd+" wrk undo ="+fcmd);
		handlestart();
	}
	
	//////////////////////////////////////
	// internal handling command string
	//////////////////////////////////////
	public void handlestart () {
		String strtmp;
		int t;

		while ( n < (fcmd.length()) ) {

			if ( (t=fcmd.indexOf('$',n)) != -1 ) {				//identificator
				
				int p = fcmd.indexOf((char)0x20,t);
				int k = fcmd.indexOf((char)'(',t);
				int l = fcmd.indexOf((char)')',t);
				int c = fcmd.indexOf((char)0x0d,t);
			
				mcmd = mcmd + fcmd.substring(n,t);

				if ( ( k > 0 && l > 0 && k < l ) && ( (p > 0 &&  k < p) || p < 0 ) ) {	//Если кавычка ближе пробела [ $txt(lala xaxa ] [ $txtxui(lala xaxa ]
					 
					//System.out.println("[$id()] cmd="+fcmd.substring(t,k)+" param="+fcmd.substring(k+1,l));

					strtmp = handleparam(fcmd.substring(t,k),fcmd.substring(k+1,l));
					n = l+1;
					t=n;
					if ( strtmp.equals("") ) { return; }
					mcmd = mcmd + strtmp;
					
				
				} else if ( p > 0 || c != -1 ) {		//Пробел есть и он ближе кавычки в обратном случает выполнилось бы условие первое.
															
					//System.out.println("[$id] p="+p+" cmd="+fcmd.substring(t,p));
					
					strtmp = handleparam(fcmd.substring(t,p),"");
					n = p;
					t = n;
					if ( strtmp.equals("") ) { return; }
					mcmd = mcmd + strtmp;
					
					
					//System.out.println("[$id] exit ");
				} else {

					//System.out.println("[$id] p="+p+" cmd="+fcmd.substring(t));
					
					n = fcmd.length();
					strtmp = handleparam(fcmd.substring(t),(String)"");
					if ( strtmp.equals("") ) { return; }
					mcmd = mcmd + strtmp;
					//System.out.println("[$id] exit ");
					break;
				}

			} else {
				
				if ( (t=fcmd.indexOf((char)0x0d,n)) != -1 ) { mcmd = mcmd + fcmd.substring(n,t); }
				else { mcmd = mcmd + fcmd.substring(n); }
				break;
			}
		}
		
		//System.out.println("mcmd ready! ="+mcmd);		

		cmddo();

	} //handle

	//////////////////////////////////////
	// add values of some pending params
	//////////////////////////////////////
	public void handleadd (String astr) {

		if ( !astr.equals("") ) { mcmd=mcmd+astr; }
		handlestart();
	} 

	//////////////////////////////////////
	// handling types of params
	//////////////////////////////////////
	public String handleparam(String istr,String pstr) {

		if ( istr.equals("$txt") ) { 
			ui.showEditbox(pstr);
			return "";
		}
		if ( istr.equals("$itxt") ) { 
			ui.showEditbox(fnick+" ");
			return "";
		}
		if ( istr.equals("$nick") || istr.equals("$item") ) {
			return fnick;
		}

		if ( istr.equals("$chan") ) {
			return lcanv.wins[lcanv.idwin].objname;
		}
		if ( istr.equals("$etopic") ) {
			ui.showEditbox(uaddon.topicToString(lcanv.wins[lcanv.idwin].topic));
			return "";
		}
		if ( istr.equals("$eexbuf") ) {
			ui.showEditbox(uaddon.topicToString(main.exbuffer));
			
			return "";
		}
		if ( istr.equals("$nitem") ) {
			
			Vector vm = ui.getMenuVector(ui.stack[ui.pstack]);
			if ( vm != null ) { return (String)""+((List)vm.elementAt(0)).getSelectedIndex(); }
			return ""+0;
		}			

		if ( !pstr.equals("") ) { return istr+"("+pstr+")"; } //errr !
		else { return istr; }
	}

	//////////////////////////////////////
	// control list,editbox,forms.
	//////////////////////////////////////
	public boolean cmdctrl (String i) {
		
		if ( i.equals("listitem_list") ) {
			//if ( uaddon.getstring(1,ui.listmenu).equals("submenu") ) {
			//	Vector vm = ui.getMenuVector(ui.listmenu);
			//	display.setCurrent((Displayable)vm.elementAt(0));

			//ui.pstack++;
			//ui.stack[ui.pstack]=(String)vm.elementAt(2);

			int idx = ui.ptlist;

			fnick=ui.liststack[idx].getString(ui.liststack[idx].getSelectedIndex());
			item=fnick;
			handleinit(ui.listcmd[idx]);		
	
			return false;
		}	

		if ( i.equals("editbox") ) {
			ui.pstack--;
			handleadd(uaddon.stringToTopic(ui.editbox.getString()));
			return false;
		}
		
		if ( i.equals("chanmode_form") ) {
			String modes = ui.getChanmodeForm();
			System.out.println("chmod="+modes);
			if ( !modes.equals("  ") ) {	
				sock.send("MODE "+uaddon.getstring(2,ui.chanparam)+modes+"\n");
			}
			showcanvas();
			return false;
		}

		return true;
	}

	//////////////////////////////////////
	// call cmd with params
	//////////////////////////////////////
	public void cmddo () {
		String tmpmsg;		//temporary string var.

		////////////////////////////////////////
		// cmd $1-
		////////////////////////////////////////


		pcmd[0] = uaddon.getlaststring(1,mcmd);
		
		if ( ccmd.equals("raw") || ccmd.equals("quote") ) {
			sock.send(pcmd[0]+"\n");	
			
			lcanv.consout(lcanv.idwin,"*raw* "+pcmd[0]+"\n");
			showcanvas();
		}

		if ( ccmd.equals("list") ) {

			sock.send("LIST "+pcmd[0]+"\n");
			showcanvas();
		}

		if ( ccmd.equals("updatebuf") ) {

			main.exbuffer=pcmd[0];
			showcanvas();					
		}
		
		if ( ccmd.equals("quit") ) {
			main.stoptry=true;
			if ( pcmd[0].equals("") ) {
				sock.send("QUIT :"+dbrms.quitmsg+"\n");
			} else {
				sock.send("QUIT :"+pcmd[0]+"\n");
			}
			sock.close();
			showcanvas();
		}


		////////////////////////////////////////
		// cmd $1 $2-
		////////////////////////////////////////

		pcmd[0] = uaddon.getstring(1,mcmd);
		pcmd[1] = uaddon.getlaststring(2,mcmd);

		System.out.println("pcmd_0="+pcmd[0]);
		System.out.println("pcmd_1="+pcmd[1]);

		if ( ccmd.equals("submenu") ) {
			Vector vm = ui.getMenuVector(pcmd[0]);
			ui.pstack++;
			ui.stack[ui.pstack]=(String)vm.elementAt(2);
			display.setCurrent((Displayable)vm.elementAt(0));
		}


		if ( ccmd.equals("whois") ) {
			sock.send("WHOIS "+pcmd[0]+"\n");
			showcanvas();
		}
		if ( ccmd.equals("msg") ) {
			sock.send("PRIVMSG "+pcmd[0]+" :"+pcmd[1]+"\n");
			int id = main.getchanid(pcmd[0]);
						
			if ( id != -1 ) { 
				lcanv.consout(id,(char)0x4+"<"+main.mynick+"> "+pcmd[1]+"\n");
			} else {
				if ( pcmd[0].startsWith("#") ) { lcanv.consout(0,(char)0x4+pcmd[0]+" <"+main.mynick+"> "+pcmd[1]+"\n"); }
				else { lcanv.consout(lcanv.idwin,(char)0x4+"-> *"+pcmd[0]+"* "+pcmd[1]+"\n"); }
			}
			showcanvas();
		}
		if ( ccmd.equals("notice") ) {
			sock.send("NOTICE "+pcmd[0]+" :"+pcmd[1]+"\n");
			showcanvas();
		}
		if ( ccmd.equals("join") ) {
			sock.send("JOIN "+pcmd[0]+"\n");
			showcanvas();
		}
		if ( ccmd.equals("nick") ) {
			if ( pcmd[1].equals("connect") ) main.mynick=pcmd[0];
			sock.send("NICK "+pcmd[0]+"\n");
			showcanvas();
		}
		if ( ccmd.equals("rejoin") ) {
			sock.send("PART "+pcmd[0]+"\n");
			sock.send("JOIN "+pcmd[0]+"\n");
			showcanvas();
		}
		if ( ccmd.equals("topic") ) {
			sock.send("TOPIC "+pcmd[0]+" :"+pcmd[1]+"\n");
			showcanvas();
		}
		if ( ccmd.equals("ctcp") ) {
			sock.send("PRIVMSG "+pcmd[0]+" :"+(char)0x01+pcmd[1]+(char)0x01+"\n");
			showcanvas();
		}

		if ( ccmd.equals("nicklist") ) {
			ui.showListitem(lcanv.wins[lcanv.idwin].nicks.keys(),"submenu menu_nicklist");		//create listitem with enumeration of hashtable and menu
		}
		if ( ccmd.equals("banlist") ) {
			System.gc();
			ui.hashmod=new Hashtable(50);
			sock.send("MODE "+pcmd[0]+" +b\n");
		}
		if ( ccmd.equals("explist") ) {
			System.gc();
			ui.hashmod=new Hashtable(50);
			sock.send("MODE "+pcmd[0]+" +e\n");
		}
		if ( ccmd.equals("invlist") ) {
			System.gc();
			ui.hashmod=new Hashtable(50);
			sock.send("MODE "+pcmd[0]+" +I\n");
		}
		if ( ccmd.equals("mode") ) {
			sock.send("MODE "+pcmd[0]+" "+pcmd[1]+"\n");
			showcanvas();
		}
		if ( ccmd.equals("part") ) {
			sock.send("PART "+pcmd[0]+"\n");
			showcanvas();
		}
		if ( ccmd.equals("query") ) {
			
			lcanv.wins[lcanv.cntwins] = new winobj();
			lcanv.initobject(lcanv.cntwins);
			lcanv.wins[lcanv.cntwins].objname=pcmd[0];
			lcanv.wins[lcanv.cntwins].topic="";
			lcanv.wins[lcanv.cntwins].nicks=new Hashtable(10);
			lcanv.idwin=lcanv.cntwins;
			lcanv.cntwins++;
			showcanvas();	
		}
		if ( ccmd.equals("me") ) {
			sock.send("PRIVMSG "+pcmd[0]+" :"+(char)0x01+"ACTION "+pcmd[1]+(char)0x01+"\n");
			int id;
									
			if ( (id = main.getchanid(pcmd[0])) == -1 ) id = 0;
 
			lcanv.consout(id,(char)0x4+"\00313 * "+main.mynick+" "+pcmd[1]+"\n");

			showcanvas();
		}
		if ( ccmd.equals("away") ) {
			sock.send("AWAY :"+pcmd[0]+"\n");
			showcanvas();
		}
		if ( ccmd.equals("invite") ) {
			sock.send("INVITE "+pcmd[0]+" "+pcmd[1]+"\n");
			showcanvas();
		}
		if ( ccmd.equals("ban") ) {

			main.banchan=pcmd[0];
			main.bannick=uaddon.getstring(1,pcmd[1]);
			main.bantype=Integer.valueOf(uaddon.getstring(2,pcmd[1])).intValue();
			
			sock.send("WHOIS "+main.bannick+"\n");
			showcanvas();
		}
		if ( ccmd.equals("kick") ) {
			sock.send("KICK "+pcmd[0]+" "+uaddon.getstring(1,pcmd[1])+" "+uaddon.getlaststring(2,pcmd[1])+"\n");
			showcanvas();
		}
		if ( ccmd.equals("kickban") ) {

			main.banchan=pcmd[0];
			main.bannick=uaddon.getstring(1,pcmd[1]);
			main.bantype=Integer.valueOf(uaddon.getstring(2,pcmd[1])).intValue();
			main.bankick=true;
			main.banreason=uaddon.getlaststring(3,pcmd[1]);
			sock.send("WHOIS "+main.bannick+"\n");
			showcanvas();
		}
		if ( ccmd.equals("winlist") ) {
			ui.hashmod=new Hashtable(50);
			for ( int c=0; c < lcanv.cntwins; c++) {
				ui.hashmod.put(lcanv.wins[c].objname,"");
			}
			ui.showListitem(ui.hashmod.keys(),"submenu menu_winlist");		//create listitem with enumeration of hashtable and menu
		}
		if ( ccmd.equals("showwin") ) {
			lcanv.idwin=main.getchanid(pcmd[0]);
			showcanvas();
		}
		if ( ccmd.equals("copytobuf") ) {
			if ( pcmd[0].equals("0") ) { main.exbuffer=lcanv.get_selected_region(); }					//copy to buffer with colors
			if ( pcmd[0].equals("1") ) { main.exbuffer=lcanv.getwctrlcode(lcanv.get_selected_region()); }			//copy to buffer w/o colors
			if ( pcmd[0].equals("2") ) { main.exbuffer=main.exbuffer+lcanv.get_selected_region(); }				//add to buffer with colors
			if ( pcmd[0].equals("3") ) { main.exbuffer=main.exbuffer+lcanv.getwctrlcode(lcanv.get_selected_region()); }	//add to buffer w/o colors			
			showcanvas();
		}

		if ( ccmd.equals("sublist") ) {

			ui.showListitem(((Hashtable)ui.hashmod.get(item)).keys(),"submenu menu_chanlist");

		}

		if ( ccmd.equals("infochan") ) {

			String ichan = (String)((Hashtable)ui.hashmod.get(item.substring(0,2))).get(item);
			
			Form iform = new Form("Info");
			iform.append("Users:"+uaddon.getstring(1,ichan)+"\n");
			iform.append("Modes"+uaddon.getstring(2,ichan)+"\n");
			iform.append("Topic:"+uaddon.getlaststring(3,ichan)+"\n");
			iform.addCommand(ui.BTN_CANCEL);
			iform.setCommandListener(ui);

			ui.pstack++;
			ui.stack[ui.pstack]="infochan_form";
			display.setCurrent((Displayable)iform);
			
			
		}


		if ( ccmd.equals("close") ) {						//close channel/query
			int id = main.getchanid(pcmd[0]);
			if ( id == 0 ) { showcanvas(); return; }			//dont close status
			if ( pcmd[0].startsWith("#") ) {
				sock.send("PART "+pcmd[0]+"\n");
			} else {
				lcanv.wins[id].nicks=null;
				int did = id+1;
				while ( did != lcanv.cntwins ) {
					lcanv.wins[id]=lcanv.wins[did];
					did++;
					id++;
				}
				lcanv.cntwins--;	
				lcanv.idwin=lcanv.cntwins-1;
				
			}
			System.gc();
			showcanvas();
		}

		if ( ccmd.equals("addmenucmd") ) {
			
			if ( pcmd[1].indexOf(":") == -1 || pcmd[1].indexOf(":")+1 > pcmd[1].length() ) return;

			Vector vm = ui.getMenuVector(pcmd[0]);
			String newname = pcmd[1].substring(0,pcmd[1].indexOf(":"));
			String newcmd =  pcmd[1].substring(pcmd[1].indexOf(":")+1);

			dbrms.modifyMenuValue( (int)((Integer)vm.elementAt(1)).intValue() ,-1,newname,newcmd);
			((List)vm.elementAt(0)).append(newname,null);
			vm.addElement((String)newcmd);
			showstack();
			
		}
		
		if ( ccmd.equals("addmenu") ) {

			int rid = dbrms.modifyMenu(-1,"");
			Vector vm = ui.getMenuVector(pcmd[0]);
			String newname = pcmd[1];
			String newcmd =  "submenu mrid"+rid;

			dbrms.modifyMenuValue((int)((Integer)vm.elementAt(1)).intValue(),-1,newname,newcmd);
			((List)vm.elementAt(0)).append(newname,null);
			vm.addElement((String)newcmd);
			vm = new Vector(7,5);
			
			List list = new List("Menu",List.IMPLICIT);
			list.addCommand(ui.BTN_OK);
			list.addCommand(ui.BTN_ADDMNUCMD);
			list.addCommand(ui.BTN_ADDMNU);
			list.addCommand(ui.BTN_DELMNU);
			list.addCommand(ui.BTN_EDITMNU);
			list.addCommand(ui.BTN_UPMNU);
			list.addCommand(ui.BTN_DOWNMNU);
			list.addCommand(ui.BTN_CANCEL);
			list.setCommandListener(ui);
			vm.addElement(list);		
			vm.addElement(new Integer(rid));
			vm.addElement("mrid"+rid);
			ui.menus.addElement((Vector)vm);
			showstack();
		}

		if ( ccmd.equals("delmenu") ) {
			
			Vector vm = ui.getMenuVector(pcmd[0]);
			int wnum = Integer.valueOf(pcmd[1]).intValue();
			String wcmd = (String)vm.elementAt(wnum+3);
			dbrms.modifyMenuValue((int)((Integer)vm.elementAt(1)).intValue(),wnum,"","");	//delete cmd from parent
			
			((List)vm.elementAt(0)).delete(wnum);		//delete in List
			vm.removeElementAt(wnum+3);			//delete in Vector

			if ( wcmd.startsWith("submenu") ) {						//delete menu
				
				vm = ui.getMenuVector(uaddon.getstring(2,wcmd));			//find vector of deleting menu
				dbrms.modifyMenu((int)((Integer)vm.elementAt(1)).intValue(),"");	//delete menu record

				ui.menus.removeElement(vm);
			} 
			showstack();
			
		}
		if ( ccmd.equals("editmenu") ) {
			Vector vm = ui.getMenuVector(pcmd[0]);
			int wnum = Integer.valueOf(pcmd[1]).intValue();
			String wcmd = (String)vm.elementAt(wnum+3);
			
			if ( wcmd.startsWith("submenu") ) {
				handleinit("writemenu "+pcmd[0]+" "+pcmd[1]+" $txt("+(String)((List)vm.elementAt(0)).getString(wnum)+"):"+wcmd);
			} else {
				handleinit("writemenu "+pcmd[0]+" "+pcmd[1]+" $txt("+(String)((List)vm.elementAt(0)).getString(wnum)+"):$txt("+wcmd+")");
			}
			return;
		}
		if ( ccmd.equals("upmenu") ) {
			Vector vm = ui.getMenuVector(pcmd[0]);
			int wnum = Integer.valueOf(pcmd[1]).intValue();			
			
			if ( wnum == 0 ) return;

			String prevname = (String)((List)vm.elementAt(0)).getString(wnum-1);
			String prevcmd = (String)vm.elementAt(wnum+3-1);

			String newname = (String)((List)vm.elementAt(0)).getString(wnum);
			String newcmd = (String)vm.elementAt(wnum+3);

			subwritemenu(vm,wnum-1,newname,newcmd);
			subwritemenu(vm,wnum,prevname,prevcmd);

			showstack();

		}
		if ( ccmd.equals("downmenu") ) {

			Vector vm = ui.getMenuVector(pcmd[0]);
			int wnum = Integer.valueOf(pcmd[1]).intValue();			
			
			System.out.println("wnum="+wnum+" size()="+(int)((List)vm.elementAt(0)).size());

			if ( wnum == ((int)((List)vm.elementAt(0)).size()-1) ) return;

			String nextname = (String)((List)vm.elementAt(0)).getString(wnum+1);
			String nextcmd = (String)vm.elementAt(wnum+3+1);

			String newname = (String)((List)vm.elementAt(0)).getString(wnum);
			String newcmd = (String)vm.elementAt(wnum+3);

			subwritemenu(vm,wnum+1,newname,newcmd);
			subwritemenu(vm,wnum,nextname,nextcmd);

			showstack();
		
		}


		////////////////////////////////////////
		// cmd $1 $2 $3-
		////////////////////////////////////////

		pcmd[0] = uaddon.getstring(1,mcmd);
		pcmd[1] = uaddon.getstring(2,mcmd);
		pcmd[2] = uaddon.getlaststring(3,mcmd);

		if ( ccmd.equals("writemenu") ) {	//"menu" "num" "new value"
			
			Vector vm = ui.getMenuVector(pcmd[0]);			//menu
			int wnum = Integer.valueOf(pcmd[1]).intValue();		//nitem
			String newname = pcmd[2].substring(0,pcmd[2].indexOf(":"));
			String newcmd =  pcmd[2].substring(pcmd[2].indexOf(":")+1);

			subwritemenu(vm,wnum,newname,newcmd);
			showstack();
		}
		


	} //cmddo 
	

	/////////////////////////////////////////////////
	//
	/////////////////////////////////////////////////
	public void subwritemenu(Vector vm,int wnum,String n,String c) {

		((List)vm.elementAt(0)).set(wnum,n,null);		//update name in list
		vm.setElementAt((String)c,wnum+3);			//update cmd in vector
			
		dbrms.modifyMenuValue( (int)((Integer)vm.elementAt(1)).intValue() ,wnum, n, c);
	}


	/////////////////////////////////////////////////
	// local function for fast call and comfortable
	/////////////////////////////////////////////////
	public void showcanvas() {
		ui.pstack=0;
		ui.ptlist=0;
		lcanv.showcanvas();
	}
		
	public void showstack() {
		Vector vm = ui.getMenuVector(ui.stack[ui.pstack]);
		display.setCurrent((Displayable)vm.elementAt(0));
	}


} //class cmdmod