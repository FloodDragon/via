/**
 *  JSFFileTemplate.java Created on 2015/1/30 10:04
 *
 */
package org.msgpack.template;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by stereo on 16-8-5.
 */
public class CharsetTemplate extends AbstractTemplate<Charset> {

    private static final Logger logger = LoggerFactory.getLogger(CharsetTemplate.class);

    @Override
    public void write(Packer pk, Charset v, boolean required) throws IOException {
        if (v == null) {
            if (required) {
                logger.error("Attempted to write null");
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
        pk.write(v.toString());
    }

    @Override
    public Charset read(Unpacker u, Charset to, boolean required) throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        String name = u.readString();
        return Charset.forName(name);
    }

    private CharsetTemplate(){}

    private static CharsetTemplate instance = new CharsetTemplate();

    public static CharsetTemplate getInstance(){
        return instance;
    }
}