package fyp.analysis;

public class ClassRelation {
    private int ownerClassId ;
    private int valueClassId ;
    private int fieldId ;
    
    public ClassRelation(int ownerClassId, int valueClassId , int fieldId){
   	this.ownerClassId = ownerClassId ;
   	this.valueClassId = valueClassId ;
   	this.fieldId = fieldId ;
    }
       
       
   /*
    public ClassRelation(int fieldId){
   	this.fieldId = fieldId ;
    }*/
       
    public void setValueClassId(int valueClassId){
   	this.valueClassId = valueClassId;
    }
    
    public int getOwnerClassId(){
	return ownerClassId;
    }
       

    public int getValueClassId(){
	return valueClassId;
    }
    
    public int getFieldId(){
	return fieldId;
    }

    
    @Override
    public boolean equals(Object obj) {
	if(((ClassRelation)obj).ownerClassId == this.ownerClassId 
   		&& ((ClassRelation)obj).valueClassId == this.valueClassId
   		&& ((ClassRelation)obj).fieldId == this.fieldId){
   	    return true ;
   	}else{
   	    return false ;
   	}
    }


    @Override
    public int hashCode() {
	int hash = 7 ;
	hash = hash*31 + ownerClassId;
	hash = hash*31 + valueClassId;
	hash = hash*31 + fieldId;
	return hash ;
    }
    
    
   	
}
