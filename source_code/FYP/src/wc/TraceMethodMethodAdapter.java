package fyp.instrument;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This MethodAdapter insert invocation of traceMethod(Object) in Trace.
 * java at the begining of a noraml method. In this way , the time of using 
 * this method, as well as the id of owner object , can be traced , considering
 *  as a ecidance of alive for this owner object in lifetime analysis.
 * @author biaobiaoqi
 * @version 2.11
 */
public class TraceMethodMethodAdapter extends MethodAdapter{

	public TraceMethodMethodAdapter(MethodVisitor arg0) {
		super(arg0);
	}

	@Override
	public void visitCode() {
		super.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "fyp/instrument/Trace",
			"traceMethod", "(Ljava/lang/Object;)V");
	}
}