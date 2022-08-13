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

package de.siphalor.nbtcrafting.dollar.part.value;

import java.util.Map;
import java.util.Objects;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;

public class ReferenceDollarPart implements DollarPart {
	private final String key;

	private ReferenceDollarPart(String key) {
		this.key = key;
	}

	public static DollarPart of(String key) {
		return new ReferenceDollarPart(key);
	}

	@Override
	public Object evaluate(Map<String, Object> reference) throws DollarEvaluationException {
		if (!reference.containsKey(key)) {
			throw new DollarEvaluationException("Could not resolve reference to '" + key + "'");
		}
		return reference.get(key);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ReferenceDollarPart that = (ReferenceDollarPart) o;
		return key.equals(that.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

	@Override
	public String toString() {
		return "Reference{" + key + "}";
	}
}
