package de.siphalor.nbtcrafting.dollar;

public class DollarUtil {
	public static boolean asBoolean(Object o) {
		if (o instanceof Number) {
			return ((Number) o).intValue() != 0;
		}
		if (o instanceof String) {
			return !o.equals("");
		}
		return false;
	}
}
