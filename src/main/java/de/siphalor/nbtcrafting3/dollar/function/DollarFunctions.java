/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.nbtcrafting3.dollar.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.siphalor.nbtcrafting3.util.NumberUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.registry.Registry;

import de.siphalor.nbtcrafting3.dollar.DollarUtil;
import de.siphalor.nbtcrafting3.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting3.dollar.exception.IllegalDollarFunctionParameterException;
import de.siphalor.nbtcrafting3.dollar.part.DollarPart;
import de.siphalor.nbtcrafting3.dollar.part.value.ValueDollarPart;
import de.siphalor.nbtcrafting3.dollar.reference.ReferenceResolver;

public class DollarFunctions {
	private static final Map<String, DollarFunction> functions = new HashMap<>();

	static {
		// ____ ____ _  _ ____ ____ ____ _
		// | __ |___ |\ | |___ |__/ |__| |
		// |__] |___ | \| |___ |  \ |  | |___

		DollarFunctions.register(new DollarFunction("ifNull") {
			@Override
			public boolean isParameterCountCorrect(int parameterCount) {
				return parameterCount == 2;
			}

			@Override
			public void checkParameter(int index, Object parameter) {}

			@Override
			public Object call(ReferenceResolver referenceResolver, DollarPart... parameters) throws DollarEvaluationException, IllegalDollarFunctionParameterException {
				Object value = parameters[0].evaluate(referenceResolver);
				if (value == null) {
					return parameters[1].evaluate(referenceResolver);
				}
				return value;
			}
		});
		DollarFunctions.register(new DollarFunction("ifEmpty") {
			@Override
			public boolean isParameterCountCorrect(int parameterCount) {
				return parameterCount == 2;
			}

			@Override
			public void checkParameter(int index, Object parameter) {}

			@Override
			public Object call(ReferenceResolver referenceResolver, DollarPart... parameters) throws DollarEvaluationException {
				Object value = parameters[0].evaluate(referenceResolver);
				if (DollarUtil.isEmpty(value)) {
					return parameters[1].evaluate(referenceResolver);
				}
				return value;
			}
		});

		// ____ ___ ____ ____ _  _ ____   / _ ___ ____ _  _ ____
		// [__   |  |__| |    |_/  [__   /  |  |  |___ |\/| [__
		// ___]  |  |  | |___ | \_ ___] /   |  |  |___ |  | ___]

		DollarFunctions.register(new StaticDollarFunction("count", new Class[]{ItemStack.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) {
				return ((ItemStack) parameters[0]).getCount();
			}
		});
		DollarFunctions.register(new StaticDollarFunction("id", new Class[]{ItemStack.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) {
				return Registry.ITEM.getId(((ItemStack) parameters[0]).getItem()).toString();
			}
		});
		DollarFunctions.register(new StaticDollarFunction("maxCount", new Class[]{ItemStack.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) {
				return ((ItemStack) parameters[0]).getMaxCount();
			}
		});
		DollarFunctions.register(new StaticDollarFunction("maxDamage", new Class[]{ItemStack.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) {
				return ((ItemStack) parameters[0]).getMaxDamage();
			}
		});

		// ____ ____ _  _ ___ ____ _ _  _ ____ ____ ____
		// |    |  | |\ |  |  |__| | |\ | |___ |__/ [__
		// |___ |__| | \|  |  |  | | | \| |___ |  \ ___]

		DollarFunctions.register(new StaticDollarFunction("size", new Class[]{List.class, CompoundTag.class, String.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) {
				Class<?> clazz = parameters[0].getClass();
				if (parameters[0] instanceof List) {
					return ((List<?>) parameters[0]).size();
				} else if (clazz == CompoundTag.class) {
					return ((CompoundTag) parameters[0]).getSize();
				} else if (clazz == String.class) {
					return ((String) parameters[0]).length();
				}
				throw new AssertionError();
			}
		});
		DollarFunctions.register(new StaticDollarFunction("map", new Class[]{List.class}, new Class[]{DollarFunction.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) throws DollarEvaluationException {
				List<?> list = (List<?>) parameters[0];
				List<Object> mappedList = new ArrayList<>(list.size());
				DollarFunction mapper = (DollarFunction) parameters[1];
				for (Object value : list) {
					try {
						mappedList.add(mapper.callDirect(referenceResolver, value));
					} catch (IllegalDollarFunctionParameterException e) {
						throw new DollarEvaluationException(e);
					}
				}
				return mappedList;
			}
		});
		DollarFunctions.register(new StaticDollarFunction("distinct", 2, new Class[]{List.class}, new Class[]{DollarFunction.class}, new Class[]{Boolean.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) throws DollarEvaluationException {
				List<?> list = (List<?>) parameters[0];
				DollarFunction unique = (DollarFunction) parameters[1];
				boolean keepFirst = parameters.length == 2 || (boolean) parameters[2];
				Map<Object, Object> map = new HashMap<>();

				for (Object value : list) {
					Object key;
					try {
						key = unique.call(referenceResolver, ValueDollarPart.of(value));
					} catch (DollarEvaluationException | IllegalDollarFunctionParameterException e) {
						throw new DollarEvaluationException("Encountered error during evaluation of distinct function", e);
					}
					if (!map.containsKey(key) || !keepFirst) {
						map.put(key, value);
					}
				}
				return new ArrayList<>(map.values());
			}
		});
		DollarFunctions.register(new StaticDollarFunction("filter", new Class[]{List.class}, new Class[]{DollarFunction.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) throws DollarEvaluationException, IllegalDollarFunctionParameterException {
				List<?> list = (List<?>) parameters[0];
				DollarFunction filter = (DollarFunction) parameters[1];
				List<Object> filteredList = new ArrayList<>(list.size());

				for (Object value : list) {
					if (DollarUtil.asBoolean(filter.callDirect(referenceResolver, value))) {
						filteredList.add(value);
					}
				}
				return filteredList;
			}
		});
		DollarFunctions.register(new DollarFunction("combine") {
			@Override
			public boolean isParameterCountCorrect(int parameterCount) {
				return parameterCount > 1;
			}

			@Override
			public void checkParameter(int index, Object parameter) throws IllegalDollarFunctionParameterException {
				if (!(parameter instanceof List) && !(parameter instanceof CompoundTag)) {
					exceptParameterType(parameter, index, List.class, CompoundTag.class);
				}
			}

			@Override
			public Object call(ReferenceResolver referenceResolver, DollarPart... parameters) throws IllegalDollarFunctionParameterException, DollarEvaluationException {
				Object[] values = new Object[parameters.length];
				for (int i = 0; i < values.length; i++) {
					values[i] = parameters[i].evaluate(referenceResolver);
				}
				if (values[0] instanceof List) {
					List<Object> result = new ArrayList<>(((List<?>) values[0]));
					for (int i = 1; i < values.length; i++) {
						if (values[i] instanceof List) {
							result.addAll((List<?>) values[i]);
						} else {
							exceptParameterType(values[i], i, List.class);
						}
					}
					return result;
				} else if (values[0] instanceof CompoundTag) {
					CompoundTag result = ((CompoundTag) values[0]).copy();
					for (int i = 1; i < values.length; i++) {
						if (values[i] instanceof CompoundTag) {
							result.copyFrom((CompoundTag) values[i]);
						} else {
							exceptParameterType(values[i], i, CompoundTag.class);
						}
					}
				}
				exceptParameterType(values[0], 0, List.class, CompoundTag.class);
				return null; // unreachable
			}
		});
		DollarFunctions.register(new StaticDollarFunction("any", new Class[]{List.class}, new Class[]{DollarFunction.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) throws DollarEvaluationException, IllegalDollarFunctionParameterException {
				List<?> list = (List<?>) parameters[0];
				DollarFunction filter = (DollarFunction) parameters[1];
				for (Object value : list) {
					if (DollarUtil.asBoolean(filter.callDirect(referenceResolver, value))) {
						return true;
					}
				}
				return false;
			}
		});
		DollarFunctions.register(new StaticDollarFunction("all", new Class[]{List.class}, new Class[]{DollarFunction.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) throws DollarEvaluationException, IllegalDollarFunctionParameterException {
				List<?> list = (List<?>) parameters[0];
				DollarFunction filter = (DollarFunction) parameters[1];
				for (Object value : list) {
					if (!DollarUtil.asBoolean(filter.callDirect(referenceResolver, value))) {
						return false;
					}
				}
				return true;
			}
		});

		// _  _ ____ ___ _  _
		// |\/| |__|  |  |__|
		// |  | |  |  |  |  |

		DollarFunctions.register(new StaticDollarFunction("mod", new Class[]{Number.class}, new Class[]{Number.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) throws DollarEvaluationException {
				long a = ((Number) parameters[0]).longValue();
				long b = ((Number) parameters[1]).longValue();
				if (b == 0) {
					throw new DollarEvaluationException("Division by zero");
				}
				if (b < 0) {
					throw new DollarEvaluationException("Modulo by negative number");
				}
				long m = a % b;
				if (m < 0) {
					return m + b;
				}
				return m;
			}
		});
		DollarFunctions.register(new StaticDollarFunction("abs", new Class[]{Number.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) {
				Number n = (Number) parameters[0];
				if (n.doubleValue() < 0) {
					return NumberUtil.negate(n);
				}
				return n;
			}
		});
		DollarFunctions.register(new StaticDollarFunction("power", new Class[]{Number.class}, new Class[]{Number.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) {
				return Math.pow(((Number) parameters[0]).doubleValue(), ((Number) parameters[1]).doubleValue());
			}
		});
		DollarFunctions.register(new VariadicNumericDollarFunction("min", Double.MAX_VALUE, (a, b) -> a.doubleValue() < b.doubleValue() ? a : b));
		DollarFunctions.register(new VariadicNumericDollarFunction("max", Double.MAX_VALUE, (a, b) -> a.doubleValue() > b.doubleValue() ? a : b));
	}

	public static void register(DollarFunction function) {
		functions.put(function.getName(), function);
	}

	public static DollarFunction get(String name) {
		return functions.get(name);
	}
}
