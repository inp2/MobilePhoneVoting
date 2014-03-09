import java.io.*;
import java.util.*;
import javax.mail.*; 

public class ReadMessage { 

  public static void main(String args[]) throws Exception { 

  String host = "imap.gmail.com";
  String user = "cs1631spr13@gmail.com";
  String password = "biomed09"; 

  // Get system properties 
   Properties properties = System.getProperties(); 

  // Get the default Session object.
  Session session = Session.getDefaultInstance(properties);

  // Get a Store object that implements the specified protocol.
  Store store = session.getStore("imaps");

  //Connect to the current host using the specified username and password.
  store.connect(host, user, password);

  //Create a Folder object corresponding to the given name.s
  Folder folder = store.getFolder("inbox");

  // Open the Folder.
  folder.open(Folder.READ_ONLY);

  Message[] message = folder.getMessages();

  // Display message.
  for (int i = 0; i < message.length; i++) {

	
  	System.out.println("------------ Message " + (i + 1) + " ------------");

  	System.out.println("SentDate : " + message[i].getSentDate());
  	System.out.println("From : " + message[i].getFrom()[0]);
  	System.out.println("Subject : " + message[i].getSubject());
  	System.out.print("Message : ");
	
  	InputStream stream = message[i].getInputStream();
  	while (stream.available() != 0) {
  	
  	String string = ((char)stream.read());
  		System.out.print((char) stream.read());
  	}	
  	System.out.println();
  }
  
  if(string.contains("701"))
  {
  	System.out.println("ADD VOTE TO TABLE");
   }

  folder.close(true);
  store.close();
  }
}
