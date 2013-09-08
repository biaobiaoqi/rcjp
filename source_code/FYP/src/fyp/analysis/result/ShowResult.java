package fyp.analysis.result;

import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import fyp.analysis.Analyzer;
import fyp.analysis.ClassRelation;
import fyp.analysis.ClassRelationRecord;
import fyp.configuration.Configuration;

/**
 * Show result by many possible ways such as a chart , a table and so on.
 * @author biaobiaoqi
 * @version 2.8
 */
public class ShowResult {
    private Map<ClassRelation,ClassRelationRecord> classesRelations;
    private static int X_INTERVAL = 100;
    
    
    public ShowResult(Map<ClassRelation,ClassRelationRecord> classesRelations){
	this.classesRelations = classesRelations;
    }
    
    
    public void show(){
	recordFile();
	makeChart();
    }
    
    
    public void recordFile(){
	try{
	    String path = Configuration.getConf(Configuration.OUTPUT_PATH)+"/" + Configuration.getConf(Configuration.FILE_RESULT);
	    BufferedWriter bfWriter = new BufferedWriter(new FileWriter(path));
	    for(Entry<ClassRelation, ClassRelationRecord> entry : Analyzer.classesRelations.entrySet()){
		bfWriter.write("parentClass:"+ entry.getKey().getOwnerClassId() + " childClass:" + entry.getKey().getValueClassId()+" fieldId:" +entry.getKey().getFieldId()  +":");
		bfWriter.write("[Lifetime type:"+ entry.getValue().lifeTimeType +"]: ");
		bfWriter.write(entry.getValue().lifeTimeTypeCount[0]+" ");
		bfWriter.write(entry.getValue().lifeTimeTypeCount[1]+" ");
		bfWriter.write(entry.getValue().lifeTimeTypeCount[2]+" ");
		bfWriter.write(entry.getValue().lifeTimeTypeCount[3]+" ");
		bfWriter.write(" [EXCLUSIVITY:"+entry.getValue().exclusivity+"]: "+entry.getValue().exclusivityCount[0]+" ");
		bfWriter.write(entry.getValue().exclusivityCount[1]+" ");
		bfWriter.write(entry.getValue().exclusivityCount[2]+" ");
		bfWriter.write(entry.getValue().exclusivityCount[3]+" ");
		bfWriter.write(" [MULTIPLICITY]: "+ entry.getValue().getMultiplicity());
		bfWriter.write(" \n");
	    }
	}catch (Exception e1) {
	    e1.printStackTrace(System.out);
	}
    }
    

    public void makeChart(){
	makeObjectCountChart();
	makeLifeTimePieChartBasedOnObjects();
	makeExclusivityPieChartBasedOnObjects();
	makeMultiplicityPieChartOnObjects();
	makeExclusivityPieChartBasedOnClasses();
	makeLifeTimePieChartBasedOnClasses();
	
    }
    
