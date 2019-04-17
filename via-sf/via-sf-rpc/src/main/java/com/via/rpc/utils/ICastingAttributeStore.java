package com.via.rpc.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 扩展属性贮存
 * 
 * @author LiuJing
 */
public interface ICastingAttributeStore extends IAttributeStore {

	public Boolean getBoolAttribute(String name);

	public Byte getByteAttribute(String name);

	public Double getDoubleAttribute(String name);

	public Integer getIntAttribute(String name);

	public List<?> getListAttribute(String name);

	public Long getLongAttribute(String name);

	public Map<?, ?> getMapAttribute(String name);

	public Set<?> getSetAttribute(String name);

	public Short getShortAttribute(String name);

	public String getStringAttribute(String name);
}
