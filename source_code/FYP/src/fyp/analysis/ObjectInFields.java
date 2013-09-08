package fyp.analysis;

import java.util.LinkedList;
import java.util.List;

public class ObjectInFields{
    private int parentClassId ;
    private int childClassId ;
    private int objectId;
    private List<AssignmentRecord> referencedObjects = new LinkedList<AssignmentRecord>(); 
	
    public ObjectInFields(int objectId ,int parentClassId , int childClassId) {
	this.objectId = objectId;
	this.parentClassId = parentClassId ;
	this.childClassId = childClassId ;
    }
    
    
    public int getOwnerClassId(){
	return this.parentClassId;
    }

    public int getValueClassId(){
	return this.childClassId;
    }
    
    public int getObjectId(){
	return objectId;
    }
    
    public int size(){
	return referencedObjects.size();
    }
    
    public boolean contain(int id){
	for(AssignmentRecord relRec : referencedObjects){
	    if(relRec.getObjectId() == id ){
		return true;
	    }
	}
	return false;
    }
	
    public AssignmentRecord get(int i){
	return referencedObjects.get(i);
    }
	
    public void add(AssignmentRecord relRec){
	referencedObjects.add(relRec);
    }
	

    @Override
    public boolean equals(Object obj) {
	if(((ObjectInFields)obj).parentClassId == this.parentClassId 
   		&& ((ObjectInFields)obj).childClassId == this.childClassId
   		&& ((ObjectInFields)obj).objectId == this.objectId){
   	    return true ;
   	}else{
   	    return false ;
   	}
    }


    @Override
    public int hashCode() {
	int hash = 7 ;
	hash = hash*31 + parentClassId;
	hash = hash*31 + childClassId;
	hash = hash*31 + objectId;
	return hash ;
    }
    
    

}
