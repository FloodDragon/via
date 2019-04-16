package org.msgpack.template;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.msgpack.util.ClassTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by stereo on 16-8-5.
 */
public class ClassTemplate extends AbstractTemplate<Class>  {

    private static final Logger logger = LoggerFactory.getLogger(ClassTemplate.class);

    @Override
    public void write(Packer pk, Class v, boolean required) throws IOException {
        if (v == null) {
            if (required) {
                logger.error("Attempted to write null");
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
        pk.write(ClassTypeUtils.getTypeStr(v));
    }

    @Override
    public Class read(Unpacker u, Class to, boolean required) throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        return ClassTypeUtils.getClass(u.readString());
    }

    private ClassTemplate(){}

    private static ClassTemplate instance = new ClassTemplate();

    public static ClassTemplate getInstance(){
        return instance;
    }
}
