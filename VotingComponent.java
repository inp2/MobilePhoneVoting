import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Object;


public class VotingComponent{ 

	private final static String passcode = "****";
		
	public static void main(String []args) throws Exception{
		
		if (args.length < 1){
            System.out.println("Too few arguments.");
            System.out.println("Run it like this: java VotingComponent UniversalInterface_IP");
            System.exit(0);
        }

		Socket universal = new Socket(args[0], 7999);

		
		MsgEncoder mEncoder = new MsgEncoder();
		final MsgDecoder mDecoder = new MsgDecoder(universal.getInputStream());
		KeyValueList L = new KeyValueList();
		L.addPair("MsgID", "23");
		L.addPair("Name", "VotingComponent");
		mEncoder.sendMsg(L, universal.getOutputStream());
		
		TallyTable tTable = null;
		
		while(true){
		
			KeyValueList list = mDecoder.getMsg();
			int msgID = Integer.parseInt(list.getValue("MsgID"));
			switch(msgID){
				//Cast Vote
				case 701: {
						if (tTable != null) {
						
							String phone = list.getValue("VoterPhoneNo");
							String candidate = list.getValue("CandidateID");
						
							int status = tTable.castVote(phone, candidate);
						
							KeyValueList valid = new KeyValueList();
							valid.addPair("MsgID", "711");
							valid.addPair("Description", "Acknowledge Vote (1 - duplicate, 2 - valid, 3 - invalid");
							valid.addPair("Status", status + "" );
							mEncoder.sendMsg(valid, universal.getOutputStream());
						
						}
						else {
							KeyValueList invalid = new KeyValueList();
							invalid.addPair("MsgID", "26");
							invalid.addPair("Description", "Tally Table not initialized");
							invalid.addPair("AckMsgID", "701");
							invalid.addPair("YesNo", "No");
							invalid.addPair("Name", "Cast Vote");
							mEncoder.sendMsg(invalid, universal.getOutputStream());
						}
					}
					break;
					
				//Request Report
				case 702: {
						if (list.getValue("Passcode").equals(passcode) && tTable != null){
							
							int winnersNum = Integer.parseInt(list.getValue("N"));
							String rankedReport = tTable.getWinner(winnersNum);
							
							System.out.println(rankedReport);
							
							KeyValueList valid = new KeyValueList();
							valid.addPair("MsgID", "712");
							valid.addPair("Description", "Acknowledge RequestReport");
							mEncoder.sendMsg(valid, universal.getOutputStream());
						
						}
						else {
							
							KeyValueList invalid = new KeyValueList();
							invalid.addPair("MsgID", "26");
							invalid.addPair("Description", "Invalid Password or Tally Table isn't initialized");
							invalid.addPair("AckMsgID", "702");
							invalid.addPair("YesNo", "No");
							invalid.addPair("Name", "Request Report");
							mEncoder.sendMsg(invalid, universal.getOutputStream());
						
						}
					
					}
					break;
				
				//Initialize Tally Table	
				case 703: {
						if (list.getValue("Passcode").equals(passcode)) {
						
							String CandidateList = list.getValue("CandidateList");
							String CandidateIDs[] = CandidateList.split("[;]");
							List<String> CandidateID = Arrays.asList(CandidateIDs);
						
							tTable = new TallyTable(CandidateID);
						
							KeyValueList valid = new KeyValueList();
							valid.addPair("MsgID", "26");
							valid.addPair("Description", "Acknowledgement (Server acknowledges that GUI component is now connected to Server)");
							valid.addPair("AckMsgID", "703");
							valid.addPair("YesNo", "Yes");
							valid.addPair("Name", "TallyTable");
							mEncoder.sendMsg(valid, universal.getOutputStream());

						}
						else  {
							KeyValueList invalid = new KeyValueList();
							invalid.addPair("MsgID", "26");
							invalid.addPair("Description", "Invalid Password");
							invalid.addPair("AckMsgID", "703");
							invalid.addPair("YesNo", "No");
							invalid.addPair("Name", "Tally Table");
							mEncoder.sendMsg(invalid, universal.getOutputStream());
						}
					}
					break;
					
				default:
					break;
			}	
		}
	}
}

