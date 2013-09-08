package fyp.instrument;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fyp.configuration.Configuration;
import fyp.configuration.TraceFileTranslator;

/**
 * To produce trace files(trace.txt , field_index.txt  and class_index.txt) <br />
 * to record behavior of a run time program.<br />
 * Static methods in this class will be invoked by instructions inserted into <br />
 * byte code of normal program. 
 * 
 * @author Shen Yapeng
 * @version 2.10
 */

public class Trace {
    //fields ,classes , objects are data structure to storing string name and index for corresponding types.
    private static Map<String,Integer> fields = new HashMap<String,Integer>();
    private static Map<String,Integer> classes = new HashMap<String,Integer>();
    private static Set<Integer> objects = Collections.synchronizedSet(new HashSet<Integer>());
	
    //fieldsCount and classesCount 
    private static int fieldsCount = 0;
    private static int classesCount = 0;
    
    private static BufferedWriter classIndexPW ;
    private static BufferedWriter fieldIndexPW ;
    private static BufferedWriter tracePW ;

	
    public static void beginTrace(){
	try{
	  
	    try {
		classIndexPW = new BufferedWriter(new FileWriter(
			Configuration.getConf(Configuration.OUTPUT_PATH) +"/" + 
			Configuration.getConf(Configuration.FILE_CLASS)));
		fieldIndexPW = new BufferedWriter(new FileWriter(
			Configuration.getConf(Configuration.OUTPUT_PATH)+"/" + 
			Configuration.getConf(Configuration.FILE_FIELD)));
		tracePW = new BufferedWriter(new FileWriter(
			Configuration.getConf(Configuration.OUTPUT_PATH) +"/" + 
			Configuration.getConf(Configuration.FILE_TRACE)));
		//for collection and array index.
		/*classIndexPW.write(TraceFileTranslator.sentenceOfClassIndex(
    			"Array", TraceFileTranslator.FLAG_ARRAY));
		classIndexPW.write(TraceFileTranslator.sentenceOfClassIndex(
    			"Collection", TraceFileTranslator.FLAG_COLLECTION));
		*/
	    } catch (FileNotFoundException e) {
		e.printStackTrace(System.out);
	    }
		
	    //If program is forced shutdown , all trace files will be closed.
	    Runtime.getRuntime().addShutdownHook(new Thread(){
		public void run(){
		    Trace.endTrace();
		}
	    });
	}catch(Exception e){
	    e.printStackTrace(System.out);
	}
    }
	
    /**
     * In case bad condition , force close output stream of trace files
     */
    public static void endTrace(){
	try{
	    classIndexPW.close() ;
	    fieldIndexPW.close() ;
	    tracePW.close() ;
	} catch (Exception e) {
	    e.printStackTrace(System.out);
	}
    }
	
	
	
    /**
     * Make sure class constructor has been traced. If the object has not been created , <br />
     * traceObjectConstructor(obj) method will be invoked.
     * @param obj
     */
    public static void makeSureTraceObjectConstructor(Object obj){
	if(!objects.contains(System.identityHashCode(obj))){ //if it hasn't been recorded , record it. 
	    traceObjectConstructor(obj); 
	}
    }
	
	
	
	/**
	 * First , make sure the class has been traced (if not yet, invoke traceClassAndField). This is synchronized .<br />
	 * Then , trace the creation of such object in trace.txt .<br />
	 * Finally , add the object ID into objects(data structure in Trace).
	 * @param classRecorded
	 */	
	public static void traceObjectConstructor(Object obj){
	    //make indexes for class and fields in index files
	    try{
		synchronized (Trace.class){
		    if (!classes.containsKey(obj.getClass().getSimpleName())){ //if the class has not been included in , put it in.		
			traceClassAndFields(obj);
		    }
		}
		
		//trace for creation of objects in trace file
		tracePW.write(TraceFileTranslator.sentenceOfCreate(
			    System.identityHashCode(obj) , classes.get(obj.getClass().getSimpleName())));
		objects.add(System.identityHashCode(obj));
		
	    }catch (IOException e){
		e.printStackTrace(System.out);
	    }
	}
	
	
	/**
	 * Trace classes in class_index.txt and classes(data structure in Trace.java).<br />
	 * Trace fields of this class in field_index.txt and fields(data structure in Trace.java).<br />
	 * The operation of tracing fields is iterative until Object class
	 * @param obj
	 */
	public static  void traceClassAndFields(Object obj){ 
	    try {
		//Trace classes in class_index.txt and classes(data structure in Trace.java).
    		classes.put(obj.getClass().getSimpleName(), classesCount);
    		classIndexPW.write(TraceFileTranslator.sentenceOfClassIndex(
    			obj.getClass().getSimpleName(), (classesCount++)));
    		
    		//Trace fields of this class in field_index.txt and fields(data structure in Trace.java).
    		// The operation of tracing fields is iterative until Object class
    		for(Class<?> cls = obj.getClass();cls != Object.class ; cls = cls.getSuperclass()){
    		   Field[] fieldsInClass = cls.getDeclaredFields();
    		   	if(fieldsInClass.length > 0){
    		   	    for (Field field : fieldsInClass) {
    		   		if(!isIgnoredType(field.getType())){ 
    		   		    fields.put(obj.getClass().getSimpleName() + "._"+field.getName(), fieldsCount);
    		   		    fieldIndexPW.write( TraceFileTranslator.sentenceOfFieldIndex(
    		   			classes.get(obj.getClass().getSimpleName()) , 
    		   			field.getName(), 
    		   			field.getType().getName() , 
    		   			(fieldsCount++)));   
    		   		}	
    		   	    }	
    			}    
    		 }
	   } catch (IOException e) {
		    e.printStackTrace(System.out);
		}
	}
	
