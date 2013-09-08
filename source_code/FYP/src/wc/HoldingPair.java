package fyp.analysis;


public class HoldingPair{
	private int ownerObjectId ;
	private int valueObjectId ;
	
	public HoldingPair(int owner, int value){
	    ownerObjectId = owner ;
	    valueObjectId = value ;
	}
	
	public int getOwnerObjectId(){
	    return ownerObjectId;
	}
	
	public int getValueObjectId(){
	    return valueObjectId;
	}
}