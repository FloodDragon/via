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
public class CharacterArrayTemplate extends AbstractTemplate<Character[]> {
	
	private CharacterArrayTemplate(){}

	@Override
	public Character[] read(Unpacker u, Character[] to, boolean required)
			throws IOException {
		if (!required && u.trySkipNil()) {
            return null;
        }
		
//        int n = u.readArrayBegin();
//        if (to == null || to.length != n) {
//        	to = new Character[n];
//        }
//        for(int i=0; i < n; i++){
//        	to[i] = (char) u.readInt();
//        }
//        u.readArrayEnd();      `
		String content = u.readString();
		char cs[] = content.toCharArray();
		int csLen = cs.length;
		 if (to == null || to.length != csLen) {
	        	to = new Character[csLen];
	        }
		for(int i=0; i < csLen;i++){
			to[i] = cs[i];
		}
		return to;
	}

	@Override
	public void write(Packer pk, Character[] target, boolean required)
			throws IOException {
		if (target == null) {
            if (required) {
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
		char cs[] = new char[target.length];
		for(int i=0; i < target.length; i++){
            if(target[i] != null) {
                cs[i] = target[i];
            }
		}
		String content = new String(cs);
		pk.write(content);
//		pk.writeArrayBegin(target.length);
//		for(Character c : target){
//			pk.write(c.charValue());
//		}
//		pk.writeArrayEnd();
	}

	public static CharacterArrayTemplate getInstance(){
		return instance;
	}
	
	private static CharacterArrayTemplate instance = new CharacterArrayTemplate();

}
