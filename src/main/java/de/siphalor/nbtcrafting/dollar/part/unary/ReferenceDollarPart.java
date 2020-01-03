package de.siphalor.nbtcrafting.dollar.part.unary;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public class ReferenceDollarPart implements DollarPart {
	private final String key;

	private ReferenceDollarPart(String key) {
		this.key = key;
	}

	public static DollarPart of(String key) {
		return new ReferenceDollarPart(key);
	}

	@Override
	public Object evaluate(Map<String, CompoundTag> reference) throws DollarEvaluationException {
		if(!reference.containsKey(key)) {
			throw new DollarEvaluationException("Could not resolve reference to nbt tag '" + key + "'");
		}
		return reference.get(key);
	}

	public static class Deserializer implements DollarPart.UnaryDeserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return Character.isJavaIdentifierStart(character);
		}

		@Override
		public DollarPart parse(DollarParser dollarParser) {
			StringBuilder stringBuilder = new StringBuilder(String.valueOf(Character.toChars(dollarParser.eat())));
			int character;
			while(true) {
				character = dollarParser.peek();
				if(Character.isJavaIdentifierPart(character)) {
					dollarParser.skip();
					stringBuilder.append(Character.toChars(character));
				} else {
					return ReferenceDollarPart.of(stringBuilder.toString());
				}
			}
		}
	}
}
