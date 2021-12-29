package software.bernie.techarium.client.tile.model;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.techarium.Techarium;
import software.bernie.techarium.tile.arboretum.ArboretumTile;
import software.bernie.techarium.tile.poweredfurnace.PoweredFurnaceTile;

public class PoweredFurnaceModel extends AnimatedGeoModel<PoweredFurnaceTile>
{
	@Override
	public ResourceLocation getAnimationFileLocation(PoweredFurnaceTile tile) {
		return Techarium.rl("animations/poweredfurnace.animation.json");
	}

	@Override
	public ResourceLocation getModelLocation(PoweredFurnaceTile tile) {
		return Techarium.rl("geo/poweredfurnace/poweredfurnace.geo.json");
	}

	@Override
	public ResourceLocation getTextureLocation(PoweredFurnaceTile tile) {
		return Techarium.rl("textures/block/animated/poweredfurnace.png");
	}
}
