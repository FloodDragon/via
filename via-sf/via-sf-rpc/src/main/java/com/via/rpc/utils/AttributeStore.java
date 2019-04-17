package com.via.rpc.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 属性存储
 * 
 * @author LiuJing
 * @version 2013.12.18 一版
 */
public class AttributeStore implements ICastingAttributeStore {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8272624839322116203L;
	protected ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<String, Object>(
			1);

	protected Map<String, Object> filterNull(Map<String, Object> values) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			String key = entry.getKey();
			if (key == null) {
				continue;
			}
			Object value = entry.getValue();
			if (value == null) {
				continue;
			}

			result.put(key, value);
		}
		return result;
	}

	public AttributeStore() {
	}

	public AttributeStore(Map<String, Object> values) {
		setAttributes(values);
	}

	public AttributeStore(IAttributeStore values) {
		setAttributes(values);
	}

	public Set<String> getAttributeNames() {
		return Collections.unmodifiableSet(attributes.keySet());
	}

	public Map<String, Object> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}

	public Object getAttribute(String name) {
		if (name == null) {
			return null;
		}
		return attributes.get(name);
	}

	public Object getAttribute(String name, Object defaultValue) {
		if (name == null) {
			return null;
		}
		if (defaultValue == null) {
			throw new NullPointerException("默认值不能为空");
		}

		Object result = attributes.putIfAbsent(name, defaultValue);
		if (result == null) {
			result = defaultValue;
		}
		return result;
	}

	public boolean hasAttribute(String name) {
		if (name == null) {
			return false;
		}
		return attributes.containsKey(name);
	}

	public boolean setAttribute(String name, Object value) {
		if (name == null) {
			return false;
		}

		if (value == null) {
			return (attributes.remove(name) != null);
		} else {
			Object previous = attributes.put(name, value);
			return (previous == null || value == previous || !value
					.equals(previous));
		}
	}

	public void setAttributes(Map<String, Object> values) {
		attributes.putAll(filterNull(values));
	}

	public void setAttributes(IAttributeStore values) {
		setAttributes(values.getAttributes());
	}

	public boolean removeAttribute(String name) {
		if (name == null) {
			return false;
		}

		return (attributes.remove(name) != null);
	}

	public void removeAttributes() {
		attributes.clear();
	}

	public Boolean getBoolAttribute(String name) {
		return (Boolean) getAttribute(name);
	}

	public Byte getByteAttribute(String name) {
		return (Byte) getAttribute(name);
	}

	public Double getDoubleAttribute(String name) {
		return (Double) getAttribute(name);
	}

	public Integer getIntAttribute(String name) {
		return (Integer) getAttribute(name);
	}

	public List<?> getListAttribute(String name) {
		return (List<?>) getAttribute(name);
	}

	public Long getLongAttribute(String name) {
		return (Long) getAttribute(name);
	}

	public Map<?, ?> getMapAttribute(String name) {
		return (Map<?, ?>) getAttribute(name);
	}

	public Set<?> getSetAttribute(String name) {
		return (Set<?>) getAttribute(name);
	}

	public Short getShortAttribute(String name) {
		return (Short) getAttribute(name);
	}

	public String getStringAttribute(String name) {
		return (String) getAttribute(name);
	}
}