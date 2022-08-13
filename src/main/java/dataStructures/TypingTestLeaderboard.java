package dataStructures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TypingTestLeaderboard
{
	private HashMap<Short, TypingSubmission> leaderboard = new HashMap<>();
	private Set<Long> userIDs = new HashSet<>();
	private short numSubmissions = 0;
	
	public TypingTestLeaderboard() {}
	public TypingTestLeaderboard(HashMap<Short, TypingSubmission> leaderboard) {this.leaderboard = leaderboard;}
	
	public boolean addSubmission(TypingSubmission submission)
	{
		if (!userIDs.contains(submission.getUserID()))
		{
			userIDs.add(submission.getUserID());
			leaderboard.put(++numSubmissions, submission);
			return true;
		}
		
		return false;
	}
	
	public TypingSubmission getSubmission(int index) {return this.leaderboard.get((short)index);}
	public Set<Long> getUserIDs() {return this.userIDs;}
	public int getNumSubmissions() {return this.numSubmissions;}
}
