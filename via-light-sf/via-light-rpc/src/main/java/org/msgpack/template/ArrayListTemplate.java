package org.msgpack.template;

/**
 * Created by stereo on 16-8-5.
 */

import org.msgpack.MessageTypeException;
import org.msgpack.TypeEnum;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.msgpack.util.ClassTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArrayListTemplate<T> extends NewAbstractTemplate<List<T>> {

    private static final Logger logger = LoggerFactory.getLogger(ArrayListTemplate.class);

    @Override
    public void write(Packer pk, List<T> v, boolean required)
            throws IOException {
        if (!(v instanceof List)) {
            if (v == null) {
                if (required) {
                    logger.error("Attempted to write null");
                    throw new MessageTypeException("Attempted to write null");
                }
                pk.writeNil();
                return;
            }
            logger.error("Attempted to write null");
            throw new MessageTypeException("The " + v.getClass().getName() +" is not a List. ");
        }
        Object valueArr[] = this.toArray(v);
        if(valueArr != null){
            pk.writeArrayBegin(v.size()+2);
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
            pk.writeArrayBegin(v.size()+1);
            pk.write(false);
            for(T t : v){
                writeValue(pk, t);
            }
        }
        pk.writeArrayEnd();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> read(Unpacker u, List<T> to, boolean required)
            throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        int n = u.readArrayBegin();
        if (to == null) {
            to = new ArrayList<T>(n);
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
            for(int i = 0; i < n-2; i++){
                to.add((T)template.read(u, null));
            }
        } else {
            for(int i = 0; i < n-1; i++){
				/*
	        	 * 依据序列化格式反序列化value值
	        	 */
                T val = (T)readValue(u);
                to.add(val);
            }
        }
        u.readArrayEnd();
        return to;
    }

    public ArrayListTemplate(TemplateRegistry registry){
        super(registry);
    }

//    private static ArrayListTemplate instance = new ArrayListTemplate();
//
//    private ArrayListTemplate(){}
//
//    public static ArrayListTemplate getInstance(){
//        return instance;
//    }
}
