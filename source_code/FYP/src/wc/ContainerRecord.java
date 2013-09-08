package fyp.analysis;

public class ContainerRecord {
    private int ownerId ;
    private int fieldId ;

    public void setOwnerField(int ownerId, int fieldId){
	this.ownerId = ownerId ;
	this.fieldId = fieldId ;
    }
    
    public int getOwnerId(){
	return ownerId;
    }
    
    public int getFieldId(){
	return fieldId;
    }
    

}
