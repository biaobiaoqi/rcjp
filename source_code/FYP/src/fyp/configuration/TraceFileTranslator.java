package fyp.configuration;

/**
 * responsible for conversion from in-memory data structure such as object id 
 * to literal sentences in trace files(trace.txt , field_index.txt and class_index.txt) and reverse.
 * Sentences are as follows :<br /> 
 * [assign] ... ... ... field owner value<br />
 * [assign] [o2a]... ... value array<br />
 * [assign] [a2o]... ... ... field owner value<br />
 * 
 * [create] ... ... object class<br />   
 * [create] [a] ... ... array size<br />
 * [create] [c] ... collection
 * 
 * [use] ... object<br />
 * [use] [c] [add] ... ... collection object
 * [use] [c] [remove] ... ... collection object
 * 
 * @author biaobiaoqi
 * @version 2.3
 */
public class TraceFileTranslator {
    
    public static int FLAG_COLLECTION = -1 ;
    public static int FLAG_ARRAY = -2 ;
    public static int FLAG_OBJECT = -3 ;
    
    
    public static boolean isCreateSentence(String sentence){
	return sentence.startsWith("[create]");
    }

    public static boolean isCreateObjectSentence(String sentence){
	return sentence.startsWith("[create]")
		&& !sentence.startsWith("[create] [a]") 
		&& !sentence.startsWith("[create] [c]");
    }

    
    public static boolean isCreateCollectionSentence(String sentence){
	return sentence.startsWith("[create] [c]");
    }

    public static boolean isCreateArraySentence(String sentence){
	return sentence.startsWith("[create] [a]");
    }
    
    
    /**
     * Check if it's a use sentence.<br />
     * [use] ... object<br />
     * [use] [c] [add] ... ... collection object
     * [use] [c] [remove] ... ... collection object
     * @param sentence
     * @return
     */
    public static boolean isUseSentence(String sentence){
	return sentence.startsWith("[use]");
    }
    
    
    public static boolean isUseCollectionAddSentence(String sentence){
	return sentence.startsWith("[use] [c] [add]");
    }
    

    public static boolean isUseCollectionRemoveSentence(String sentence){
	return sentence.startsWith("[use] [c] [remove]");
    }
    
    public static boolean isUseObjectSentence(String sentence){
	return sentence.startsWith("[use]") && ! sentence.startsWith("[use] [c]");
    }
    
    /**
     * Check if it's assign sentence.<br />
     * [assign] ... ... ... field owner value<br />
     * [assign] [o2a]... ... value array<br />
     * [assign] [a2o]... ... ... field owner value<br />
     * @param sentence
     * @return
     */
    public static boolean isAssignObjectSentence(String sentence){
	return sentence.startsWith("[assign]") && !sentence.startsWith("[assign] [")
		&& !(sentence.split(" ")[1].equals("null")) ; //exclude the case : field id is null
	//TODO why there is some case that field is null!!?
    }
    
    public static boolean isAssignToArraySentence(String sentence){
	return sentence.startsWith("[assign] [o");
    }
    
    public static boolean isAssignFromArraySentence(String sentence){
	return sentence.startsWith("[assign] [a");
    }
    
    /**
     * Get the id of field from the assign sentence.<br />
     * [assign] ... ... ... field owner value<br />
     * [assign] [a2o] ... ... ... field owner value<br />
     * [assign] [o2a] ... ... value array
     * @param sentence
     * @return
     */
    public static int getIdOfFieldTypeFromAssign(String sentence){
	if(isAssignObjectSentence(sentence)){
	    return new Integer(sentence.split(" ")[1]);
	}else if(isAssignFromArraySentence(sentence)){
	    return new Integer(sentence.split(" ")[2]);
	}else{
	    //TODO  throw errors ?
	    return -1;
	}
    }
    
