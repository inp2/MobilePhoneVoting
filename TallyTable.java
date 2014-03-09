import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Object;

public class TallyTable {

	HashMap<String, String> Voters;  		//<Voterphone#, VoterID>
	HashMap<String, Integer> Votes;		 	//<VoterID, #ofVotes>
	List<String> candidates;
	
	public TallyTable(List<String> cand) {
	
		Voters = new HashMap<String, String>();
		Votes = new HashMap<String, Integer>();
		
		candidates = cand;
		
	}	
	
	public int castVote(String voterID, String candID) {
		
		if (candidates.contains(candID)) {
			if (Voters.containsKey(voterID)) {
				
				String oldCandidate = Voters.get(voterID);
				Votes.put(oldCandidate, Votes.get(oldCandidate) - 1);
				
				Voters.put(voterID, candID);
			
				if (Votes.containsKey(candID)) Votes.put(candID, Votes.get(candID) + 1);
				else Votes.put(candID, 1);
			
				return 1;
			
			}
			else {
				
				Voters.put(voterID, candID);
				
				if (Votes.containsKey(candID)) Votes.put(candID, Votes.get(candID) + 1);
				else Votes.put(candID, 1);
			
				return 3;
			
			}
		} else return 2;
		
	}
	
	public String getWinner( int n ) {
		String out = "";
		for (int i = 0; i < candidates.size(); i++ ){ 
			out = out.concat(candidates.get(i) + "," + Votes.get(candidates.get(i))+";");
		}
		return out;
	}	
}