package de.siphalor.nbtcrafting.dollar.function;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
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
		DollarFunctions.register(new StaticDollarFunction("stackCount", new Class[]{ItemStack.class}) {
			@Override
			protected Object execute(Object[] parameters, DollarRuntime.Context context) {
				ItemStack stack = (ItemStack) parameters[0];
				return stack.getCount();
			}
		});
		DollarFunctions.register(new DollarFunction("min") {
			@Override
			public boolean isParameterCountCorrect(int parameterCount) {
				return parameterCount >= 1;
			}

			@Override
			public Object call(Object[] parameters, DollarRuntime.Context context) throws DollarEvaluationException {
				Object tmp = tryResolveReference(parameters[0], context::resolveReference);
				if (!(tmp instanceof Number)) {
					exceptParameterType(tmp, 0, Number.class);
				}
				Number min = (Number) tmp;
				for (int p = 1; p < parameters.length; p++) {
					tmp = tryResolveReference(parameters[p], context::resolveReference);
					if (!(tmp instanceof Number)) {
						exceptParameterType(tmp, p, Number.class);
					}
					Number n = (Number) tmp;
					if (n.doubleValue() < min.doubleValue()) {
						min = n;
					}
				}
				return min;
			}
		});
	}

	public static void register(DollarFunction function) {
		functions.put(function.getName(), function);
	}

	public static DollarFunction get(String name) {
		return functions.get(name);
	}
}
