package com.gildedgames.aether.common.loot.modifiers;

import com.gildedgames.aether.common.registry.AetherTags;
import com.gildedgames.aether.core.util.LevelUtil;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;

public class RemoveSeedsModifier extends LootModifier {
    public RemoveSeedsModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    public List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        if (LevelUtil.inTag(context.getLevel(), AetherTags.Dimensions.NO_WHEAT_SEEDS)) {
            generatedLoot.removeIf((itemStack) -> itemStack.is(Items.WHEAT_SEEDS));
        }
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<RemoveSeedsModifier> {
        @Override
        public RemoveSeedsModifier read(ResourceLocation name, JsonObject json, LootItemCondition[] conditionsIn) {
            return new RemoveSeedsModifier(conditionsIn);
        }

        @Override
        public JsonObject write(RemoveSeedsModifier instance) {
            return makeConditions(instance.conditions);
        }
    }
}
