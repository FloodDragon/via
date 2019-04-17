package org.msgpack.template.builder;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.IntegerTemplate;
import org.msgpack.template.StringTemplate;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StackTraceElementTemplate extends AbstractTemplate<StackTraceElement> {

	private static final Logger logger = LoggerFactory.getLogger(StackTraceElementTemplate.class);
	
	private StringTemplate stringTemp = StringTemplate.getInstance();
	
	private IntegerTemplate integerTemplate = IntegerTemplate.getInstance();
	
	@Override
	public void write(Packer pk, StackTraceElement v, boolean required)
			throws IOException {
		if (v == null) {
			logger.error("Attempted to write null");
            throw new MessageTypeException("Attempted to write null");
        }
		pk.writeArrayBegin(4);
		stringTemp.write(pk, v.getClassName());
		stringTemp.write(pk, v.getFileName());
		stringTemp.write(pk, v.getMethodName());
		integerTemplate.write(pk, v.getLineNumber());
		pk.writeArrayEnd();
	}

	@Override
	public StackTraceElement read(Unpacker u, StackTraceElement to,
								  boolean required) throws IOException {
		if(u.trySkipNil()){
			return null;
		}
		u.readArrayBegin();
		String className = stringTemp.read(u, null);
		String fileName = stringTemp.read(u, null);
		String methodName = stringTemp.read(u, null);
		Integer lineNumber = integerTemplate.read(u, null);
		u.readArrayEnd();
		to = new StackTraceElement(className, methodName, fileName, lineNumber);
		return to;
	}

	private static StackTraceElementTemplate instance = new StackTraceElementTemplate();
	
	private StackTraceElementTemplate(){}
	
	public static StackTraceElementTemplate getInstance(){
		return instance;
	}
}
