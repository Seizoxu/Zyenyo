package dataStructures;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.BsonField;

import org.apache.commons.lang3.StringUtils;

/*
 * creates a LeaderboardConfig object that can be passed to getLeaderboards in Database.java, which ensures that an invalid leaderboard configuration can never be passed in. for example, the leaderboard of BEST ACCURACY makes sense, but leaderboard of SUM ACCURACY does not.
 *
 */
public class LeaderboardConfig {
	private LeaderboardScope lbScope;
	private LeaderboardStatisticType lbStatistic;

	public LeaderboardConfig(LeaderboardStatisticType lbStatistic, LeaderboardScope lbScope) {
		this.lbStatistic = lbStatistic;

		switch (lbStatistic) {
			case TP: switch(lbScope) {
				default: this.lbScope = lbScope;
			} break;
			case WPM: switch(lbScope) {
				// WPM SUM does not seem like a useful leaderboard.
				case SUM: this.lbScope = LeaderboardScope.AVERAGE; break;
				default: this.lbScope = lbScope;
			} break;
			case ACCURACY: switch(lbScope) {
				// ACCURACY BEST is not a useful leaderboard.
				case BEST: this.lbScope = LeaderboardScope.AVERAGE; break;
				// ACCURACY SUM makes absolutely no sense.
				case SUM: this.lbScope = LeaderboardScope.AVERAGE; break;
				default: this.lbScope = lbScope;
			} break;
		}
		
	}

	public String getStatistic() {
		String stat = "";

		switch(lbStatistic) {
			case TP: switch(lbScope) {
				// tp in users collection is named totalTp whereas in tests it is just tp
				case SUM: stat = "totalTp"; break;
				default: stat = "tp"; break;
			} break;
			case ACCURACY: stat = "accuracy"; break;
			case WPM: stat = "wpm"; break;
		}

		return stat;
	}
	
	public String getCollection() {
		String collection = "";

		switch(lbStatistic) {
			case TP: switch(lbScope) {
				case SUM: collection = "users"; break;
				default: collection = "tests"; break;
			} break;
			default: collection = "tests"; break;
		}

		return collection;
	}

	public BsonField getAccumulationStrategy() {
		switch(lbScope) {
			case BEST: return Accumulators.max(getStatistic(), "$" + getStatistic());
			case AVERAGE: return Accumulators.avg(getStatistic(), "$" + getStatistic());
			case SUM: return Accumulators.sum(getStatistic(), "$" + getStatistic());
		}

		// this should technically never reach but java is not smart enough to know that all cases of the enum have been covered, so it complains without it.
		return Accumulators.sum(getStatistic(), "$" + getStatistic());
	}

	public String getLeaderboardTitle() {
		return String.format("Global %s %s Leaderboards", StringUtils.capitalize(lbScope.toString().toLowerCase()), StringUtils.capitalize(String.join(" ", StringUtils.splitByCharacterTypeCamelCase(getStatistic()))));
	}

}
