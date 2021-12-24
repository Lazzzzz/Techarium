package software.bernie.techarium.tile.arboretum;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.capability.IFluidHandler;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.techarium.block.base.MachineBlock;
import software.bernie.techarium.client.screen.draw.IDrawable;
import software.bernie.techarium.helper.EventHelper;
import software.bernie.techarium.item.UpgradeItem;
import software.bernie.techarium.machine.addon.fluid.FluidTankAddon;
import software.bernie.techarium.machine.addon.inventory.DrawableInventoryAddon;
import software.bernie.techarium.machine.addon.ExposeType;
import software.bernie.techarium.machine.addon.inventory.InventoryAddon;
import software.bernie.techarium.machine.addon.progressbar.ProgressBarAddon;
import software.bernie.techarium.machine.controller.MachineController;
import software.bernie.techarium.machine.sideness.FaceConfig;
import software.bernie.techarium.machine.sideness.Side;
import software.bernie.techarium.recipe.recipe.ArboretumRecipe;
import software.bernie.techarium.registry.ParticlesRegistry;
import software.bernie.techarium.registry.RecipeRegistry;
import software.bernie.techarium.tile.base.MultiblockMasterTile;
import software.bernie.techarium.util.Vector2i;
import software.bernie.techarium.util.loot.ChancedItemStack;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import static software.bernie.techarium.client.screen.draw.GuiAddonTextures.ARBORETUM_DRAWABLE;
import static software.bernie.techarium.client.screen.draw.GuiAddonTextures.ARBORETUM_OUTPUT_SLOT;
import static software.bernie.techarium.registry.BlockRegistry.ARBORETUM;
import static software.bernie.techarium.registry.BlockRegistry.ARBORETUM_TOP;

public class ArboretumTile extends MultiblockMasterTile<ArboretumRecipe> implements IAnimatable {
    private static final int SIZE_X = 172;
    private static final int SIZE_Y = 184;

    private AnimationFactory factory = new AnimationFactory(this);

    private int prevLightLevel;
    @Getter
    @Setter
    private int lastRenderedTickProgress = 0;

    @Getter
    @Setter
    private boolean isOpening = false;

