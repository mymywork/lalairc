/**************************************************
*
* 	      User Interface module.
*
***************************************************/
import java.io.*;
import java.lang.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

public class uimod implements CommandListener {

	public concanvas lcanv;
	public netsocket sock;
	public database dbrms;
	public lala main;
	public addon uaddon;
	public cmdmod cmd;
	public semaphore sm;
	public scolor sclr;
	public Display display;

	public Command CurrentCommand;
	public Command CurrentAction;
  
	//////////////////////////////////////////////////////////////////////////////////////

	//Main menu

	private Command CMD_RUNCON   = new Command("Connect", Command.SCREEN, 1); 
	private Command CMD_PROFILE  = new Command("Profile", Command.SCREEN, 1); 
	private Command CMD_OPTIONS  = new Command("Options", Command.SCREEN, 1); 
	private Command CMD_QUIT     = new Command("Quit", Command.SCREEN, 1); 

	//Profile Menu

	private Command CMD_ADDPROFILE    = new Command("Add Profile", Command.SCREEN, 1); 
	private Command CMD_EDITPROFILE   = new Command("Edit Profile", Command.SCREEN, 1); 
	private Command CMD_DELPROFILE    = new Command("Delete Profile", Command.SCREEN, 1);
	private Command CMD_SELECTPROFILE = new Command("Select Profile", Command.SCREEN, 1); 

	// Profile TextFields

	TextField txtprofilename;
	TextField txtnick;
	TextField txtuser; 	
	TextField txtreal; 	
	TextField txtaltnick; 	
	TextField txtircsrv; 	
	TextField txtport; 	
	TextField txtchans; 	
	TextField txtsrvpass; 	
	TextField txtnspass; 

	// Options Items

	ChoiceGroup optgrp;
	ChoiceGroup uoptgrp;
	ChoiceGroup encgrp;
	ChoiceGroup fontgrp;
	ChoiceGroup congrp;

	TextField opttrysnum;
	TextField opttimeout;
	TextField optbufsize;
	TextField optquitmsg;
	TextField optctcpver;


	TextField optconhost;
	TextField optconport;

	// ModeChan Items

	TextField bopt[];
	TextField copt[];
	ChoiceGroup dopt;

	// Forms

	Form	fchanmod;
	Form	fprofile;
	Form	foptions;
	Form	mmenu;
	List	lprofiles;
	TextBox	editbox;

	// ListItem
	
	//List	listitem;
	//String  listmenu;		//called after select item in list

	// Button simple

	public Command BTN_OK     	= new Command("OK", Command.SCREEN, 1);
	public Command BTN_ADDMNUCMD	= new Command("Add command", Command.SCREEN, 1);
	public Command BTN_ADDMNU  	= new Command("Add menu", Command.SCREEN, 1);
	public Command BTN_DELMNU  	= new Command("Del item/menu", Command.SCREEN, 1);
	public Command BTN_EDITMNU 	= new Command("Edit menu", Command.SCREEN, 1);	
	public Command BTN_UPMNU 	= new Command("Up", Command.SCREEN, 1);	
	public Command BTN_DOWNMNU 	= new Command("Down", Command.SCREEN, 1);	
	public Command BTN_CANCEL  	= new Command("Cancel", Command.CANCEL, 1);

	// Addon button functions

	private Command BTN_PASTE       = new Command("Paste buffer", Command.SCREEN, 1);
	private Command BTN_ICOLOR      = new Command("Insert color code", Command.SCREEN, 1);
	private Command BTN_ULINE	= new Command("Underline code", Command.SCREEN, 1);
	private Command BTN_INVERSE	= new Command("Inverse code", Command.SCREEN, 1);
	private Command BTN_PLAIN	= new Command("Plain code", Command.SCREEN, 1);
	private Command BTN_BOLD	= new Command("Bold code", Command.SCREEN, 1);

	// Canvas menu button

	public  Command BTN_MENU    = new Command("menu", Command.SCREEN, 1);

	/////////////////////////////
	
	Hashtable hashmod;		//bans,exps,invs

	String 	chanparam;		//global chan param for chanmodes

