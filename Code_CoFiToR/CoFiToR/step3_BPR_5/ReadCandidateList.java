import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class ReadCandidateList
{
    public static void readCandidateList() throws IOException 
	{
    	BufferedReader brInputCandidateList = new BufferedReader(new FileReader(RSVD2BPR.fnInputCandidateList)); 
    	String line = null;
    	while ((line = brInputCandidateList.readLine())!=null)
    	{
    		String[] terms = line.split("\\s+|,|;");
    		int userID = Integer.parseInt(terms[0]);
    		int itemID = Integer.parseInt(terms[1]);
    		
			if(RSVD2BPR.CandidateList.containsKey(userID))
	    	{
	    		ArrayList<Integer> itemList = RSVD2BPR.CandidateList.get(userID);
	    		itemList.add(itemID);
	    		RSVD2BPR.CandidateList.put(userID, itemList);
	    	}
	    	else
	    	{
	    		ArrayList<Integer> itemList = new ArrayList<Integer>();
	    		itemList.add(itemID);
	    		RSVD2BPR.CandidateList.put(userID, itemList);
	    	}
    	}
    	brInputCandidateList.close();
    }    
}
