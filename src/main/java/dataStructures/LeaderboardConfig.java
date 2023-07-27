package dataStructures;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.BsonField;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/*
 * creates a LeaderboardConfig object that can be passed to getLeaderboards in Database.java, which ensures that an invalid leaderboard configuration can never be passed in. for example, the leaderboard of BEST ACCURACY makes sense, but leaderboard of SUM ACCURACY does not.
 *
 */
public class LeaderboardConfig {
	private LeaderboardScope lbScope;
	private String lbStatistic;
	private String collection = "tests";
	private ArrayList<BsonField> accumulators = new ArrayList<BsonField>();

	public LeaderboardConfig(LeaderboardStatisticType lbStatistic, LeaderboardScope lbScope) {
		this.lbScope = lbScope;

		switch (lbStatistic) {
			case TP: 
				this.lbStatistic = "tp";
				switch(lbScope) {
					case SUM: 
						this.lbStatistic = "totalTp";
						this.collection = "users";
						this.accumulators.add(Accumulators.sum(this.lbStatistic, "$" + this.lbStatistic));
						this.accumulators.add(Accumulators.first("userTag", "$userTag"));
						break;
					case AVERAGE:
						this.accumulators.add(Accumulators.avg(this.lbStatistic, "$" + this.lbStatistic));
						break;
					case BEST:
						this.accumulators.add(Accumulators.max(this.lbStatistic, "$" + this.lbStatistic));
						break;
				} break;
			case WPM: 
				this.lbStatistic = "wpm";
				switch(lbScope) {
					case BEST: 
						this.accumulators.add(Accumulators.max(this.lbStatistic, "$" + this.lbStatistic));
						break;
					case AVERAGE:
					default: 
						this.lbScope = LeaderboardScope.AVERAGE;
						this.accumulators.add(Accumulators.avg(this.lbStatistic, "$" + this.lbStatistic));
						
				} break;
			case ACCURACY: 
				this.lbStatistic = "accuracy";
				switch(lbScope) {
					case AVERAGE:
					default:
						this.lbScope = LeaderboardScope.AVERAGE;
						this.accumulators.add(Accumulators.avg(this.lbStatistic, "$" + this.lbStatistic));
						
				} break;
			case TESTS:
				this.lbStatistic = "totalTests";
				switch(lbScope) {
					case SUM:
					default:
						this.lbScope = LeaderboardScope.SUM;
						// 1.0 so that this value is a double otherwise there will be headaches.
						this.accumulators.add(Accumulators.sum(this.lbStatistic, 1.0));
				} break;
				
		}
	}

	public String getStatistic() {
		return this.lbStatistic;
	}
	
	public String getCollection() {
		return this.collection;
	}

	public List<BsonField> getAccumulationStrategies() {
		return this.accumulators;
	}

	public String getLeaderboardTitle() {
		return String.format("Global %s %s Leaderboards", StringUtils.capitalize(lbScope.toString().toLowerCase()), StringUtils.capitalize(String.join(" ", StringUtils.splitByCharacterTypeCamelCase(this.lbStatistic))));
	}

}