	//////////////////////////////////////////////////////////////////////////////////////

	//int	cntmenus=0;		// WARRING !!!
	Vector	menus;			// 0 status, 1 chan, 2 querys
	String	stack[]= new String[10];
	int	pstack=0;

	int	lststck=4;
	int	ptlist=0;

	List	liststack[] = new List[lststck];
	String  listcmd[] = new String[lststck];
	

	public  uimod () {


		///// Create Options Form /////

		optgrp = new ChoiceGroup("Options",ChoiceGroup.MULTIPLE);	//9
		optgrp.append("SkipMotd",null);
		optgrp.append("PingPong",null);
		optgrp.append("AutoRejoin",null);
		optgrp.append("ShowHost",null);
		optgrp.append("TimeStamp",null);
		optgrp.append("ColorsOff",null);
		optgrp.append("AutoReconnect",null);
		optgrp.append("JPQN",null);
		optgrp.append("Polling",null);		

		fontgrp = new ChoiceGroup("Font",ChoiceGroup.EXCLUSIVE);
		fontgrp.append("Small",null);
		fontgrp.append("Medium",null);
		fontgrp.append("Large",null);

		opttrysnum = new TextField("Count of reconnect trys:","",4,TextField.NUMERIC);
		opttimeout = new TextField("Reconnect timeout:","",4,TextField.NUMERIC);

		optbufsize = new TextField("Buffer lines:","",4,TextField.NUMERIC);
		optquitmsg = new TextField("Quit message:","",50,TextField.ANY);
		optctcpver = new TextField("Ctcp ver-reply:","",50,TextField.ANY);

		congrp = new ChoiceGroup("Firewall",ChoiceGroup.EXCLUSIVE);
		congrp.append("Direct",null);
		congrp.append("Proxy",null);
		congrp.append("Socks4",null);
		congrp.append("Socks5",null);

		optconhost = new TextField("Proxy/Socks Host:","",50,TextField.ANY);
		optconport = new TextField("Proxy/Socks Port:","",50,TextField.NUMERIC);


		foptions = new Form("Options");
		foptions.append(optgrp);
		foptions.append(fontgrp);

		foptions.append(opttrysnum);
		foptions.append(opttimeout);

		foptions.append(optbufsize);
		foptions.append(optquitmsg);
		foptions.append(optctcpver);
		foptions.append(congrp);
		foptions.append(optconhost);
		foptions.append(optconport);

		foptions.addCommand(BTN_OK);
		foptions.addCommand(BTN_CANCEL);
		foptions.setCommandListener(this);

		///// Create Profile Form /////

		txtprofilename   = new TextField("Profile name:","",50,TextField.ANY);
		txtnick 	 = new TextField("Nick:","",50,TextField.ANY);
		txtuser 	 = new TextField("User:","",50,TextField.ANY);
		txtreal 	 = new TextField("Real:","",50,TextField.ANY);
		txtaltnick 	 = new TextField("AltNick:","",50,TextField.ANY);
		txtircsrv 	 = new TextField("Server:","",50,TextField.ANY);
		txtport 	 = new TextField("Port:","",50,TextField.NUMERIC);
		
		uoptgrp = new ChoiceGroup("utf-options",ChoiceGroup.MULTIPLE);
		uoptgrp.append("UTF8Auto",null);
		uoptgrp.append("UTF8Write",null);	

		encgrp = new ChoiceGroup("Encoding",ChoiceGroup.EXCLUSIVE);
		encgrp.append("KOI-8",null);
		encgrp.append("CP-1251",null);
		encgrp.append("CP-1255",null);
		encgrp.append("UTF-8",null);

		txtchans 	 = new TextField("Chans:","",50,TextField.ANY);
		txtsrvpass 	 = new TextField("Server password:","",50,TextField.ANY);
		txtnspass 	 = new TextField("Nickserv password:","",50,TextField.ANY);

		fprofile = new Form("Profile");
		fprofile.append(txtprofilename);
		fprofile.append(txtnick);
		fprofile.append(txtuser);
		fprofile.append(txtreal);
		fprofile.append(txtaltnick);
		fprofile.append(txtircsrv);
		fprofile.append(txtport);
		fprofile.append(uoptgrp);
		fprofile.append(encgrp);
		fprofile.append(txtchans);
		fprofile.append(txtsrvpass);
		fprofile.append(txtnspass);

		fprofile.addCommand(BTN_OK);
		fprofile.addCommand(BTN_CANCEL);

		fprofile.setCommandListener(this);

		///// Create edit box //////

		editbox = new TextBox("enter text:","",300,TextField.ANY);	
		editbox.addCommand(BTN_OK);
		editbox.addCommand(BTN_PASTE);
		editbox.addCommand(BTN_ICOLOR);
		editbox.addCommand(BTN_ULINE);
		editbox.addCommand(BTN_INVERSE);
		editbox.addCommand(BTN_PLAIN);
		editbox.addCommand(BTN_BOLD);

		editbox.addCommand(BTN_CANCEL);
		editbox.setCommandListener(this);
		
		// Append button menu to ConCanvas
		

	}

