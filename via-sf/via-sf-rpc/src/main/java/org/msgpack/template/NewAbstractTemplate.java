package org.msgpack.template;

import org.msgpack.MessageTypeException;
import org.msgpack.TypeEnum;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.msgpack.util.ClassLoaderUtils;
import org.msgpack.util.ClassTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by stereo on 16-8-5.
 */
public abstract class NewAbstractTemplate<T> extends AbstractTemplate<T> {

    private static final Logger logger = LoggerFactory.getLogger(NewAbstractTemplate.class);

    protected StringTemplate stringTemplate = StringTemplate.getInstance();

    protected TemplateRegistry registry;

    public NewAbstractTemplate(TemplateRegistry registry){
        this.registry = registry;
    }

    protected void writeValue(Packer pk, Object v) throws IOException {
        if(v == null){
            pk.writeArrayBegin(1);
            pk.writeNil();
            pk.writeArrayEnd();
            return;
        }
        pk.writeArrayBegin(2);
        Class<?> valueClass = v.getClass();
        String valueIndex = TypeEnum.getIndex(valueClass);
        //非基本类型
        if(valueIndex == null){
            String valueClassName = ClassTypeUtils.getTypeStr(valueClass);
            pk.write(valueClassName);
            Template template;
            try {
                template = registry.lookup(valueClass);
            } catch(MessageTypeException e){
                throw e;
            }
            template.write(pk, v);
        } else {
            pk.write(valueIndex);
            pk.write(v);
        }
        pk.writeArrayEnd();
    }

    protected Object readValue(Unpacker u) throws IOException {
        Class<?> valueClass = null;
        Template valTemplate = null;
        int len = u.readArrayBegin();
        if(len == 1){
            u.readArrayEnd();
            return null;
        }
        Object value = null;
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
            try {
                valTemplate = registry.lookup(valueClass);
            } catch (MessageTypeException e){
                throw e;
            } finally {
                if(valTemplate != null){
                    if(TypeEnum.getIndex(valueClass) != null){
                        value = valTemplate.read(u, null);
                    } else {
                        try {
                            value = valTemplate.read(u, ClassLoaderUtils.newInstance(valueClass));
                        }catch(Exception e){
                            value = valTemplate.read(u, null);
                        }
                    }
                } else {
                    logger.error("Could not find the template:" + valueClass.getName());
                    throw new MessageTypeException("Serializer data error occurred. Could not find the template:" + valueClass.getName());
                }
            }
        }
        u.readArrayEnd();
        return value;
    }

    protected Object[] toArray(Collection coll){
        if(coll.isEmpty()){
            return null;
        }
        Iterator it = coll.iterator();
        Class elementClass = null;
        if(it.hasNext()){
            Object value = it.next();
            if(value == null){
                return null;
            }
            elementClass = value.getClass();
        }
        Object[] targetArray=(Object[]) Array.newInstance(elementClass, coll.size());
        try{
            Object[] myArray = coll.toArray(targetArray);
            return myArray;
        } catch(Exception e){
            return null;
        }
    }
}
