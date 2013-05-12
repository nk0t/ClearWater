package nk0t.mods.clearwater;

import net.minecraft.block.Block;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = "ClearWater", name = "ClearWater", version = "2.0.0")
public class ClearWaterEntryPoint
{

    @Mod.Init
    public void init(FMLInitializationEvent event)
    {
        Block.waterStill.setLightOpacity(0);
        Block.waterMoving.setLightOpacity(0);
    }
}
