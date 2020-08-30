/**************************************************
*
* 		Net socket module.
*
***************************************************/
import java.io.*;
import java.lang.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

public class netsocket {

	public addon		uaddon;
	public database 	dbrms;
	public lala		main;		

	private DataInputStream  strm_rd;
	private DataOutputStream strm_wr;
	private StreamConnection streamconnection;

	// proxy


	//// crlf ////

	static String crlf = "\r\n";

	private byte buf[];
	private int size,tmpsize,p=-1;

	public byte[] inet_addr(String hoststr) {

		byte hst[] = new byte[4];
		int s=0,e,i=0;
	
		while ( (e=hoststr.indexOf(".",s)) != -1 ) {
			
			hst[i]=(byte)Integer.parseInt(hoststr.substring(s,e));
			i++;
			s=e+1;
			if ( i == 3 ) break; 
		}
		
		hst[i]=(byte)Integer.parseInt(hoststr.substring(s));

		return hst;
	}

	public String socks4connect(String host, int port, String sockshost,int socksport) {
		
		String cstat;
		buf = new byte[50];

		if ( (cstat=connect(sockshost, socksport)) != null ) { return cstat; }
		
		buf[0] = 0x4;				//ver
		buf[1] = 0x1;				//connect
		buf[2] = (byte)(port>>8);		//port octet
		buf[3] = (byte)(port);			//port octet
		
		System.arraycopy(inet_addr(host),0,buf,4,4);
		buf[8] = 0x0;	

		send(buf,0,9);

		size=recv(buf,0,50);

		String debug = new String(buf,0,buf.length);

		System.out.println("JAVA DEBUG"+debug);

		if ( buf[1] != (byte)0x5a ) { return "error:socks4 not accpet connect code="+buf[1]; }
		
		p = (8 - 4);

		return null;
		
	}

	public String socks5connect(String host, int port, String sockshost,int socksport) {
		
		String cstat;
		buf = new byte[256];

		if ( (cstat=connect(sockshost, socksport)) != null ) { return cstat; }
		
		byte lala[] = { 0x5, 0x1, 0x0 };

		send(lala,0,lala.length);		//socks5 no authenticate.
		
		recv(buf,0,256);
		
		if ( buf[1] == (byte)0xff ) return "error:socks5 not accept auth method";


		buf[0] = 0x5;				//ver
		buf[1] = 0x1;				//connect
		buf[2] = 0x0;				//rsv
		buf[3] = 0x3;				//domain
		buf[4] = (byte)(int)host.length();

		System.arraycopy(host.getBytes(),0,buf,5,host.length());
		
		buf[4+1+host.length()] = (byte)(port>>8);	//port octet
		buf[4+2+host.length()] = (byte)(port);		//port octet
		
		send(buf,0,7+host.length());
		
		size=recv(buf,0,256);
		
		if ( buf[1] != (byte)0x0 ) return "error:socks server error code "+(byte)buf[1];

		if ( buf[3] == (byte)0x01 ) tmpsize = 4;
		if ( buf[3] == (byte)0x03 ) tmpsize = 1+buf[4];
		if ( buf[3] == (byte)0x04 ) tmpsize = 0x10;
	
		tmpsize=tmpsize+6;		

		p=tmpsize-4;

		return null;
	}

	public String proxyconnect(String host, int port,String proxyhost,int proxyport) {

		String cstat;
		buf = new byte[1000];
			
		p=-1;
		size=0;

		if ( (cstat=connect(proxyhost, proxyport)) != null ) { return cstat; }
		send("CONNECT "+host+":"+port+" HTTP/1.1"+crlf+"Host:proxyclient"+crlf+crlf);
			
		while ( (tmpsize = recv(buf,size,1000-size)) != -1 ) {
			size = size+tmpsize;
			cstat = new String(buf,0,size);
			if ( (p=cstat.indexOf(crlf+crlf)) != -1 ) { break; }
			if ( size == 1000 ) { break; }
		}
			
		if ( p == -1 ) { 
			//return "error: proxy header very long."; 
			p=-4;						//for stupid proxies. whose not send HTTP [Code]
		} else if ( size >= 12 ) {

			String http = new String(buf,0,12);
			if ( http.toLowerCase().startsWith("http") ) {
				if ( !http.substring(9,12).equals("200") ) {
					return "error: proxy failed with code "+http.substring(9,12); 
				} 
			} else {
				p=-4;
			}
		}

		//System.out.println("cstat"+cstat+" size="+size+" pton"+cstat.indexOf(crlf+crlf));
		//String lala = new String(buf,(p+4),(size-(p+4)));
		//System.out.println("last_header="+lala);
			
		return null;
	}

