package dev.shadowsoffire.apotheosis.adventure.affix.reforging;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingMenu.ReforgingResultSlot;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingScreen;
import dev.shadowsoffire.apotheosis.adventure.client.AdventureContainerScreen;
import dev.shadowsoffire.apotheosis.adventure.client.GhostVertexBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ReforgingScreen extends AdventureContainerScreen<ReforgingMenu> {

    /**
     * This texture is 256x384, which must be reflected in blit() calls.
     */
    public static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/reforge.png");
    public static final ResourceLocation ANIMATED_TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/reforge_animation.png");

    protected boolean hasMainItem = false;
    protected int animationTick = 0;
    protected int maxSlot = -1;

    protected int availableOpacity = 0xAA;

    public ReforgingScreen(ReforgingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 266;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float pPartialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, pPartialTick);
        RenderSystem.disableBlend();
        this.renderTooltip(gfx, mouseX, mouseY);

        int dust = this.menu.getDustCount();
        int mats = this.menu.getMatCount();
        int levels = this.menu.player.experienceLevel;

        for (int idx = 0; idx < 3; ++idx) {
            Slot slot = this.getMenu().getSlot(3 + idx);
            if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                ItemStack choice = slot.getItem();
                if (choice.isEmpty()) continue;
                List<Component> tooltips = new ArrayList<>();

                int dustCost = this.menu.getDustCost(idx);
                int matCost = this.menu.getMatCost(idx);
                int levelCost = this.menu.getLevelCost(idx);
                boolean creative = this.minecraft.player.isCreative();

                tooltips.add(Component.translatable("text.apotheosis.reforge_cost").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
                tooltips.add(CommonComponents.EMPTY);
                if (dustCost > 0) {
                    tooltips.add(Component.translatable("%s %s", dustCost, Items.GEM_DUST.get().getName(ItemStack.EMPTY)).withStyle(creative || dust >= dustCost ? ChatFormatting.GRAY : ChatFormatting.RED));
                }
                if (matCost > 0) {
                    tooltips.add(Component.translatable("%s %s", matCost, this.menu.getSlot(1).getItem().getHoverName().getString()).withStyle(creative || mats >= matCost ? ChatFormatting.GRAY : ChatFormatting.RED));
                }
                String key = idx == 0 ? "container.enchant.level.one" : "container.enchant.level.many";
                tooltips.add(Component.translatable(key, idx + 1).withStyle(creative || levels >= levelCost ? ChatFormatting.GRAY : ChatFormatting.RED));

                tooltips.add(Component.literal(" "));
                tooltips.add(Component.translatable("container.enchant.level.requirement", levelCost).withStyle(creative || levels >= levelCost ? ChatFormatting.GRAY : ChatFormatting.RED));

                this.drawOnLeft(gfx, tooltips, this.getGuiTop() + 45);
                break;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partials, int x, int y) {
        int left = this.getGuiLeft();
        int top = this.getGuiTop();
        int xCenter = (this.width - this.imageWidth) / 2;
        int yCenter = (this.height - this.imageHeight) / 2;
        gfx.blit(TEXTURE, xCenter, yCenter, 0, 0, this.imageWidth, this.imageHeight, 256, 384);

        for (int idx = 0; idx < 3; idx++) {
            if (this.maxSlot >= idx && this.animationTick == 0) {
                gfx.blit(TEXTURE, left + 20 + 46 * idx, top + 129, 20 + 46 * idx, 273, 46, 35, 256, 384);
            }
        }

        boolean hadItem = this.hasMainItem;
        this.hasMainItem = this.menu.getSlot(0).hasItem();

        final int FRAME_TIME = 20;

        if (!hadItem && hasMainItem) {
            this.animationTick = FRAME_TIME;
        }

        if (this.hasMainItem) {
            // 127x112 12 frames at 24fps
            // pos 26,15
            float delta = Mth.clamp((FRAME_TIME - this.animationTick - partials) / FRAME_TIME, 0, 1);
            int frame = Mth.lerpInt(delta, 0, 20);
            gfx.blit(ANIMATED_TEXTURE, left + 26, top + 15, 127, 112, 0, frame * 112, 127, 112, 127, 2240);
        }
    }

    protected int darken(int rColor, int factor) {
        int r = rColor >> 16 & 0xFF, g = rColor >> 8 & 0xFF, b = rColor & 0xFF;
        r /= factor;
        g /= factor;
        b /= factor;
        return r << 16 | g << 8 | b;
    }

    protected void drawBorderedString(GuiGraphics gfx, String str, int x, int y, int color, int shadowColor) {
        Component comp = Component.literal(str);
        gfx.drawString(this.font, comp, x, y - 1, shadowColor, false);
        gfx.drawString(this.font, comp, x - 1, y, shadowColor, false);
        gfx.drawString(this.font, comp, x, y + 1, shadowColor, false);
        gfx.drawString(this.font, comp, x + 1, y, shadowColor, false);
        gfx.drawString(this.font, comp, x, y, color, false);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        for (int k = 0; k < 3; ++k) {
            double d0 = pMouseX - (i + 60);
            double d1 = pMouseY - (j + 14 + 19 * k);
            if (d0 >= 0.0D && d1 >= 0.0D && d0 < 108.0D && d1 < 19.0D && this.menu.clickMenuButton(this.minecraft.player, k)) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, k);
                return true;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void containerTick() {
        int dust = this.menu.getDustCount();
        int mats = this.menu.getMatCount();
        int levels = this.menu.player.experienceLevel;

        this.maxSlot = -1;

        for (int idx = 0; idx < 3; idx++) {
            Slot slot = this.getMenu().getSlot(3 + idx);
            if (!slot.hasItem()) break;

            int dustCost = this.menu.getDustCost(idx);
            int matCost = this.menu.getMatCost(idx);
            int levelCost = this.menu.getLevelCost(idx);

            if ((dust >= dustCost && levels >= levelCost && mats >= matCost) || this.minecraft.player.getAbilities().instabuild) {
                this.maxSlot++;
            }
        }

        int ticks = this.minecraft.player.tickCount % 60;
        float sin = Mth.sin((ticks / 60F) * Mth.PI);
        float delta = sin * sin;
        this.availableOpacity = Mth.lerpInt(delta, 0x88, 0xDD);

        if (this.animationTick > 0) {
            this.animationTick--;
        }
    }

    @Override
    public void renderSlot(GuiGraphics pGuiGraphics, Slot pSlot) {
        if (pSlot instanceof ReforgingResultSlot) {
            if (this.animationTick == 0) {
                int opacity = this.maxSlot >= pSlot.getContainerSlot() ? this.availableOpacity : 0x40;
                SalvagingScreen.renderGuiItem(pGuiGraphics, pSlot.getItem(), pSlot.x, pSlot.y, GhostVertexBuilder.makeGhostBuffer(opacity));
            }
        }
        else {
            super.renderSlot(pGuiGraphics, pSlot);
        }
    }

}
