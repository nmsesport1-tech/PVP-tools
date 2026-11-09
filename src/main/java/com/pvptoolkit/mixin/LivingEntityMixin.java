package com.pvptoolkit.mixin;

import com.pvptoolkit.KillEffects;
import com.pvptoolkit.PvpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /** Eigene Schlagdauer (nur für den eigenen Spieler, nur Animation). */
    @Inject(method = "getCurrentSwingDuration", at = @At("HEAD"), cancellable = true)
    private void pvptoolkit$swingDuration(CallbackInfoReturnable<Integer> cir) {
        PvpConfig c = PvpConfig.get();
        if (c.swingEnabled && (Object) this == Minecraft.getInstance().player) {
            cir.setReturnValue(Math.max(1, c.swingTicks));
        }
    }

    /** Kill-Partikel und No-Death-Animation (nur Client-Seite, nur andere Spieler/Mobs). */
    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void pvptoolkit$death(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide()) return;
        if (self == Minecraft.getInstance().player) return;
        PvpConfig c = PvpConfig.get();
        if (c.playersOnly && !(self instanceof Player)) return;

        if (self.deathTime == 0 && c.killParticles) {
            KillEffects.spawn(self);
        }
        if (c.noDeathAnimation && self.deathTime >= c.deathDelay) {
            self.discard();
            ci.cancel();
        }
    }
}
