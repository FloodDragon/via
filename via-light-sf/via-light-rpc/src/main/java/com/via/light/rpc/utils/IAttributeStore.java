package com.via.light.rpc.utils;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 属性贮存基础接口
 * 
 * @author LiuJing
 */
public interface IAttributeStore extends Serializable {

	public Set<String> getAttributeNames();

	public Map<String, Object> getAttributes();

	public boolean setAttribute(String name, Object value);

	public void setAttributes(Map<String, Object> values);

	public void setAttributes(IAttributeStore values);

	public Object getAttribute(String name);

	public Object getAttribute(String name, Object defaultValue);

	public boolean hasAttribute(String name);

	public boolean removeAttribute(String name);

	public void removeAttributes();
}
