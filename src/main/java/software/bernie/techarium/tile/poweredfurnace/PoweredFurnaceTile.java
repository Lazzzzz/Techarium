package software.bernie.techarium.tile.poweredfurnace;

import static software.bernie.techarium.client.screen.draw.GuiAddonTextures.ARBORETUM_DRAWABLE;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.techarium.block.base.MachineBlock;
import software.bernie.techarium.helper.NetworkHelper;
import software.bernie.techarium.machine.addon.ExposeType;
import software.bernie.techarium.machine.addon.inventory.InventoryAddon;
import software.bernie.techarium.machine.addon.progressbar.ProgressBarAddon;
import software.bernie.techarium.machine.controller.MachineController;
import software.bernie.techarium.machine.sideness.FaceConfig;
import software.bernie.techarium.machine.sideness.Side;
import software.bernie.techarium.network.tile.UpdateAnimationPoweredFurnacePacket;
import software.bernie.techarium.recipe.recipe.PoweredFurnaceRecipe;
import software.bernie.techarium.registry.BlockRegistry;
import software.bernie.techarium.registry.RecipeRegistry;
import software.bernie.techarium.tile.base.MultiblockMasterTile;

public class PoweredFurnaceTile extends MultiblockMasterTile<PoweredFurnaceRecipe> implements IAnimatable {

	private AnimationFactory factory = new AnimationFactory(this);

    @Getter
    @Setter
    private boolean isOpening = false;
    
    @Getter
    @Setter   
    private animationState state = animationState.IDLE;
    
    
	public PoweredFurnaceTile() {
		super(BlockRegistry.POWERED_FURNACE.getTileEntityType());
	}

	@Override
	public void tick() {
		super.tick();   	
		if (!level.isClientSide()) {
			updateAnimationState();
		}
	}
	
	private void updateAnimationState() {
		boolean dirty = false;
		
		if (getController().getCurrentRecipe() != null) {
			if (state == animationState.IDLE) {
				state = animationState.WORKING;
				dirty = true;
			}
		} else {
			if (state == animationState.WORKING) {
				state = animationState.IDLE;
				dirty = true;
			}
		}
		
		if (dirty) {
			UpdateAnimationPoweredFurnacePacket packet = new UpdateAnimationPoweredFurnacePacket(getBlockPos(), state);
			NetworkHelper.sendToAllClient(level, packet);
		}
	}

	@Override
	public AnimationFactory getFactory() {
		return factory;
	}

	@Override
	public boolean shouldCheckForRecipe() {
		return true;
	}

	@Override
	public boolean checkRecipe(IRecipe<?> recipe) {
		return recipe.getType() == RecipeRegistry.POWERED_FURNACE_RECIPE_TYPE && recipe instanceof PoweredFurnaceRecipe;
	}
	
	@Override
	public Class<PoweredFurnaceRecipe> getRecipeClass() {
		return PoweredFurnaceRecipe.class;
	}

	@Override
	protected MachineController<PoweredFurnaceRecipe> createMachineController() {
		MachineController<PoweredFurnaceRecipe> controller = new MachineController<>(this, () -> this.worldPosition, ARBORETUM_DRAWABLE);
		controller.setBackground(ARBORETUM_DRAWABLE, 172, 184);
        controller.setPowered(true);
        controller.setEnergyStorage(10000, 10000, 8, 35);
        
        ProgressBarAddon progressBarAddon = new ProgressBarAddon(this, 8, 26, 500, "techarium.gui.mainprogress");
       
        controller.addProgressBar(progressBarAddon
                .setCanProgress(value -> {
                	PoweredFurnaceRecipe recipe = getController().getCurrentRecipe();
                    return recipe != null && matchRecipe(recipe);
                })
                .setOnProgressFull(() -> {
                	handleProgressFinish(getController().getCurrentRecipe());
                })
                .setOnProgressTick(() -> {
                    if (controller.getCurrentRecipe() != null) {
                        controller.getLazyEnergyStorage().ifPresent(iEnergyStorage -> iEnergyStorage
                        		.extractEnergy(controller.getCurrentRecipe().getRfPerTick(), false));
                    }
                })
        );
        
        controller.addInventory(new InventoryAddon(this, "input", 49, 35, 1)
                .setOnSlotChanged((itemStack, integer) -> forceCheckRecipe())
                .setSlotStackSize(0, 64).setExposeType(ExposeType.INPUT));
        
        controller.addInventory(new InventoryAddon(this, "output", 49, 67, 1)
        		.setOnSlotChanged((itemStack, integer) -> forceCheckRecipe())
                .setSlotStackSize(0, 64).setExposeType(ExposeType.OUTPUT));

        return controller;
	}
	
