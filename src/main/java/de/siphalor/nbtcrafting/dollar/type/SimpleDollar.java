package de.siphalor.nbtcrafting.dollar.type;

import de.siphalor.nbtcrafting.api.nbt.NbtException;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public class SimpleDollar extends Dollar {
	protected final String path;

	public SimpleDollar(DollarPart expression, String path) {
		super(expression);
		this.path = path;
	}

	@Override
	public void apply(ItemStack stack, Map<String, Object> references) throws DollarException {
		CompoundTag compoundTag = stack.getOrCreateTag();
		String[] pathParts = NbtUtil.splitPath(path);
		try {
			NbtUtil.put(compoundTag, pathParts, evaluate(references));
		} catch (NbtException e) {
			e.printStackTrace();
		}
	}
}
