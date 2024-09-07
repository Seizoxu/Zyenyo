package asynchronous.typing;

import java.io.File;

import java.awt.Color;
import java.awt.BasicStroke;

import java.util.concurrent.ExecutionException;
import java.util.LinkedHashMap;

import org.jfree.chart.JFreeChart; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.ChartUtilities; 
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.axis.CategoryLabelPositions;

import org.json.JSONObject;

import dataStructures.InfoCard;
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
	private boolean requestGlobal = false;
	private Runnable sendHelp = new Runnable()
		{@Override public void run() {channel.sendMessageEmbeds(InfoCard.INCORRECT_SYNTAX.build()).queue();}};
	public TypeStats(MessageReceivedEvent event, String[] args)
	{
		this.event = event;
		this.channel = event.getChannel();
		this.args = args;
	}
	
	@Override
	public void run()
	{
		try
		{
			channel.sendTyping().queue();
			
			// Get command parameters.
			if (args.length == 1) {idStr = event.getAuthor().getId();}
			else if (args.length == 2)
			{
				if (args[1].equals("-g"))
				{
					idStr = event.getAuthor().getId();
					requestGlobal = true;
				}
				else {idStr = args[1].subSequence(2, args[1].length()-1).toString();}
			}
			else if (args.length == 3 && args[2].equals("-g"))
			{
				idStr = args[1].subSequence(2, args[1].length()-1).toString();
				requestGlobal = true;
			}
			else {sendHelp.run(); return;}
			// Yes, the above line blocks the curr. thread, but this class is already another thread,
			// and it doesn't need to go further.
			
			Long id = Long.parseLong(idStr); // Used for error checking; will change later.
			String testsTaken="", title;
			JSONObject json;
			
			if (requestGlobal)
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

}
