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
	private String lbStatistic;
	private String collection = "tests";
	private BsonField accumulator;

	public LeaderboardConfig(LeaderboardStatisticType lbStatistic, LeaderboardScope lbScope) {
		this.lbScope = lbScope;

		switch (lbStatistic) {
			case TP: 
				this.lbStatistic = "tp";
				switch(lbScope) {
					case SUM: 
						this.lbStatistic = "totalTp";
						this.collection = "users";
						this.accumulator = Accumulators.sum(this.lbStatistic, "$" + this.lbStatistic);
						break;
					case AVERAGE:
						this.accumulator = Accumulators.avg(this.lbStatistic, "$" + this.lbStatistic);
						break;
					case BEST:
						this.accumulator = Accumulators.max(this.lbStatistic, "$" + this.lbStatistic);
						break;
				} break;
			case WPM: 
				this.lbStatistic = "wpm";
				switch(lbScope) {
					case BEST: 
						this.accumulator = Accumulators.max(this.lbStatistic, "$" + this.lbStatistic);
						break;
					case AVERAGE:
					default: 
						this.lbScope = LeaderboardScope.AVERAGE;
						this.accumulator = Accumulators.avg(this.lbStatistic, "$" + this.lbStatistic);
						
				} break;
			case ACCURACY: 
				this.lbStatistic = "accuracy";
				switch(lbScope) {
					case AVERAGE:
					default:
						this.lbScope = LeaderboardScope.AVERAGE;
						this.accumulator = Accumulators.avg(this.lbStatistic, "$" + this.lbStatistic);
						
			} break;
		}
	}

	public String getStatistic() {
		return this.lbStatistic;
	}
	
	public String getCollection() {
		return this.collection;
	}

	public BsonField getAccumulationStrategy() {
		return this.accumulator;
	}

	public String getLeaderboardTitle() {
		return String.format("Global %s %s Leaderboards", StringUtils.capitalize(lbScope.toString().toLowerCase()), StringUtils.capitalize(String.join(" ", StringUtils.splitByCharacterTypeCamelCase(this.lbStatistic))));
	}

}
