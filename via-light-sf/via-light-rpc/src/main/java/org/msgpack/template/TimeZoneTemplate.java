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
import java.util.TimeZone;

/**
 * Created by stereo on 16-8-5.
 */
public class TimeZoneTemplate extends AbstractTemplate<TimeZone> {

    private static final Logger logger = LoggerFactory.getLogger(TimeZoneTemplate.class);

    @Override
    public void write(Packer pk, TimeZone v, boolean required) throws IOException {
        if (v == null) {
            if (required) {
                logger.error("Attempted to write null");
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
        pk.write(v.getID());
    }

    @Override
    public TimeZone read(Unpacker u, TimeZone to, boolean required) throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        String id = u.readString();
        return TimeZone.getTimeZone(id);
    }

    private TimeZoneTemplate(){}

    private static TimeZoneTemplate instance = new TimeZoneTemplate();

    public static TimeZoneTemplate getInstance(){
        return instance;
    }
}