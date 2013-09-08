package fyp.analysis;


public class AssignmentRecord {
    private int fieldId ;
    private int objectId ;
    private int timeRecord ;
  
    
    public AssignmentRecord(int objectId, int timeRecord ,int fieldId){
	this.objectId = objectId;
	this.timeRecord = timeRecord;
	this.fieldId = fieldId;
    }
    
    public int getFieldId(){
	return fieldId;
    }
    
    public int getObjectId(){
	return objectId;
    }
    
    public int getTime(){
	return timeRecord;
    }
    
    
    
    @Override
    public boolean equals(Object obj) {
	if(   ((AssignmentRecord)obj).fieldId == this.fieldId   
		&& ((AssignmentRecord)obj).objectId == this.objectId
		&& ((AssignmentRecord)obj).timeRecord == this.timeRecord ){
	    return true;
	}else{
	    return false;
	}
	
    }

    @Override
    public int hashCode() {
	int hash = 7 ;
	hash = hash*31 + fieldId;
	hash = hash*31 + objectId;
	hash = hash*31 + timeRecord;
	return hash ;
    }

    
}
