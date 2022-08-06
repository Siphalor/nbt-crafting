package de.siphalor.nbtcrafting.dollar.function;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.ArrayUtils;

import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;
import de.siphalor.nbtcrafting.dollar.Literal;

public abstract class StaticDollarFunction extends DollarFunction {
	private final Class<?>[][] parameterClasses;
	private final int minParameters;

	public StaticDollarFunction(String name, int minParameters, Class<?>[]... parameterClasses) {
		super(name);
		this.minParameters = minParameters;
		this.parameterClasses = parameterClasses;
	}

	public StaticDollarFunction(String name, Class<?>[]... parameterClasses) {
		this(name, parameterClasses.length, parameterClasses);
	}

	@Override
	public boolean isParameterCountCorrect(int parameterCount) {
		return parameterCount >= minParameters && parameterCount <= parameterClasses.length;
	}

	@Override
	public Object call(Object[] parameters, DollarRuntime.Context context) throws DollarEvaluationException {
		for (int p = 0; p < parameters.length; p++) {
			Class<?>[] classes = parameterClasses[p];
			if (classes.length == 0) {
				continue;
			}
			Object parameter = parameters[p];
			if (parameter == null) {
				if (ArrayUtils.contains(classes, null)) {
					continue;
				}
				exceptParameterType(null, p, classes);
			}
			if (parameter.getClass() == Literal.class) {
				if (!ArrayUtils.contains(classes, null)) {
					parameter = tryResolveReference(parameter, context::resolveReference);
				}
			}
			if (parameter.getClass() == ItemStack.class) {
				if (
						!ArrayUtils.contains(classes, ItemStack.class)
						&& ArrayUtils.contains(classes, CompoundTag.class)
				) {
					parameter = NbtUtil.getTagOrEmpty((ItemStack) parameter);
				}
			}
			boolean matching = false;
			for (Class<?> parameterClass : classes) {
				if (parameterClass.isAssignableFrom(parameter.getClass())) {
					matching = true;
					break;
				}
			}

			if (!matching) {
				exceptParameterType(parameter, p, classes);
			}
		}

		return execute(parameters, context);
	}

	protected abstract Object execute(Object[] parameters, DollarRuntime.Context context);
}
