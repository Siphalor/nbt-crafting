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

package de.siphalor.nbtcrafting.dollar.part.binary;

import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ConstantDollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public abstract class BinaryDollarOperator implements DollarPart {
	private final DollarPart first;
	private final DollarPart second;

	public BinaryDollarOperator(DollarPart first, DollarPart second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public Object evaluate(ReferenceResolver referenceResolver) throws DollarEvaluationException {
		return apply(first.evaluate(referenceResolver), second.evaluate(referenceResolver));
	}

	public abstract Object apply(Object first, Object second) throws DollarEvaluationException;

	protected static DollarPart shortCircuitConstant(BinaryDollarOperator operator) throws DollarDeserializationException {
		if (operator.first instanceof ConstantDollarPart && operator.second instanceof ConstantDollarPart) {
			try {
				return ValueDollarPart.of(operator.apply(((ConstantDollarPart) operator.first).getConstantValue(), ((ConstantDollarPart) operator.second).getConstantValue()));
			} catch (DollarEvaluationException e) {
				throw new DollarDeserializationException("Failed to short-circuit dollar operator " + operator.getClass().getSimpleName(), e);
			}
		}
		return operator;
	}
}
