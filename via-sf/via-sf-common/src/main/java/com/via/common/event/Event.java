
package com.via.common.event;


public interface Event<TYPE extends Enum<TYPE>> {
	public TYPE getType();
	public long getTimestamp();
	public String toString();
}
