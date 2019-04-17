package org.msgpack.template;

import org.msgpack.MessageTypeException;
import org.msgpack.TypeEnum;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.msgpack.util.ClassTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 自定义Set序列化模板
 * @param <E>
 */
public class HashSetTemplate<E> extends NewAbstractTemplate<Set<E>> {
	
	private static final Logger logger = LoggerFactory.getLogger(HashSetTemplate.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<E> read(Unpacker u, Set<E> to, boolean required) throws IOException {
		if (!required && u.trySkipNil()) {
            return null;
        }
		int n = u.readArrayBegin();
        if (to == null) {
            to = new HashSet<E>(n);
        } else {
            to.clear();
        }
        boolean flag = u.readBoolean();
        if(flag){
        	Template template = null;
			Class<?> valueClass = null;
        	String valueIndex = u.readString();
			if(valueIndex != null){
				valueClass = TypeEnum.getType(valueIndex);
				if(valueClass == null) {
					try {
						valueClass = ClassTypeUtils.getClass(valueIndex);
					} catch (Exception e) {
						logger.error("An exception occurred while serializing data:", e);
						throw new MessageTypeException("Serializer data error occurred. Value of class can be not found.");
					}
				}
			} else {
				throw new MessageTypeException("Serializer data error occurred. The data's format is not right.");
			}
			try {
    			template = registry.lookup(valueClass);
    		} catch(MessageTypeException e){
    			//CodecUtils.checkAndRegistryClass(valueClass, new HashSet<Class<?>>());
    			template = registry.lookup(valueClass);
    		}
			for(int i=0; i < n - 2; i++){
				to.add((E)template.read(u, null));
			}
        } else {
	        for(int i=0; i < n - 1; i++){
	        	/*
	        	 * 依据序列化格式反序列化value值
	        	 */
	        	E val = (E)readValue(u);
	        	to.add(val);
	        }
        }
        u.readArrayEnd();
		return to;
	}

	@Override
	public void write(Packer pk, Set<E> target, boolean required)
			throws IOException {
        if (target == null) {
            if (required) {
            	logger.error("Attempted to write null");
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }

		if (!(target instanceof Set)) {
			logger.error("The " + target.getClass().getName() + " is not a Map.");
            throw new MessageTypeException("The " + target.getClass().getName() + " is not a Map.");
        }
		int size = target.size();
		Object valueArr[] = this.toArray(target);
		if(valueArr != null){
			pk.writeArrayBegin( size + 2);
			pk.write(true);
			boolean isInit = true;
			Template template = null;
			for(Object value : valueArr){
				
				if(isInit){
					isInit = false;
					Class<?> valueClass = value.getClass();
			        String valueIndex = TypeEnum.getIndex(valueClass);
			        //非基本类型
			        if(valueIndex == null){
			        	valueIndex = ClassTypeUtils.getTypeStr(valueClass);
			        }
			        pk.write(valueIndex);
		            try {
		    			template = registry.lookup(valueClass);
		    		} catch(MessageTypeException e){
		    			//CodecUtils.checkAndRegistryClass(valueClass, new HashSet<Class<?>>());
		    			template = registry.lookup(valueClass);
		    		}
				}
				template.write(pk, value);
			}
		} else {
			pk.writeArrayBegin( size + 1);
			pk.write(false);
			for(E t : target){
				writeValue(pk, t);
			}
		}
		pk.writeArrayEnd();
	}

//	private static HashSetTemplate instance = new HashSetTemplate();
//
//	private HashSetTemplate(){}
//
//	public static HashSetTemplate getInstance(){
//		return instance;
//	}

	public HashSetTemplate(TemplateRegistry registry){
		super(registry);
	}
}
