package dev.shadowsoffire.apotheosis.potion;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class PotionModuleClient {

    @SubscribeEvent
    public static void colors(RegisterColorHandlersEvent.Item e) {
        e.register((stack, tint) -> tint == 0 ? -1 : PotionUtils.getColor(stack), Apoth.Items.POTION_CHARM.get());
    }

    @SubscribeEvent
    public static void client(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            ItemProperties.register(Apoth.Items.POTION_CHARM.get(), Apotheosis.loc("enabled"), (stack, level, entity, tint) -> {
                return stack.hasTag() && stack.getTag().getBoolean("charm_enabled") ? 1 : 0;
            });
        });

        MinecraftForge.EVENT_BUS.addListener(PotionModuleClient::showBlacklistedPotions);
    }

    public static void showBlacklistedPotions(ItemTooltipEvent e) {
        if (e.getItemStack().getItem() == Items.POTION) {
            Potion potion = PotionUtils.getPotion(e.getItemStack());

            if (!PotionCharmItem.isValidPotion(potion)) {
                e.getToolTip().add(Component.translatable("misc.apotheosis.blacklisted_potion").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        }
    }

}
