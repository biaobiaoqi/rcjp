package fyp.analysis;

import java.util.ArrayList;
import java.util.List;

public class ObjectRecord   {
    private int classId;
    private int born ;
    private int dead ;

    private List<AssignmentRecord> owningObjects = new ArrayList<AssignmentRecord>(); 
    private List<AssignmentRecord> ownedByObjects = new ArrayList<AssignmentRecord>();
     
    
    public ObjectRecord(int time, int id , int classId) {
	setBorn(time);
	setDead(time);
	this.classId = classId;
    }	
    
    public int getClassId(){
	return classId;
    }
	
    public int getBorn(){
	return born ;
    }
	
    public int getDead(){
	return dead;
    }
	
    public void setDead(int dead){
	this.dead = dead ;
    }

    public void setBorn(int born) {
	this.born = born ;
    }
    
    public List<AssignmentRecord> getOwningObjects(){
	return owningObjects;
    }
    
    public List<AssignmentRecord> getOwnedByObjects(){
	return ownedByObjects;
    }
 
}