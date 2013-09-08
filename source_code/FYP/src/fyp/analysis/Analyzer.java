package fyp.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The way to analyze the life time , exclusivity relationship and transferals
 * @author biaobiaoqi
 * @version 2.7
 */

public class Analyzer {
  	//The Reference list is a list for all objects in this field type . 
  	//And the Reference type contains a list of owners of the field object .
  	//It maintains references from fields to owners
  	public static List<LinkedList<ObjectInFields>> fields
  						= new ArrayList<LinkedList<ObjectInFields>>();
  	
  	//All objects are in the hash map , indexed by  the identified object id
  	//Start time and end time are stored in ObjectRecord , as well as two reference list ,
  	//which show assignment behaviors of this object both as parent and child.
  	public static Map<Integer, ObjectRecord> objects = new HashMap<Integer, ObjectRecord>();

  	//All arrays are in the hash map , indexed by the identified object id of array.
  	public static Map<Integer, ContainerRecord> containers = new HashMap<Integer, ContainerRecord>();
  	
  	//record the object counts through out the life time of software
  	public static List<Integer> objectCount = new ArrayList<Integer>();
  	
  	//class-class relation
  	public static Map<ClassRelation,ClassRelationRecord> classesRelations =
  		 	new HashMap<ClassRelation,ClassRelationRecord>();

    
  	public static void updateObjectDeadTime(int objectId , int time){
  	    if( objects.containsKey(objectId)){
   		//update the objectCount through life time of software
		int lastEnd =  objects.get(objectId).getDead();
		for(int i = lastEnd + 1 ; i != time ; i ++){ 
		     objectCount.set(i,  objectCount.get(i)+1); 
		}
		 objects.get(objectId).setDead(time);
   	    }
   	}
   	
  	
  	/**
  	 * Check If objectId does created by sentences in trace file.
  	 * @param objectId
  	 * @return
  	 */
  	public static boolean isValidObject(int objectId){
   	    if( objects.containsKey(objectId)){
   		return true ;
   	    }else{
   		return false ;
   	    }
   	}
  	
  	
  	/**
   	 * Get class id from an object id.<br />
   	 * 
   	 * @param objectId 
   	 * @return class ID.
   	 * @throws Exception <B>if there is no such object created , throw exception</B>
   	 */
	public static int getClassId(int objectId) throws Exception{
	    if( objects.containsKey(objectId)){
		return  objects.get(objectId).getClassId();
	    }else{
		throw new Exception("Array in static method.");
	    }
	}
	
