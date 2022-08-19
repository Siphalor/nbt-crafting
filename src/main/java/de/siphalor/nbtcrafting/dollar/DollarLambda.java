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

package de.siphalor.nbtcrafting.dollar;

import org.apache.commons.lang3.ArrayUtils;

import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.exception.IllegalDollarFunctionParameterException;
import de.siphalor.nbtcrafting.dollar.function.DollarFunction;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public class DollarLambda extends DollarFunction {
	private final String[] parameterNames;
	private final DollarPart body;

	public DollarLambda(String[] parameterNames, DollarPart body) {
		super("<lambda>");
		this.parameterNames = parameterNames;
		this.body = body;
	}

	@Override
	public boolean isParameterCountCorrect(int parameterCount) {
		return parameterCount == parameterNames.length;
	}

	@Override
	public void checkParameter(int index, Object parameter) {}

	@Override
	public Object callDirect(ReferenceResolver referenceResolver, Object... parameters) throws DollarEvaluationException {
		if (parameters.length != parameterNames.length) {
			throw new DollarEvaluationException("Invalid number of arguments for lambda");
		}

		return body.evaluate(ref -> {
			int index = ArrayUtils.indexOf(parameterNames, ref);
			if (index >= 0) {
				return parameters[index];
			}
			return referenceResolver.resolve(ref);
		});
	}

	@Override
	public Object call(ReferenceResolver referenceResolver, DollarPart... parameters) throws DollarEvaluationException, IllegalDollarFunctionParameterException {
		Object[] parameterValues = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			parameterValues[i] = parameters[i].evaluate(referenceResolver);
		}
		return callDirect(referenceResolver, parameterValues);
	}
}
