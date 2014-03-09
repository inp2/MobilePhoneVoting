import java.net.*;
import java.io.*;

public class Test {

	public static void main(String[] args) throws IOException{
		Socket testSocket = new Socket("127.0.0.1", 7999);
		System.out.println("connected");
	}
}
