package fyp.instrument;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Trace creation , operations of loading and storing members  of array.
 * @author biaobiaoqi
 * @version 2.3
 */
public class TraceArrayMethodAdapter extends MethodAdapter {

	public TraceArrayMethodAdapter(MethodVisitor arg0) {
		super(arg0);
	}
	
	/**
	 * Instrument aaload instruction and aastore. <br />
	 * Primitive types are excluded ,as well as String and boxed types.
	 * Object in the array will be traced.
	 */
	@Override
	public void visitInsn(int opcode) {
	    if(opcode ==Opcodes.AALOAD ){ //
		super.visitInsn(opcode);
		mv.visitInsn(Opcodes.DUP);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "fyp/instrument/Trace",
					"traceObjectUse", "(Ljava/lang/Object;)V");
	    }else if(opcode ==Opcodes.AASTORE ){  //execute the instruction by relected method
		//TODO 完成obj 和 array的赋值关系。【关键】
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "fyp/instrument/Trace",
			"traceStoreArray","([Ljava/lang/Object;ILjava/lang/Object;)V");
	    }else{
		super.visitInsn(opcode);
	    }
	}

	//TODO
	/**
	 * instrument multinewarray instruction
	 */
	@Override
	public void visitMultiANewArrayInsn(String desc, int dims){
		super.visitMultiANewArrayInsn(desc, dims);
		//duplicate the object reference of the array to invoke the traceNewArray method.
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn(desc);
		mv.visitIntInsn(Opcodes.BIPUSH, dims);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "fyp/instrument/Trace", 
					"traceMultiArray", "(Ljava/lang/Object;Ljava/lang/String;I)V");
	}

	
	/**
	 * instrument anewarray instruction.
	 * @param type It's the class type of the array. 
	 */
	@Override
	public void visitTypeInsn(int opcode, String type) {
	    // temporary do not use int size .for verify error:expecting integer in stack
		
		
		//TODO：取消 boxed type 和 String 的trace
		if (opcode == Opcodes.ANEWARRAY && !isIgnoreType(type)){
		    mv.visitInsn(Opcodes.DUP);  //duplicate the size(int)
		    super.visitTypeInsn(opcode, type);
		    
		    //duplicate the object reference of the array and insert under size(int)
		    mv.visitInsn(Opcodes.DUP_X1);
		    mv.visitLdcInsn(type);
		    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "fyp/instrument/Trace", "traceNewArray",
			    	"(ILjava/lang/Object;Ljava/lang/String;)V");
		}else{
		    super.visitTypeInsn(opcode, type);
		}
	}
	
	
	/**
	 * instrument newarray instruction 
	 */
	@Override
	public void visitIntInsn(int opcode, int type) {
		super.visitIntInsn(opcode, type);
		
		//TODO 取消 primitive type 的trace
		if (opcode == Opcodes.NEWARRAY ){
			//ignore such primitive types!
		}
	}
	

	    /**
	     * Ignored type include : primitive types , String , boxed types .
	     * @param type
	     * @return
	     */
	    public static boolean isIgnoreType(String type){
		/*return ( type.equals("I")||type.equals("J") ||type.equals("Z") ||type.equals("C")  ||type.equals("B") 
			||type.equals("D")||type.equals("F")||type.equals("S")||type.equals("java/lang/String")
			||type.equals("java/lang/Integer")||type.equals("java/lang/Long")
			||type.equals("java/lang/Short")||type.equals("java/lang/Character")
			||type.equals("java/lang/Boolean")||type.equals("java/lang/Double")
			||type.equals("java/lang/Float") )  ;	*/
		return  type.endsWith("I") || type.endsWith("J") || type.endsWith("Z") || type.endsWith("B") 
			|| type.endsWith("C") || type.endsWith("D") || type.endsWith("F") || type.endsWith("S") 
			|| type.endsWith("java/lang/String")
			|| type.endsWith("java/lang/Integer") || type.endsWith("Ljava/lang/Long")  
			|| type.endsWith("java/lang/Boolean") || type.endsWith("Ljava/lang/Character") 
			|| type.endsWith("java/lang/Short") || type.endsWith("Ljava/lang/Float")
			|| type.endsWith("java/lang/Double");
	    }
	    

}
