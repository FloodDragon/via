/**
 * 
 */
package org.msgpack.template;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.sql.Time;

/**
 * Created by stereo on 16-8-5.
 */
public class TimeTemplate extends AbstractTemplate<Time> {

	@Override
	public void write(Packer pk, Time target, boolean required) throws IOException {
		if (target == null) {
            if (required) {
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
        pk.write((long) target.getTime());
		
	}

	@Override
	public Time read(Unpacker u, Time to, boolean required) throws IOException {
		 if (!required && u.trySkipNil()) {
	            return null;
	        }
	        long temp = u.readLong();
	        return new Time(temp);
	}

	private TimeTemplate(){}
	
	private static TimeTemplate instance = new TimeTemplate();
	
	public static TimeTemplate getInstance(){
		return instance;
	}
}
