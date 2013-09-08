import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class TestWithOutMemoryProblem {

    public static BufferedReader br;
    public static BufferedWriter bw ;
    
    public static void main(String[]args) throws IOException{
		Runtime.getRuntime().addShutdownHook(new Thread(){
		    public void run(){
			
			try {
			    br.close();
			} catch (IOException e) {
			    e.printStackTrace();
			}
			try {
			    bw.close();
			} catch (IOException e) {
			    e.printStackTrace();
			}
		    }
		});
		//TODO
		
	    File file = new File("/home/biaobiaoqi/DATA/WorkSpace/FYP/result/trace.txt");
	    br = new BufferedReader(new FileReader(file));
	    file = new File("/home/biaobiaoqi/DATA/WorkSpace/FYP/result/test_trace.txt");
	    bw = new BufferedWriter(new FileWriter(file));
	    for(String str = br.readLine() ; str != null ; str = br.readLine()){
		    	bw.write(str+"\n");
		} 	
    }

}