  	/**
  	 * Add assignment to three data structure in Analyzer: fields, objects, classesRelation.
  	 * @param valueId
  	 * @param fieldId
  	 * @param ownerId
  	 * @param time
  	 * @param isContainer
  	 * @throws Exception
  	 */
  	public static void assignment(int valueId , int fieldId , int ownerId, 
  					int time , boolean isContainer) throws Exception{
	    if( isValidObject(valueId)){
		updateObjectDeadTime(ownerId,time); 
		if(ownerId != valueId){  //avoid update dead time of the same object for twice
		     updateObjectDeadTime(valueId,time);
		}

		
		ClassRelation tmpClassRelation= 
			new ClassRelation(getClassId(ownerId), getClassId(valueId),fieldId);
  	
		
		//Add the assignment to classesRelations 
		if(! classesRelations.containsKey(tmpClassRelation)){//Make sure certain relationship has been included in classesRelation
		    classesRelations.put(tmpClassRelation, 
			    new ClassRelationRecord(fieldId));
		}

		classesRelations.get(tmpClassRelation)
			.objectRelationList
			.add(new HoldingPair(ownerId,valueId));//Add objects pair into classesRelation

		
		if(isContainer){//Mark the multiplicity property
		    classesRelations.get(tmpClassRelation).setMultiplicity();
		}
		
		//Add the assignment to objects.
		 objects.get(ownerId).getOwningObjects().add(new AssignmentRecord(valueId,time,fieldId));
		 objects.get(valueId).getOwnedByObjects().add(new AssignmentRecord(ownerId,time,fieldId));
		 
		//Add the assignment to fields
		LinkedList<ObjectInFields> tmpField = fields.get(fieldId);
		ObjectInFields tmpObjectInFields =  
			  	new ObjectInFields(valueId,getClassId(ownerId), getClassId(valueId));
		if(tmpField.contains(tmpObjectInFields)){
		    tmpField.get(tmpField.indexOf(tmpObjectInFields)).
		    		add(new AssignmentRecord(ownerId, time,fieldId));
		}else{
		    tmpObjectInFields.add(new AssignmentRecord(ownerId, time,fieldId));
		    tmpField.add(tmpObjectInFields);
		}
	    }else{
		//This class should be ignored as its construction can not be traced .
		//It's maybe a class from java.lang and so on
	    }
	}
  	
  	
    	/**
	 * Analyze lifetime relationships for every class-(field)-class pair which exist <br />
	 * in the program.<br />
	 * There are four kinds of lifetime :<br/>
	 * 1.owner hold the value
	 * 2.value's born is early than owner.
	 * 3.value's dead is later than owner.
	 * 4.value hold the owner
	 * @param classesRelation the information of life time type will be held by an array in classesRelation
	 * @param objects  provide the life time of every object
	 */
	public static void analyzeLifeTimeRelationships(){
	    for (ClassRelationRecord lifeRelationRecord : classesRelations.values()) {
		Iterator<HoldingPair> it = lifeRelationRecord.objectRelationList.iterator();
		while(it.hasNext()){
		    HoldingPair pair = it.next();
		    int ownerBornTime = objects.get( pair.getOwnerObjectId()).getBorn();
		    int ownerDeadTime = objects.get( pair.getOwnerObjectId()).getDead();
		    
		    int valueBornTime = objects.get( pair.getValueObjectId()).getBorn();
		    int valueDeadTime = objects.get( pair.getValueObjectId()).getDead();

		    lifeRelationRecord.setLifeTimeTypeOnObjectLevel(ownerBornTime, ownerDeadTime, valueBornTime, valueDeadTime);
		}
	    }
	    
	    for (ClassRelationRecord excluRelationRecord : classesRelations.values()) {
		excluRelationRecord.setLifetimeOnClassLevel();
	    }
	}
	
	
	/**
	 * Analyze the property of exclusivity and transitivity for every class.
	 * As we can not get the accurate dead time of an object , it's difficult to get such a right data.
	 */
	public static void analyzeExclusivityAndTransferal(){
	    //Loop in all fields. indexOfField can not be ignored since it's a identification of field
	    for(int indexOfField = 0 ; indexOfField < fields.size(); indexOfField++){
		LinkedList<ObjectInFields> objectListBelongToAField = fields.get(indexOfField);
		
		//Loop all objects as child object in certain field
		for(ObjectInFields tmpObjectOwnedByList : objectListBelongToAField){
		    ClassRelation tmpClassRelation = 
			    new ClassRelation(tmpObjectOwnedByList.getOwnerClassId(),
				    		tmpObjectOwnedByList.getValueClassId(),
				    		indexOfField);
		 
		    if(tmpObjectOwnedByList.size()== 0){
			 //Such relationships haven't been created. We will ignore it.
		    }else if(tmpObjectOwnedByList.size() == 1){ 
			//If there is only one owner for object in this field  
			//We can check if this object has only one owner ever( in objects)
			if(objects.get(tmpObjectOwnedByList.getObjectId())
					.getOwnedByObjects().size() == 1){
			    classesRelations.get(tmpClassRelation)
			    		.setExclusivityOnObjectLevel(ClassRelationRecord.EXCLUSIVITY_GLOBAL);
			}else{
			    classesRelations.get(tmpClassRelation)
			    		.setExclusivityOnObjectLevel(ClassRelationRecord.EXCLUSIVITY_LOCAL);
			}
		    }else{
			boolean transferalFlag = true;
			//loop for all owners ,checking for transferring
			for(int j = 0 ; j != tmpObjectOwnedByList.size()-1 ; j++){
			    int formerOwnerId = tmpObjectOwnedByList.get(j).getObjectId();
			    int latterOwnerAssignTime = tmpObjectOwnedByList.get(j+1).getTime();
			    int fieldId = tmpObjectOwnedByList.get(j).getFieldId();
			    
			    ObjectRecord formerOwner = objects.get(formerOwnerId);
			    
			    boolean transferalFlag2 = false;
			    
			    //get the start index for the object assigned to 
			    //owner in the owningObjects list. 
			    int index =formerOwner.getOwningObjects()
				    .indexOf(new AssignmentRecord(tmpObjectOwnedByList.getObjectId(),
					    tmpObjectOwnedByList.get(j).getTime(),
					    fieldId)) ;
			    //traverse all assignment of the formerOwner , find out the same field , and compare the time.
			    while( (++ index) != formerOwner.getOwningObjects().size()){
				
				AssignmentRecord tmpAssignmentRecord = formerOwner.getOwningObjects()
										.get(index);
				if(tmpAssignmentRecord.getFieldId() ==  fieldId){
				    //TODO 新的putfield的id不是原来的那个obj的id。
				    if(tmpAssignmentRecord.getTime() < latterOwnerAssignTime){
					transferalFlag2 = true;
				    }
				    break;
				}
			    }
			    if(!transferalFlag2){
				transferalFlag = false;
			    }
			}
			if(transferalFlag){
			    classesRelations.get(tmpClassRelation)
			    		.setExclusivityOnObjectLevel(ClassRelationRecord.EXCLUSIVITY_TRANSFER);
			}else{
			    classesRelations.get(tmpClassRelation)
			    		.setExclusivityOnObjectLevel(ClassRelationRecord.EXCLUSIVITY_NO);
			}

		    } 
		 
		}
		 
	    }
	    
	    for (ClassRelationRecord excluRelationRecord : classesRelations.values()) {
		excluRelationRecord.setExclusivityOnClassLevel();
	    }
	}
}
