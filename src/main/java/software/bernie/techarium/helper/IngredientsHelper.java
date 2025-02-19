package software.bernie.techarium.helper;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public class IngredientsHelper {

	public static ItemStack getFirstIngredient(Ingredient ingredient) {
		if (ingredient == null) return ItemStack.EMPTY;
		
		ItemStack[] stacks = ingredient.getItems();
		if (stacks.length == 0) return ItemStack.EMPTY;
		return stacks[0];
	}
	
	public static boolean isItemInIngredient(ItemStack stack, Ingredient ingredient) {
		if (ingredient == null) return false;
		
		ItemStack[] stacks = ingredient.getItems();
		if (stacks.length == 0) return false;
		
		for (ItemStack s : stacks) {
			if (ItemStack.isSame(s, stack))
				return true;
		}
	
		return false;
	}
}
