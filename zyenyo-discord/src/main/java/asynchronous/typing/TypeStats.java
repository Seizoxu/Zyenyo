package asynchronous.typing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.json.JSONObject;

import com.mongodb.client.AggregateIterable;

import dataStructures.TypeStatsScope;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;

public class TypeStats implements Runnable
{
	private MessageReceivedEvent event;
	private MessageChannel channel;
	private String idStr;
	private String[] args;
	private TypeStatsScope scope = TypeStatsScope.RECENT;
	public TypeStats(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.channel = event.getChannel();
		this.args = args;
	}
	//TODO: Add a sendHelp here for whenever this command takes further arguments.
	
	@Override
	public void run()
	{
		try
		{
			channel.sendTyping().queue();
			
			// Get command parameters.
			for (String cmd : args)
			{
				// Not using .getAsMention() to avoid replies being the primary mention.
				Matcher matcher = Pattern.compile("<@\\d+>").matcher(cmd);
				if (matcher.find())
				{
					idStr = event.getMessage().getMentionedMembers().get(0).getId();
				}
				else {idStr = event.getAuthor().getId();}
				
				switch(cmd.toLowerCase())
				{
				case "-g":
					scope = TypeStatsScope.GLOBAL;
					break;
				}
			}
			
			Long id = Long.parseLong(idStr); // Used for error checking; will change later.
			String testsTaken="", title;
			JSONObject json;
			
			if (scope == TypeStatsScope.GLOBAL)
			{
				json = new JSONObject(Database.getGlobalStats(idStr));
				testsTaken = String.format("Tests Taken: **`%s`**%n", json.get("tests").toString());
				title = "Global Typing Statistics for " + event.getJDA().retrieveUserById(id).submit().get().getAsTag();
			}
			else
			{
				json = new JSONObject(Database.getStats(idStr, false));
				title = "Recent Typing Statistics for " + event.getJDA().retrieveUserById(id).submit().get().getAsTag();
			}
			
			double averageWpm = Double.parseDouble(json.get("averageWpm").toString());
			double averageAcc = Double.parseDouble(json.get("averageAcc").toString());
			double bestWpm = Double.parseDouble(json.get("bestWpm").toString());
			double deviation = Double.parseDouble(json.get("deviation").toString());
			double typingPoints = Double.parseDouble(json.get("weightedTp").toString());
			double playtime = Double.parseDouble(json.get("playtime").toString());
			String rank = getRank(averageWpm);

			File chart = this.getChart();
			
			channel.sendMessageEmbeds(new EmbedBuilder()
					.addField(title,
							String.format("%s"
									+ "Weighted Typing Points: **`%.2f`**%n"
									+ "Best WPM: **`%.2f`**%n"
									+ "Average WPM: **`%.2f`**%n"
									+ "Deviation: **`%.2f`**%n"
									+ "Average Accuracy: **`%.2f%%`**%n"
									+ "Playtime: **`%.0f hours %.0f minutes`**%n"
									+ "Rank: **`%s`**",
									testsTaken, typingPoints, bestWpm, averageWpm, deviation, averageAcc,
									Math.floor(playtime / (1000 * 60 * 60)), Math.floor( (playtime / (1000 * 60))%60 ),
									rank), false)
					.addField("WPM Breakdown",
							wpmRange(), false)
					.setColor(new Color(180, 50, 80))
					.setImage("attachment://chart.png")
					.build()).addFile(chart, "chart.png")
			.queue();
		}
		catch (InterruptedException | NumberFormatException e)
		{
			channel.sendMessageEmbeds(new EmbedBuilder()
					.addField("Error: Cannot fetch user stats.", "Reason: User not in database.", false)
					.build())
			.queue();
		}
		catch (ExecutionException e)
		{
			channel.sendMessageEmbeds(new EmbedBuilder()
					.addField("Error: Cannot fetch user stats.", "Reason: User does not exist.", false)
					.build())
			.queue();
		}
	}

	private static String getRank(double wpm)
	{
		String rank = "Undetermined";
		
		if (wpm < 50) {rank = "Novice";}
		else if (wpm < 60) {rank = "Iron";}
		else if (wpm < 70) {rank = "Bronze";}
		else if (wpm < 80) {rank = "Silver";}
		else if (wpm < 90) {rank = "Gold";}
		else if (wpm < 100) {rank = "Diamond" ;}
		else if (wpm < 110) {rank = "Demon";}
		else if (wpm < 130) {rank = "Demi God";}
		else if (wpm < 150) {rank = "God";}
		else if (wpm < 250) {rank = "Untouchable";}
		else if (wpm >= 250) {rank = "Suspicious";}
		// personally, I aim for Integer.MAX_VALUE WPM on my tests
		
		return rank;
	}
	
	//TODO: this can likely be abstracted when we want to generate other sorts of charts in the future.
	private File getChart() {
		try
		{

			LinkedHashMap<String, Integer> monthlyPlaycount = Database.playcount(idStr);

			DefaultCategoryDataset lineChartDataset = new DefaultCategoryDataset();
			monthlyPlaycount.forEach((monthStr, count) -> lineChartDataset.addValue(count, "playcount", monthStr));

			JFreeChart lineChartObject = ChartFactory.createLineChart(
					"Playcount","",
					"",
					lineChartDataset,PlotOrientation.VERTICAL,
					true,true,false);
			lineChartObject.removeLegend();

			CategoryPlot plot = (CategoryPlot) lineChartObject.getPlot();

			plot.getDomainAxis().setCategoryLabelPositions(
					CategoryLabelPositions.UP_45);
			plot.setDomainGridlinesVisible(true);
			plot.setRangeGridlinesVisible(false);
			plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));

			int width = 640;    /* Width of the image */
			int height = 380;   /* Height of the image */ 
			File lineChart = new File( "LineChart.png" ); 
			ChartUtilities.saveChartAsPNG(lineChart ,lineChartObject, width ,height);
			return lineChart;
		}
		catch (Exception e) {e.printStackTrace();}

		return null;
	}
	
	
	/**
	 * Calculates and returns the count of each WPM range the user has hit in every prompt.
	 * @return String
	 */
	private String wpmRange()
	{
		StringBuilder wpmBreakdown = new StringBuilder();
		AggregateIterable<Document> topPlays = Database.getTopPlays(idStr, "wpm", Integer.MAX_VALUE, true);
		Map<Integer, Integer> buckets = new TreeMap<>(Collections.reverseOrder());
		
		for (Document currentPlay : topPlays)
		{
			int wpm = (int) (Math.floor(currentPlay.getDouble("wpm") / 10) * 10);
			buckets.merge(wpm, 1, Integer::sum);
		}
		
		buckets.forEach(
				(wpm, count) -> 
				wpmBreakdown.append(String.format("**`%" + 3 + "d+ WPM:`** %d\n", wpm, count))
				);

		return wpmBreakdown.toString();
	}
}
