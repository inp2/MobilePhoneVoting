import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.*;
import com.webobjects.foundation.*;
import com.webobjects.foundation.xml.*;
public class JsonSimpleExample {
     public static void main(String[] args) {
 
 	String XML = "<Msg><Head><MsgID>20</MsgID><Description>Create InputProcessor Component</Description></Head><Body><Item><Key>Passcode</Key><Value>****</Value></Item><Item><Key>SecurityLevel</Key><Value>3</Value></Item><Item><Key>Name</Key><Value>InputProcessor</Value></Item><Item><Key>SourceCode</Key><Value>InputProcessor.jar</Value></Item><Item><Key>InputMsgID 1</Key><Value>30</Value></Item><Item><Key>OutputMsgID 1</Key><Value>31</Value></Item><Item><Key>OutputMsgID 2</Key><Value>33</Value></Item><Item><Key>OutputMsgID 3</Key><Value>35</Value></Item><Item><Key>Component Description</Key><Value>InputProcessor parses the data and extracts vital signals.</Value></Item></Body></Msg>";
	
	XMLSerializer xmlSerializer = new XMLSerializer();
	JSONObject json = xmlSerializer.read(XML);
 	
 	}
}



