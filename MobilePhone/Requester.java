import java.io.*;
import java.net.*;
public class Requester{
	Socket requestSocket;
	ObjectOutputStream oos;
 	ObjectInputStream ois;
 	String message;
	Requester(){}
	void run()
	{
		try{
			//1. creating a socket to connect to the server
			requestSocket = new Socket("127.0.0.1", 7999);
			System.out.println("Connected to localhost in port 7999");
			//2. get Input and Output streams
			oos = new ObjectOutputStream(requestSocket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(requestSocket.getInputStream());
			System.out.println("before do");
			oos.flush();
			//3: Communicating with the server
			do{
				System.out.println("after do");
				try{
					System.out.println("try block");
					message = (String)ois.readObject();
					System.out.println("server>" + message);
					sendMessage("Hi my server");
					message = "bye";
					sendMessage(message);
				}
				catch(ClassNotFoundException classNot){
					System.err.println("data received in unknown format");
				}
			}while(!message.equals("bye"));
		}
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//4: Closing connection
			try{
				ois.close();
				oos.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	void sendMessage(String msg)
	{
		try{
			System.out.println("sendMessage try block");
			oos.writeObject(msg);
			oos.flush();
			System.out.println("did you get it?");
			System.out.println("client>" + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	public static void main(String args[])
	{
		Requester client = new Requester();
		client.run();
	}
}