    public void makeObjectCountChart(){
	List<Integer> countData = Analyzer.objectCount;
    	XYSeries series = new XYSeries("OBJECTS COUNT CHART");
    	int lifeTimeInterval = countData.size()/X_INTERVAL ;
    	
    	for( int i = 0 ; i != X_INTERVAL ; i ++){
    	    try{
    		series.add(i+1 , countData.get(i*lifeTimeInterval));
    	    }catch(Exception e){
    		System.out.println(e.toString());
    	    }
    	}
    	
    	series.add(X_INTERVAL+1 , 0);

    	// Add the series to your data set
    	XYSeriesCollection dataset = new XYSeriesCollection();
    	dataset.addSeries(series);
    	
    	// Generate the graph
    	JFreeChart chart = ChartFactory.createXYLineChart(
                            	"OBJECTS COUNT CHART",
                            	"TIME("+lifeTimeInterval+"instructions/1)",
                            	"OBJECTS COUNT",
                            	dataset,PlotOrientation.VERTICAL, true,true,false);      
    	try {
    	    String path =Configuration.getConf(Configuration.OUTPUT_PATH)+"/" + 
    		    	Configuration.getConf(Configuration.CHART_OBJECT_COUNT);
    	    ChartUtilities.saveChartAsJPEG(new File(path), chart, 500, 300);
    	} catch (IOException e) {
    	    e.printStackTrace(System.out);
    	}
    }
    
    
    public void makeLifeTimePieChartBasedOnObjects(){
	int[] lifeTimeTypes = {0,0,0,0};
	for(ClassRelationRecord classRelationRecord : classesRelations.values()){
	    lifeTimeTypes[0] += classRelationRecord.lifeTimeTypeCount[0];
	    lifeTimeTypes[1] += classRelationRecord.lifeTimeTypeCount[1];
	    lifeTimeTypes[2] += classRelationRecord.lifeTimeTypeCount[2];
	    lifeTimeTypes[3] += classRelationRecord.lifeTimeTypeCount[3];
	}
	
	String[] desc = {"Lifetime 1" , "Lifetime 2", "Lifetime 3", "Lifetime 4"};
	makePieChart(lifeTimeTypes, 
		desc,
		"Life Time Pie Chart Based On Objects", 
		"Lifetime(Object)PieChart");
    }
    
    
    public void makeExclusivityPieChartBasedOnObjects(){
   	int[] exclusivity = {0,0,0,0};
   	for(ClassRelationRecord classRelationRecord : classesRelations.values()){
   	    exclusivity[0] += classRelationRecord.exclusivityCount[0];
   	    exclusivity[1] += classRelationRecord.exclusivityCount[1];
   	    exclusivity[2] += classRelationRecord.exclusivityCount[2];
   	    exclusivity[3] += classRelationRecord.exclusivityCount[3];
   	}
   	 
   	String[] desc = {"non-exclusive" , "transferal-exclusive", "global-exclusive", "local-exclusive"};
   	makePieChart(exclusivity, 
   		desc,
   		"Exclusivity Pie Chart Based On Objects", 
   		"Exclusivity(Object)PieChart");
    }
    
    
    public void makeMultiplicityPieChartOnObjects(){
   	int[] multiplicity = {0,0};
   	for(ClassRelationRecord classRelationRecord : classesRelations.values()){
   	    if(classRelationRecord.multiplicity){
   		multiplicity[1] ++ ;
   	    }else{
   		multiplicity[0] ++;
   	    }
   	}
 
   	String[] desc = {"No" , "Yes"};
   	makePieChart(multiplicity, 
   		desc,
   		"Multiplicity Pie Chart", 
   		"MultiplicityPieChart");
    }
    

    public void makePieChart(int[] iarray , String[] desc , String title , String fielName){
	int totalCount =0 ;
	for(int i = 0 ; i != iarray.length ; i++)
	{
	    totalCount += iarray[i];
	}
	
	DefaultPieDataset dataset = new DefaultPieDataset();
	for(int i = 0 ; i != iarray.length ; i++)
	{
	    /*
	    DecimalFormat df = new DecimalFormat("0.00");
	    dataset.setValue(desc[i]+":"+df.format(iarray[i]*1.0 / totalCount) , 
		     	new Double(iarray[i]*1.0 / totalCount));*/
	    NumberFormat num = NumberFormat.getPercentInstance(); 
	    num.setMaximumIntegerDigits(3); 
	    num.setMaximumFractionDigits(2); 
	    dataset.setValue(desc[i]+":"+num.format(iarray[i]*1.0 / totalCount) , 
		     	new Double(iarray[i]*1.0 / totalCount));

	}
        
	
	JFreeChart chart = ChartFactory.createPieChart(
	            title,  // chart title
	            dataset,             // data
	            true,               // include legend
	            true,
	            false
	        );
	
	 PiePlot plot = (PiePlot) chart.getPlot();
	 plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
	 plot.setNoDataMessage("No data available");
	 plot.setCircular(false);
	 plot.setLabelGap(0.02);
	        
	 try {
	     String path = Configuration.getConf(Configuration.OUTPUT_PATH)+"/"+fielName+".jpg";
	     ChartUtilities.saveChartAsJPEG(new File(path), chart, 500, 300);
	 } catch (IOException e) {
	     e.printStackTrace(System.out);
	 }
    }
    
    public void makeExclusivityPieChartBasedOnClasses(){
	int[] counts = {0,0,0,0};
   	for(ClassRelationRecord classRelationRecord : classesRelations.values()){
   	    counts[classRelationRecord.exclusivity] += 1;
   	}
   	 
   	String[] desc = {"non-exclusive" , "transferal-exclusive", "global-exclusive", "local-exclusive"};
   	makePieChart(counts, 
   		desc,
   		"Exclusivity Pie Chart Based On Classes", 
   		"Exclusivity(Class)PieChart");
    }
    
    public void makeLifeTimePieChartBasedOnClasses(){
	int[] counts = {0,0,0,0};
	
	for(ClassRelationRecord tmpClassRelationRecord : classesRelations.values()){
	    counts[tmpClassRelationRecord.lifeTimeType] += 1;
	}
	String[] desc = {"Lifetime 1" , "Lifetime 2", "Lifetime 3", "Lifetime 4"};
	makePieChart(counts, 
		desc,
		"Life Time Pie Chart Based On Classes", 
		"Lifetime(Class)PieChart");
    }
}
