package de.siphalor.nbtcrafting.dollar.part.value;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Pair;

import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public class ObjectConstructDollarPart implements DollarPart {
	private final Pair<String, DollarPart>[] properties;

	private ObjectConstructDollarPart(Pair<String, DollarPart>[] properties) {
		this.properties = properties;
	}

	@SafeVarargs
	public static DollarPart of(Pair<String, DollarPart>... properties) throws DollarDeserializationException {
		ObjectConstructDollarPart instance = new ObjectConstructDollarPart(properties);
		for (Pair<String, DollarPart> property : properties) {
			if (!(property.getRight() instanceof ConstantDollarPart)) {
				return instance;
			}
		}
		try {
			return ValueDollarPart.of(instance.evaluate(null));
		} catch (DollarEvaluationException e) {
			throw new DollarDeserializationException("Failed to short-circuit dollar object construct", e);
		}
	}

	@Override
	public Object evaluate(ReferenceResolver referenceResolver) throws DollarEvaluationException {
		CompoundTag compound = new CompoundTag();
		for (Pair<String, DollarPart> property : properties) {
			compound.put(property.getLeft(), NbtUtil.asTag(property.getRight().evaluate(referenceResolver)));
		}
		return compound;
	}
}
