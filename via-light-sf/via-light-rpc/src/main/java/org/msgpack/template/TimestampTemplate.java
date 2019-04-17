/**
 * 
 */
package org.msgpack.template;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.sql.Timestamp;

/**
 * Created by stereo on 16-8-5.
 */
public class TimestampTemplate extends AbstractTemplate<Timestamp> {

	@Override
	public void write(Packer pk, Timestamp target, boolean required) throws IOException {
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
	public Timestamp read(Unpacker u, Timestamp to, boolean required) throws IOException {
		 if (!required && u.trySkipNil()) {
	            return null;
	        }
	        long temp = u.readLong();
	        return new Timestamp(temp);
	}

	private TimestampTemplate(){}
	
	private static TimestampTemplate instance = new TimestampTemplate();
	
	public static TimestampTemplate getInstance(){
		return instance;
	}
}
