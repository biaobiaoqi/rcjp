package fyp.instrument;


import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * This class implements ClassFileTransformer and override transform method<br/>
 * which can change the byte code that transfers to JVM . What's more , pr-<br/>
 * emain method is a static entrance for program using the transform method.<br/>
 * In this way , byte code is instrumented. <br />
 * The way to using this class is add " -javaagent:ASMInstrument.jar" when <br/>
 * run any java program.(ASMInstrument.jar has got Premain-Class: fyp.inst-<br/>
 * rument.ASMAgent in the manifest file). 
 * @author biaobiaoqi
 * @version 2.10
 */

public class ASMAgent implements ClassFileTransformer{
	
    /**
     * The entrance for bytecode instrument.
     * @param agentArgs
     * @param inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
	Trace.beginTrace(); // it's important for trace files
	inst.addTransformer(new ASMAgent());
    }

	@Override
	public byte[] transform(ClassLoader loader, String className,Class<?> classBeingRedefined, 
					ProtectionDomain protectionDomain,byte[] classfileBuffer)
					throws IllegalClassFormatException {
	    byte[] retVal = null;
	    if(isInstrumentable(className)){
		//Using ASM framework to instrument Java bytecode
		try{
		    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		    ASMClassAdapter mca = new ASMClassAdapter(cw);
		    ClassReader cr = new ClassReader(classfileBuffer);
		    cr.accept(mca, 0);
		    retVal = cw.toByteArray();
		}catch(Exception e){
		    retVal = classfileBuffer; //In case of exception.
		   //TODO if any exception occured , output certain class file.
		    /*try{ 
		    File f = new File("/home/biaobiaoqi/classfiles/" +className.replace('/', '.')+".class");
		    if(!f.exists()){
			if(!f.createNewFile()){
			    throw new Exception("文件不存在，创建失败！");
			}
		    }
		    FileOutputStream file = new FileOutputStream(f);
			 		
		    file.write( retVal);
		    file.close();
		    }catch (Exception e1) {
			System.out.println(className+" "+e1.toString());
		    }*/
		}
		try{ 
		    File f = new File("/home/biaobiaoqi/classfiles/" +className.replace('/', '.')+".class");
		    if(!f.exists()){
			if(!f.createNewFile()){
			    throw new Exception("文件不存在，创建失败！");
			}
		    }
		    FileOutputStream file = new FileOutputStream(f);
			 		
		    file.write( retVal);
		    file.close();
		    }catch (Exception e1) {
			System.out.println(className+" "+e1.toString());
		    }
	    }else{
		retVal = classfileBuffer ;
	    }
	    return retVal;
	}
	
	
	 
	    /**
	     * Check if certain class
	     * @param className. In "java.lang.String" style
	     * @return
	     */
	    public static boolean isInstrumentable(String className) 
	    {
			className = className.replace('.', '/');
			return ! (isSystemClass(className) || isInstrumentClass(className));
	    }
		
	    /** 
	     * Check if a class is a system class, based on the package name.
	     * System classes should not be instrumented.
	     * @param className The (fully-qualified) name of the class
	     * @return True if the class is a system class.
	     * Got these by examining rt.jar from the JRE libraries.
	     */
	    public static boolean isSystemClass(String className) {
		return 
			className.startsWith("com/sun")
			|| className.startsWith("java/")
			|| className.startsWith("javax/")
			|| className.startsWith("org/ietf")
			|| className.startsWith("org/omg")
			|| className.startsWith("org/w3c")
			|| className.startsWith("org/xml")
			|| className.startsWith("sun/")
			|| className.startsWith("sunw/")
			|| className.startsWith("org/objectweb/asm");
	    }
		
	    /**
	     * Check if a class is from fyp package. Classes from this package should not be instrumented.
	     * @param className
	     * @return
	     */
	    public static boolean isInstrumentClass(String className) {
		return className.startsWith("fyp"); 
	    }
	    
}
