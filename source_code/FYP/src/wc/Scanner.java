package fyp.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import fyp.analysis.result.ShowResult;
import fyp.configuration.Configuration;
import fyp.configuration.TraceFileTranslator;

/**
 * Scan the trace files produced by instrument package.<br />
 * Then pass the data into Analyzer to analyze.
 * @author biaobiaoqi
 * @version 2.7
 */
public class Scanner  {    
    	//as one instructions in trace file being analyzed , it pluses one.
	public static int timer = 0;
	
	//Counts for field ,giving every field an index.
	public static int fieldCount = 0 ;
	
	public static void main(String[] args) throws IOException, InterruptedException{
	   long st = System.currentTimeMillis();
	   long st1 = st ;
	    System.out.println("-- start analysis --");
	   
	    //build fields 
	    File traceFile = new File(Configuration.getConf(Configuration.OUTPUT_PATH)+"/" + 
		    	Configuration.getConf(Configuration.FILE_FIELD));
	    
	    
	    BufferedReader br = new BufferedReader(new FileReader(traceFile));
	    for(String str = br.readLine() ; str != null ; str = br.readLine()){
		buildFields(str);		
	    }
	    System.out.println("-- fields has been built --");

	    traceFile = new File(Configuration.getConf(Configuration.OUTPUT_PATH)+"/" + 
		    		Configuration.getConf(Configuration.FILE_TRACE));
	    br = new BufferedReader(new FileReader(traceFile));	
	    for(String str = br.readLine() ; str != null ; str = br.readLine()){
		timer ++;
		Analyzer.objectCount.add(new Integer(0)); //initiate objectCount with value 0. //time goes on .
		if(timer >= Integer.MAX_VALUE){
		    System.exit(1);
		}
			
		if(TraceFileTranslator.isCreateSentence(str)){
		    processCreateSentence(str);	
		}
			
		if(TraceFileTranslator.isUseSentence(str)){
		    try {
			processUseSentence(str);
		    } catch (Exception e) {
			e.printStackTrace(System.out);
		    }	
		
		}
			
		if(TraceFileTranslator.isAssignFromArraySentence(str) 
			||TraceFileTranslator.isAssignObjectSentence(str)
			||TraceFileTranslator.isAssignToArraySentence(str)){
		    processAssignSentence(str);    
		   	  
		}
		
		if(timer%10000 == 0){
		    System.out.println(timer + " T:" +(System.currentTimeMillis() - st1));
		    st1 = System.currentTimeMillis();
		}
	    } 	
	    
	    System.out.println("start life time analysis");
	    Analyzer.AnalyzeLifeTimeRelationships();
	    System.out.println("start exclusivity analysis");
	    Analyzer.AnalyzeExclusivityAndTransferal();
	    System.out.println("-- end analysis --");
	   
	    ShowResult showResult = new ShowResult(Analyzer.classesRelations);
	    showResult.show();
	    
	    System.out.println("-- end draw --");
	    
	    System.out.println("TOTAL TIME:" + (System.currentTimeMillis() - st));
	}
	
	
	public static void buildFields(String str){
    	    //Integer l = new Integer(str.substring(str.indexOf(" = ")+3, str.length()));
	    Analyzer.fields.add(fieldCount++, new LinkedList<ObjectInFields>());
       	}

	
	/**
	 * [create] ... ... object class<br />   
	 * [create] [a] ... ... array size<br />
	 * @param sentence
	 */
	public static void  processCreateSentence(String sentence){
	    int objectId = 0 ;
	    int classId = 0;
	    try {
		objectId = TraceFileTranslator.getIdOfObjectFromCreate(sentence);
		classId = TraceFileTranslator.getIdOfClassFromCreate(sentence);
	    } catch (Exception e) { //There is no proper class ID and object ID.
		e.printStackTrace(System.out);
	    }

	    if(TraceFileTranslator.isCreateObjectSentence(sentence)){
		//insert the object id and class id into objectsHashMap
		Analyzer.objects.put( objectId , new ObjectRecord(timer,objectId,classId) );
	    }else if(TraceFileTranslator.isCreateArraySentence(sentence) 
		    || TraceFileTranslator.isCreateCollectionSentence(sentence)){
		Analyzer.containers.put(objectId, new ContainerRecord()); 
	    }
	}

	
	/**
	 * If it's a [use] sentenceuction , update the end time of the object.
	 * [use] ... object<br />
	 * [use] [c] ... ... collection method<br />
	 * update the objectCount through lifetime.
	 * @param str : sentenceuctions in trace file
	 * @throws Exception 
	 */
   	public static void processUseSentence(String sentence) throws Exception{
   	    if(TraceFileTranslator.isUseCollectionAddSentence(sentence)){
   		int valueId = TraceFileTranslator.getIdOfCollectionFromUse(sentence);
   		int collectionId = TraceFileTranslator.getIdOfObjectFromUse(sentence);
   		assignToContainers(valueId, collectionId);
   	    }else if(TraceFileTranslator.isUseCollectionRemoveSentence(sentence)){
   		//TODO
   	    }else if(TraceFileTranslator.isUseObjectSentence(sentence)){
   		int objectId = 0;
   		try {
   		    objectId = TraceFileTranslator.getIdOfObjectFromUse(sentence);
   		} catch (Exception e) {//There is no proper object ID.
   		    e.printStackTrace(System.out);
   		}
   		if(TraceFileTranslator.isUseSentence(sentence)){ // update endtime of the object and the objectCount
   		    Analyzer.updateObjectDeadTime(objectId,timer);
   		}
   	    }else{
   		throw new Exception(sentence);
   	    }
   	    
   	  
   	}


