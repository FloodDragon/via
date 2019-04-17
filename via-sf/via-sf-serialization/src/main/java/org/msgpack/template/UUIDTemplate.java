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
import java.util.UUID;


/**
 * Created by stereo on 16-8-5.
 */
public class UUIDTemplate extends AbstractTemplate<UUID> {

    private static final Logger logger = LoggerFactory.getLogger(UUIDTemplate.class);

    @Override
    public void write(Packer pk, UUID v, boolean required) throws IOException {
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
    public UUID read(Unpacker u, UUID to, boolean required) throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        String path = u.readString();
        return UUID.fromString(path);
    }

    private UUIDTemplate(){}

    private static UUIDTemplate instance = new UUIDTemplate();

    public static UUIDTemplate getInstance(){
        return instance;
    }
}