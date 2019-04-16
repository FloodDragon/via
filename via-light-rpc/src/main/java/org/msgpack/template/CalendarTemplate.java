package org.msgpack.template;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by stereo on 16-8-5.
 */
public class CalendarTemplate extends AbstractTemplate<Calendar> {

    private static final Logger logger = LoggerFactory.getLogger(CalendarTemplate.class);

    @Override
    public void write(Packer pk, Calendar v, boolean required) throws IOException {
        if (v == null) {
            if (required) {
                logger.error("Attempted to write null");
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
        pk.write(v.getTimeInMillis());
    }

    @Override
    public Calendar read(Unpacker u, Calendar to, boolean required) throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        long time = u.readLong();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }

    private CalendarTemplate(){}

    private static CalendarTemplate instance = new CalendarTemplate();

    public static CalendarTemplate getInstance(){
        return instance;
    }
}
