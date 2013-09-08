package fyp.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Configuration
{
    public static String OUTPUT_PATH = "OutputPath";
    public static String FILE_FIELD = "fieldFileName";
    public static String FILE_CLASS = "classFilename";
    public static String FILE_TRACE = "traceFileName";
    public static String FILE_RESULT = "resultFileName";
    public static String CHART_LIFETIME = "lifetimeChartName";
    public static String CHART_EXCLUSIVITY = "exclusivityChartName";
    public static String CHART_MULTIPLICITY = "multiplicityChartName";
    public static String CHART_OBJECT_COUNT = "objectCountChartName";

    /**
     * get configuration value
     * @param key 
     * @return the value of the key 
     */
    public static String getConf(String key)
    {
	Properties property = new Properties();
        try {
            File directory = new File(".");//set working directory 
            FileInputStream inputFile;
            if(directory.getCanonicalPath().endsWith("FYP")){
        	inputFile = new FileInputStream(directory.getCanonicalPath()+"/pro/fyp.properties");
            }else{
        	inputFile = new FileInputStream(directory.getCanonicalPath()+"/../pro/fyp.properties");
            }
	    property.load(inputFile);
            inputFile.close();
        } catch (FileNotFoundException ex) {
            System.out.println("read configuration failure! reason:it does not exist in that place");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("load configuration file failure !");
            ex.printStackTrace();
        }
	
        if(property.containsKey(key)){
            String value = property.getProperty(key);
            property.clear();
            return value;
        }
        else{ 
            return "./";
        }
    }
    
     
}//end class ReadConfigInfo
