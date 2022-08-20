package de.siphalor.nbtcrafting.dollar.part.binary;

import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ConstantDollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public class LogicalOrDollarOperator implements DollarPart {
	private final DollarPart left;
	private final DollarPart right;

	private LogicalOrDollarOperator(DollarPart left, DollarPart right) {
		this.left = left;
		this.right = right;
	}

	public static DollarPart of(DollarPart left, DollarPart right) {
		if (left instanceof ConstantDollarPart) {
			if (DollarUtil.asBoolean(((ConstantDollarPart) left).getConstantValue())) {
				return ValueDollarPart.of(true);
			}
			if (right instanceof ConstantDollarPart) {
				return ValueDollarPart.of(DollarUtil.asBoolean(((ConstantDollarPart) right).getConstantValue()));
			}
		}
		return new LogicalOrDollarOperator(left, right);
	}

	@Override
	public Object evaluate(ReferenceResolver referenceResolver) throws DollarEvaluationException {
		return DollarUtil.asBoolean(left.evaluate(referenceResolver)) || DollarUtil.asBoolean(right.evaluate(referenceResolver));
	}
}