   	/**
   	 * If it's Assignment sentence : <br/>
   	 * [assign] ... ... ... field owner value<br />
   	 * [assign] [o2a]... ... value array<br />
   	 * [assign] [a2o]... ... ... field owner value<br />
   	 * @param str sentence in trace file.
   	 */
   	public static void processAssignSentence(String sentence){
   	    if(TraceFileTranslator.isAssignObjectSentence(sentence)){
   		try{
   		    int fieldId = TraceFileTranslator.getIdOfFieldTypeFromAssign(sentence);
           	    int ownerId = TraceFileTranslator.getIdOfOwnerObjetFromAssign(sentence);
           	    int valueId = TraceFileTranslator.getIdOfValueObjectFromAssign(sentence);
           	    if( Analyzer.objects.containsKey(valueId) ){  //excluding from collection case
           		Analyzer.assignment(valueId , fieldId , ownerId, timer , false);
           	    }
   		}catch(Exception e){
   		    System.out.println("1" +e.toString()+sentence);
   		 e.printStackTrace(System.out);
   	    	}
   	    }else if(TraceFileTranslator.isAssignToArraySentence(sentence)){ // [o2a]
   		//Array is just a container , through the container , objects should be 
   		//connected to original owner.
   		try{
   		    int valueId = TraceFileTranslator.getIdOfValueObjectFromAssign(sentence);
   		    int containerId = TraceFileTranslator.getIdOfOwnerObjetFromAssign(sentence);
   		    assignToContainers(valueId, containerId);
   		}catch(Exception e){
   		    //If it's a array in a static method , just ignore the case .Or print out the exception stack trace.
   		    if(!e.getMessage().equals("Array in static method.")){
   	   		    e.printStackTrace(System.out);
   		    }
   		}
   	    }else { // [a2o] and from collection case 
   		try{
   		    int fieldId = TraceFileTranslator.getIdOfFieldTypeFromAssign(sentence);
   		    int ownerId = TraceFileTranslator.getIdOfOwnerObjetFromAssign(sentence);
   		    int containerId = TraceFileTranslator.getIdOfValueObjectFromAssign(sentence);
   		    
   		    Analyzer.updateObjectDeadTime(ownerId,timer);
   		    if(Analyzer.containers.containsKey(containerId)){
   			Analyzer.containers.get(containerId).setOwnerField(ownerId, fieldId);
   		    }else{ //TODO 
   			//不可测代码中方法的返回值！ 即不可测的代码中返回的创造好的  array 或者 collection
   		//	System.out.println(sentence);
   		//	throw new Exception(" ");
   		    }
   		} catch(Exception e){
   		    e.printStackTrace(System.out);
   		}
   	    }
	}
   	
   	public static void assignToContainers(int valueId , int containerId) throws Exception{
   	    if(Analyzer.containers.containsKey(containerId)){
   		int ownerId = Analyzer.containers.get(containerId).getOwnerId();
   		int fieldId = Analyzer.containers.get(containerId).getFieldId();
   		Analyzer.assignment(valueId , fieldId , ownerId, timer ,true );
   	    }else{
			//TODO 可能会有在 constructor中创建的collection
   	    }
   	}
   	
   	
}