	////////////////////////////////////////
	// Make List in Vector!!!!!!!!!!!!!!
	////////////////////////////////////////
	/*public void makeVectorList(Vector v) {
		

		List list = new List("Menu",List.IMPLICIT);

		v.setElementAt(list,0);

		list.addCommand(BTN_OK);
		list.addCommand(BTN_CANCEL);
		list.setCommandListener(this);

		for (int i=2; i != v.size();i++ ) {

			list.append((String)v.elementAt(i),null);
		}
	}
	*/
	////////////////////////////////////////
	// Create Chanmode Form
	////////////////////////////////////////
	public void createChanmodeForm() {

		fchanmod = new Form("Chan modes");

		fchanmod.addCommand(BTN_OK);
		fchanmod.addCommand(BTN_CANCEL);
		fchanmod.setCommandListener(this);

		bopt = new TextField[main.bmodes.length()];

		for (int i = 0; i < main.bmodes.length() ; i++ ) {
			bopt[i] = new TextField(main.bmodes.substring(i,i+1),"",30,TextField.ANY);
			fchanmod.append(bopt[i]);
		}

		copt = new TextField[main.cmodes.length()];

		for (int i = 0; i < main.cmodes.length() ; i++ ) {
			copt[i] = new TextField(main.cmodes.substring(i,i+1),"",30,TextField.ANY);
			fchanmod.append(copt[i]);
		}
	
		dopt = new ChoiceGroup("sets",ChoiceGroup.MULTIPLE);
		fchanmod.append(dopt);

		for (int i = 0; i < main.dmodes.length() ; i++ ) {
			dopt.append(main.dmodes.substring(i,i+1),null);
		}
		
	}

