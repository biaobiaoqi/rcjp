package fyp.instrument;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A MethodAdapter for constructors . Make sure an object has been recorded in trace file
 * as well as in objects(data structure in Trace.java) till the end of the constructor 
 * (check at return instruction).<br />
 * The time mark of this instruction can be taken as the born time of a object .
 * @author biaobiaoqi
 * @version 2.11
 */
public class TraceConstructorMethodAdapter extends MethodAdapter {
    
    /**
     * Make sure an object has not been recorded in trace file
     * till the end of the constructor (check at return instruction).
     * It's a visitor design model .
     * @param arg0
     */
	public TraceConstructorMethodAdapter(MethodVisitor arg0) {
		super(arg0);
	}

	
	@Override
	public void visitInsn(int opcode) {
	    //Make sure an object has not been recorded in trace file
	    //till the end of the constructor (check at return instruction).
	    //It's a visitor design model .
	    if(opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN || opcode == Opcodes.FRETURN
				|| opcode == Opcodes.DRETURN || opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN){
		//put the object into stack as parameter for the following method call
		mv.visitVarInsn(Opcodes.ALOAD, 0);  
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "fyp/instrument/Trace",
							"makeSureTraceObjectConstructor", "(Ljava/lang/Object;)V");
	    }
	    super.visitInsn(opcode);
	}

}
