/**
 * 
 */
package org.msgpack.template;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;

/**
 * 字符数组序列化模板
 * @author 张俊峰
 *
 */
public class CharArrayTemplate extends AbstractTemplate<char[]> {
	
	private CharArrayTemplate(){}

	@Override
	public char[] read(Unpacker u, char[] to, boolean required)
			throws IOException {
		if (!required && u.trySkipNil()) {
            return null;
        }
		String content = u.readString();
		
//        int n = u.readArrayBegin();
//        if (to == null || to.length != n) {
//        	to = new char[n];
//        }
//        for(int i=0; i < n; i++){
//        	to[i] = (char) u.readInt();
//        }
//        u.readArrayEnd();
		return content.toCharArray();
	}

	@Override
	public void write(Packer pk, char[] target, boolean required)
			throws IOException {
		if (target == null) {
            if (required) {
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
		String content = new String(target);
		pk.write(content);
//		pk.writeArrayBegin(target.length);
//		for(char c : target){
//			pk.write(c);
//		}
//		pk.writeArrayEnd();
	}

	public static CharArrayTemplate getInstance(){
		return instance;
	}
	
	private static CharArrayTemplate instance = new CharArrayTemplate();
}