	////////////////////////////////////////
	// Set Chanmode Form
	////////////////////////////////////////
	public void setChanmodeForm(String p) {

		chanparam=p;

		String smods = uaddon.getstring(3,p);
		
		boolean sets[] = new boolean[dopt.size()];
		for (int i=0; i < main.dmodes.length(); i++ ) { sets[i]=false; }
		for (int i=0; i < main.bmodes.length(); i++ ) { bopt[i].setString(""); }
		for (int i=0; i < main.cmodes.length(); i++ ) { copt[i].setString(""); }

		main.setmods(sets,main.dmodes,smods,true);
		dopt.setSelectedFlags(sets);
		
		int n = 4;

		for (int i=0; i < smods.length() ; i++) {
			for (int b=0,c=0; b < main.bmodes.length()  ; b++ ) {
								
				if ( main.bmodes.charAt(b) == smods.charAt(i) ) {
					bopt[b].setString(uaddon.getstring(n,p));
					n++;
					break;
				}			
			}
			for (int c=0;  c < main.cmodes.length() ; c++) {
				if ( main.cmodes.charAt(c) == smods.charAt(i) ) {
					copt[c].setString(uaddon.getstring(n,p));
					n++;
					break;
				}
			}
		}
		
		pstack++;
		stack[pstack]="chanmode_form";
		display.setCurrent(fchanmod);	

	}
	////////////////////////////////////////
	// Get Chanmode Form
	////////////////////////////////////////
	public String getChanmodeForm() {

		String nmods=" ";
		String pmods=" ";
		String smods = uaddon.getstring(3,chanparam);

		
		boolean asets[] = new boolean[dopt.size()];
		boolean bsets[] = new boolean[dopt.size()];
		for (int i=0; i < dopt.size(); i++ ) { asets[i]=false; bsets[i]=false; }

		main.setmods(asets,main.dmodes,smods,true);
		dopt.getSelectedFlags(bsets);

		for (int i=0; i < dopt.size(); i++) {
			if ( asets[i] == true && bsets[i] == false ) { nmods=nmods+"-"+main.dmodes.charAt(i); }
			if ( asets[i] == false && bsets[i] == true ) { nmods=nmods+"+"+main.dmodes.charAt(i); }
		}

		
		int n = 4;

		for (int i=0; i < smods.length() ; i++) {
			for (int b=0; b < main.bmodes.length() ; b++ ) {
								
				if ( main.bmodes.charAt(b) == smods.charAt(i) ) {
					if ( bopt[b].getString().equals("") ) {
						nmods=nmods+"-"+main.bmodes.charAt(b);
						pmods=pmods+" "+uaddon.getstring(n,chanparam);
					} else if ( bopt[b].getString().equals(uaddon.getstring(n,chanparam)) ) { bopt[b].setString(""); }
					n++;
					break;
				}			
			}
			for (int c=0;  c < main.cmodes.length() ; c++) {
				if ( main.cmodes.charAt(c) == smods.charAt(i) ) {
					if ( copt[c].getString().equals("") ) {
						nmods=nmods+"-"+main.cmodes.charAt(c);
					} else if ( copt[c].getString().equals(uaddon.getstring(n,chanparam)) ) { copt[c].setString(""); }
					n++;
					break;
				}
			}
		}

		for (int b=0; b < main.bmodes.length() ; b++ ) {
			if ( !bopt[b].getString().equals("") ) {
				nmods=nmods+"+"+main.bmodes.charAt(b);
				pmods=pmods+" "+bopt[b].getString();
			}
		}
		for (int c=0; c < main.cmodes.length() ; c++ ) {
			if ( !copt[c].getString().equals("") ) {
				nmods=nmods+"+"+main.cmodes.charAt(c);
				pmods=pmods+" "+copt[c].getString();
			}
		}
		
		return nmods+pmods;
	}

	////////////////////////////////////////
	// Edit Profile
	////////////////////////////////////////
	public void showProfile() {

		txtprofilename.setString(dbrms.profilename);
		txtnick.setString(dbrms.nick);
		txtuser.setString(dbrms.user);
		txtreal.setString(dbrms.real);
		txtaltnick.setString(dbrms.altnick);
		txtircsrv.setString(dbrms.ircsrv);
		txtport.setString(Integer.toString(dbrms.port));

		uoptgrp.setSelectedIndex(0,dbrms.utf8autord);
		uoptgrp.setSelectedIndex(1,dbrms.utf8write);

		encgrp.setSelectedIndex(dbrms.encindex,true);

		txtchans.setString(dbrms.chans);
		txtsrvpass.setString(dbrms.srvpass);
		txtnspass.setString(dbrms.nspass);
		
		display.setCurrent(fprofile);
	}