    /**
     * Get the id of owner object from the assign sentence.<br />
     * [assign] ... ... ... field owner value<br />
     * [assign] [a2o] ... ... ... field owner value<br />
     * [assign] [o2a] ... ... value array
     * @param sentence
     * @return
     */
    public static int getIdOfOwnerObjetFromAssign(String sentence){
	if(isAssignObjectSentence(sentence)){
	    return new Integer(sentence.split(" ")[2]);
	}else if(isAssignFromArraySentence(sentence)){
	    return new Integer(sentence.split(" ")[3]);
	}else if(isAssignToArraySentence(sentence)){
	    return new Integer(sentence.split(" ")[3]);
	}else{
	    //TODO  throw errors ?
	    return -1;
	}
    }
    
    /**
     * Get the id of value object from the assign sentence.<br />
     * [assign] ... ... ... field owner value<br />
     * [assign] [a2o] ... ... ... field owner value<br />
     * [assign] [o2a] ... ... value array
     * @param sentence
     * @return
     */
    public static int getIdOfValueObjectFromAssign(String sentence){
	if(isAssignObjectSentence(sentence)){
	    return new Integer(sentence.split(" ")[3]);
	}else if(isAssignFromArraySentence(sentence)){
	    return new Integer(sentence.split(" ")[4]);
	}else if(isAssignToArraySentence(sentence)){
	    return new Integer(sentence.split(" ")[2]);
	}else{
	    //TODO  throw errors ?
	    return -1;
	}
    }
    

    /**
     * [use] [c] [add] ... ... collection object
     * [use] [c] [remove] ... ... collection object
     * @return
     */
    public static int getOwnerIdFromUseCollection(String sentence){
	return new Integer(sentence.split(" ")[3]);
    }
    
    /**
     * [use] [c] [add] ... ... collection object
     * [use] [c] [remove] ... ... collection object
     * @return
     */
    public static int getValueIdFromUseCollection(String sentence){
	return new Integer(sentence.split(" ")[4]);
    }
    
    
    /**
     * Get the id of object from  [create] and [use] sentence .<br /> 
     * There are four kinds of these sentences : <br /> 
     * [create] ... ... object class<br /> 
     * [create] [a] ... ... array size<br />
     * When it's not a regular create sentence ,throw exception.
     *@param sentence : sentence in trace file
     *@return object id .
     * @throws Exception 
     */
    public static int getIdOfObjectFromCreate(String sentence) throws Exception{
	if(isCreateArraySentence(sentence) || isCreateCollectionSentence(sentence)){  
	    return new Integer(sentence.split(" ")[2]);
	}else if(isCreateObjectSentence(sentence)  ){ 
	    return new Integer(sentence.split(" ")[1]);
	}else{
	    throw new Exception("not a normal create sentence :" + sentence);
	}
    }
    
    /**
     * [use] ... object<br />
     * [use] [c] [add] ... ... collection object<br />
     * [use] [c] [remove] ... ... collection object<br />
     * @param sentence
     * @return
     */
    public static int getIdOfObjectFromUse(String sentence){
	if(isUseObjectSentence(sentence)){ 
	    return new Integer(sentence.split(" ")[1]);
	}else if(isUseCollectionAddSentence(sentence) || isUseCollectionRemoveSentence(sentence)){ 
	    return new Integer(sentence.split(" ")[4]);
	}else{
	    return -1;
	}
    }
    
    public static int getIdOfCollectionFromUse(String sentence) throws Exception{
	if(isUseCollectionAddSentence(sentence) || isUseCollectionRemoveSentence(sentence)){
	    return new Integer(sentence.split(" ")[3]);
	}else{
	    throw new Exception(sentence);
	}
    }
    
   
    /**
     * [create] [a] ... ... array size
     * @param sentence
     * @return
     */
    public static int getSizeOfArrayFromCreate(String sentence){
	return new Integer(sentence.split(" ")[3]);
    }
    
