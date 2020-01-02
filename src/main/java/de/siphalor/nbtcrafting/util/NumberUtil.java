package de.siphalor.nbtcrafting.util;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class NumberUtil {
	public static final List<Class<?>> NUMBER_TYPES = ImmutableList.of(
			Character.class,
			Byte.class,
			Short.class,
			Integer.class,
			Long.class,
			Float.class,
			Double.class
	);

	public static int findSmallestType(Number a, Number b) {
		int typeA = NUMBER_TYPES.indexOf(a.getClass());
		int typeB = NUMBER_TYPES.indexOf(b.getClass());

		return Math.max(typeA, typeB);
	}

	public static Number denullify(Number number) {
		if(number == null) {
			return (byte) 0;
		}
		return number;
	}

	public static Number sum(Number a, Number b) {
		a = denullify(a);
		b = denullify(b);
		switch(findSmallestType(a, b)) {
			case 0:
			case 1:
				return (byte) (a.byteValue() + b.byteValue());
			case 2:
				return (short) (a.shortValue() + b.shortValue());
			case 3:
				return a.intValue() + b.intValue();
			case 4:
				return a.longValue() + b.longValue();
			case 5:
				return a.floatValue() + b.floatValue();
			default:
				return a.doubleValue() + b.doubleValue();
		}
	}

	public static Number difference(Number a, Number b) {
		a = denullify(a);
		b = denullify(b);
		switch(findSmallestType(a, b)) {
			case 0:
			case 1:
				return (byte) (a.byteValue() - b.byteValue());
			case 2:
				return (short) (a.shortValue() - b.shortValue());
			case 3:
				return a.intValue() - b.intValue();
			case 4:
				return a.longValue() - b.longValue();
			case 5:
				return a.floatValue() - b.floatValue();
			default:
				return a.doubleValue() - b.doubleValue();
		}
	}

	public static Number product(Number a, Number b) {
		a = denullify(a);
		b = denullify(b);
		switch(findSmallestType(a, b)) {
			case 0:
			case 1:
				return (byte) (a.byteValue() * b.byteValue());
			case 2:
				return (short) (a.shortValue() * b.shortValue());
			case 3:
				return a.intValue() * b.intValue();
			case 4:
				return a.longValue() * b.longValue();
			case 5:
				return a.floatValue() * b.floatValue();
			default:
				return a.doubleValue() * b.doubleValue();
		}
	}

	public static Number quotient(Number a, Number b) {
		a = denullify(a);
		b = denullify(b);
		switch(findSmallestType(a, b)) {
			case 0:
			case 1:
				return (byte) (a.byteValue() / b.byteValue());
			case 2:
				return (short) (a.shortValue() / b.shortValue());
			case 3:
				return a.intValue() / b.intValue();
			case 4:
				return a.longValue() / b.longValue();
			case 5:
				return a.floatValue() / b.floatValue();
			default:
				return a.doubleValue() / b.doubleValue();
		}
	}
}
