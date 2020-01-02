package de.siphalor.nbtcrafting.dollar.part.unary;

import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import net.minecraft.nbt.*;

public class InverseDollarOperator extends UnaryDollarOperator {
	public InverseDollarOperator(DollarPart dollarPart) {
		super(dollarPart);
	}

	@Override
	public Tag evaluate(Tag tag) {
		if(tag instanceof AbstractNumberTag) {
			if(tag instanceof DoubleTag) {
				return DoubleTag.of(-((DoubleTag) tag).getDouble());
			} else if(tag instanceof FloatTag) {
				return FloatTag.of(-((FloatTag) tag).getFloat());
			} else if(tag instanceof LongTag) {
				return LongTag.of(-((LongTag) tag).getLong());
			} else if(tag instanceof IntTag) {
				return IntTag.of(-((IntTag) tag).getInt());
			} else if(tag instanceof ShortTag) {
				return ShortTag.of((short) -((ShortTag) tag).getShort());
			}
		}
		return IntTag.of(0);
	}

	public static class Deserializer implements DollarPart.UnaryDeserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '-';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser) throws DollarException {
			return new InverseDollarOperator(dollarParser.parseUnary());
		}
	}
}
