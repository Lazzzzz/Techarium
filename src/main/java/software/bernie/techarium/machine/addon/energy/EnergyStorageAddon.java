package software.bernie.techarium.machine.addon.energy;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;
import org.apache.commons.lang3.tuple.Pair;
import software.bernie.techarium.client.screen.draw.IDrawable;
import software.bernie.techarium.machine.interfaces.IFactory;
import software.bernie.techarium.machine.interfaces.IToolTippedAddon;
import software.bernie.techarium.machine.interfaces.IWidgetProvider;
import software.bernie.techarium.machine.screen.widget.EnergyAutoWidget;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import static software.bernie.techarium.client.screen.draw.GuiAddonTextures.DEFAULT_ENERGY_BAR;

public class EnergyStorageAddon extends EnergyStorage implements IWidgetProvider, INBTSerializable<CompoundNBT>, IToolTippedAddon {

    private final int xPos;
    private final int yPos;
    private IDrawable asset;
    private Pair<Integer, Integer> assetSizeXY;
    private Pair<Integer, Integer> guiXY;

    public EnergyStorageAddon(int totalEnergy, int xPos, int yPos, Pair<Integer, Integer> guiXY) {
        this(totalEnergy, totalEnergy, xPos, yPos, guiXY);
    }

    public EnergyStorageAddon(int totalEnergy, int maxIO, int xPos, int yPos, Pair<Integer, Integer> guiXY) {
        this(totalEnergy, maxIO, maxIO, xPos, yPos, guiXY);
    }

    public EnergyStorageAddon(int totalEnergy, int maxIn, int maxOut, int xPos, int yPos, Pair<Integer, Integer> guiXY) {
        super(totalEnergy, maxIn, maxOut);
        this.yPos = yPos;
        this.xPos = xPos;
        this.assetSizeXY = Pair.of(12, 48);
        this.guiXY = guiXY;
    }

    public int getYPos() {
        return yPos;
    }

    public int getXPos() {
        return xPos;
    }

    public Pair<Integer, Integer> getAssetSizeXY() {
        return assetSizeXY;
    }

    public Pair<Integer, Integer> getGuiXY() {
        return guiXY;
    }

    public IDrawable getAsset() {
        if (asset == null) {
            asset = DEFAULT_ENERGY_BAR;
        }
        return asset;
    }

    public EnergyStorageAddon setAsset(IDrawable asset, int sizeX, int sizeY) {
        this.assetSizeXY = Pair.of(sizeX, sizeY);
        this.asset = asset;
        return this;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("energy", this.energy);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.energy = nbt.getInt("energy");
    }

    @Override
    public List<IFactory<? extends Widget>> getGuiWidgets() {
        return Lists.newArrayList(() -> {
            return new EnergyAutoWidget(this);
        });
    }

    @Override
    public void renderToolTip(Screen screen, int x, int y, int xCenter,int yCenter,int mouseX, int mouseY) {
        if (mouseX >= x + getXPos() && mouseX <= x + getXPos() + getAssetSizeXY().getKey()) {
            if (mouseY >= y + getYPos() && mouseY <= y + getYPos() + getAssetSizeXY().getValue()) {
                screen.renderTooltip(Arrays.asList(TextFormatting.GOLD + "Power:", (new DecimalFormat()).format(getEnergyStored()) + TextFormatting.GOLD + "/" + TextFormatting.WHITE + (new DecimalFormat()).format(getMaxEnergyStored()) + TextFormatting.DARK_AQUA + " FE"), mouseX - xCenter, mouseY- yCenter);
            }
        }
    }
}