	////////////////////////////////////////
	// Get Profile Vaules from user
	///////////////////////////////////////
	public void getProfileValues() {

		dbrms.profilename=txtprofilename.getString();
		dbrms.nick=txtnick.getString(); 	
		dbrms.user=txtuser.getString(); 	
		dbrms.real=txtreal.getString(); 	
		dbrms.altnick=txtaltnick.getString(); 	
		dbrms.ircsrv=txtircsrv.getString(); 	
		dbrms.port=Integer.valueOf(txtport.getString()).intValue(); 	

		boolean[] uopts= new boolean[2];
		uoptgrp.getSelectedFlags(uopts);
	
		dbrms.utf8autord=uopts[0];
		dbrms.utf8write=uopts[1];

		dbrms.encindex=encgrp.getSelectedIndex();		

		dbrms.chans=txtchans.getString();
		dbrms.srvpass=txtsrvpass.getString();	
		dbrms.nspass=txtnspass.getString();

	}

	
	////////////////////////////////////////
	// Show options
	////////////////////////////////////////
	public void showOptions() {

		optgrp.setSelectedIndex(0,dbrms.skpmotd);
		optgrp.setSelectedIndex(1,dbrms.pingpong);
		optgrp.setSelectedIndex(2,dbrms.autorejoin);
		optgrp.setSelectedIndex(3,dbrms.showhost);
		optgrp.setSelectedIndex(4,dbrms.timestamp);
		optgrp.setSelectedIndex(5,dbrms.colorsoff);
		optgrp.setSelectedIndex(6,dbrms.autoreconn);
		optgrp.setSelectedIndex(7,dbrms.jpqn);
		optgrp.setSelectedIndex(8,dbrms.polling);

		fontgrp.setSelectedIndex(dbrms.fontindex,true);

		opttrysnum.setString(Integer.toString(dbrms.trysnum));
		opttimeout.setString(Integer.toString(dbrms.timeout));

		optbufsize.setString(Integer.toString(dbrms.bufsize));
		optquitmsg.setString(dbrms.quitmsg);
		optctcpver.setString(dbrms.ctcpver);

		congrp.setSelectedIndex(dbrms.contype,true);
		optconhost.setString(dbrms.conhost);
		optconport.setString(Integer.toString(dbrms.conport));
	
		display.setCurrent(foptions);

	}
	
	////////////////////////////////
	// Get Options Values
	////////////////////////////////
	public void getOptionsValues() {

		boolean[] opts= new boolean[9];
		optgrp.getSelectedFlags(opts);
	
		dbrms.skpmotd=opts[0];
		dbrms.pingpong=opts[1];
		dbrms.autorejoin=opts[2];
		dbrms.showhost=opts[3];
		dbrms.timestamp=opts[4];
		dbrms.colorsoff=opts[5];
		dbrms.autoreconn=opts[6];
		dbrms.jpqn=opts[7];
		dbrms.polling=opts[8];


		dbrms.fontindex=fontgrp.getSelectedIndex();
	
		dbrms.trysnum=Integer.valueOf(opttrysnum.getString()).intValue();
		dbrms.timeout=Integer.valueOf(opttimeout.getString()).intValue();

		dbrms.bufsize=Integer.valueOf(optbufsize.getString()).intValue();
		dbrms.quitmsg=optquitmsg.getString();
		dbrms.ctcpver=optctcpver.getString();

		dbrms.contype=congrp.getSelectedIndex();
		dbrms.conhost=optconhost.getString();
		dbrms.conport=Integer.valueOf(optconport.getString()).intValue();


	}

	//////////////////////////
	// makeMainMenu
	//////////////////////////
	public void makeMainMenu() {

		mmenu = new Form("Main");
		mmenu.append("lame-irc-client");
		mmenu.addCommand(CMD_RUNCON);
		mmenu.addCommand(CMD_PROFILE);
		mmenu.addCommand(CMD_OPTIONS);
		mmenu.addCommand(CMD_QUIT);
		mmenu.setCommandListener(this);
		dbrms.loadOptions();			//loadOptions
		dbrms.loadMenus();			//loadOptions
		dbrms.getProfileNameId();		//make profile array of id's
	}


	/////////////////////////
	// Create List Profiles
	/////////////////////////
	public void showListProfiles() {

		String[] pnames = dbrms.getProfileNameId();
		lprofiles = new List("List",List.IMPLICIT,pnames,null);
		lprofiles.addCommand(CMD_ADDPROFILE);
		lprofiles.addCommand(CMD_EDITPROFILE);
		lprofiles.addCommand(CMD_DELPROFILE);
		lprofiles.addCommand(CMD_SELECTPROFILE);
		lprofiles.setCommandListener(this);
		if ( dbrms.activeProfile != -1 ) lprofiles.setSelectedIndex(dbrms.activeProfile,true);
		display.setCurrent(lprofiles);

	}	