    @Override
    public Map<BlockPos, MachineBlock<?>> getMachineSlaveLocations() {
        Map<BlockPos, MachineBlock<?>> map = super.getMachineSlaveLocations();
        map.put(worldPosition.above(), BlockRegistry.POWERED_FURNACE_TOP.get());
        return map;
    }

    public InventoryAddon getInput() {
    	return getInventoryByName("input");
    }
    
    public InventoryAddon getOutput() {
    	return getInventoryByName("output");
    }
    
	@Override
	public boolean matchRecipe(PoweredFurnaceRecipe currentRecipe) {
        if (getController().getEnergyStorage().getEnergyStored() < currentRecipe.getRfPerTick()) {
            return false;
        }
		
		if (currentRecipe.isVanillaRecipe()) {
			return checkVanilla();
		}
		
        if (!currentRecipe.getIngredients().get(0).test(getInput().getStackInSlot(0))) {
            return false;
        }
		
		return getOutput().canInsertItems(currentRecipe.getOutput().getCachedOutput());
	}

	private boolean checkVanilla() {			
		for (FurnaceRecipe recipe : level.getRecipeManager().getAllRecipesFor(IRecipeType.SMELTING)) {
			Ingredient input = recipe.getIngredients().get(0);
			ItemStack output = recipe.getResultItem();
			
			if (input.test(getInput().getStackInSlot(0)) && getOutput().canInsertItem(output.copy())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void handleProgressFinish(PoweredFurnaceRecipe currentRecipe) {
        if (level == null) {
            return;
        }
        if (level.isClientSide()) {
            return;
        }
        
        if (currentRecipe.isVanillaRecipe()) {
    		for (FurnaceRecipe recipe : level.getRecipeManager().getAllRecipesFor(IRecipeType.SMELTING)) {
    			Ingredient input = recipe.getIngredients().get(0);
    			ItemStack output = recipe.getResultItem();
    			
    			if (input.test(getInput().getStackInSlot(0)) && getOutput().canInsertItem(output.copy())) {
    				getInput().extractItem(0, 1, false);
    				getOutput().insertItems(List.of(output.copy()), false);
        			return;
    			}
    		}
        }
        
        getInput().extractItem(0, 1, false);
        getOutput().insertItems(currentRecipe.getOutput().getCachedOutput(), false);
        currentRecipe.getOutput().reloadCache();
	}

	@Override
	public void forceCheckRecipe() {
        if (getController().getCurrentRecipe() != null) {
            if (!matchRecipe(castRecipe(getController().getCurrentRecipe()))) {
                getController().resetCurrentRecipe();
            }
        } else {
            getController().setShouldCheckRecipe();
        }
        updateMachineTile();
	}
	
    @Override
    protected Map<Side, FaceConfig> setFaceControl() {
        Map<Side, FaceConfig> faceMap = new EnumMap<>(Side.class);
        for (Side side : Side.values()) {
            if (side == Side.FRONT || side == Side.UP) {
                faceMap.put(side, FaceConfig.NONE);
            } else {
                faceMap.put(side, FaceConfig.ENABLED);
            }
        }
        return faceMap;
    }
    
    private <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {    	
    	if (isOpening) {
            event.getController().setAnimation(new AnimationBuilder()
            		.addAnimation("idle", false));
            
            if (event.getController().getAnimationState() == AnimationState.Stopped) {
            	setOpening(false);
            }
   
    	} else if (getState() == animationState.WORKING) {
        	GeckoLibCache.getInstance().parser.setValue("q.anim_time", level.getGameTime() / 20f);
    		
            event.getController().setAnimation(new AnimationBuilder()
                    .addAnimation("expand", false)
                    .addAnimation("smelt", true));
        
    	} else {
    		if (event.getController().getCurrentAnimation() != null && event.getController().getCurrentAnimation().animationName.equals("idle")) {
   				event.getController().setAnimation(new AnimationBuilder()
   						.addAnimation("idle", true));
        	} else {
                event.getController().setAnimation(new AnimationBuilder()
                		.addAnimation("retract", false)
                		.addAnimation("idle", true));
        	}
        }
    	
        return PlayState.CONTINUE;
    } 

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::animationPredicate));
    }
    
    @Override
    public CompoundNBT save(CompoundNBT compound) {
    	compound.putInt("state", getState().ordinal());
    	return super.save(compound);
    }
    
    @Override
    public void load(BlockState state, CompoundNBT nbt) {
    	setState(animationState.values()[nbt.getInt("state")]);
    	super.load(state, nbt);
    }
    
	public enum animationState {
		WORKING,
		IDLE;
	}
    
}