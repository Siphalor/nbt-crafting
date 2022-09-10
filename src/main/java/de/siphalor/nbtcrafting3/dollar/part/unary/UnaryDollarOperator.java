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

package de.siphalor.nbtcrafting3.dollar.part.unary;

import de.siphalor.nbtcrafting3.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting3.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting3.dollar.part.DollarPart;
import de.siphalor.nbtcrafting3.dollar.part.value.ConstantDollarPart;
import de.siphalor.nbtcrafting3.dollar.part.value.ValueDollarPart;
import de.siphalor.nbtcrafting3.dollar.reference.ReferenceResolver;

public abstract class UnaryDollarOperator implements DollarPart {
	DollarPart dollarPart;

	public UnaryDollarOperator(DollarPart dollarPart) {
		this.dollarPart = dollarPart;
	}

	@Override
	public final Object evaluate(ReferenceResolver referenceResolver) throws DollarEvaluationException {
		return apply(dollarPart.evaluate(referenceResolver));
	}

	public abstract Object apply(Object value) throws DollarEvaluationException;

	protected static DollarPart shortCircuitConstant(UnaryDollarOperator operator) throws DollarDeserializationException {
		if (operator.dollarPart instanceof ConstantDollarPart) {
			try {
				return ValueDollarPart.of(operator.apply(((ConstantDollarPart) operator.dollarPart).getConstantValue()));
			} catch (DollarEvaluationException e) {
				throw new DollarDeserializationException("Failed to short-circuit dollar operator " + operator.getClass().getSimpleName(), e);
			}
		}
		return operator;
	}
}
