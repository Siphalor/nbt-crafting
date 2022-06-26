package de.siphalor.nbtcrafting.util;

public class StringCodepointIterator {
	private final int[] codepoints;
	private final int length;
	private int index;

	public StringCodepointIterator(String string) {
		this.codepoints = string.codePoints().toArray();
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
