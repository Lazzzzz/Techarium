package software.bernie.techarium.recipe.recipe;

import static software.bernie.techarium.registry.RecipeRegistry.POWERED_FURNACE_RECIPE_TYPE;
import static software.bernie.techarium.registry.RecipeRegistry.POWERED_FURNACE_SERIALIZER;

import com.google.gson.JsonObject;

import lombok.Builder;
import lombok.Getter;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import software.bernie.techarium.recipe.AbstractMachineRecipe;
import software.bernie.techarium.util.loot.ChancedItemStackList;

public class PoweredFurnaceRecipe extends AbstractMachineRecipe {

	@Getter
    protected final Ingredient ingredient;
	
	@Getter
    protected final ChancedItemStackList output;
	
	@Builder(buildMethodName = "construct")
	public PoweredFurnaceRecipe(ResourceLocation id, Ingredient ingredient, ChancedItemStackList result, int progressPerTick, int maxProgress, int rfPerTick) {
	    super(id, POWERED_FURNACE_RECIPE_TYPE, progressPerTick, maxProgress, rfPerTick);
	    this.ingredient = ingredient;
	    this.output = result;
	}
	
	@Override
	public IRecipeSerializer<?> getSerializer() {
	    return POWERED_FURNACE_SERIALIZER.get();
	}
	
	@Override
	public boolean isSpecial() {
	    return true;
	}
	
	@Override
	protected TechariumRecipeBuilder.Result getResult(ResourceLocation id) {
	    return new Result(id);
	}
	
	public boolean isVanillaRecipe() {
		return getOutput().getStackList().get(0).getStack().isEmpty();
	}
	
	public class Result extends AbstractMachineRecipe.Result {
	    public Result(ResourceLocation id) {
	        super(id);
	    }
	
	    @Override
	    public void serializeRecipeData(JsonObject json) {
	        super.serializeRecipeData(json);
	        json.add("ingredient", getIngredient().toJson());
	        json.add("output", getOutput().toJSON());
	    }
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients() {
	    NonNullList<Ingredient> ingredients = super.getIngredients();
	    	ingredients.add(getIngredient());
	    
	    return ingredients;
	}

}
