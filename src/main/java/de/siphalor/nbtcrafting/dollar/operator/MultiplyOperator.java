package de.siphalor.nbtcrafting.dollar.operator;

import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.token.DollarToken;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class MultiplyOperator implements BinaryOperator {
	@Override
	public @Nullable Object apply(@Nullable Object left, @Nullable Object right, @NotNull Function<String, Object> referenceResolver) throws DollarEvaluationException {
		left = assertNotNull(left, 0);
		left = tryResolveReference(left, referenceResolver);
		right = assertNotNull(right, 1);
		right = tryResolveReference(right, referenceResolver);
		if (left instanceof Number) {
			if (right instanceof Number) {
				return NumberUtil.product((Number) left, (Number) right);
			} else if (right instanceof String) {
				return repeat((String) right, ((Number) left).intValue());
			}
			exceptUsage(left, right);
		} else if (left instanceof String) {
			if (right instanceof Number) {
				return repeat((String) left, ((Number) right).intValue());
			}
			exceptUsage(left, right);
		}
		return null;
	}

	private String repeat(String base, int times) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = times; i > 0; i--) {
			stringBuilder.append(base);
		}
		return stringBuilder.toString();
	}

	@Contract("_, _ -> fail")
	private void exceptUsage(Object left, Object right) throws DollarEvaluationException {
		throw new DollarEvaluationException(
				"Parameters for multiplication must either be Number only, or one Number and one String, got: "
						+ left + ":" + left.getClass().getSimpleName() + " and "
						+ right + ":" + right.getClass().getSimpleName()
		);
	}

	@Override
	public int getPrecedence() {
		return 30;
	}

	@Override
	public DollarToken.@NotNull Type getTokenType() {
		return DollarToken.Type.INFIX_OPERATOR;
	}
}
