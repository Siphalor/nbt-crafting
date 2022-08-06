package de.siphalor.nbtcrafting.util;

public class StringCodepointIterator {
	private final int[] codepoints;
	private final int length;
	private int index;

	public StringCodepointIterator(String string) {
		this.codepoints = new int[string.length()];
		for (int i = 0; i < codepoints.length; i++) {
			codepoints[i] = string.codePointAt(i);
		}
		this.length = codepoints.length;
	}

	public int next() {
		if (index >= length)
			return -1;
		return codepoints[index++];
	}

	public int getIndex() {
		return index;
	}
}
