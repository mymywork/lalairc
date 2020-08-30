/**************************************************
*
* 	    Semaphore funtions module.
*
***************************************************/
import java.io.*;
import java.lang.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

public class semaphore {

	public synchronized void waitsignal() throws InterruptedException {
  		this.wait();
	}

	public synchronized void notifysignal() {
  		this.notify();
	}

}