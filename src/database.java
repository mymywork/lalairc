/**************************************************
*
* 		Database rms library.
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
// Database
////////////////////////////////////////////////////
public class database {

	public uimod	ui;
	
        // Profile
	
	public String	profilename;
	public String	nick;
	public String	user;
	public String	real;
	public String	altnick;
	public String	ircsrv;
	public int	port;
	public boolean 	utf8autord;
	public boolean 	utf8write;
	public int 	encindex;		//number of opt
	public String	chans;
	public String	srvpass;
	public String	nspass;

	// Options

	public boolean 	skpmotd;
	public boolean 	pingpong;
	public boolean 	autorejoin;
	public boolean 	showhost;
	public boolean 	timestamp;
	public boolean 	colorsoff;
	public boolean 	autoreconn;
	public boolean 	jpqn;
	public boolean 	polling;


	public int     	fontindex;

	public int 	trysnum;
	public int 	timeout;

	public int 	bufsize;
	public String 	quitmsg;
	public String 	ctcpver;

	public int	contype;
	public String 	conhost;
	public int	conport;

	// other

	public int activeProfile=-1;
	public int arrayProfiles[];

	public database() {

		resetProfile();

		skpmotd=true;		//boolean
		pingpong=false;
		autorejoin=false;
		showhost=true;
		timestamp=false;
		colorsoff=false;
		autoreconn=true;
		jpqn=true;
		polling=true;
				
		fontindex=1;

		// Options

		trysnum	= 3;
		timeout = 10;

		bufsize = 10;		//String
		quitmsg = "ciao";
		ctcpver = "lame-ircclient on Linux 2.6.32-rc8 (%hw%)";
		
		contype = 0;
		conhost = "127.0.0.1";
		conport = 1080;

	}

	////////////////////////////////////
	// Reset Profile vaules by default
	////////////////////////////////////
	public void resetProfile() {

		profilename = "default";
		nick = "duser";
		user = "mobile";
		real = "Charlie Root";
		altnick = "duser_";
		ircsrv = "irc.dogm.net";
		port = 6667;
		chans = "#stk";
		srvpass = "";
		nspass = "";
		encindex=1;		//int
		utf8autord=true;
		utf8write=true;		
	}
	

	////////////////////////////////////
	// Load Options
	////////////////////////////////////
	public void loadOptions() {
	
		try
		{
			RecordStore recordstore = RecordStore.openRecordStore("options", true);
            		System.out.println("[numrec]="+recordstore.getNumRecords());
		
			if ( recordstore.getNumRecords() != 0 ) {
            		// Read section "options"
				try
            			{
                		System.out.println("[read options]");
		
					DataInputStream istrm = new DataInputStream(new ByteArrayInputStream(recordstore.getRecord(1)));
					activeProfile = istrm.readInt();
					
					istrm = new DataInputStream(new ByteArrayInputStream(recordstore.getRecord(2)));
					skpmotd = istrm.readBoolean();
					pingpong = istrm.readBoolean();
					autorejoin = istrm.readBoolean();
					showhost = istrm.readBoolean();
					timestamp = istrm.readBoolean();
					colorsoff = istrm.readBoolean();
					autoreconn = istrm.readBoolean();
					jpqn = istrm.readBoolean();
					polling = istrm.readBoolean();

					fontindex = istrm.readInt();

					trysnum = istrm.readInt();
					timeout = istrm.readInt();

					bufsize = istrm.readInt();
					quitmsg = istrm.readUTF();
					ctcpver = istrm.readUTF();

					contype = istrm.readInt();
					conhost = istrm.readUTF();
					conport = istrm.readInt();
					
                			istrm.close();

            			}
            			catch(Exception exp1)
            			{  /* getRecord */  }

			} 
			// Write default section "options"
			else {

				byte bufbyte[] = new byte[0];				//add null for last may set.
                		recordstore.addRecord(bufbyte, 0, bufbyte.length);	//for activeprofile
				recordstore.addRecord(bufbyte, 0, bufbyte.length);	//for options

				safeActiveProfile();
				safeOptions();
			}

			recordstore.closeRecordStore();


		}
		catch(Exception exp2)
            	{  /* openRecordStore fail create ? */  }
	}
	///////////////////////////
	// Write Options in rms
	///////////////////////////
	public void safeOptions() {
		try
        	{
			ByteArrayOutputStream bostrm = new ByteArrayOutputStream();
			DataOutputStream ostrm = new DataOutputStream(bostrm);

              		ostrm.writeBoolean(skpmotd);
			ostrm.writeBoolean(pingpong);
			ostrm.writeBoolean(autorejoin);
			ostrm.writeBoolean(showhost);
			ostrm.writeBoolean(timestamp);
			ostrm.writeBoolean(colorsoff);
			ostrm.writeBoolean(autoreconn);
			ostrm.writeBoolean(jpqn);
			ostrm.writeBoolean(polling);

			ostrm.writeInt(fontindex);

			ostrm.writeInt(trysnum);
			ostrm.writeInt(timeout);

			ostrm.writeInt(bufsize);
			ostrm.writeUTF(quitmsg);
			ostrm.writeUTF(ctcpver);

			ostrm.writeInt(contype);
			ostrm.writeUTF(conhost);
			ostrm.writeInt(conport);
				
			byte bufbyte[] = bostrm.toByteArray();
			ostrm.close();
			bostrm.close();
			
			RecordStore recordstore = RecordStore.openRecordStore("options", true);
				
			recordstore.setRecord(2, bufbyte, 0, bufbyte.length);
			recordstore.closeRecordStore();
		}
        	catch(Exception rmssetopt)
        	{ /* rms */ }

	}
	////////////////////////////////
	// Set Active Profile in rms
	////////////////////////////////
	public void safeActiveProfile() {
		try
		{
			
			ByteArrayOutputStream bostrm = new ByteArrayOutputStream();
            		DataOutputStream ostrm = new DataOutputStream(bostrm);
            		ostrm.writeInt(activeProfile);
            		byte bufbyte[] = bostrm.toByteArray();
            		ostrm.close();
            		bostrm.close();
            		RecordStore recordstore = RecordStore.openRecordStore("options", true);
            		recordstore.setRecord(1, bufbyte, 0, bufbyte.length);
            		recordstore.closeRecordStore();	
		}
		catch(Exception rmssetprfl)
        	{ /* rms */ }
		
	}

	///////////////////////////////////////////
	// Get Profile Ids and Names from store 1.
	///////////////////////////////////////////
	public String[] getProfileNameId()
	{
        	String names[] = null;
        	try
        	{
        		RecordStore recordstore = RecordStore.openRecordStore("profile", true);
        		if ( recordstore.getNumRecords() == 0 ) {
				recordstore.addRecord(new byte[4], 0, 4);
				arrayProfiles = new int[0];
				names = new String[0];
			} else {
				DataInputStream istrm = new DataInputStream(new ByteArrayInputStream(recordstore.getRecord(1)));
				recordstore.closeRecordStore();
				int i = istrm.readInt();
				names = new String[i];
				arrayProfiles = new int[i];
				for ( int z = 0; z < names.length; z++ ) {
					names[z] = istrm.readUTF();		//name of profile
					arrayProfiles[z] = istrm.readInt();	//id of record
					
					System.out.println("idRecord ="+arrayProfiles[z]);
		
				}
				istrm.close();
			}
        	}
       		catch(Exception expprflid)
        	{ /* rms */ }
        return names;
	}
	/////////////////////////////////
	// Load Profile from rms
	/////////////////////////////////
	public void loadProfile(int i) {
		if ( i < 0 ) { resetProfile(); } else 
		if ( i < arrayProfiles.length ) {
			try
	            	{
                		int r = arrayProfiles[i];

                		RecordStore recordstore = RecordStore.openRecordStore("profile", false);
                		if(r > 0)
	               		{
                    			DataInputStream istrm = new DataInputStream(new ByteArrayInputStream(recordstore.getRecord(r)));
					profilename = istrm.readUTF();
					nick = istrm.readUTF();
					user = istrm.readUTF();
					real = istrm.readUTF();
					altnick = istrm.readUTF();
					ircsrv = istrm.readUTF();
					port = istrm.readInt();
					
					utf8autord = istrm.readBoolean();
					utf8write = istrm.readBoolean();
					encindex  = istrm.readInt();					

					chans = istrm.readUTF();
					srvpass = istrm.readUTF();
					nspass = istrm.readUTF();
				}
				recordstore.closeRecordStore();
			}
			catch(Exception expldrprfl)
			{ /* rms */  }
		}
	}

	////////////////////////
	// Add Profile
	////////////////////////
	public void addProfile() {
		editProfile(arrayProfiles.length);
	}	

	////////////////////////
	// Edit Profile
	////////////////////////	
	public void editProfile(int i){
		try
        	{
			RecordStore recordstore = RecordStore.openRecordStore("profile", false);
			editProfileNameRecord(recordstore, i, profilename);
			ByteArrayOutputStream bostrm = new ByteArrayOutputStream();
			DataOutputStream ostrm = new DataOutputStream(bostrm);

			ostrm.writeUTF(profilename);
			ostrm.writeUTF(nick);
			ostrm.writeUTF(user);
			ostrm.writeUTF(real);
			ostrm.writeUTF(altnick);
			ostrm.writeUTF(ircsrv);
			ostrm.writeInt(port);
			ostrm.writeBoolean(utf8autord);
			ostrm.writeBoolean(utf8write);
			ostrm.writeInt(encindex);
			ostrm.writeUTF(chans);
			ostrm.writeUTF(srvpass);
			ostrm.writeUTF(nspass);
           
			byte bufbyte[] = bostrm.toByteArray();
			ostrm.close();
			
			recordstore.setRecord(arrayProfiles[i], bufbyte, 0, bufbyte.length);
			recordstore.closeRecordStore();
	        }
        	catch(Exception expedprfl)
        	{ /* rms */ }
	}	

	//////////////////////////
	// editProfileNameRecord
	//////////////////////////
	private void editProfileNameRecord(RecordStore recordstore, int i, String s) throws Exception
	{
        	boolean flag = false;
        	byte bufbyte[] = recordstore.getRecord(1);
        	DataInputStream istrm = new DataInputStream(new ByteArrayInputStream(bufbyte));
        	ByteArrayOutputStream bostrm = new ByteArrayOutputStream();
        	DataOutputStream ostrm = new DataOutputStream(bostrm);
        	
		int k = istrm.readInt();		//count of profiles
        	if(s == null && i >= 0 && i < k)	//delete
        	{
            		k--;
            		arrayProfiles = new int[k];	//new array of new length
        	} else
        	if(s != null && ( i < 0 || i >= k ) )	//add new profile
        	{
            		k++;
            		arrayProfiles = new int[k];	//recreate array
            		flag = true;			//flag
        	} else
        	if(s == null) return;			
        		
		ostrm.writeInt(k);			//write count of profiles

       		int j;
        	for(j = 0; j < k && (j < k - 1 || !flag) ; j++)		// j != k - 1 for add profile.
        	{
            		if(j == i)					//only for delete/edit.
            		{
                		if(s != null)
                		{
                    			istrm.readUTF();
                    			ostrm.writeUTF(s);			//edit profile name
					arrayProfiles[j] = istrm.readInt();	//update array of idRecord
					ostrm.writeInt(arrayProfiles[j]);
					continue;
				}
			istrm.readUTF();
			istrm.readInt();
			}
		ostrm.writeUTF(istrm.readUTF());
		arrayProfiles[j] = istrm.readInt();				//update array of idRecord
		ostrm.writeInt(arrayProfiles[j]);
		}

		if(flag)							//add profile
		{
			ostrm.writeUTF(s);
			arrayProfiles[j] = recordstore.getNextRecordID();
			ostrm.writeInt(arrayProfiles[j]);
			recordstore.addRecord(new byte[0], 0, 0);
		}
		bufbyte = bostrm.toByteArray();
		istrm.close();
		ostrm.close();
		recordstore.setRecord(1, bufbyte, 0, bufbyte.length);
		if(activeProfile >= arrayProfiles.length) activeProfile = arrayProfiles.length - 1; //modify current profile.
	
	} //editProfileNameRecord

	///////////////////////////////////
	// Delete Profile
	///////////////////////////////////
	public void deleteProfile(int i)
	{
		try
		{
			RecordStore recordstore = RecordStore.openRecordStore("profile", false);
			recordstore.deleteRecord(arrayProfiles[i]);
			editProfileNameRecord(recordstore, i, null);	// delete record of profile from record(1) of profile names and ids
			recordstore.closeRecordStore();
		}
		catch(Exception exception)
		{ /* rms */ }
	}
	/////////////////////////
	// Remove All
	/////////////////////////
	public void removeStoreAll() {
                try
                {
                    RecordStore.deleteRecordStore("profile");
                    RecordStore.deleteRecordStore("options");
                }
		catch(Exception exception)
		{ /* rms */ }		
	}

	/////////////////////////
	// Remove Menu
	/////////////////////////
	public void removeMenu() {
                try
                {
                    RecordStore.deleteRecordStore("menus");
                }
		catch(Exception exception)
		{ /* rms */ }		
	}
	

	/////////////////////////
	// load menu
	/////////////////////////
	public void loadMenus() {
		//removeMenu();
		System.out.println("loadMenus");
		try
        	{
        		RecordStore recordstore = RecordStore.openRecordStore("menus", true);
        		if ( recordstore.getNumRecords() == 0 ) {
				recordstore.addRecord(new byte[4], 0, 4);
				writeDefaultMenu();
			}
			DataInputStream istrm = new DataInputStream(new ByteArrayInputStream(recordstore.getRecord(1)));
			recordstore.closeRecordStore();

			int i = istrm.readInt();					//count of menus id's
			System.out.println("all menus count="+i);
			ui.menus = new Vector(i,5);
			//ui.cntmenus = i;
			for ( int z = 0; z < i; z++ ) {
					
				ui.menus.addElement(loadOneMenu(istrm.readInt()));		//id of record

			}
			istrm.close();
        	}
       		catch(Exception expprflid)
        	{ /* rms */ }

		System.out.println("all menus count="+ui.menus.size());
		for ( int z = 0; z < ui.menus.size(); z++ ) {
					
			System.out.println("system stringid="+((Vector)ui.menus.elementAt(z)).elementAt(2));
			for (int a=0; a < (((Vector)ui.menus.elementAt(z)).size()-3); a++) {
				System.out.println("mcmd="+((Vector)ui.menus.elementAt(z)).elementAt(a+3));
			}
			

		}	
	
	}
	/////////////////////////
	// load one menu
	/////////////////////////
	public Vector loadOneMenu (int rid) {
		Vector v=null;
		System.out.println("loadOneMenu");
		try	{
                		RecordStore recordstore = RecordStore.openRecordStore("menus", false);
                		
				DataInputStream istrm = new DataInputStream(new ByteArrayInputStream(recordstore.getRecord(rid)));
				
				int c = istrm.readInt();	//number of elements of menu	
				v = new Vector(c+3,5);
					
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

				v.addElement(list);
				v.addElement(new Integer(rid));	//rid
				v.addElement(istrm.readUTF());	//system string identificator. "menu_status"
					                    			
				for ( int m = 1; m <= c; m++ ) {
					list.append(istrm.readUTF(),null);	//visible name of command
					v.addElement(istrm.readUTF());		//command
				}
				recordstore.closeRecordStore();
			}
			catch(Exception exp)
			{ /* rms */  }
		return v;

	}

	/////////////////////////
	// writeDefaultMenu
	/////////////////////////
	public void writeDefaultMenu()
	{
		System.out.println("writeDefaultMenu");
		int id;

		// Status

		id = modifyMenu(-1,"menu_status");
		
		modifyMenuValue(id,-1,"Msgto","msg $txt $txt");
		modifyMenuValue(id,-1,"IrcCmd","submenu menu_irccmd");
		modifyMenuValue(id,-1,"Copy&Paste","submenu menu_copypaste");
		modifyMenuValue(id,-1,"Windows","winlist");
		modifyMenuValue(id,-1,"Disconnect","quit");
	
		// Channel
		
		id = modifyMenu(-1,"menu_chan");
		
		modifyMenuValue(id,-1,"Msg","msg $chan $txt");
		modifyMenuValue(id,-1,"Nicklist","nicklist");
		modifyMenuValue(id,-1,"IrcCmd","submenu menu_irccmd");
		modifyMenuValue(id,-1,"ChanCmd","submenu menu_chancmd");
		modifyMenuValue(id,-1,"Copy&Paste","submenu menu_copypaste");
		modifyMenuValue(id,-1,"Close","part $chan");

		// Query
		
		id = modifyMenu(-1,"menu_query");
		
		modifyMenuValue(id,-1,"Msg","msg $nick $txt");
		modifyMenuValue(id,-1,"Whois","whois $nick");
		modifyMenuValue(id,-1,"Notice","notice $nick $txt");
		modifyMenuValue(id,-1,"Ctcp","submenu menu_ctcp");
		modifyMenuValue(id,-1,"Copy&Paste","submenu menu_copypaste");
		modifyMenuValue(id,-1,"Close","close $nick");

		// IrcCmd

		id = modifyMenu(-1,"menu_irccmd");
		
		modifyMenuValue(id,-1,"Join","join $txt");
		modifyMenuValue(id,-1,"Away","away $nick");
		modifyMenuValue(id,-1,"NickChg","nick $txt");
		modifyMenuValue(id,-1,"ListChans","list $txt");
		modifyMenuValue(id,-1,"ListChans with param","list $txt");
		modifyMenuValue(id,-1,"RawCmd","raw $txt");
		modifyMenuValue(id,-1,"Reconnect","reconnect");
		
		// ChanCmd

		id = modifyMenu(-1,"menu_chancmd");
		
		modifyMenuValue(id,-1,"Rejoin","rejoin $chan");
		modifyMenuValue(id,-1,"Topic","topic $chan $etopic");
		modifyMenuValue(id,-1,"ChanModes","mode $chan");
		modifyMenuValue(id,-1,"Invite","invite $txt $chan");
		modifyMenuValue(id,-1,"ListBan","banlist $chan");
		modifyMenuValue(id,-1,"ListExp","explist $chan");
		modifyMenuValue(id,-1,"ListInv","invlist $chan");

		// Nicklist

		id = modifyMenu(-1,"menu_nicklist");
		
		modifyMenuValue(id,-1,"Msg","msg $nick $txt");
		modifyMenuValue(id,-1,"Insert","msg $chan $itxt");
		modifyMenuValue(id,-1,"Notice","notice $nick $txt");
		modifyMenuValue(id,-1,"Open Query","query $nick");
		modifyMenuValue(id,-1,"Whois","whois $nick");
		modifyMenuValue(id,-1,"Invite","invite $nick $txt");
		modifyMenuValue(id,-1,"Action ->","submenu menu_action");
		modifyMenuValue(id,-1,"Ctcp ->","submenu menu_ctcp");
		modifyMenuValue(id,-1,"Operator ->","submenu menu_operator");

		// Ctcp

		id = modifyMenu(-1,"menu_ctcp");
		
		modifyMenuValue(id,-1,"VERSION","ctcp $nick VERSION");
		modifyMenuValue(id,-1,"TIME","ctcp $nick TIME");
		modifyMenuValue(id,-1,"FINGER","ctcp $nick FINGER");
		modifyMenuValue(id,-1,"USERINFO","ctcp $nick USERINFO");

		// Operator

		id = modifyMenu(-1,"menu_operator");
		
		modifyMenuValue(id,-1,"bans ->","submenu menu_bans");
		modifyMenuValue(id,-1,"kicks ->","submenu menu_kicks");
		modifyMenuValue(id,-1,"kickban ->","submenu menu_kickban");
		modifyMenuValue(id,-1,"+q","mode $chan +q $nick");
		modifyMenuValue(id,-1,"-q","mode $chan -q $nick");
		modifyMenuValue(id,-1,"+a","mode $chan +a $nick");
		modifyMenuValue(id,-1,"-a","mode $chan -a $nick");
		modifyMenuValue(id,-1,"+o","mode $chan +o $nick");
		modifyMenuValue(id,-1,"-o","mode $chan -o $nick");
		modifyMenuValue(id,-1,"+h","mode $chan +h $nick");
		modifyMenuValue(id,-1,"-h","mode $chan -h $nick");
		modifyMenuValue(id,-1,"+v","mode $chan +v $nick");
		modifyMenuValue(id,-1,"-v","mode $chan -v $nick");

		// Banlist

		id = modifyMenu(-1,"menu_banlist");
		
		modifyMenuValue(id,-1,"Add","mode $chan +b $itxt");
		modifyMenuValue(id,-1,"Edit","mode $chan -b+b $item $itxt");
		modifyMenuValue(id,-1,"Delete","mode $chan -b $item");
		modifyMenuValue(id,-1,"Info","modinfo $item");

		// Explist

		id = modifyMenu(-1,"menu_explist");
		
		modifyMenuValue(id,-1,"Add","mode $chan +e $itxt");
		modifyMenuValue(id,-1,"Edit","mode $chan -e+e $item $itxt");
		modifyMenuValue(id,-1,"Delete","mode $chan -e $item");
		modifyMenuValue(id,-1,"Info","modinfo $item");
		
		// Invlist

		id = modifyMenu(-1,"menu_invlist");
		
		modifyMenuValue(id,-1,"Add","mode $chan +I $itxt");
		modifyMenuValue(id,-1,"Edit","mode $chan -I+I $item $itxt");
		modifyMenuValue(id,-1,"Delete","mode $chan -I $item");
		modifyMenuValue(id,-1,"Info","modinfo $item");

		// Action		

		id = modifyMenu(-1,"menu_action");
		
		modifyMenuValue(id,-1,"/me nick <text>","me $chan $itxt");
		modifyMenuValue(id,-1,"/me потрогал nick","me $chan потрогал $nick");
		modifyMenuValue(id,-1,"/me поcтучал по nick","me $chan постучал по $nick");
		modifyMenuValue(id,-1,"/me потыкал nick","me $chan потыкал $nick");
		modifyMenuValue(id,-1,"/me разобрал по частям nick","me $chan разобрал по частям $nick");

		// Ban

		id = modifyMenu(-1,"menu_bans");
		
		modifyMenuValue(id,-1,"nick!*@*","ban $chan $nick 0");
		modifyMenuValue(id,-1,"*!qssl@*","ban $chan $nick 1");
		modifyMenuValue(id,-1,"*!*@host","ban $chan $nick 2");
		modifyMenuValue(id,-1,"nick!qssl@*","ban $chan $nick 3");
		modifyMenuValue(id,-1,"*!qssl@host","ban $chan $nick 4");
		modifyMenuValue(id,-1,"nick!*@host","ban $chan $nick 5");
		modifyMenuValue(id,-1,"nick!qssl@host","ban $chan $nick 6");

		// Kicks

		id = modifyMenu(-1,"menu_kicks");
		
		modifyMenuValue(id,-1,"no reason","kick $chan $nick no reason");
		modifyMenuValue(id,-1,"gcc head.c -o head","kick $chan $nick gcc head.c -o head");
		modifyMenuValue(id,-1,"Connection reset by peer","kick $chan $nick Connection reset by peer");
		modifyMenuValue(id,-1,"Ping timeout","kick $chan $nick Ping timeout");
		modifyMenuValue(id,-1,"Client Exited","kick $chan $nick Client Exited");
		modifyMenuValue(id,-1,"гамосек","kick $chan $nick гамосек");
		modifyMenuValue(id,-1,"[enter reason]","kick $chan $nick $txt");

		// KickBan

		id = modifyMenu(-1,"menu_kickban");
		
		modifyMenuValue(id,-1,"nick!*@* (have fun!)","kickban $chan $nick 0 have fun!");
		modifyMenuValue(id,-1,"*!qssl@* (have fun!)","kickban $chan $nick 1 have fun!");
		modifyMenuValue(id,-1,"*!*@host (have fun!)","kickban $chan $nick 2 have fun!");
		modifyMenuValue(id,-1,"nick!qssl@* (have fun!)","kickban $chan $nick 3 have fun!");
		modifyMenuValue(id,-1,"*!qssl@host (have fun!)","kickban $chan $nick 4 have fun!");
		modifyMenuValue(id,-1,"nick!*@host (have fun!)","kickban $chan $nick 5 have fun!");
		modifyMenuValue(id,-1,"nick!qssl@host (have fun!)","kickban $chan $nick 6 have fun!");
		modifyMenuValue(id,-1,"*!*@host [enter reason]","kickban $chan $nick 2 $txt");

		// Winlist

		id = modifyMenu(-1,"menu_winlist");
		
		modifyMenuValue(id,-1,"show","showwin $item");
		modifyMenuValue(id,-1,"close","close $item");


		// Copypaste

		id = modifyMenu(-1,"menu_copypaste");
		
		modifyMenuValue(id,-1,"copy selected","copytobuf 0");
		modifyMenuValue(id,-1,"copy selected w/o colors","copytobuf 1");
		modifyMenuValue(id,-1,"add  selected","copytobuf 2");
		modifyMenuValue(id,-1,"add  selected w/o colors","copytobuf 3");
		modifyMenuValue(id,-1,"edit buffer","updatebuf $eexbuf");

		// Chanlist

		id = modifyMenu(-1,"menu_chanlist");
		
		modifyMenuValue(id,-1,"Join","join $item");
		modifyMenuValue(id,-1,"ChanInfo","infochan");
		
	}

	public int modifyMenu(int i,String s)
	{
		int v=0;;
		try {
			System.out.println("modifyMenu");
			RecordStore recordstore = RecordStore.openRecordStore("menus", false);
        		byte bufbyte[] = recordstore.getRecord(1);

	        	DataInputStream istrm = new DataInputStream(new ByteArrayInputStream(bufbyte));
        	
			ByteArrayOutputStream bostrm = new ByteArrayOutputStream();
        		DataOutputStream ostrm = new DataOutputStream(bostrm);
        	
			int k = istrm.readInt();		//count of menu ids

			boolean flag=false;

			if ( i != -1 ) { k--; } 		//if delete ?
			else { k++; flag=true; }		//add

			System.out.println("modifyMenu [count ids]="+k);

			ostrm.writeInt(k);			//write count of profiles

        		for(int j = 0; j < k && (j < k - 1 || !flag) ; j++)			// j != k - 1 for add profile.
        		{
            			v=istrm.readInt(); 		
				if ( v == i ) { v=istrm.readInt(); }	//only for delete, skip deleting id
				ostrm.writeInt(v);
			}
		
			v=0;

			if( i == -1 )					//add menu id
			{
				v = recordstore.getNextRecordID();
				ostrm.writeInt(v);

				ByteArrayOutputStream mbstrm = new ByteArrayOutputStream();
        			DataOutputStream mostrm = new DataOutputStream(mbstrm);
        		
				mostrm.writeInt(0);					//count of lines of new menu
				if ( s.equals("") ) { mostrm.writeUTF("mrid"+v); }
				else { mostrm.writeUTF(s); }
				bufbyte = mbstrm.toByteArray();
				mbstrm.close();
				mostrm.close();
				recordstore.addRecord(bufbyte, 0, bufbyte.length);
			}
			bufbyte = bostrm.toByteArray();
			istrm.close();
			ostrm.close();
			recordstore.setRecord(1, bufbyte, 0, bufbyte.length);
		
		
		} catch(Exception exp) { /* rms */  }

		return v;	//if delete 0, if add idRecord

	} //modifyMenu

	public void modifyMenuValue(int rid, int i, String s,String v)
	{
		try {
			System.out.println("modifyMenuValue");
        		boolean flag = false;
			RecordStore recordstore = RecordStore.openRecordStore("menus", false);

        		byte bufbyte[] = recordstore.getRecord(rid);						//get menu record
        		DataInputStream istrm = new DataInputStream(new ByteArrayInputStream(bufbyte));

        		ByteArrayOutputStream bostrm = new ByteArrayOutputStream();
        		DataOutputStream ostrm = new DataOutputStream(bostrm);
        	
			int k = istrm.readInt();		//count "name:value" menu lines.
        		if(s == null && i >= 0 && i < k)	//delete
        		{
            			k--;
            		} else
        		if(s != null && ( i < 0 || i >= k ) )	//add new menu line
        		{
            			k++;
            			flag = true;			//flag
        		} else
        		if(s == null) return;			
        		
			ostrm.writeInt(k);			//write count menu lines
			ostrm.writeUTF(istrm.readUTF());	//stringid.

       			int j;
        		for(j = 0; j < k && (j < k - 1 || !flag) ; j++)		// j != k - 1 for add profile.
        		{
            			if(j == i)					//only for delete/edit.
            			{
                			if (s != null) {
                    				istrm.readUTF();
                    				ostrm.writeUTF(s);			//edit name of cmd
						istrm.readUTF();
                    				ostrm.writeUTF(v);			//edit cmd
						continue;
					}
				
					istrm.readUTF();		//skip if not edit.
					istrm.readUTF();
				}
			
				ostrm.writeUTF(istrm.readUTF());
				ostrm.writeUTF(istrm.readUTF());
			}

			if(flag)							//add profile
			{
				ostrm.writeUTF(s);
				ostrm.writeUTF(v);
			}
			bufbyte = bostrm.toByteArray();
			istrm.close();
			ostrm.close();
			recordstore.setRecord(rid, bufbyte, 0, bufbyte.length);

		} catch(Exception exp) { /* rms */  }
	
	} //modifyMenuValue


} // class database

