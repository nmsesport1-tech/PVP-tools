package com.pvptoolkit.mixin;

import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceMenu.class)
public interface FurnaceMenuAccessor {
    @Accessor("data")
    ContainerData pvptoolkit$getData();
}
