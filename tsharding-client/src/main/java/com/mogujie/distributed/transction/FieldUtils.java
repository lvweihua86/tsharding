package com.mogujie.distributed.transction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.mogujie.distributed.transction.ChainedTransactionInteceptor.Entity;
import com.mogujie.trade.tsharding.route.orm.base.ReflectUtil;

public class FieldUtils {
	/**
	 * 解析参数
	 * 
	 * @param mapper
	 * @param entity
	 * @param args
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static List<Object> parserParam(Class<?> mapper, Entity entity, Object[] args)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		RouteParam routeParam = entity.getRouteParam(mapper);
		int index = entity.getParamIndex(mapper);
		Object param = args[index];
		return parserParamList(param, routeParam.value());
	}

	private static List<Object> parserParamList(Object param, String expression)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		List<Object> list = new ArrayList<>();
		if (expression.indexOf(".") == -1) {
			append(list, param);
			return list;
		} else {
			String[] expressions = expression.split("\\.");
			return parserParamList(param, expressions, list);
		}
	}

	@SuppressWarnings("unchecked")
	private static void append(List<Object> list, Object param) {
		if (param != null) {
			if (List.class.isAssignableFrom(param.getClass())) {
				list.addAll((List<Object>) param);
			} else {
				list.add(param);
			}
		}
	}

	private static List<Object> parserParamList(Object param, String[] expressions, List<Object> list)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Object value = param;
		for (int i = 1; i < expressions.length; i++) {
			String fieldName = expressions[i];
			if (isList(value)) {// List 类型必须为表达式的最后节点
				LIST: {
					if (expressions.length == i + 1) {// 叶子节点
						List<Object> nodes = (List<Object>) value;
						for (Object node : nodes) {
							node = getValue(node, fieldName);
							append(list, node);
						}
						return list;
					} else {
						throw new IllegalArgumentException(
								"不支持的数据类型:`" + param + "` expressions:`" + expressions + "`");
					}
				}
			} else {
				value = getValue(value, fieldName);
				if (value != null && expressions.length == i + 1) {
					append(list, value);
					return list;
				}
			}
		}
		return list;
	}

	private static boolean isList(Object obj) {
		if (obj != null && List.class.isAssignableFrom(obj.getClass())) {
			return true;
		}
		return false;
	}

	private static Object getValue(Object bean, String name)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		if (bean != null) {
			Field field = ReflectUtil.getDeclaredField(bean, name);
			if (field != null) {
				field.setAccessible(true);
				return field.get(bean);
			} else {
				throw new NoSuchFieldError(bean + " not find field:" + name);
			}
		}
		return null;
	}

}
