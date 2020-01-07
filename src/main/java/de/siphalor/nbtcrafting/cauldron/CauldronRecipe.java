package de.siphalor.nbtcrafting.cauldron;

import com.google.common.collect.ImmutableMap;
import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.ingredient.IIngredient;
import de.siphalor.nbtcrafting.util.RecipeUtil;
import de.siphalor.nbtcrafting.util.ServerRecipe;
import de.siphalor.nbtcrafting.util.nbt.NbtHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;

import java.util.Map;

public class CauldronRecipe implements Recipe<TemporaryCauldronInventory>, ServerRecipe {
	private final Identifier identifier;
	public final Ingredient input;
	public final ItemStack output;
	public final int levels;
	private final Dollar[] outputDollars;

	public CauldronRecipe(Identifier id, Ingredient ingredient, ItemStack output, int levels) {
		this.identifier = id;
		this.input = ingredient;
		this.output = output;
		this.levels = levels;
		this.outputDollars = DollarParser.extractDollars(output.getTag(), false);
	}

	public void write(PacketByteBuf packetByteBuf) {
		packetByteBuf.writeIdentifier(identifier);
		input.write(packetByteBuf);
		packetByteBuf.writeItemStack(output);
		packetByteBuf.writeShort(levels);
	}

	public static CauldronRecipe from(PacketByteBuf packetByteBuf) {
		Identifier identifier = packetByteBuf.readIdentifier();
		Ingredient input = Ingredient.fromPacket(packetByteBuf);
		ItemStack output = packetByteBuf.readItemStack();
		int levels = packetByteBuf.readShort();
		return new CauldronRecipe(identifier, input, output, levels);
	}

	@Override
	public boolean matches(TemporaryCauldronInventory inventory, World world) {
		return inventory.getLevel() >= levels && input.test(inventory.getInvStack(0));
	}

	@Override
	public ItemStack craft(TemporaryCauldronInventory inventory) {
		inventory.setLevel(inventory.getLevel() - levels);

		Map<String, Object> reference = ImmutableMap.of("ingredient", NbtHelper.getTagOrEmpty(inventory.getInvStack(0)));

		//noinspection ConstantConditions
		ItemStack remainder = ((IIngredient)(Object) input).getRecipeRemainder(inventory.getInvStack(0), reference);
		if(remainder != null)
			inventory.setInvStack(0, remainder);

		return RecipeUtil.applyDollars(output.copy(), outputDollars, reference);
	}

	@Override
	public boolean fits(int i, int i1) {
		return false;
	}

	@Override
	public ItemStack getOutput() {
		return output;
	}

	@Override
	public Identifier getId() {
		return identifier;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return NbtCrafting.CAULDRON_RECIPE_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return NbtCrafting.CAULDRON_RECIPE_TYPE;
	}
}
