package com.pvptoolkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PvpConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pvp_toolkit.json");

    // Swing
    public boolean swingEnabled = false;
    public int swingTicks = 6;

    // Kill-Effekte
    public boolean playersOnly = true;
    public boolean killParticles = true;
    public int particle = 0;
    public int particleCount = 20;
    public boolean noDeathAnimation = false;
    public int deathDelay = 2;

    // Ofen
    public boolean furnaceTimer = true;

    private static PvpConfig instance = new PvpConfig();

    public static PvpConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try (Reader r = Files.newBufferedReader(FILE)) {
            PvpConfig c = GSON.fromJson(r, PvpConfig.class);
            if (c != null) instance = c;
        } catch (Exception e) {
            PvpToolkitClient.LOGGER.error("Konnte Config nicht laden", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(instance, w);
            }
        } catch (Exception e) {
            PvpToolkitClient.LOGGER.error("Konnte Config nicht speichern", e);
        }
    }
}
