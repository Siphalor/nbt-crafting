package de.siphalor.nbtcrafting.dollars;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.util.Map;

public class ReferenceDollarPart implements DollarPart {
	private String key;

	public ReferenceDollarPart(String key) {
		this.key = key;
	}

	@Override
	public Tag evaluate(Map<String, CompoundTag> reference) throws DollarException {
		if(!reference.containsKey(key)) {
			throw new DollarException("Could not resolve reference to nbt tag '" + key + "'");
		}
		return reference.get(key);
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser, boolean hasOtherPart) {
			return Character.isJavaIdentifierPart(character);
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
			StringBuilder stringBuilder = new StringBuilder(String.valueOf(Character.toChars(dollarParser.eat())));
			int character;
			while(true) {
				character = dollarParser.peek();
				if(Character.isJavaIdentifierPart(character)) {
					stringBuilder.append(Character.toChars(dollarParser.eat()));
				} else {
					return new ReferenceDollarPart(stringBuilder.toString());
				}
			}
		}
	}
}
