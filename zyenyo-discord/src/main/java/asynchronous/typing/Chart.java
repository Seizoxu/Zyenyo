package asynchronous.typing;

import java.io.IOException;
import java.io.File;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import org.jfree.chart.JFreeChart; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.ChartUtilities; 
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


public class Chart implements Runnable
{
	private MessageReceivedEvent event;
	public Chart(MessageReceivedEvent event) {this.event = event;}

    @Override
    public void run()
    {
		try
		{

			DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
			line_chart_dataset.addValue( 15 , "schools" , "1970" );
			line_chart_dataset.addValue( 30 , "schools" , "1980" );
			line_chart_dataset.addValue( 60 , "schools" , "1990" );
			line_chart_dataset.addValue( 120 , "schools" , "2000" );
			line_chart_dataset.addValue( 240 , "schools" , "2010" ); 
			line_chart_dataset.addValue( 300 , "schools" , "2014" );

			JFreeChart lineChartObject = ChartFactory.createLineChart(
					"Schools Vs Years","Year",
					"Schools Count",
					line_chart_dataset,PlotOrientation.VERTICAL,
					true,true,false);

			int width = 640;    /* Width of the image */
			int height = 480;   /* Height of the image */ 
			File lineChart = new File( "LineChart.png" ); 
			ChartUtilities.saveChartAsPNG(lineChart ,lineChartObject, width ,height);

			EmbedBuilder embed = new EmbedBuilder().setTitle("Playcount").setImage("attachment://chart.png");
			event.getChannel().sendMessageEmbeds( embed.build() ).addFile(lineChart, "chart.png").queue();

		}
		catch (Exception e) {e.printStackTrace();}
	}
}