	////////////////////////////
	// showListitem
	////////////////////////////
	public void showListitem(Enumeration e,String menu) {
		
		pstack++;
		ptlist++;
		
		stack[pstack]="listitem_list";

		List listitem = new List("list",List.IMPLICIT);
		listitem.addCommand(BTN_OK);
		listitem.addCommand(BTN_CANCEL);
		listitem.setCommandListener(this);
		
		while ( e.hasMoreElements() ) listitem.append((String)e.nextElement(),null);

		listcmd[ptlist]=(String)menu;
		liststack[ptlist]=(List)listitem;
					
		display.setCurrent(listitem);
	}

	///////////////////////////
	// Create showEditbox
	///////////////////////////
	public void showEditbox(String s) {
						
			pstack++;
			stack[pstack]="editbox";			//activate editbox
			if ( editbox.size() != 0 ) { editbox.delete(0,editbox.size()); }
			editbox.setString(s);
			display.setCurrent(editbox);
	}

	/////////////////////////////////
	// Get Vector of Menu String ID
	/////////////////////////////////
	public Vector getMenuVector(String s) {

		for (int i=0; i < menus.size(); i++ ) {
			if ( ((Vector)menus.elementAt(i)).contains(s) ) { return (Vector)menus.elementAt(i); }
		}
		return null;
	}

