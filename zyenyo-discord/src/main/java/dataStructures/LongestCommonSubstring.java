package dataStructures;

/**
 * Finds the longest common substring present in the second string,
 * from the first string.
 * Note, this is NOT Longest Common Subsequence.
 */
public class LongestCommonSubstring
{
	public static String find(String needle, String haystack)
	{
		int needleLength = needle.length();
		int haystackLength = haystack.length();

		int[][] dp = new int[needleLength+1][haystackLength+1];
		int maxLength = 0;
		int endIndex = 0;

		for (int i = 1; i <= needleLength; i++)
		{
			for (int j = 1; j <= haystackLength; j++)
			{
				if (needle.charAt(i-1) == haystack.charAt(j-1))
				{
					dp[i][j] = dp[i-1][j-1] + 1;
					if (dp[i][j] > maxLength)
					{
						maxLength = dp[i][j];
						endIndex = i-1;
					}
				}
				else
				{
					dp[i][j] = 0;
				}
			}
		}

		return needle.substring(endIndex - maxLength + 1, endIndex + 1);
	}
}