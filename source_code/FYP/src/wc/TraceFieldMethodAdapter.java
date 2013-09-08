package fyp.instrument;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Trace the put_field instruction and get_field instruction. 
 * If the object construction has not been traced yet , it will 
 * be traced before the put_field. To make analysis straightforward,
 * some types are ignored : Primitive types , String and boxed types.
 * @author biaobiaoqi
 * @version 2.11
 */

public class TraceFieldMethodAdapter extends MethodAdapter{

    public TraceFieldMethodAdapter(MethodVisitor arg0) {
	super(arg0);
    }

    /**
     * Trace the put_field instruction and get_field instruction.
     * Ignore the following types :<br /> 
     * Primitive types , String and boxed types.
     */
    @Override
    public void visitFieldInsn(int opcode, String owner,
            					String fieldName, String desc) {
	//"$" exclude inner class case: 
	//there will be a put_field which can not be traced in inner class constructor before <init>
	if (opcode == Opcodes.PUTFIELD && !fieldName.contains("$") ){ 
	    if(isIgnoreType(desc)){
		super.visitFieldInsn(opcode, owner, fieldName, desc);
		return;
	    }else if(isArray(desc)){   //assign array(excluding primitive types and boxed types and String and so on.)
		 mv.visitInsn(Opcodes.DUP2);
		 mv.visitLdcInsn(fieldName);
		 mv.visitInsn(Opcodes.ICONST_1); // true
		 mv.visitMethodInsn(Opcodes.INVOKESTATIC, "fyp/instrument/Trace", "traceAssignment", 
						"(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Z)V");
	    }else{//assign normal objects
		    mv.visitInsn(Opcodes.DUP2);
		    mv.visitLdcInsn(fieldName);
		    mv.visitInsn(Opcodes.ICONST_0); // false
		    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "fyp/instrument/Trace", "traceAssignment", 
						"(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Z)V");
	    }
	}else if (opcode == Opcodes.GETFIELD ){
	    if(isIgnoreType(desc)){
		super.visitFieldInsn(opcode, owner, fieldName, desc);
		return;
	    }
	    mv.visitInsn(Opcodes.DUP);
	    mv.visitLdcInsn(fieldName);
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "fyp/instrument/Trace", "traceGetField", 
					"(Ljava/lang/Object;Ljava/lang/String;)V");
	}else if (opcode == Opcodes.PUTFIELD && fieldName.contains("$") ){  
	    // it's the first special put_field in a constructor of an inner class.   
	    //TODO 暂时放弃了 inner class的处理。
	}
	super.visitFieldInsn(opcode, owner, fieldName, desc);
    }
	
    
    public boolean isIgnoreType(String desc ){
	return  desc.endsWith("I") || desc.endsWith("J") || desc.endsWith("Z") || desc.endsWith("B")
		|| desc.endsWith("C") || desc.endsWith("D") || desc.endsWith("F") || desc.endsWith("S") 
		|| desc.endsWith("Ljava/lang/String;")
		|| desc.endsWith("Ljava/lang/Integer;") || desc.endsWith("Ljava/lang/Long;")  
		|| desc.endsWith("Ljava/lang/Boolean;") || desc.endsWith("Ljava/lang/Character;") 
		|| desc.endsWith("Ljava/lang/Short;") || desc.endsWith("Ljava/lang/Float;")
		|| desc.endsWith("Ljava/lang/Double;");
    }
    
    
    public boolean isArray(String desc){
	return desc.startsWith("[");
    }

}
