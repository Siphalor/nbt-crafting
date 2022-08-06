package de.siphalor.nbtcrafting.dollar.function;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.registry.Registry;

import de.siphalor.nbtcrafting.dollar.DollarRuntime;

public class DollarFunctions {
	private static final Map<String, DollarFunction> functions = new HashMap<>();

	static {
		DollarFunctions.register(new StaticDollarFunction("ifNull", new Class[0], new Class[0]) {
			@Override
			protected Object execute(Object[] parameters, DollarRuntime.Context context) {
				if (parameters[0] == null) {
					return parameters[1];
				}
				return parameters[0];
			}
		});
		DollarFunctions.register(new StaticDollarFunction("count", new Class[]{ItemStack.class}) {
			@Override
			protected Object execute(Object[] parameters, DollarRuntime.Context context) {
				return ((ItemStack) parameters[0]).getCount();
			}
		});
		DollarFunctions.register(new StaticDollarFunction("id", new Class[]{ItemStack.class}) {
			@Override
			protected Object execute(Object[] parameters, DollarRuntime.Context context) {
				return Registry.ITEM.getId(((ItemStack) parameters[0]).getItem()).toString();
			}
		});
		DollarFunctions.register(new StaticDollarFunction("size", new Class[]{ListTag.class, CompoundTag.class, String.class}) {
			@Override
			protected Object execute(Object[] parameters, DollarRuntime.Context context) {
				Class<?> clazz = parameters[0].getClass();
				if (clazz == ListTag.class) {
					return ((ListTag) parameters[0]).size();
				} else if (clazz == CompoundTag.class) {
					return ((CompoundTag) parameters[0]).getSize();
				} else if (clazz == String.class) {
					return ((String) parameters[0]).length();
				}
				throw new AssertionError();
			}
		});
		DollarFunctions.register(new StaticDollarFunction("power", new Class[]{Number.class}, new Class[]{Number.class}) {
			@Override
			protected Object execute(Object[] parameters, DollarRuntime.Context context) {
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