	    /**
	     * 
	     */
	    public static boolean isIgnoredType(Class<?> cls){
		if(cls.equals(int.class) || cls.equals(double.class) || cls.equals(float.class)
			    || cls.equals(String.class)|| cls.equals(byte.class)|| cls.equals(short.class)
			    || cls.equals(boolean.class)|| cls.equals(long.class)
			    || cls.equals(char.class)){
		    return true ;
		}else{
		    return false;
		}
	    }
	    

	/**
	 * Trace assignment in trace.txt.
	 * @param fieldName the name of the field
	 * @param owner , the object who owns the field
	 * @param value , the value of the field
	 */
	public static void traceAssignment( Object owner , Object value ,String fieldName ,boolean isArray){	
	    // trace fields assignment in trace file.
	    try{
		//This can not be ignored!!
		makeSureTraceObjectConstructor(owner);
		
		//This can not ignored! Exclude ignoring types and null assignment
		// if value == null , then id is 0
		if(fields.get(owner.getClass().getSimpleName() + "._"+fieldName)!= null 
			&& System.identityHashCode(value) != 0){ 
		    tracePW.write(TraceFileTranslator.sentenceOfAssignment( 
				    	fields.get(owner.getClass().getSimpleName() + "._"+fieldName), 
				    	System.identityHashCode(owner), 
				    	System.identityHashCode(value),
				    	isArray));
		}
	    }catch (IOException e){
		e.printStackTrace(System.out);
	    }
	}
	
	
	
	public static void traceGetField( Object owner  ,String fieldName ){
		/*try{
			// 
			tracePW.write("[use] "+ System.identityHashCode(owner) + "[Object]\n");
		}catch (IOException e){
			e.printStackTrace();
		}*/
		//avoid getfields  in  constructor. In that case , the object will be used before creation.
	}
	
	/**
	 * Trace method of instrumented object.
	 * @param obj
	 */
	public static void traceMethod( Object obj ){
	    try{
		if(!objects.contains(System.identityHashCode(obj))){
    		    traceObjectConstructor(obj);
    		    objects.add(System.identityHashCode(obj));
		}
		tracePW.write(TraceFileTranslator.sentenceOfObjectUse(System.identityHashCode(obj))); //无法保证它不是继承自collection的！
		}catch (IOException e){
    		    System.out.println(e.toString());
    		}
	}
	

 	public static void traceNewArray(int size ,Object array, String type){   
 	    try{
 		tracePW.write(TraceFileTranslator.sentenceOfArrayCreate(
 				System.identityHashCode(array),size));
 	    }catch (IOException e){
		e.printStackTrace();
 	    }
	}
 	
 	
 	public static void traceMultiArray(Object array , String desc , int dims){
 	    //TODO multi array 还没有处理过！
	/*	try{
 		// trace fields assignment in trace file.
 	//	tracePW.write("MultiArray with " +dims +" dimension :" + 
 		//		System.identityHashCode(array) + "("+ desc +") is created "+"\n");
			tracePW.write(TraceFileTranslator.sentenceOfArrayCreate(
 				System.identityHashCode(array), TraceFileTranslator.FLAG_ARRAY));
		}catch (IOException e){
			e.printStackTrace();
		}*/
 	}

	/**
	 * trace use of object from array or collection
	 * @param obj  the object been used.
	 */
	public static void traceObjectUse(Object obj){ 
	    if(objects.contains(System.identityHashCode(obj))){  //If obj belongs to String or boxed types , 
		 						 //It can not be traced!
		try{
		    tracePW.write(TraceFileTranslator.sentenceOfObjectUse(System.identityHashCode(obj)));
		}catch (IOException e){
		    e.printStackTrace(System.out);
		}
	    } 
	}
	
	
	/**
	 * Trace object assign to array.<br />
	 * [assign] [o2a] ... ... value array
	 * @param owner  the owner array 
	 * @param value  the object 
	 * @param i  index of the array.
	 */
	public static void traceStoreArray(Object[] array , int i , Object value ){
	    array[i] = value ;  //important!
	    try{
		tracePW.write(TraceFileTranslator.sentenceOfAssignmentToArray(System.identityHashCode(value), 
					System.identityHashCode(array)));
	    }catch (IOException e){
		e.printStackTrace(System.out);
	    }
	}
	
	
	public static void traceCollectionConstructor(Object object){
	    try{
		tracePW.write(TraceFileTranslator
				.sentenceOfCollectionCreate(System.identityHashCode(object)));
	    }catch (IOException e){
		e.printStackTrace();
	    }
	}

