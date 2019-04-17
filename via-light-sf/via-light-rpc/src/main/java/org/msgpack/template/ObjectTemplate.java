package org.msgpack.template;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 自定义Object序列化模板
 */
public class ObjectTemplate extends NewAbstractTemplate<Object> {
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectTemplate.class);
	
	@Override
	public void write(Packer pk, Object value, boolean required) throws IOException {
		if (value == null) {
            if (required) {
            	logger.error("Attempted to write null");
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
		writeValue(pk, value);
	}

	@Override
	public Object read(Unpacker u, Object to, boolean required)
			throws IOException {
		if (!required && u.trySkipNil()) {
            return null;
        }
		to = readValue(u);
    	return to;
	}

	public ObjectTemplate(TemplateRegistry registry)
	{
		super(registry);
	}

	//private static ObjectTemplate instance = new ObjectTemplate();
	
	//private ObjectTemplate(){}
	
	//public static ObjectTemplate getInstance(){
	//	return instance;
	//}
}
