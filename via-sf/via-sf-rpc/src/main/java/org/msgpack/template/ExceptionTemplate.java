package org.msgpack.template;


import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.msgpack.util.ClassLoaderUtils;
import org.msgpack.util.ClassTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExceptionTemplate<T> extends AbstractTemplate<T> {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionTemplate.class);

	private static Map<Class<?>, Template> fieldTemplateCache = new ConcurrentHashMap<Class<?>, Template>();
	private static ConcurrentHashMap<Class, Constructor> constructorCache = new ConcurrentHashMap<Class, Constructor>();
	private static ConcurrentHashMap<Class, Object[]> constructorArgsCache = new ConcurrentHashMap<Class, Object[]>();
	private TemplateRegistry registry;

	private static byte HAS_VALUE = 0x01;
	private static byte NOT_HAS_VALUE = 0x02;
	private Unsafe unsafe = getUnsafe();

	public ExceptionTemplate(TemplateRegistry registry){
		this.registry = registry;
	}

	@Override
	public void write(Packer pk, T v, boolean required)
			throws IOException {
		if (v == null) {
			if (required) {
				logger.error("Attempted to write null");
				throw new MessageTypeException("Attempted to write null");
			}
			pk.writeNil();
			return;
		}



		//解析异常
		Class<?> clazz = v.getClass();
		Field fields[] = ClassTypeUtils.getFields(clazz);
		pk.writeArrayBegin(fields.length + 1);
		pk.write(ClassTypeUtils.getTypeStr(clazz));
		try {
			loop:for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				Class<?> declareClass = field.getDeclaringClass();
				Template fieldTemp = null;
				//获取Field value
				long offset = unsafe.objectFieldOffset(field);
				Object value = unsafe.getObject(v,offset);
				if ("stackTrace".equals(field.getName()))
				{
					Method method = declareClass.getDeclaredMethod("getStackTrace");
					Object obj = declareClass.cast(v);
					//values = StackTraceElement[]
					Object values[] = (Object[])method.invoke(obj);
					if(values != null)
					{
						//写入异常栈信息
						pk.writeArrayBegin(values.length + 2);
						pk.write(field.getName());
						pk.write(HAS_VALUE);
						for (Object o : values)
						{
							if (fieldTemp == null)
							{
								fieldTemp = this.registerAndGetTemplate(o.getClass());
							}
							fieldTemp.write(pk, o);
						}
						pk.writeArrayEnd();
						continue loop;
					}
					else
						value = null;
				}
				if (value == null || ("cause".equals(field.getName()) && v == value) || "suppressedExceptions".equals(field.getName()))
				{
					pk.writeArrayBegin(2);
					pk.write(field.getName());
					pk.write(NOT_HAS_VALUE);
					pk.writeArrayEnd();
				}else
				{
					Class<?> valueClass = value.getClass();
					fieldTemp = this.registerAndGetTemplate(valueClass);
					//写入剩余写入FieldValue
					pk.writeArrayBegin(4);
					pk.write(field.getName());
					pk.write(HAS_VALUE);
					pk.write(ClassTypeUtils.getTypeStr(valueClass));
					fieldTemp.write(pk, value);
					pk.writeArrayEnd();
				}

				/* 重构
				if(fieldClass.isArray())
				{
					Object values[] = null;

					//类型是StackTraceElement
					if("stackTrace".equals(f.getName()))
					{
						Method method = declareClass.getDeclaredMethod("getStackTrace");
						Object obj = declareClass.cast(v);
						//values = StackTraceElement[]
						values = (Object[])method.invoke(obj);
					} else {
						values = (Object[])value;
					}
					if(values == null)
					{
						pk.writeNil();
						continue;
					} else {
						//写入异常栈信息
						pk.writeArrayBegin(values.length);
						for (Object o : values)
						{
							if (fieldTemp == null)
								fieldTemp = this.registerAndGetTemplate(o.getClass());
							fieldTemp.write(pk, o);
						}
					}
				}
				else
				{
					if(("cause".equals(f.getName()) && v == value) || fieldClass.isPrimitive() || value == null)
					{
						pk.writeArrayBegin(1);
						if(fieldClass.isPrimitive())
						{
							fieldTemp = this.registerAndGetTemplate(fieldClass);
							fieldTemp.write(pk, value);
						} else
							pk.writeNil();
					} else
					{
						Class<?> valueClass = value.getClass();
						if(valueClass.getName().indexOf("$") > 0)
							pk.writeArrayBegin(0);
						else
						{
							pk.writeArrayBegin(2);
							fieldTemp = this.registerAndGetTemplate(valueClass);
							pk.write(ClassTypeUtils.getTypeStr(valueClass));
							fieldTemp.write(pk, value);
						}
					}
				}
				*/
			}
		} catch(Exception e){
			logger.error("An exception occurred while serializing data: ", e);
			throw new MessageTypeException(clazz.getName() + ":", e);
		}
		pk.writeArrayEnd();
	}

	@Override
	public T read(Unpacker u, T to, boolean required)
			throws IOException {
		if (!required && u.trySkipNil()) {
			return null;
		}

		Class<?> clazz = null;
		int fieldLen = u.readArrayBegin() - 1;
		String className = u.readString();
		try {
			if(to !=null)
			{
				if(!(to instanceof Throwable))
				{
					throw new MessageTypeException("Type mismatch:" + className);
				}
				if(!to.getClass().getCanonicalName().equals(className))
				{
					clazz = ClassTypeUtils.getClass(className);
					to = (T)newInstance(clazz);
				}
				else
				{
					clazz = to.getClass();
				}
			} else if(to == null)
			{
				clazz = ClassTypeUtils.getClass(className);
				to = (T)newInstance(clazz);
			}

			Field fields[] = ClassTypeUtils.getFields(clazz);
			int minLen = fieldLen > fields.length? fields.length : fieldLen;

			for (int i = 0; i < minLen; i++)
			{
				Field field = fields[i];
				Class<?> fieldClass = field.getType();
				Class<?> fieldDeclareClass = field.getDeclaringClass();
				Template fieldTemp;
				int len = u.readArrayBegin();
				String fieldName =  u.readString();
				if (field.getName().equals(fieldName))
				{
					boolean hasValue = u.readByte() == HAS_VALUE ? true :false;
					if (hasValue)
					{
						if ("stackTrace".equals(fieldName))
						{
							Class<?> compentType = fieldClass.getComponentType();
							fieldTemp = this.registerAndGetTemplate(compentType);
							int valuesLen = len - 2;
							Object values[] = (Object[]) Array.newInstance(compentType, valuesLen);
							for (int j = 0; j < valuesLen; j++)
							{
								if (Throwable.class.isAssignableFrom(compentType))
								{
									values[j] = fieldTemp.read(u, newInstance(compentType));
								} else
								{
									try {
										values[j] = fieldTemp.read(u, ClassLoaderUtils.newInstance(compentType));
									} catch (InstantiationException e) {
										values[j] = fieldTemp.read(u, null);
									} catch (NoSuchMethodException e) {
										values[j] = fieldTemp.read(u, null);
									} catch (SecurityException e) {
										values[j] = fieldTemp.read(u, null);
									}
								}
							}
						}else
						{
							Object value;
							String valueClassName = u.readString();
							Class<?> valueClass = ClassTypeUtils.getClass(valueClassName);
							fieldTemp = this.registerAndGetTemplate(valueClass);
							if(Throwable.class.isAssignableFrom(valueClass))
							{
								value = fieldTemp.read(u, newInstance(valueClass));
							}
							else
							{
								try {
									value = fieldTemp.read(u, ClassLoaderUtils.newInstance(valueClass));
								} catch (InstantiationException e) {
									value = fieldTemp.read(u, null);
								}catch (NoSuchMethodException e) {
									value = fieldTemp.read(u, null);
								}catch (SecurityException e) {
									value = fieldTemp.read(u, null);
								}
							}
							writePrivateField(to, fieldDeclareClass, field, value);
						}
					}
				}else
					throw new MessageTypeException("Class Field mismatch:" + className);
				u.readArrayEnd();
			}

			/*
			for(int k=0; k < minLen; k++)
			{
				Field f = fields[k];
				Class<?> fclass = f.getType();
				Class<?> declareClass = f.getDeclaringClass();
				Template fieldTemp = null;
				if(fclass.isArray())
				{
					Object values[] = null;
					if (!u.trySkipNil())
					{
						Class<?> compentType = fclass.getComponentType();
						fieldTemp = this.registerAndGetTemplate(compentType);
						int len = u.readArrayBegin();
						values = (Object[]) Array.newInstance(compentType, len);
						for (int i = 0; i < len; i++)
						{
							if (Throwable.class.isAssignableFrom(compentType))
							{
								values[i] = fieldTemp.read(u, newInstance(compentType));
							} else
							{
								try {
									values[i] = fieldTemp.read(u, ClassLoaderUtils.newInstance(compentType));
								} catch (InstantiationException e) {
									values[i] = fieldTemp.read(u, null);
								} catch (NoSuchMethodException e) {
									values[i] = fieldTemp.read(u, null);
								} catch (SecurityException e) {
									values[i] = fieldTemp.read(u, null);
								}
							}
						}
						u.readArrayEnd();
					}
					writePrivateField(to, declareClass, f, values);
				}
				else
				{
					int len = u.readArrayBegin();
					if(len == 2)
					{
						String valueClassName = u.readString();
						Class<?> valueClass = ClassTypeUtils.getClass(valueClassName);
						fieldTemp = this.registerAndGetTemplate(valueClass);
						Object value = null;
						if(Throwable.class.isAssignableFrom(valueClass))
						{
							value = fieldTemp.read(u, newInstance(valueClass));
						}
						else
						{
							try {
								value = fieldTemp.read(u, ClassLoaderUtils.newInstance(valueClass));
							} catch (InstantiationException e) {
								value = fieldTemp.read(u, null);
							}catch (NoSuchMethodException e) {
								value = fieldTemp.read(u, null);
							}catch (SecurityException e) {
								value = fieldTemp.read(u, null);
							}
						}
						writePrivateField(to, declareClass, f, value);
					}
					else if(len == 1)
					{
						if(fclass.isPrimitive())
						{
							fieldTemp = this.registerAndGetTemplate(fclass);
							Object value = fieldTemp.read(u, null);
							writePrivateField(to, declareClass, f, value);
						}
					}
					u.readArrayEnd();
				}
			}
			*/
		}catch(Exception e){
			logger.error("An exception occurred while serializing data:", e);
			throw new MessageTypeException(className + ":", e);
		}
		u.readArrayEnd();
		return to;
	}

	private void writePrivateField(Object target,
								   Class<?> targetClass, Field field, Object value) {
		try {
			if(!targetClass.equals(target.getClass())){
				target = targetClass.cast(target);
			}
			long offset = unsafe.objectFieldOffset(field);
			unsafe.putObject(target,offset,value);
			//field.set(target, value);
		} catch (Exception e) {
			logger.error("", e);
			throw new MessageTypeException(e);
		}
	}


	private Object readField(Object target, Class<?> targetClass, Field field) {

		try {
			if(!targetClass.equals(target.getClass())){
				target = targetClass.cast(target);
			}
			Object valueReference = field.get(target);
			return valueReference;
		} catch (Exception e) {
			logger.error("", e);
			throw new MessageTypeException(e);
		}
	}

	private Template registerAndGetTemplate(Class<?> type){
		Template temp = fieldTemplateCache.get(type);
		if(temp != null)
			return temp;
		try
		{
			temp = this.registry.lookup(type);
			fieldTemplateCache.put(type, temp);
		} catch(MessageTypeException ex1){
			try{
				type.asSubclass(Throwable.class);
				this.registry.register(type, this);
				fieldTemplateCache.put(type, this);
				return this;
			} catch(ClassCastException e) {
				this.registry.register(type);
				temp = this.registry.lookup(type);
				fieldTemplateCache.put(type, temp);
			}
		}
		return temp;
	}

	private <T> T newInstance(Class<T> clazz) throws Exception {
		Constructor _constructor = constructorCache.get(clazz);
		Object[] _constructorArgs = constructorArgsCache.get(clazz);
		if(_constructor == null) {
			Constructor constructors[] = clazz.getDeclaredConstructors();
			long bestCost = Long.MAX_VALUE;

			for (int i = 0; i < constructors.length; i++) {
				Class param[] = constructors[i].getParameterTypes();
				long cost = 0;

				for (int j = 0; j < param.length; j++) {
					cost = 4 * cost;
					if (Object.class.equals(param[j]))
						cost += 1;
					else if (String.class.equals(param[j]))
						cost += 2;
					else if (int.class.equals(param[j]))
						cost += 3;
					else if (long.class.equals(param[j]))
						cost += 4;
					else if (param[j].isPrimitive())
						cost += 5;
					else
						cost += 6;
				}
				if (cost < 0 || cost > (1 << 48)) {
					cost = 1 << 48;
				}
				cost += (long) param.length << 48;
				if (cost < bestCost) {
					_constructor = constructors[i];
					bestCost = cost;
				}
			}

			if (_constructor != null) {
				_constructor.setAccessible(true);
				Class[] params = _constructor.getParameterTypes();
				_constructorArgs = new Object[params.length];
				for (int i = 0; i < params.length; i++) {
					_constructorArgs[i] = getParamArg(params[i]);
				}
				constructorCache.put(clazz, _constructor);
				constructorArgsCache.put(clazz, _constructorArgs);
			}
		}
		if (_constructor != null) {
			return (T) _constructor.newInstance(_constructorArgs);
		} else {
			return clazz.newInstance();
		}
	}

	private static Object getParamArg(Class cl) {
		if (! cl.isPrimitive())
			return null;
		else if (boolean.class.equals(cl))
			return Boolean.FALSE;
		else if (byte.class.equals(cl))
			return new Byte((byte) 0);
		else if (short.class.equals(cl))
			return new Short((short) 0);
		else if (char.class.equals(cl))
			return new Character((char) 0);
		else if (int.class.equals(cl))
			return Integer.valueOf(0);
		else if (long.class.equals(cl))
			return Long.valueOf(0);
		else if (float.class.equals(cl))
			return Float.valueOf(0);
		else if (double.class.equals(cl))
			return Double.valueOf(0);
		else
			throw new UnsupportedOperationException();
	}
}