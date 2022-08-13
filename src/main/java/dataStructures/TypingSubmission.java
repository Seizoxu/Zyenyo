package dataStructures;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TypingSubmission
{
	private long userID;
	private String userTag;
	private double wordsPerMinute;
	private double accuracy;
	
	public TypingSubmission() {};
	
	public TypingSubmission(long userID, String userTag, double wordsPerMinute, double accuracy)
	{
		this.userID = userID;
		this.userTag = userTag;
		this.wordsPerMinute = wordsPerMinute;
		this.accuracy = accuracy;
	}
	
	public void setUserID(long userID) {this.userID = userID;}
	public void setUserTag(String userTag) {this.userTag = userTag;}
	public void setWPM(double wordsPerMinute) {this.wordsPerMinute = wordsPerMinute;}
	public void setAccuracy(double accuracy) {this.accuracy = accuracy;}
	
	public long getUserID() {return this.userID;}
	public String getUserTag() {return this.userTag;}
	public double getWPM() {return this.wordsPerMinute;}
	public double getTruncatedWPM(int precision) {return BigDecimal.valueOf(wordsPerMinute).setScale(precision, RoundingMode.HALF_UP).doubleValue();}
	public double getAccuracy() {return this.accuracy;}
}
