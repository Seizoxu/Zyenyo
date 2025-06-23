package asynchronous.typing;

import java.awt.BasicStroke;
import java.io.File;
import java.util.LinkedHashMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import zyenyo.Database;


public class Chart implements Runnable
{
	private MessageReceivedEvent event;
	public Chart(MessageReceivedEvent event) {this.event = event;}

    @Override
    public void run()
    {
		try
		{

			String id = event.getAuthor().getId();
			LinkedHashMap<String, Integer> monthlyPlaycount = Database.playcount(id);

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

			EmbedBuilder embed = new EmbedBuilder().setTitle("Playcount").setImage("attachment://chart.png");
			event.getChannel().sendMessageEmbeds( embed.build() ).addFile(lineChart, "chart.png").queue();

		}
		catch (Exception e) {e.printStackTrace();}
	}
}
