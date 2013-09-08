package fyp.instrument;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Extends from ClassAdapter , which is an adapter for ClassVisitor in ASM framework. 
 * @author biaobiaoqi
 * @version 2.10
 */
public class ASMClassAdapter extends ClassAdapter {
	public ASMClassAdapter(ClassVisitor cv) {
	    super(cv);
	}

	/**
	 * all method should be decorated by PutFieldMethodAdapter;
	 * for constructor , there will be a TraceConstructorMethodAdapter to decorate.
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions){  
	    MethodVisitor mv ;
	    mv = cv.visitMethod(access, name, desc, signature,  exceptions);
	    if (mv != null && name.equals("<init>")){ // Deal with constructors  
		//Make sure construction of object has been traced.
		mv = new TraceConstructorMethodAdapter(mv); 
		
		//Trace array operations
		mv = new TraceArrayMethodAdapter(mv);
		
		//TODO can not use collectionmethodadapter here .....
		// Do not use traceCollectionMethodAdapter to decorate constructors .
		// In a special case : a class inherits from a collection class .  
		// The construction of that object will be recorded twice 
		// as tracecolleactionmethodadapter can trace invokespecial for collections
		
		//Trace put_field instruction . 
		mv = new TraceFieldMethodAdapter(mv);
		
	    }else if( (access & Opcodes.ACC_STATIC) == 0){   //exclude static functions (including <clinit>)
		mv = new TraceCollectionMethodAdapter(mv);  //Trace collection 
		mv = new TraceArrayMethodAdapter(mv);	//trace array operations
		mv = new TraceFieldMethodAdapter(mv); //trace put_field and get_field instructions
		mv = new TraceMethodMethodAdapter(mv); //trace the normal method
	    }
	    else if((access & Opcodes.ACC_STATIC) != 0){//static functions : SHOULD NOT trace owner 
								//of the method as there is no owner object here.
		mv = new TraceCollectionMethodAdapter(mv); //Trace collection operations
		mv = new TraceArrayMethodAdapter(mv); //trace array operations
		mv = new TraceFieldMethodAdapter(mv);//trace put_field and get_field instructions
	    }
		
	    return mv;
	}   
}
