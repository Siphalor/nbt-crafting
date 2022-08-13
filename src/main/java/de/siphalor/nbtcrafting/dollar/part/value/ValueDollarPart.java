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

import java.util.Objects;

import de.siphalor.nbtcrafting.dollar.DollarUtil;

public class ValueDollarPart implements ConstantDollarPart {
	private final Object value;

	private ValueDollarPart(Object value) {
		this.value = value;
	}

	public static ValueDollarPart of(Object value) {
		return new ValueDollarPart(value);
	}

	@Override
	public Object getConstantValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueDollarPart that = (ValueDollarPart) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "Value{" + DollarUtil.asString(value) + "}";
	}
}
