package de.siphalor.nbtcrafting.dollar.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.registry.Registry;

import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public class DollarFunctions {
	private static final Map<String, DollarFunction> functions = new HashMap<>();

	static {
		// ____ ____ _  _ ____ ____ ____ _
		// | __ |___ |\ | |___ |__/ |__| |
		// |__] |___ | \| |___ |  \ |  | |___

		DollarFunctions.register(new StaticDollarFunction("ifNull", new Class[0], new Class[0]) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) {
				if (parameters[0] == null) {
					return parameters[1];
				}
				return parameters[0];
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
		DollarFunctions.register(new StaticDollarFunction("map", new Class[]{List.class}, new Class[]{DollarPart.class}) {
			@Override
			protected Object apply(Object[] parameters, ReferenceResolver referenceResolver) throws DollarEvaluationException {
				List<?> list = (List<?>) parameters[0];
				List<Object> mappedList = new ArrayList<>(list.size());
				DollarPart mapper = (DollarPart) parameters[1];
				for (int i = 0; i < list.size(); i++) {
					int finalI = i;
					mappedList.add(mapper.evaluate(ref -> {
						if ("it".equals(ref)) {
							return list.get(finalI);
						}
						return referenceResolver.resolve(ref);
					}));
				}
				return mappedList;
			}
		});

		// _  _ ____ ___ _  _
		// |\/| |__|  |  |__|
		// |  | |  |  |  |  |

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
