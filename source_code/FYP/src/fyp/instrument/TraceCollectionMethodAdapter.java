package fyp.instrument;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TraceCollectionMethodAdapter extends MethodAdapter	{
	public TraceCollectionMethodAdapter(MethodVisitor mv) {
		super(mv);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
	    if (opcode == Opcodes.INVOKEVIRTUAL){
		if(isCollection(owner)){
		    if(isNoParams(desc)){   //instrument when there is no parameter , need not think about the return value
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(name); //the method name
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
				"fyp/instrument/Trace", "traceCollectionNoParam",
				"(Ljava/lang/Object;Ljava/lang/String;)V");
			super.visitMethodInsn(opcode,  owner,  name,  desc);
		    }else { //instrument when there is some parameters
			int count = countParams(desc);
			//create object array to hold parameters
			mv.visitIntInsn(Opcodes.BIPUSH, count);
			mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
			
			for(int i = count -1 ; i >= 0 ; i--){
			    if(isPrimitiveType(desc,i)){    // turn primitive type into boxed types
				mv.visitInsn(Opcodes.SWAP);
				char c;
				try {
				    c = getPrimitiveType(desc,i);
				    switch(c){
				    	case  'I' :
				    	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", 
				    		    "valueOf", "(I)Ljava/lang/Integer;");
								    	    break;
				    	case  'J' :
				    	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", 
				    		    "valueOf", "(J)Ljava/lang/Long;");
				    	    break;
				    	case  'S' :
				    	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short",
				    		    "valueOf", "(S)Ljava/lang/Short;");
				    	    break;
				    	case  'C' :
				    	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", 
				    		    "valueOf", "(C)Ljava/lang/Character;");
				    	    break;
				    	case  'Z' :
				    	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", 
				    		    "valueOf", "(Z)Ljava/lang/Boolean;");
				    	    break;
				    	case  'F' :
				    	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float",
				    		    "valueOf", "(F)Ljava/lang/Float;");
				    	    break;
				    	case  'D' :
										    	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", 
				    		    "valueOf", "(D)Ljava/lang/Double;");
				    	    break;
				    }
				} catch (Exception e) {
				    e.printStackTrace();
				}
				mv.visitInsn(Opcodes.SWAP);
			    }
						
			    mv.visitInsn(Opcodes.SWAP);
			    mv.visitInsn(Opcodes.DUP2);
			    mv.visitIntInsn(Opcodes.BIPUSH, i); // position
			    mv.visitInsn(Opcodes.SWAP);
			    mv.visitInsn(Opcodes.AASTORE);
			    mv.visitInsn(Opcodes.POP);
						
			}
					
			mv.visitLdcInsn(owner);
			mv.visitLdcInsn(name);
			mv.visitLdcInsn(desc);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "fyp/instrument/Trace",
				"traceCollectionManyParams", 
				"(Ljava/lang/Object;[Ljava/lang/Object;Ljava/lang/String;" +
				"Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
			if(!desc.endsWith("V")){  //cast object to  return type
			    mv.visitTypeInsn(Opcodes.CHECKCAST, getReturnType(desc).replace('.', '/'));
			}

			if(desc.endsWith("I")){  //turn into primitive values
			    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
				    "java/lang/Integer", "intValue", "()I");
			}else if(desc.endsWith("J")){
			    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
				    "java/lang/Long", "longValue", "()J");
			}else if(desc.endsWith("S")){
			    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
				    "java/lang/Short", "shortValue", "()S");
			}else if(desc.endsWith("C")){
			    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
				    "java/lang/Character", "charValue", "()C");
			}else if(desc.endsWith("Z")){
			    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
				    "java/lang/Boolean", "booleanValue", "()Z");
			}else if(desc.endsWith("F")){
			    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
				    "java/lang/Float", "floatValue", "()F");
			}else if(desc.endsWith("D")){
			    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
				    "java/lang/Double", "doubleValue", "()D");
			}
		    }	
		}else{ // it's not a collection
		    super.visitMethodInsn(opcode,  owner,  name,  desc);
		}
			
	    }else if(opcode == Opcodes.INVOKESPECIAL){   // instrument constructor for collections
		if(isCollection(owner)){
		    if( name.equals("<init>")){
			//TODO the order of dup and invokespecial
			super.visitMethodInsn(opcode,  owner,  name,  desc);
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
				"fyp/instrument/Trace", "traceCollectionConstructor", 
				"(Ljava/lang/Object;)V");
		    }else{
			super.visitMethodInsn(opcode,  owner,  name,  desc);  // Do not forget it!!
		    }
		}else{
		    super.visitMethodInsn(opcode,  owner,  name,  desc);
		}
	    }else {  //do not miss this !
			super.visitMethodInsn(opcode,  owner,  name,  desc);
	    }	
	}

	
	public boolean isPrimitiveType(String desc , int pos){
	    String str = getParams(desc);
	    int count = 0 ;
	    for (int i = 0 ; i < str.length() ; i++){
		if (str.charAt(i) == 'L'){
		    count ++ ;
		    i = str.indexOf(';',i);
		    if (count-1 == pos){
			return false ;
		    }
		}else if(str.charAt(i) == '['){

		}else{
		    count ++;
		    if (count-1 == pos){
			return true ;
		    }
		}
	    }
	    return false;
	}
	
	
	public char getPrimitiveType(String desc , int pos)throws Exception{
	    String str = getParams(desc);
	    int count = 0 ;
	    for (int i = 0 ; i < str.length() ; i++){
		if (str.charAt(i) == 'L'){
		    count ++ ;
		    i = str.indexOf(';',i);
		}else if(str.charAt(i) == '['){

		}else{
		    count ++;
		    if (count-1 == pos){
			return str.charAt(i) ;
		    }
		}
	    }
	    throw new Exception();
	}
	
	public boolean isNoParams(String desc){
		return (desc.indexOf('(')+1 == desc.indexOf(')'));
	}
	
	public String getParams(String desc){
		return desc.substring(desc.indexOf('(')+1, desc.indexOf(')'));
	}
	
	public int  countParams(String desc){
	    String str = desc.substring(desc.indexOf('(')+1, desc.indexOf(')'));
	    int count = 0;
	    for (int i = 0 ; i < str.length() ; i++){
		if (str.charAt(i) == 'L'){
		    count ++ ;
		    i = str.indexOf(';',i);
		}else if(str.charAt(i) == '['){

		}else{
		    count ++;
		}
	    }
	    return count ;
	}
	
	public  String getReturnType(String str){
	    String cls = str.substring(str.indexOf(")")+1);
	    if(cls.startsWith("[")){
		//TODO: array
	    }else if(cls.startsWith("L")){
		cls = cls.substring(1,cls.length()-1);
		cls = cls.replace('/', '.');
	    }else{  // primitive types
		if(cls.equals("I"))
		    cls = "java.lang.Integer";
		else if(cls.equals("J"))
		    cls = "java.lang.Long";
		else if(cls.equals("Z"))
		    cls = "java.lang.Boolean";
		else if(cls.equals("C"))
		    cls = "java.lang.Char";
		else if(cls.equals("D"))
		    cls = "java.lang.Double";
		else if(cls.equals("F"))
		    cls = "java.lang.Float";
		else if(cls.equals("S"))
		    cls = "java.lang.Short";
	    }
	    return cls;
	}
	

	public boolean isCollection(String className){
	    className = className.replace('.', '/');
	    return className.endsWith("java/util/HashSet")||className.endsWith("java/util/TreeSet")
		||className.endsWith("java/util/LinkedHashSet")||className.endsWith("java/util/ArrayList")
		||className.endsWith("java/util/ArrayDeque")||className.endsWith("java/util/LinkedList")
		||className.endsWith("java/util/PriorityQueue")||className.endsWith("java/util/HashMap")
		||className.endsWith("java/util/LinkedHashMap")||className.endsWith("java/util/TreeMap")
		||className.endsWith("java/util/Vector")||className.endsWith("HashTable")
		||className.endsWith("java/util/WeakHashMap")||className.endsWith("java/util/IdentityHashMap")
		||className.endsWith("java/util/concurrent/CopyOnWriteArrayList")
		||className.endsWith("java/util/concurrent/CopyOnWriteArraySet")
		||className.endsWith("java/util/EnumSet")||className.endsWith("java/util/EnumMap")
		||className.endsWith("java/util/concurrent/ConcurrentLinkedQueue")
		||className.endsWith("java/util/concurrent/LinkedBlockingQueue")
		||className.endsWith("java/util/concurrent/ArrayBlockingQueue")
		||className.endsWith("java/util/concurrent/PriorityBlockingQueue")
		||className.endsWith("java/util/concurrent/DelayQueue")
		||className.endsWith("java/util/concurrent/SynchronousQueue")
		||className.endsWith("java/util/concurrent/LinkedBlockingDeque")
		||className.endsWith("java/util/concurrent/ConcurrentHashMap")
		||className.endsWith("java/util/concurrent/ConcurrentSkipListSet")
		||className.endsWith("java/util/concurrent/ConcurrentSkipListMap")
		||className.endsWith("java/util/List")||className.endsWith("java/util/Map")
		||className.endsWith("AbstractCollection")||className.endsWith("AbstractSet ")
		||className.endsWith("AbstractList ")||className.endsWith("AbstractSequentialList")
		||className.endsWith("AbstractQueue")||className.endsWith("AbstractMap");
	}
}
