package com.pvptoolkit;

import com.pvptoolkit.mixin.FurnaceMenuAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PvpToolkitClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("pvptoolkit");
    private static boolean openMenu = false;

    @Override
    public void onInitializeClient() {
        PvpConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            for (String name : new String[]{"pvptoolkit", "pvp"}) {
                dispatcher.register(ClientCommandManager.literal(name).executes(ctx -> {
                    openMenu = true;
                    return 1;
                }));
            }
        });

        // Menü erst im nächsten Tick öffnen, sonst schließt der Chat es wieder.
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (openMenu) {
                openMenu = false;
                mc.setScreen(new PvpScreens.MainMenu(null));
            }
        });

        // Ofen-Timer
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof AbstractFurnaceScreen<?> furnaceScreen) {
                ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> {
                    if (!PvpConfig.get().furnaceTimer) return;
                    AbstractFurnaceMenu menu = furnaceScreen.getMenu();
                    ContainerData data = ((FurnaceMenuAccessor) menu).pvptoolkit$getData();
                    int litLeft = data.get(0);
                    int cooking = data.get(2);
                    int cookingTotal = data.get(3);
                    Minecraft mc = Minecraft.getInstance();
                    int left = (s.width - 176) / 2;
                    int top = (s.height - 166) / 2;
                    if (litLeft > 0) {
                        String fuel = ((litLeft + 19) / 20) + "s";
                        graphics.drawString(mc.font, fuel, left + 56 - mc.font.width(fuel) - 4, top + 37, 0xFFFFAA00, true);
                    }
                    if (cooking > 0 && cookingTotal > 0) {
                        int secs = (cookingTotal - cooking + 19) / 20;
                        String t = secs + "s";
                        graphics.drawString(mc.font, t, left + 91 - mc.font.width(t) / 2, top + 24, 0xFF55FF55, true);
                    }
                });
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> PvpConfig.save());
    }
}
