package org.msgpack.template;

import org.msgpack.MessageTypeException;
import org.msgpack.TypeEnum;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.msgpack.util.ClassTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 用于Map参数的序列化和反序列化
 * @param <K>
 * @param <V>
 */
public class HashMapTemplate<K,V> extends NewAbstractTemplate<Map<K, V>> {
	
	private static final Logger logger = LoggerFactory.getLogger(HashMapTemplate.class);
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void write(Packer pk, Map<K, V> v, boolean required)	throws IOException {
        if (v == null) {
            if (required) {
            	logger.error("Attempted to write null");
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
		if (!(v instanceof Map)) {
			logger.error("The " + v.getClass().getName() + " is not a Map.");
            throw new MessageTypeException("The " + v.getClass().getName() + " is not a Map.");
        }
//		if(){
//			
//		}
        Template keyTemplate = null;
        Map<K, V> map = (Map<K, V>) v;
        /*
         * 把map作为一个bean输出。
         */
        int len = 1;
        Object valueArr[] = this.toArray(v.values());
        if(valueArr != null){
        	if(map.size() != 0){
	        	len = map.size() * 2 + 3;
	        }
        	pk.writeArrayBegin(len);
	        pk.write(true);
	        boolean keyFlag = false;
	        Template valueTemplate = null;
	        for (Map.Entry<K, V> pair : map.entrySet()) {
	        	K k = pair.getKey();
	        	V value = pair.getValue();
	        	if(!keyFlag){
	        		/*
	        		 * key和value的class是不变的，在第一条信息中写上key和value的class名称（全路径）
	        		 * 如果修改规范，value的class不变的话，可以放在头部
	        		 */
		    		Class<?> keyClass = k.getClass();
		    		String keyIndex = TypeEnum.getIndex(keyClass);
		    		keyIndex = keyIndex == null?	ClassTypeUtils.getTypeStr(keyClass) : keyIndex;
		    		pk.write(keyIndex);
		        	try {
		    			keyTemplate = this.registry.lookup(keyClass);
		    		} catch(MessageTypeException e){
		    			//CodecUtils.checkAndRegistryClass(keyClass, new HashSet<Class<?>>());
		    			keyTemplate = this.registry.lookup(keyClass);
		    		}
		        	
		        	Class<?> valueClass = value.getClass();
		    		String valueIndex = TypeEnum.getIndex(valueClass);
		    		valueIndex = valueIndex == null?	ClassTypeUtils.getTypeStr(valueClass) : valueIndex;
		    		pk.write(valueIndex);
		        	try {
		    			valueTemplate = this.registry.lookup(valueClass);
		    		} catch(MessageTypeException e){
		    			//CodecUtils.checkAndRegistryClass(valueClass, new HashSet<Class<?>>());
		    			valueTemplate = this.registry.lookup(valueClass);
		    		}
		        	keyFlag = true;
	        	}
	    		keyTemplate.write(pk, k);
	    		valueTemplate.write(pk, value);
	        }
        } else {
	        if(map.size() != 0){
	        	len = map.size() * 2 + 2;
	        }
	        pk.writeArrayBegin(len);
	        pk.write(false);
	        boolean keyFlag = false;
	        for (Map.Entry<K, V> pair : map.entrySet()) {
	        	K k = pair.getKey();
	        	if(!keyFlag){
	        		/*
	        		 * key的class是不变的，在第一条信息中写上key的class名称（全路径）
	        		 * 如果修改规范，value的class不变的话，可以放在头部
	        		 */
		    		Class<?> keyClass = k.getClass();
		    		String keyIndex = TypeEnum.getIndex(keyClass);
		    		keyIndex = keyIndex == null?	ClassTypeUtils.getTypeStr(keyClass) : keyIndex;
		        	//stringTemplate.write(pk, keyIndex);
		    		pk.write(keyIndex);
		        	try {
		    			keyTemplate = this.registry.lookup(keyClass);
		    		} catch(MessageTypeException e){
		    			//CodecUtils.checkAndRegistryClass(keyClass, new HashSet<Class<?>>());
		    			keyTemplate = this.registry.lookup(keyClass);
		    		}
		        	keyFlag = true;
	        	}
	    		keyTemplate.write(pk, k);
	        	
	    		/*
	    		 * value格式为：class全名称，值
	    		 * 先写入class全名称，然后再写入值
	    		 */
	            V value = pair.getValue();
	            writeValue(pk, value);
	        }
        }
        pk.writeArrayEnd();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map<K, V> read(Unpacker u, Map<K, V> to, boolean required)
			throws IOException {
		if (!required && u.trySkipNil()) {
            return null;
        }
		/*
		 * 作为Bean读入，然后把读取的值放入map
		 */
		int len = u.readArrayBegin();
		Map<K, V> map;
		boolean flag = u.readBoolean();
		if (to != null) {
	        map = (Map<K, V>) to;
	        map.clear();
	    } else {
	        map = new HashMap<K, V>(len/2);
	    }
		if(len > 1){
			int n = len/2 -1;
			if(flag){
				String keyIndex = u.readString();
				String valueIndex = u.readString();
				Template keyTemplate = null;
				Template valueTemplate = null;
				if(keyIndex == null){
					logger.error("Serializer data error occurred. Key of class can be not found.");
		    		throw new MessageTypeException("Serializer data error occurred. Key of class can be not found.");
		    	}
				/*
				 * 取得key的class全名称
				 * 然后取得序列化模板
				 */
				Class<?> keyClass = null;
				if(keyIndex.length() < 3){
					keyClass = TypeEnum.getType(keyIndex);
				} else {
					try {
						keyClass = ClassTypeUtils.getClass(keyIndex);
					} catch (Exception e) {
						logger.error("An exception occurred while serializing data:", e);
						throw new MessageTypeException("Serializer data error occurred. Value of class can be not found.");
					}
				}
				try {
					keyTemplate = this.registry.lookup(keyClass);
				} catch(MessageTypeException e){
					//CodecUtils.checkAndRegistryClass(keyClass, new HashSet<Class<?>>());
					keyTemplate = this.registry.lookup(keyClass);
				}
				/*
				 * 取得value的class全名称
				 * 然后取得序列化模板
				 */

				if(valueIndex == null){
					logger.error("Serializer data error occurred. Value of class can be not found.");
		    		throw new MessageTypeException("Serializer data error occurred. Value of class can be not found.");
		    	}
				Class<?> valueClass = null;
				if(valueIndex.length() < 3){
					valueClass = TypeEnum.getType(valueIndex);
				} else {
					try {
						valueClass = ClassTypeUtils.getClass(valueIndex);
					} catch (Exception e) {
						logger.error("An exception occurred while serializing data:", e);
						throw new MessageTypeException("Serializer data error occurred. Value of class can be not found.");
					}
				}
				try {
					valueTemplate = this.registry.lookup(valueClass);
				} catch(MessageTypeException e){
					//CodecUtils.checkAndRegistryClass(valueClass, new HashSet<Class<?>>());
					valueTemplate = this.registry.lookup(valueClass);
				}
				for(int i=0; i < n; i++){
					map.put((K)keyTemplate.read(u, null), (V)valueTemplate.read(u, null));
				}
			} else {
				String keyIndex = u.readString();
				Template keyTemplate = null;
				if(keyIndex == null){
					logger.error("Serializer data error occurred. Key of class can be not found.");
		    		throw new MessageTypeException("Serializer data error occurred. Key of class can be not found.");
		    	}
				/*
				 * 取得key的class全名称
				 * 然后取得序列化模板
				 */
				Class<?> keyClass = null;
				if(keyIndex.length() < 3){
					keyClass = TypeEnum.getType(keyIndex);
				} else {
					try {
						keyClass = ClassTypeUtils.getClass(keyIndex);
					} catch (Exception e) {
						logger.error("An exception occurred while serializing data:", e);
						throw new MessageTypeException("Serializer data error occurred. Value of class can be not found.");
					}
				}
				try {
					keyTemplate = this.registry.lookup(keyClass);
				} catch(MessageTypeException e){
					//CodecUtils.checkAndRegistryClass(keyClass, new HashSet<Class<?>>());
					keyTemplate = this.registry.lookup(keyClass);
				}
		       
		        for(int i=0; i < n; i++){
		        	/*
		        	 * 依据序列化格式反序列化value值
		        	 */
		        	K key = (K)keyTemplate.read(u, null);
		        	V val = (V)readValue(u);
	        		map.put(key, val);
		        }
			}
		}
        u.readArrayEnd();
        return map;
	}

	public HashMapTemplate(TemplateRegistry registry){
		super(registry);
	}
	
//	private static HashMapTemplate instance = new HashMapTemplate();
//	private HashMapTemplate(){}
//	public static HashMapTemplate getInstance(){
//		return instance;
//	}
}
