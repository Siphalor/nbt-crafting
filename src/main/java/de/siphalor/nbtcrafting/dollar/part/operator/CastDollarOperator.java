package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.*;

import java.io.IOException;
import java.util.Map;

public class CastDollarOperator implements DollarPart {
	private DollarPart dollarPart;
	private int typeId;

	public CastDollarOperator(DollarPart dollarPart, int typeId) {
		this.dollarPart = dollarPart;
		this.typeId = typeId;
	}

	@Override
	public Tag evaluate(Map<String, CompoundTag> reference) throws DollarException {
		Tag tag = dollarPart.evaluate(reference);
		switch(typeId) {
			case 'd':
				if(tag instanceof AbstractNumberTag)
					return DoubleTag.of(((AbstractNumberTag) tag).getDouble());
				return DoubleTag.of(0D);
			case 'f':
				if(tag instanceof AbstractNumberTag)
					return FloatTag.of(((AbstractNumberTag) tag).getFloat());
				return FloatTag.of(0F);
			case 'b':
			case 'c':
			case 'C':
				if(tag instanceof AbstractNumberTag)
					return ByteTag.of(((AbstractNumberTag) tag).getByte());
				return ByteTag.of((byte) 0);
			case 's':
				if(tag instanceof AbstractNumberTag)
					return ShortTag.of(((AbstractNumberTag) tag).getShort());
				return ShortTag.of((short) 0);
			case 'i':
				if(tag instanceof AbstractNumberTag)
					return IntTag.of(((AbstractNumberTag) tag).getInt());
				return IntTag.of(0);
			case 'l':
				if(tag instanceof AbstractNumberTag)
					return LongTag.of(((AbstractNumberTag) tag).getLong());
				return LongTag.of(0L);
			case '"':
			case '\'':
			case 'a':
				return StringTag.of(NbtHelper.asString(tag));
			default:
				return null;
		}
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '#';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
			dollarParser.skip();
			return new CastDollarOperator(lastDollarPart, dollarParser.eat());
		}
	}
}
