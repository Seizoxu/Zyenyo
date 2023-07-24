package dataStructures;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record TypingSubmission(long userID, String userTag, double wordsPerMinute, double accuracy, double typingPoints, double timeTakenMillis, String userTypingSubmission, String promptTitle)
	{public double getTruncatedWPM(int precision) {return BigDecimal.valueOf(wordsPerMinute).setScale(precision, RoundingMode.HALF_UP).doubleValue();}}