	//////////////////
	// irc connect
	//////////////////
	public String connect(String host, int port)
	{
		try
		{
			streamconnection = (StreamConnection)Connector.open("socket://" + host + ":" + port, 3);
			strm_rd = streamconnection.openDataInputStream();
			strm_wr = streamconnection.openDataOutputStream();

			
		}
		catch(Exception exception)
		{
			String s3 = "Error trying to connect to IRC server, aborting... ";
			s3 = s3 + "Exception: " + exception.toString();
			return s3;
		}
		return null;
	}

	/////////////////
	// internal recv
	/////////////////
	private int recv(byte[] data,int off,int len) {
		try
		{
			return strm_rd.read(data,off,len);
		}
		catch(Exception expsend)
		{
			return -1;
		}
	}

	public void send(byte[] data,int off,int len) {
			
		try
		{
			strm_wr.write(data,off,len);;
		}
		catch(Exception expsend)
		{ /* exp */ }
	}
	/////////////////
	// recv
	/////////////////
	public int recvnew(byte[] data) 
	{	
		try
		{
    			int i,j,cr=0;
			if ( p != -1 ) {				//used proxy
				
				p=p+4;
				size=size-p;

				String lala = new String(buf,p,size);
				System.out.println("proxy_cut:"+lala);				

				//System.arraycopy(buf,p+4,data,0,size);
				for (i=0; i < size; ++i)
    				{	
					j = (int)buf[p+i];
					if (data[i] == 10) break;
					data[i]=(byte)j;				
				}
				if ((i > 0) && (data[(i - 1)] == 13)) { --i; }
				
				if ( (i < size) && (( buf[p+i] == 10 ) || ( buf[p+i] == 13 )) ) { i++; cr=1; }
				if ( (i < size) && (( buf[p+i] == 10 ) || ( buf[p+i] == 13 )) ) { i++; cr=1; }

				if ( i == size ) { p=-1; }
				else { p=p+i-4; return i; }
				
				if ( cr == 1 ) { return i; }

			}		
			
			for (i=0; i < 512; ++i)
    			{	
				j = strm_rd.read();
      				if (j == -1) { return -4; }
	      			data[i] = (byte)j;
				if (data[i] == 10) break;
 
			}
			

    			if (i == 512) return 0;
    
			if ((i > 0) && (data[(i - 1)] == 13)) { --i; }
    			return i;
		} 
		catch(Exception exprcv)
		{
			
			return -5;
		}
	}

	/////////////////
	// recv
	/////////////////
	public int recv(byte[] data) 
	{
		if ( p != -1 ) {
			
			size=size-(p+4);
			System.arraycopy(buf,p+4,data,0,size);
			p=-1;
			if ( size != 0 ) return size;
		}		

		try
		{
			return strm_rd.read(data);
		}
		catch(Exception expsend)
		{
			return -1;
		}
	}
	/////////////////
	// send
	/////////////////
	public void send(String data) 
	{
		//byte[] tmp;
		
		try
		{
			strm_wr.write(uaddon.stringToByteArray(data,main.ircencode,dbrms.utf8write));
			strm_wr.flush();
		}
		catch(Exception expsend)
		{ /* exp */ }
	}

	public void close() { 
		try
		{
			streamconnection.close();
		} 
		catch(Exception expclose) 
		{ /* exp */ }
	}
	public int avail() {
		try
      		{
        		return (strm_rd.available());
      		}
      		catch (Exception availexp)
      		{
        		return -1;
      		}
	}
	

}  //class netsocket
