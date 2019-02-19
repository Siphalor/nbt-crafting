package de.siphalor.nbtcrafting.util;

public class NbtNumberRange {
	public double begin;
	public double end;
	
	private NbtNumberRange(double begin, double end) {
		this.begin = begin;
		this.end = end;
	}
	
	public boolean matches(double c) {
		return c >= begin && c <= end; 
	}
	
	public static NbtNumberRange equals(double a) {
		return new NbtNumberRange(a, a);
	}
	
	public static NbtNumberRange between(double a, double b) {
		return new NbtNumberRange(Math.min(a, b), Math.max(a, b));
	}
	
	public static NbtNumberRange fromInfinity(double end) {
		return new NbtNumberRange(Double.NEGATIVE_INFINITY, end);
	}
	
	public static NbtNumberRange toInfinity(double begin) {
		return new NbtNumberRange(begin, Double.POSITIVE_INFINITY);
	}
	
	public static NbtNumberRange ofString(String string) {
		if(!string.contains("..")) {
			try {
				return NbtNumberRange.equals(Double.parseDouble(string));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		int position = string.indexOf("..");
		if(position == 0) {
			try {
				return NbtNumberRange.fromInfinity(Double.parseDouble(string.substring(2)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else if(position == string.length() - 2) {
			try {
				return NbtNumberRange.toInfinity(Double.parseDouble(string.substring(0, string.length() - 2)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else {
			try {
				return NbtNumberRange.between(Double.parseDouble(string.substring(0, position - 1)), Double.parseDouble(string.substring(position + 2)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return NbtNumberRange.between(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
}
