

package fyp.analysis;

import java.util.LinkedList;

/**
 * a record unit for lifetime relationship between two classes.
 * @author biaobiaoqi
 * @version 2.13
 */
public class ClassRelationRecord {
    public static int EXCLUSIVITY_NO = 0;
    public static int EXCLUSIVITY_TRANSFER = 1;
    public static int EXCLUSIVITY_GLOBAL = 2;
    public static int EXCLUSIVITY_LOCAL = 3;
 
    public int fieldId ;
    public LinkedList<HoldingPair>  objectRelationList = new LinkedList<HoldingPair>();
    
    
    //record the number of cases in each life time case
    public int[] lifeTimeTypeCount = {0,0,0,0};
    public int lifeTimeType = 0;
    
    //record the number of cases in each exclusivity case  
    //EXCLUSIVITY_NO means it's not exclusivity.
    //EXCLUSIVITY_TRANSFER means assignment can be transfered between objects.
    //EXCLUSIVITY_GLOBAL means object is owned by only one object ever.
    //EXCLUSIVITY_LOCAL means object is owned by only one object in certain field and be owned by object from other field.
    public int[] exclusivityCount = {0,0,0,0}; 
    public int exclusivity = 0;
    
    
    public boolean multiplicity = false;
    
    public ClassRelationRecord(int fieldId){
	this.fieldId = fieldId;
    }
    
    public void setLifeTimeTypeOnObjectLevel(int ownerBornTime , int ownerDeadTime , int valueBornTime , int valueDeadTime){
	 if(ownerBornTime < valueBornTime){
	     if(ownerDeadTime > valueDeadTime){  //case 1
		 lifeTimeTypeCount[0] ++;
	     }else{ //case 3
		 lifeTimeTypeCount[2] ++;
	     }
	 }else{
	     if(ownerDeadTime > valueDeadTime){ //case 2
		 lifeTimeTypeCount[1] ++;
	     }else{  // case 4
		 lifeTimeTypeCount[3] ++;
	     }
	 }
    }
    
    public void setExclusivityOnObjectLevel(int index){
	this.exclusivityCount[index] ++ ;
    }
    
    public void setMultiplicity(){
	this.multiplicity = true;
    }
    
    public boolean getMultiplicity(){
	return this.multiplicity;
    }
    
    public void setExclusivityOnClassLevel(){
	for(int i = 0 ; i !=4 ; i++){
	    if(exclusivityCount[i] != 0){
		exclusivity = i;
	    }
	}
    }
    
    public void setLifetimeOnClassLevel(){
	    int tmp = 0;
	    for(int i = 0; i != 4 ; i ++){
		if(lifeTimeTypeCount[i] != 0){ //There is at least one in this type
		    tmp = tmp *2 + 1;
		}else{ //There is non in this type
		    tmp = tmp *2;
		}
	    }
	    if(tmp == 8){ //only type 1.
		lifeTimeType = 0;
	    }else if(tmp == 4 || tmp == 12){ //Only type 2 or type 1 and type 2 , resulting in type 2
		lifeTimeType = 1;
	    }else if(tmp == 2 || tmp == 10){ //Only type 3 or type 3 and type 1, resulting in type 3
		lifeTimeType =2;
	    }else{ //All other cases result in type 4.
		lifeTimeType = 3;
	    }

    }
}