    private <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
        if (isOpening) {
            event.getController().setAnimation(
                    new AnimationBuilder().addAnimation("Arboretum.anim.deploy", false).addAnimation(
                            "Arboretum.anim.idle", true));
        } else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("Arboretum.anim.idle", true));
        }
        return PlayState.CONTINUE;
    }


    public ArboretumTile() {
        super(ARBORETUM.getTileEntityType());
    }

    @Override
    public void tick() {
        super.tick();
        BlockState state = getFluidInventory().getFluid().getFluid().defaultFluidState().createLegacyBlock();
        int newLightLevel = state.getBlock().properties.lightEmission.applyAsInt(state);
        if (prevLightLevel != newLightLevel) {
            prevLightLevel = newLightLevel;
            level.getLightEngine().checkBlock(worldPosition);
        }
        
        if (level.isClientSide()) {
        	getController().getMultiProgressBar().getProgressBarAddons().forEach(bar -> {
        		if (bar.getProgress() == bar.getMaxProgress() - 1) 
        			spawnDestroyParticles();
        	});
        	
        	if (EventHelper.isChristmas())
        		spawnSnowParticles();
        }
    }
    
    private void spawnSnowParticles() {
		Random rand = getLevel().getRandom();
    	for (int i = 0; i < 5; i++) {
    		double dx = getBlockPos().getX() + 0.5f + ((rand.nextDouble() - 0.5) / 20f);
    		double dy = getBlockPos().getY() + 1.8f;
    		double dz = getBlockPos().getZ() + 0.5f + ((rand.nextDouble() - 0.5) / 20f);
    		
    		level.addParticle(ParticlesRegistry.ARBORETUM.get(), dx, dy, dz, 1, 1, 1);
    	}
    }

    private void spawnDestroyParticles() {
    	Random rand = getLevel().getRandom();
    	Block log = getLogOutput();
    	
    	if (log == null)
    		return;
    	
    	MaterialColor color = log.defaultMaterialColor();
    	float red = ((color.col >> 16) & 0xff) / 255f;
    	float green = ((color.col >> 8) & 0xff) / 255f;
    	float blue = (color.col & 0xff) / 255f;
    	
    	for (int i = 0; i < 100; i++) {
    		double dx = getBlockPos().getX() + 0.5f + ((rand.nextDouble() - 0.5) / 20f);
    		double dy = getBlockPos().getY() + 0.5f + rand.nextDouble();
    		double dz = getBlockPos().getZ() + 0.5f + ((rand.nextDouble() - 0.5) / 20f);
    		
    		level.addParticle(ParticlesRegistry.ARBORETUM.get(), dx, dy, dz, red, green, blue);
    	}
    }
    
    @Override
    protected MachineController<ArboretumRecipe> createMachineController() {
        return machineController(ARBORETUM_DRAWABLE);
    }

    public Block getLogOutput() {
    	MachineController<ArboretumRecipe> controler = getController();
    	ArboretumRecipe recipe = controler.getCurrentRecipe();
    	
		if (recipe == null) return null;
		if (recipe.getOutput() == null) return null;
    	
		Block log = Blocks.OAK_LOG;
    	
		for (ChancedItemStack itemStack : recipe.getOutput().getStackList()) {
			Block block = Block.byItem(itemStack.getStack().getItem());

			if (block instanceof RotatedPillarBlock) {
				log = block;
				break;
			}
		}
		
		return log;
    }
    
    private MachineController<ArboretumRecipe> machineController(IDrawable background) {
        MachineController<ArboretumRecipe> controller = createController();
        controller.setBackground(background, SIZE_X, SIZE_Y);
        controller.setPowered(true);
        controller.setEnergyStorage(10000, 10000, 8, 35);

        ProgressBarAddon progressBarAddon = new ProgressBarAddon(this, 8, 26, 500, "techarium.gui.mainprogress");
        controller.addProgressBar(progressBarAddon
                .setCanProgress(value -> {
                    ArboretumRecipe recipe = getController().getCurrentRecipe();
                    return recipe != null && matchRecipe(recipe);
                })
                .setOnProgressFull(() -> handleProgressFinish(getController().getCurrentRecipe()))
                .setOnProgressTick(() -> {
                    if (controller.getCurrentRecipe() != null) {
                        controller.getLazyEnergyStorage().ifPresent(iEnergyStorage -> iEnergyStorage.extractEnergy(controller.getCurrentRecipe().getRfPerTick(), false));
                    }
                })
        );

        controller.addTank(new FluidTankAddon(controller.getBackgroundSizeXY(),"fluidInput", 10000, 23, 34,
                (fluidStack -> true)).setExposeType(ExposeType.INPUT));

        controller.addInventory(new InventoryAddon(this, "soilInput", 49, 67, 1)
                .setInsertPredicate((itemStack, integer) -> this.level.getRecipeManager()
                        .getAllRecipesFor(RecipeRegistry.ARBORETUM_RECIPE_TYPE).stream()
                        .anyMatch(recipe -> recipe.getSoilIn().test(itemStack)))
                .setOnSlotChanged((itemStack, integer) -> forceCheckRecipe()).setSlotStackSize(0, 1).setExposeType(ExposeType.INPUT));

        controller.addInventory(new InventoryAddon(this, "cropInput", 49, 35, 1)
                .setInsertPredicate((itemStack, integer) -> Block.byItem(itemStack.getItem()) != Blocks.AIR)
                .setOnSlotChanged((itemStack, integer) -> forceCheckRecipe()).setSlotStackSize(0, 1).setExposeType(ExposeType.INPUT));

        controller.addInventory(new InventoryAddon(this, "upgradeSlot", 83, 81, 4)
                .setInsertPredicate((itemStack, integer) -> itemStack.getItem() instanceof UpgradeItem).setSlotPositionWithOffset(20).setExposeType(ExposeType.INPUT));

        controller.addInventory(
                new DrawableInventoryAddon(this, "output", 172, 34, ARBORETUM_OUTPUT_SLOT, 172, 34, 29, 81, 3)
                        .setInsertPredicate((itemStack, integer) -> false)
                        .setOnSlotChanged((itemStack, integer) -> forceCheckRecipe())
                        .setSlotLimit(64)
                        .setSlotPosition(index -> new Vector2i(10,8).add(0, index*18))
                        .setExposeType(ExposeType.OUTPUT));

        return controller;
    }

    public InventoryAddon getCropInventory() {
        return getInventoryByName("cropInput");
    }

    public InventoryAddon getOutputInventory() {
        return getInventoryByName("output");
    }

    public InventoryAddon getSoilInventory() {
        return getInventoryByName("soilInput");
    }

    public FluidTankAddon getFluidInventory() {
        return getFluidTankByName("fluidInput");
    }

    public ProgressBarAddon getProgressBar() {
        return getController().getMultiProgressBar().getProgressBarAddons().stream().filter(addon -> addon.getName().contains("techarium.gui.mainprogress")).findFirst().orElseThrow(NullPointerException::new);
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

    private MachineController<ArboretumRecipe> createController() {
        return new MachineController<>(this, () -> this.worldPosition, ARBORETUM_DRAWABLE);
    }

    @Override
    public boolean shouldCheckForRecipe() {
        return !getCropInventory().getStackInSlot(0).isEmpty();
    }

    @Override
    public boolean checkRecipe(IRecipe<?> recipe) {
        return recipe.getType() == RecipeRegistry.ARBORETUM_RECIPE_TYPE && recipe instanceof ArboretumRecipe;
    }

    @Override
    public Class<ArboretumRecipe> getRecipeClass() {
        return ArboretumRecipe.class;
    }

    @Override
    public boolean matchRecipe(ArboretumRecipe currentRecipe) {
        if (!currentRecipe.getCropType().test(getCropInventory().getStackInSlot(0))) {
            return false;
        }
        if (!currentRecipe.getSoilIn().test(getSoilInventory().getStackInSlot(0))) {
            return false;
        }
        if (getController().getEnergyStorage().getEnergyStored() < currentRecipe.getRfPerTick()) {
            return false;
        }
        if (!(getFluidInventory().getFluid().containsFluid(currentRecipe.getFluidIn()))) {
            return false;
        }

        return getOutputInventory().canInsertItems(currentRecipe.getOutput().getCachedOutput());
    }

    @Override
    public void handleProgressFinish(ArboretumRecipe currentRecipe) {
        if (level == null) {
            return;
        }
        if (level.isClientSide()) {
            return;
        }
        getFluidInventory().drainForced(currentRecipe.getFluidIn().getAmount(), IFluidHandler.FluidAction.EXECUTE);
        getOutputInventory().insertItems(currentRecipe.getOutput().getCachedOutput(), false);
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
    public Map<BlockPos, MachineBlock<?>> getMachineSlaveLocations() {
        Map<BlockPos, MachineBlock<?>> map = super.getMachineSlaveLocations();
        map.put(worldPosition.above(), ARBORETUM_TOP.get());
        return map;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller", 0, this::animationPredicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