	/////////////////////////////////////////////////////
	// Command Actions
	/////////////////////////////////////////////////////
	public void commandAction(Command c, Displayable d) {

	///// Main Menu /////

	if (c == CMD_RUNCON ) {
		CurrentCommand=null;
						//create arraysProfiles with id's
		dbrms.loadProfile(dbrms.activeProfile);

		main.ircencode=uaddon.getencodebyindex(dbrms.encindex);	//encode
		
		sm.notifysignal();		//signal unlock main thread
		System.gc();
	}

	if (c == CMD_PROFILE ) {
		CurrentCommand=c;
		showListProfiles();
	}
	
	if (c == CMD_OPTIONS ) {
		CurrentCommand=c;
		dbrms.loadOptions();
		showOptions();
	}

	if (c == CMD_QUIT ) {
		main.notifyDestroyed();
	}

	///// Profile Menu /////

	if (c == CMD_ADDPROFILE ) {
		CurrentCommand=c;
		dbrms.resetProfile();
		showProfile();
	}

	if (c == CMD_EDITPROFILE ) {
		CurrentCommand=c;
		if ( lprofiles.getSelectedIndex() != -1 ) { 
			lprofiles.setSelectedIndex(lprofiles.getSelectedIndex(),true);
			dbrms.loadProfile(lprofiles.getSelectedIndex());
			showProfile();
		}
	}

	if (c == CMD_DELPROFILE ) {
		dbrms.deleteProfile(lprofiles.getSelectedIndex());
		showListProfiles();
	}

	if (c == CMD_SELECTPROFILE ) {
		dbrms.activeProfile=lprofiles.getSelectedIndex();
		dbrms.safeActiveProfile();
		display.setCurrent(mmenu);
	}

	///// Canvas Button Menu /////

	if (c == BTN_MENU ) {

		main.lockmenu=true; 		//lockmenu for connect cycle.
		
		if ( lcanv.wins[lcanv.idwin].objname.equals("status") ) {		// status
			display.setCurrent((Displayable)((Vector)menus.elementAt(0)).elementAt(0));  
			pstack++;
			stack[pstack]=(String)((Vector)menus.elementAt(0)).elementAt(2);
		}
		else if ( lcanv.wins[lcanv.idwin].objname.startsWith("#") ) {		//chan
			display.setCurrent((Displayable)((Vector)menus.elementAt(1)).elementAt(0)); 
			pstack++;
			stack[pstack]=(String)((Vector)menus.elementAt(1)).elementAt(2);
		}
		else {  
			display.setCurrent((Displayable)((Vector)menus.elementAt(2)).elementAt(0));		//query
			pstack++;
			stack[pstack]=(String)((Vector)menus.elementAt(2)).elementAt(2);
			cmd.fnick=lcanv.wins[lcanv.idwin].objname;
		}

	}

	///// Other /////

	if (c == BTN_OK || c == List.SELECT_COMMAND) {

		///// Options /////
		
		if ( CurrentCommand == CMD_OPTIONS ) {
			getOptionsValues();
			dbrms.safeOptions();
			display.setCurrent(mmenu);
		}

		///// Profile /////

		if ( CurrentCommand == CMD_ADDPROFILE ) {
			getProfileValues();
			dbrms.addProfile();
			showListProfiles();

		}
		if ( CurrentCommand == CMD_EDITPROFILE ) {
			getProfileValues();
			dbrms.editProfile(lprofiles.getSelectedIndex());
			showListProfiles();
		}

		if ( CurrentCommand != null ) return;

		///// EditBox /////

		if ( cmd.cmdctrl(stack[pstack]) ) { 

		///// Menu [OK] /////

			Vector vm = getMenuVector(stack[pstack]);
			cmd.handleinit((String)vm.elementAt(((List)vm.elementAt(0)).getSelectedIndex()+3));
			return;

		}		

	} //// end button ok ////

	///// button cancel /////

	if ( c == BTN_CANCEL ) {

		if ( CurrentCommand != null ) {
			if ( CurrentCommand == CMD_ADDPROFILE || CurrentCommand == CMD_EDITPROFILE ) { display.setCurrent(lprofiles); }
			if ( CurrentCommand ==  CMD_OPTIONS ) { display.setCurrent(mmenu); }
			return;
		}
		
		if ( stack[pstack].equals("listitem_list") ) { liststack[ptlist]=null; listcmd[ptlist]=null; ptlist--; System.gc(); }
		
		pstack--;
		if ( pstack == 0 ) { lcanv.showcanvas(); }
		else if ( stack[pstack].equals("listitem_list") ) { display.setCurrent((List)liststack[ptlist]); }
		else {
			Vector vm = getMenuVector(stack[pstack]);
			display.setCurrent((Displayable)vm.elementAt(0));
			return;
		}

	} //// end button cancel ////


	///// editbox buttons /////

	if ( c == BTN_PASTE ) {
		editbox.insert(uaddon.topicToString(main.exbuffer),editbox.getCaretPosition());
		display.setCurrent(editbox);
	}

	if ( c == BTN_ICOLOR ) {
		display.setCurrent(sclr);			
	}

	if ( c == BTN_ULINE ) {
		//System.out.println("Caret="+editbox.getCaretPosition()+" length="+editbox.getString().length());
		
		editbox.insert("%u%",editbox.getCaretPosition());
		display.setCurrent(editbox);
	}
	if ( c == BTN_INVERSE ) {
		editbox.setString(editbox.getString()+"%i%");
		display.setCurrent(editbox);
	}
	if ( c == BTN_PLAIN ) {
		editbox.setString(editbox.getString()+"%p%");
		display.setCurrent(editbox);
	}
	if ( c == BTN_BOLD ) {
		editbox.setString(editbox.getString()+"%b%");
		display.setCurrent(editbox);
	}
	
	///// end editbox buttons //////

	///// menu context buttons /////

	if ( c == BTN_ADDMNUCMD ) {
		cmd.handleinit("addmenucmd "+stack[pstack]+" $txt(name:cmd)");		//add menu cmd to current menu
	}

	if ( c == BTN_ADDMNU ) {
		cmd.handleinit("addmenu "+stack[pstack]+" $txt(newmenu)");			
	}
	if ( c == BTN_DELMNU ) {
		cmd.handleinit("delmenu "+stack[pstack]+" $nitem");		
	}
	if ( c == BTN_EDITMNU ) {				
		cmd.handleinit("editmenu "+stack[pstack]+" $nitem");
	}
	if ( c == BTN_UPMNU ) {				
		cmd.handleinit("upmenu "+stack[pstack]+" $nitem");
	}
	if ( c == BTN_DOWNMNU ) {				
		cmd.handleinit("downmenu "+stack[pstack]+" $nitem");
	}
	
	///// end editbox buttons //////


	} // commandAction

} //class ui
