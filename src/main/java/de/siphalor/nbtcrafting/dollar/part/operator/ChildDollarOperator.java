package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;
import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class ChildDollarOperator extends BinaryDollarOperator {
	public ChildDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	@Override
	public Object apply(Object first, Object second) {
		if(first instanceof CompoundTag) {
			String key = second.toString();
			if(((CompoundTag) first).contains(key)) {
				return NbtHelper.toDollarValue(((CompoundTag) first).get(key));
			}
		} else if(first instanceof ListTag && second instanceof Number) {
			int index = ((Number) second).intValue();
			if(index < ((ListTag) first).size()) {
				return NbtHelper.toDollarValue(((ListTag) first).get(index));
			}
		}
		return null;
	}

	public static class DotDeserializer implements Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '.';
		}

		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) {
			dollarParser.skip();
			StringBuilder stringBuilder = new StringBuilder();
			int character = dollarParser.eat();
			if(Character.isJavaIdentifierStart(character)) {
				stringBuilder.append(Character.toChars(character));
				while(true) {
					character = dollarParser.peek();
					if(Character.isJavaIdentifierPart(character)) {
						dollarParser.skip();
						stringBuilder.append(Character.toChars(character));
					} else {
						break;
					}
				}
				return new ChildDollarOperator(lastDollarPart, ValueDollarPart.of(stringBuilder.toString()));
			}
			return null;
		}
	}

	public static class BracketDeserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '[';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) {
			dollarParser.skip();
			return new ChildDollarOperator(lastDollarPart, dollarParser.parseTo(']'));
		}
	}
}