    /**
     * Get id of class from [create] sentence:<br />
     * [create] ... ... object class<br />
     * [create] [a] ... ... array size<br />
     * [create] [c] ... collection<br />
     * @param sentence
     * @return the class type the sentence created
     * @throws Exception 
     */
    public static int getIdOfClassFromCreate(String sentence) throws Exception{   //TODO 对于 array  和 collection，这个定位有问题！
	if(isCreateArraySentence(sentence)){
	    return FLAG_ARRAY;
	}else if(isCreateObjectSentence(sentence) || isCreateCollectionSentence(sentence)){
	    return new Integer(sentence.split(" ")[2]);	    
	}else{
	    throw new Exception(sentence);
	}
    }
    
    /**
     * Get sentence standing for creation of normal object and collection <br />
     * [create] ... ... object class<br />
     * This sentence will be put into trace file.
     * @param objectId the object ID which is created
     * @param classId the class ID which is created. When it's -1 , standing for collection. <br />
     *  When it's -2 , standing for array.
     * @return String that will be put into trace file.
     */
    public static String sentenceOfCreate(int objectId , int classId){
	//return new String("[create] " + objectId + " " + classId + " object class\n");
	return new String("[create] " + objectId + " " + classId + "\n");
    }
    
    
    public static String sentenceOfCollectionCreate(int objectId){
	return new String("[create] [c] " + objectId + "\n");
    }
    
    
    /**
     * Get sentence standing for creation of array:<br />
     * [create] [a] ... ... array size<br />
     * This sentence will be put into trace file.
     * @param objectId the object ID which is created
     * @param size the class ID which is created. When it's -1 , standing for collection. <br />
     *  When it's -2 , standing for array.
     * @param size the size of the array
     * @return String that will be put into trace file.
     */
    public static String sentenceOfArrayCreate(int objectId  , int size){
	 //   return new String("[create] [a] " + objectId + " " + size +" object size\n");
	   return new String("[create] [a] " + objectId + " " + size +"\n");
    }
    
    
    /**
     * Get sentence standing for using of object :<br />
     * [use] ... object<br />
     * [use] [c] ... ... collection method<br />
     * This sentence will be put into trace file.
     * @param objectId
     * @param classId
     * @param methodName
     * @return
     */
    public static String sentenceOfObjectUse(int objectId ){
	return new String("[use] " + objectId + "\n");
    }
    
    
    public static String sentenceOfCollectionUse(int collectionId , int objectId , String methodName){
	    return new String("[use] [c] [" + methodName + "] " + collectionId + " " + objectId + "\n");
    }
    
    /**
     * Get sentence of assignment. There are two kinds of sentences:
     * [assign] [a] ... ... ... field owner value<br />
     * [assign] ... ... ... field owner value<br />
     * @param fieldId
     * @param ownerId
     * @param valueId
     * @return
     */
    public static String sentenceOfAssignment(int fieldId , int ownerId , int valueId ,boolean isArray){
	if(isArray){
	   // return new String("[assign] [a2o] "+  fieldId +" " + ownerId + " "+ valueId+" field owner value\n");
	    return new String("[assign] [a2o] "+  fieldId +" " + ownerId + " "+ valueId+"\n");
	}else{
	  //  return new String("[assign] "+  fieldId +" " + ownerId + " "+ valueId+" field owner value\n");
	    return new String("[assign] "+  fieldId +" " + ownerId + " "+ valueId+"\n");
	}
    }
    
    public static String sentenceOfAssignmentToArray(Object value , Object array){
   	//return new String("[assign] [o2a] "+ value + " " + array + " value array\n");
	return new String("[assign] [o2a] "+ value + " " + array + "\n");
    }
    
    
    /**
     * Get sentence for fields_index.txt file.
     * @param classId
     * @param fieldName
     * @param fieldType
     * @param fieldIndex
     * @return
     */
    public static String sentenceOfFieldIndex(int classId , String fieldName , String fieldType , int fieldIndex){
	return new String(classId + "._" + fieldName + "(" + fieldType + ")" + fieldIndex + "\n");
    }
    
    /**
     * Get sentence for classes_index.txt file.
     * @param className
     * @param classIndex
     * @return
     */
    public static String sentenceOfClassIndex(String className , int classIndex){
	return new String(className + " = " + classIndex + "\n");
    }
    
   
    
}
