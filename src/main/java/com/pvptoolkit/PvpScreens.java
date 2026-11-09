package com.pvptoolkit;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public final class PvpScreens {
    private PvpScreens() {}

    private static final class IntSlider extends AbstractSliderButton {
        private final String label;
        private final int min;
        private final int max;
        private final IntConsumer setter;

        IntSlider(int x, int y, int w, int h, String label, int min, int max, int initial, IntConsumer setter) {
            super(x, y, w, h, Component.empty(), (double) (Math.max(min, Math.min(max, initial)) - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
            updateMessage();
        }

        private int current() {
            return min + (int) Math.round(this.value * (max - min));
        }

        @Override
        protected void updateMessage() {
            if (label == null) return;
            setMessage(Component.literal(label + ": " + current()));
        }

        @Override
        protected void applyValue() {
            setter.accept(current());
        }
    }

    private static Button toggle(int x, int y, int w, String label, BooleanSupplier get, Consumer<Boolean> set) {
        return Button.builder(toggleText(label, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(toggleText(label, get.getAsBoolean()));
        }).bounds(x, y, w, 20).build();
    }

    private static Component toggleText(String label, boolean on) {
        return Component.literal(label + ": " + (on ? "ON" : "OFF"));
    }

    public abstract static class Base extends Screen {
        protected final Screen parent;

        protected Base(Component title, Screen parent) {
            super(title);
            this.parent = parent;
        }

        @Override
        public void onClose() {
            PvpConfig.save();
            this.minecraft.setScreen(parent);
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
            super.render(g, mouseX, mouseY, delta);
            g.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        }
    }

    // ---------------------------------------------------------------- Hauptmenü
    public static class MainMenu extends Base {
        public MainMenu(Screen parent) {
            super(Component.literal("PvP Toolkit"), parent);
        }

        @Override
        protected void init() {
            int cx = this.width / 2;
            int y = this.height / 4 + 10;
            this.addRenderableWidget(Button.builder(Component.literal("Swing"),
                    b -> this.minecraft.setScreen(new SwingScreen(this))).bounds(cx - 100, y, 200, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Kill Effects"),
                    b -> this.minecraft.setScreen(new KillScreen(this))).bounds(cx - 100, y + 26, 200, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Furnace"),
                    b -> this.minecraft.setScreen(new FurnaceScreen(this))).bounds(cx - 100, y + 52, 200, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                    .bounds(cx - 100, y + 88, 200, 20).build());
        }
    }

    // -------------------------------------------------------------------- Swing
    public static class SwingScreen extends Base {
        public SwingScreen(Screen parent) {
            super(Component.literal("Swing"), parent);
        }

        @Override
        protected void init() {
            PvpConfig c = PvpConfig.get();
            int cx = this.width / 2;
            int y = this.height / 4 + 10;
            this.addRenderableWidget(toggle(cx - 100, y, 200, "Custom swing speed", () -> c.swingEnabled, v -> c.swingEnabled = v));
            this.addRenderableWidget(new IntSlider(cx - 100, y + 26, 200, 20, "Swing duration (ticks)", 1, 20, c.swingTicks, v -> c.swingTicks = v));
            this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                    .bounds(cx - 100, y + 62, 200, 20).build());
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
            super.render(g, mouseX, mouseY, delta);
            g.drawCenteredString(this.font, Component.literal("6 ticks = normal. Lower = faster swing."),
                    this.width / 2, this.height / 4 + 100, 0xFFAAAAAA);
        }
    }

    // ------------------------------------------------------------- Kill Effects
    public static class KillScreen extends Base {
        public KillScreen(Screen parent) {
            super(Component.literal("Kill Effects"), parent);
        }

        @Override
        protected void init() {
            PvpConfig c = PvpConfig.get();
            int cx = this.width / 2;
            int y = this.height / 4 - 10;
            this.addRenderableWidget(toggle(cx - 100, y, 200, "Players only", () -> c.playersOnly, v -> c.playersOnly = v));
            this.addRenderableWidget(toggle(cx - 100, y + 24, 200, "Kill particles", () -> c.killParticles, v -> c.killParticles = v));
            this.addRenderableWidget(Button.builder(particleLabel(), b -> {
                c.particle = (c.particle + 1) % KillEffects.NAMES.length;
                b.setMessage(particleLabel());
            }).bounds(cx - 100, y + 48, 200, 20).build());
            this.addRenderableWidget(new IntSlider(cx - 100, y + 72, 200, 20, "Particle count", 1, 60, c.particleCount, v -> c.particleCount = v));
            this.addRenderableWidget(toggle(cx - 100, y + 100, 200, "No death animation", () -> c.noDeathAnimation, v -> c.noDeathAnimation = v));
            this.addRenderableWidget(new IntSlider(cx - 100, y + 124, 200, 20, "Visible time (ticks)", 0, 20, c.deathDelay, v -> c.deathDelay = v));
            this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                    .bounds(cx - 100, y + 156, 200, 20).build());
        }

        private static Component particleLabel() {
            return Component.literal("Particle: " + KillEffects.NAMES[PvpConfig.get().particle % KillEffects.NAMES.length]);
        }
    }

    // ------------------------------------------------------------------ Furnace
    public static class FurnaceScreen extends Base {
        public FurnaceScreen(Screen parent) {
            super(Component.literal("Furnace"), parent);
        }

        @Override
        protected void init() {
            PvpConfig c = PvpConfig.get();
            int cx = this.width / 2;
            int y = this.height / 4 + 10;
            this.addRenderableWidget(toggle(cx - 100, y, 200, "Smelting timer", () -> c.furnaceTimer, v -> c.furnaceTimer = v));
            this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                    .bounds(cx - 100, y + 36, 200, 20).build());
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
            super.render(g, mouseX, mouseY, delta);
            g.drawCenteredString(this.font, Component.literal("Shows remaining smelt and fuel seconds in the furnace screen."),
                    this.width / 2, this.height / 4 + 70, 0xFFAAAAAA);
        }
    }
}