	public static void traceCollectionNoParam( Object owner ,String name){
		/*try{
		 tracePW.write(TraceFileTranslator.sentenceOfUse(System.identityHashCode(owner), name));
		}catch (IOException e){
			e.printStackTrace();
		}*/
	}
	
	
	public static Object traceCollectionManyParams(Object owner , Object[] params, 
			String className , String methodName , String desc){
	    try{
		
		//TODO Now , we just record the add(Object o) and remove(Object o) methods
		if(desc.equals("(Ljava/lang/Object;)Z") ){  
		    if(methodName.equals("add")){
			tracePW.write(TraceFileTranslator
				.sentenceOfCollectionUse( System.identityHashCode(owner),
					System.identityHashCode(params[0]),
					methodName));
		    }else if(methodName.equals("remove")){
			tracePW.write(TraceFileTranslator
				.sentenceOfCollectionUse( System.identityHashCode(owner),
					System.identityHashCode(params[0]),
					methodName));
		    }
		}
		
	    }catch (IOException e){
		e.printStackTrace();
	    }
		
	    try {
		Class<?> cls;
		cls = Class.forName(className.replace('/', '.'));
		Class<?>[] types = getParamsTypes(desc);
			
		Method method = cls.getMethod(methodName, types);
		
		return method.invoke(owner, params);
	    } catch (ClassNotFoundException e) {
		e.printStackTrace();
	    } catch (SecurityException e) {
		e.printStackTrace();
	    } catch (NoSuchMethodException e) {
		e.printStackTrace();
	    } catch (IllegalArgumentException e) {
		e.printStackTrace();
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
	    } catch (InvocationTargetException e) {
		e.printStackTrace();
	    }
	    return new Object();
	}
	
	

	public static Class<?>[] getParamsTypes(String desc){
	    String str = desc.substring(desc.indexOf('(')+1, desc.indexOf(')'));
	    ArrayList<String> al = new ArrayList<String>() ;  
		
	    int count = 0;
		
	    // marks for array
	    int arraycount = 0;
	    String arraytype = "";
		
	    for (int i = 0 ; i < str.length() ; i++){
		if (str.charAt(i) == 'L'){
		    //take care that "java/lang/Object" and "[Ljava/lang/Object;" 
		    for(int j = 0 ; j!=arraycount ; j++){
			arraytype  += "[";
		    }
		    if(arraycount != 0 ){
			al.add(arraytype + "L"+str.substring(i+1,str.indexOf(';',i)+1).replace('/', '.'));
		    }else{
			al.add(arraytype+str.substring(i+1,str.indexOf(';',i)).replace('/', '.'));
		    }
		    arraycount = 0 ;
		    count ++ ;
		    i = str.indexOf(';',i);
		}else if(str.charAt(i) == '['){
		    arraycount ++ ;
		}else{
		    for(int j = 0 ; j!=arraycount ; j++){
			arraytype  += "[";
		    }
		    if(str.charAt(i) =='I' ){
			al.add(arraytype + "I");
		    }else if(str.charAt(i) =='J' ){
			al.add(arraytype + "J");
		    }else if(str.charAt(i) =='Z' ){
			al.add(arraytype + "Z");
		    }else if(str.charAt(i) =='C' ){
			al.add(arraytype + "C");
		    }else if(str.charAt(i) =='D' ){
			al.add(arraytype + "D");
		    }else if(str.charAt(i) =='F' ){
			al.add(arraytype + "F");
		    }else if(str.charAt(i) =='S' ){
			al.add(arraytype + "S");
		    }	
		    count ++ ;
		    arraycount  = 0;
		}
	    }
		
	    Class<?>[] classes = new Class[count];
	    for ( int i = 0 ; i != count ; i ++){
		String className = al.get(i);
		try {
		    if(className.equals("I"))
			classes[i] = int.class;
		    else if(className.equals("J"))
			classes[i] = long.class;
		    else if(className.equals("Z"))
			classes[i] = boolean.class;
		    else if(className.equals("C"))
			classes[i] = char.class;
		    else if(className.equals("D"))
			classes[i] = double.class;
		    else if(className.equals("F"))
			classes[i] = float.class;
		    else if(className.equals("S"))
			classes[i] = short.class;
		    else{
			classes[i] = Class.forName(className);
		    }
		} catch (ClassNotFoundException e) {
		    e.printStackTrace();
		}
	    }
	    return classes;
	}
	
}