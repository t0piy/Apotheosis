package dev.shadowsoffire.apotheosis.adventure.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AffixCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_AFFIX = (ctx, builder) -> SharedSuggestionProvider.suggest(AffixRegistry.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("affix").requires(c -> c.hasPermission(2));

        builder.then(
            Commands.literal("apply")
                .then(Commands.argument("affix", ResourceLocationArgument.id()).suggests(SUGGEST_AFFIX)
                    .then(Commands.argument("level", FloatArgumentType.floatArg(0, 1))
                        .executes(c -> applyAffix(c, ResourceLocationArgument.getId(c, "affix"), FloatArgumentType.getFloat(c, "level"))))
                    .executes(c -> applyAffix(c, ResourceLocationArgument.getId(c, "affix"), c.getSource().getLevel().random.nextFloat()))));

        root.then(builder);
    }

    public static int applyAffix(CommandContext<CommandSourceStack> c, ResourceLocation affixId, float level) {
        DynamicHolder<Affix> afx = AffixRegistry.INSTANCE.holder(affixId);
        if (!afx.isBound()) {
            return fail(c, "Unkonwn affix: " + affixId, -1);
        }

        Entity entity = c.getSource().getEntity();
        if (entity instanceof LivingEntity living) {
            ItemStack held = living.getMainHandItem();
            if (held.isEmpty()) {
                return fail(c, "The target entity must have an item in their main hand.", -2);
            }

            DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(held);
            if (!rarity.isBound()) {
                return fail(c, "The target item must have a set rarity.", -3);
            }

            LootCategory cat = LootCategory.forItem(held);
            if (cat.isNone()) {
                return fail(c, "The target item must have a valid loot category", -4);
            }

            if (!afx.get().canApplyTo(held, cat, rarity.get())) {
                return fail(c, "The selected affix cannot be applied to the target item.", -5);
            }

            AffixHelper.applyAffix(held, new AffixInstance(afx, held, rarity, level));
            c.getSource().sendSuccess(() -> Component.translatable("Successfully applied affix %s with level %s to %s", affixId, level, held.getDisplayName()), true);
            return 0;
        }
        else {
            return fail(c, "/apoth affix must be executed by a living entity.", -10);
        }
    }

    public static int fail(CommandContext<CommandSourceStack> c, String msg, int code) {
        c.getSource().sendFailure(Component.translatable(msg));
        return code;
    }
}
