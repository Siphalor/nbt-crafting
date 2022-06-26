package de.siphalor.nbtcrafting.dollar.operator;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.token.DollarToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.IntPredicate;

public class ComparisonOperator implements BinaryOperator {
	private final IntPredicate predicate;

	public ComparisonOperator(IntPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public @Nullable Object apply(@Nullable Object left, @Nullable Object right, @NotNull Function<String, Object> referenceResolver) throws DollarEvaluationException {
		left = assertNotNull(left, 0);
		right = assertNotNull(right, 1);
		left = tryResolveReference(left, referenceResolver);
		right = tryResolveReference(right, referenceResolver);

		if (left instanceof Number) {
			right = assertParameterType(right, 1, Number.class);
			if (
					left instanceof Double || left instanceof Float
					|| right instanceof Double || right instanceof Float
			) {
				return predicate.test(Double.compare(
						((Number) left).doubleValue(),
						((Number) right).doubleValue()
				));
			}
			return predicate.test(Long.compare(
					((Number) left).longValue(),
					((Number) right).longValue()
			));
		} else if (left instanceof String) {
			return predicate.test(((String) left).compareTo(DollarUtil.asString(right)));
		}
		exceptParameterType(left, 0, Number.class, String.class);
		return null; // unreachable
	}

	@Override
	public int getPrecedence() {
		return 100;
	}

	@Override
	public @NotNull DollarToken.Type getTokenType() {
		return DollarToken.Type.INFIX_OPERATOR;
	}
}
