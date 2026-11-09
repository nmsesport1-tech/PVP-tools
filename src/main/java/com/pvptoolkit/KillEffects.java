package com.pvptoolkit;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

public final class KillEffects {
    private KillEffects() {}

    public static final String[] NAMES = {
            "Flame", "Soul Flame", "Electric Spark", "Cloud", "Heart", "Crit", "End Rod", "Totem"
    };

    public static ParticleOptions particle(int index) {
        return switch (Math.floorMod(index, NAMES.length)) {
            case 0 -> ParticleTypes.FLAME;
            case 1 -> ParticleTypes.SOUL_FIRE_FLAME;
            case 2 -> ParticleTypes.ELECTRIC_SPARK;
            case 3 -> ParticleTypes.CLOUD;
            case 4 -> ParticleTypes.HEART;
            case 5 -> ParticleTypes.CRIT;
            case 6 -> ParticleTypes.END_ROD;
            default -> ParticleTypes.TOTEM_OF_UNDYING;
        };
    }

    public static void spawn(LivingEntity e) {
        PvpConfig c = PvpConfig.get();
        ParticleOptions p = particle(c.particle);
        RandomSource r = e.getRandom();
        double w = e.getBbWidth();
        double h = e.getBbHeight();
        for (int i = 0; i < c.particleCount; i++) {
            double x = e.getX() + (r.nextDouble() - 0.5) * w * 1.5;
            double y = e.getY() + r.nextDouble() * h;
            double z = e.getZ() + (r.nextDouble() - 0.5) * w * 1.5;
            e.level().addParticle(p, x, y, z,
                    (r.nextDouble() - 0.5) * 0.1, 0.05 + r.nextDouble() * 0.1, (r.nextDouble() - 0.5) * 0.1);
        }
    }
}
