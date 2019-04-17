/**
 *  URLTemplate.java Created on 2015/1/30 10:11
 *
 */
package org.msgpack.template;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * Created by stereo on 16-8-5.
 */
public class URITemplate extends AbstractTemplate<URI> {

    private static final Logger logger = LoggerFactory.getLogger(URITemplate.class);

    @Override
    public void write(Packer pk, URI v, boolean required) throws IOException {
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
    public URI read(Unpacker u, URI to, boolean required) throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        String path = u.readString();
        return URI.create(path);
    }

    private URITemplate(){}

    private static URITemplate instance = new URITemplate();

    public static URITemplate getInstance(){
        return instance;
    }
}