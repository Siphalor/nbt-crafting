package de.siphalor.nbtcrafting.dollars.operator;

import de.siphalor.nbtcrafting.dollars.DollarException;
import de.siphalor.nbtcrafting.dollars.DollarParser;
import de.siphalor.nbtcrafting.dollars.DollarPart;
import net.minecraft.nbt.*;

import java.io.IOException;

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

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser, boolean hasOtherPart) {
			return !hasOtherPart && character == '-';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
			return new InverseDollarOperator(dollarParser.parse(priority));
		}
	}
}
