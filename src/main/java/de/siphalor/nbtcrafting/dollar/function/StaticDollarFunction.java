package de.siphalor.nbtcrafting.dollar.function;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.ArrayUtils;

import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.exception.IllegalDollarFunctionParameterException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

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
	public void checkParameter(int index, Object parameter) throws IllegalDollarFunctionParameterException {
		Class<?>[] classes = parameterClasses[index];
		if (classes.length == 0) {
			return;
		}
		if (parameter == null) {
			if (ArrayUtils.contains(classes, null)) {
				return;
			}
			exceptParameterType(null, index, classes);
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
			exceptParameterType(parameter, index, classes);
		}
	}

	@Override
	public Object call(DollarPart[] parameters, ReferenceResolver referenceResolver) throws DollarEvaluationException, IllegalDollarFunctionParameterException {
		Object[] parameterValues = new Object[parameters.length];
		for (int p = 0; p < parameters.length; p++) {
			if (Arrays.equals(new Class<?>[] { DollarPart.class }, parameterClasses[p])) {
				parameterValues[p] = parameters[p];
			} else {
				parameterValues[p] = parameters[p].evaluate(referenceResolver);
				checkParameter(p, parameterValues[p]);
			}
		}

		return apply(parameterValues, referenceResolver);
	}

	protected abstract Object apply(Object[] parameters, ReferenceResolver referenceResolver) throws DollarEvaluationException;
}
