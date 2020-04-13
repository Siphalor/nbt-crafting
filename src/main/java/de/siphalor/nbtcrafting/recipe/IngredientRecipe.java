package de.siphalor.nbtcrafting.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.nbt.NbtHelper;
import de.siphalor.nbtcrafting.api.recipe.NBTCRecipe;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Map;

public class IngredientRecipe<I extends Inventory> implements NBTCRecipe<I> {
	private final Identifier identifier;
	protected final Ingredient base;
	protected final Ingredient ingredient;
	protected final ItemStack result;
	protected final Dollar[] resultDollars;
	protected final RecipeType<? extends IngredientRecipe<I>> recipeType;
	protected final RecipeSerializer<? extends IngredientRecipe<I>> serializer;

	public IngredientRecipe(Identifier identifier, Ingredient base, Ingredient ingredient, ItemStack result, RecipeType<? extends IngredientRecipe<I>> recipeType, RecipeSerializer<? extends IngredientRecipe<I>> serializer) {
		this.identifier = identifier;
		this.base = base;
		this.ingredient = ingredient;
		this.result = result;
		this.resultDollars = DollarParser.extractDollars(result.getTag(), false);
		this.recipeType = recipeType;
		this.serializer = serializer;
	}

	@Override
	public boolean matches(I inv, World world) {
		if (ingredient != null && ingredient.test(inv.getStack(1))) {
			return base.test(inv.getStack(0));
		}
		return false;
	}

	@Override
	public boolean fits(int width, int height) {
		return false;
	}

	@Override
	public ItemStack craft(I inv) {
		return RecipeUtil.applyDollars(result.copy(), resultDollars, buildDollarReference(inv));
	}

	@Override
	public ItemStack getOutput() {
		return result;
	}

	@Override
	public Identifier getId() {
		return identifier;
	}

	public Ingredient getBase() {
		return base;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	@Override
	public DefaultedList<Ingredient> getPreviewInputs() {
		return DefaultedList.copyOf(Ingredient.EMPTY, base, ingredient);
	}

	@Override
	public RecipeType<?> getType() {
		return recipeType;
	}

	@Override
	public RecipeSerializer<? extends IngredientRecipe<I>> getSerializer() {
		return serializer;
	}

	public Map<String, Object> buildDollarReference(I inv) {
		return ImmutableMap.of(
				"base", NbtHelper.getTagOrEmpty(inv.getStack(0)),
				"ingredient", NbtHelper.getTagOrEmpty(inv.getStack(1))
		);
	}

	public void readCustomData(JsonObject json) { }

	public void readCustomData(PacketByteBuf buf) { }

	public void writeCustomData(PacketByteBuf buf) { }

	public interface Factory<R extends IngredientRecipe<?>> {
		R create(Identifier id, Ingredient base, Ingredient ingredient, ItemStack result, Serializer<R> serializer);
	}

	public static class Serializer<R extends IngredientRecipe<?>> implements RecipeSerializer<R> {
		private final Factory<R> factory;

		public Serializer(Factory<R> factory) {
			this.factory = factory;
		}

		@Override
		public R read(Identifier id, JsonObject json) {
			Ingredient base = Ingredient.fromJson(JsonHelper.getObject(json, "base"));
			Ingredient ingredient;
			if (json.has("ingredient")) {
				ingredient = Ingredient.fromJson(JsonHelper.getObject(json, "ingredient"));
			} else {
				ingredient = Ingredient.EMPTY;
			}
			ItemStack result = ShapedRecipe.getItemStack(JsonHelper.getObject(json, "result"));
			R recipe = factory.create(id, base, ingredient, result, this);
			recipe.readCustomData(json);
			return recipe;
		}

		@Override
		public R read(Identifier id, PacketByteBuf buf) {
			Ingredient base = Ingredient.fromPacket(buf);
			Ingredient ingredient = Ingredient.fromPacket(buf);
			ItemStack result = buf.readItemStack();
			R recipe = factory.create(id, base, ingredient, result, this);
			recipe.readCustomData(buf);
			return recipe;
		}

		@Override
		public void write(PacketByteBuf buf, R recipe) {
			recipe.base.write(buf);
			recipe.ingredient.write(buf);
			buf.writeItemStack(recipe.result);
			recipe.writeCustomData(buf);
		}
	}
}
