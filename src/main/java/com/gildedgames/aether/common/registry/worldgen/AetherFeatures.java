package com.gildedgames.aether.common.registry.worldgen;

import com.gildedgames.aether.Aether;
import com.gildedgames.aether.common.block.state.properties.AetherBlockStateProperties;
import com.gildedgames.aether.common.registry.AetherBlocks;
import com.gildedgames.aether.common.registry.AetherTags;
import com.gildedgames.aether.common.world.builders.AetherFeatureBuilders;
import com.gildedgames.aether.common.world.foliageplacer.CrystalFoliagePlacer;
import com.gildedgames.aether.common.world.foliageplacer.HolidayFoliagePlacer;
import com.gildedgames.aether.common.world.gen.configuration.AercloudConfiguration;
import com.gildedgames.aether.common.world.gen.configuration.AetherLakeConfiguration;
import com.gildedgames.aether.common.world.gen.configuration.SimpleDiskConfiguration;
import com.gildedgames.aether.common.world.gen.feature.AercloudFeature;
import com.gildedgames.aether.common.world.gen.feature.AetherLakeFeature;
import com.gildedgames.aether.common.world.gen.feature.CrystalIslandFeature;
import com.gildedgames.aether.common.world.gen.feature.SimpleDiskFeature;
import com.gildedgames.aether.common.world.gen.placement.*;
import com.gildedgames.aether.common.world.treedecorator.HolidayTreeDecorator;
import com.gildedgames.aether.core.AetherConfig;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.OptionalInt;

@Mod.EventBusSubscriber(modid = Aether.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AetherFeatures {
    public static Feature<SimpleDiskConfiguration> SIMPLE_DISK = new SimpleDiskFeature(SimpleDiskConfiguration.CODEC);
    public static Feature<AercloudConfiguration> AERCLOUD = new AercloudFeature(AercloudConfiguration.CODEC);
    //public static Feature<NoneFeatureConfiguration> HOLYSTONE_SPHERE = new HolystoneSphereFeature(NoneFeatureConfiguration.CODEC); // This is for Gold Dungeons
    public static Feature<NoneFeatureConfiguration> CRYSTAL_ISLAND = new CrystalIslandFeature(NoneFeatureConfiguration.CODEC);
    public static Feature<AetherLakeConfiguration> LAKE = new AetherLakeFeature(AetherLakeConfiguration.CODEC);

    @SubscribeEvent //This cannot be moved to DeferredRegister or the features won't be able to be added to the biomes at registry time.
    public static void register(RegistryEvent.Register<Feature<?>> event) {
        IForgeRegistry<Feature<?>> registry = event.getRegistry();
        register(registry, "simple_disk", SIMPLE_DISK);
        register(registry, "aercloud", AERCLOUD);
        register(registry, "crystal_island", CRYSTAL_ISLAND);
        register(registry, "lake", LAKE);
    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> void register(IForgeRegistry<Feature<?>> registry, String name, F value) {
        value.setRegistryName(new ResourceLocation(Aether.MODID, name).toString());
        registry.register(value);
    }

    public static class ConfiguredFeatures {
        public static final Holder<ConfiguredFeature<AercloudConfiguration, ?>> COLD_AERCLOUD = register("cold_aercloud", AERCLOUD,
                AetherFeatureBuilders.createAercloudConfig(16, States.COLD_AERCLOUD));
        public static final Holder<ConfiguredFeature<AercloudConfiguration, ?>> BLUE_AERCLOUD = register("blue_aercloud", AERCLOUD,
                AetherFeatureBuilders.createAercloudConfig(8, States.BLUE_AERCLOUD));
        public static final Holder<ConfiguredFeature<AercloudConfiguration, ?>> GOLDEN_AERCLOUD = register("golden_aercloud", AERCLOUD,
                AetherFeatureBuilders.createAercloudConfig(4, States.GOLDEN_AERCLOUD));
        public static final Holder<ConfiguredFeature<AercloudConfiguration, ?>> PINK_AERCLOUD = register("pink_aercloud", AERCLOUD,
                AetherFeatureBuilders.createAercloudConfig(1, States.PINK_AERCLOUD));

        public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> CRYSTAL_ISLAND_CONFIGURED_FEATURE = register("crystal_island", CRYSTAL_ISLAND, NoneFeatureConfiguration.INSTANCE);

        public static final Holder<ConfiguredFeature<TreeConfiguration, ?>> SKYROOT_TREE_CONFIGURED_FEATURE = register("skyroot_tree", Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(States.SKYROOT_LOG),
                        new StraightTrunkPlacer(4, 2, 0),
                        BlockStateProvider.simple(States.SKYROOT_LEAVES),
                        new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                        new TwoLayersFeatureSize(1, 0, 1)
                ).ignoreVines().build());

        public static final Holder<ConfiguredFeature<TreeConfiguration, ?>> GOLDEN_OAK_TREE_CONFIGURED_FEATURE = register("golden_oak_tree", Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(States.GOLDEN_OAK_LOG),
                        new FancyTrunkPlacer(9, 5, 0),
                        BlockStateProvider.simple(States.GOLDEN_OAK_LEAVES),
                        new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4),
                        new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(10))
                ).ignoreVines().build());

        public static final Holder<ConfiguredFeature<TreeConfiguration, ?>> CRYSTAL_TREE_CONFIGURED_FEATURE = register("crystal_tree", Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(States.SKYROOT_LOG),
                        new StraightTrunkPlacer(7, 0, 0),
                        new WeightedStateProvider(new SimpleWeightedRandomList.Builder<BlockState>().add(States.CRYSTAL_LEAVES, 4).add(States.CRYSTAL_FRUIT_LEAVES, 1).build()),
                        new CrystalFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), ConstantInt.of(6)),
                        new TwoLayersFeatureSize(1, 0, 1)).ignoreVines().build());

        public static final Holder<ConfiguredFeature<TreeConfiguration, ?>> HOLIDAY_TREE_CONFIGURED_FEATURE = register("holiday_tree", Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(States.SKYROOT_LOG),
                        new StraightTrunkPlacer(9, 0, 0),
                        new WeightedStateProvider(new SimpleWeightedRandomList.Builder<BlockState>().add(States.HOLIDAY_LEAVES, 4).add(States.DECORATED_HOLIDAY_LEAVES, 1).build()),
                        new HolidayFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), ConstantInt.of(8)),
                        new TwoLayersFeatureSize(1, 0, 1)).ignoreVines()
                        .decorators(ImmutableList.of(new HolidayTreeDecorator(new WeightedStateProvider(new SimpleWeightedRandomList.Builder<BlockState>().add(States.SNOW, 10).add(States.PRESENT, 1).build()))))
                        .build());

        public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> FLOWER_PATCH_CONFIGURED_FEATURE = register("flower_patch", Feature.FLOWER,
                AetherFeatureBuilders.grassPatch(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(States.PURPLE_FLOWER, 2)
                        .add(States.WHITE_FLOWER, 2)
                        .add(States.BERRY_BUSH, 1)), 64
                ));

        public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> GRASS_PATCH_CONFIGURED_FEATURE = register("grass_patch", Feature.RANDOM_PATCH,
                AetherFeatureBuilders.grassPatch(BlockStateProvider.simple(Blocks.GRASS), 32));

        public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> TALL_GRASS_PATCH_CONFIGURED_FEATURE = register("tall_grass_patch", Feature.RANDOM_PATCH,
                AetherFeatureBuilders.tallGrassPatch(BlockStateProvider.simple(Blocks.TALL_GRASS)));

        public static final Holder<ConfiguredFeature<SimpleDiskConfiguration, ?>> QUICKSOIL_SHELF_CONFIGURED_FEATURE = register("quicksoil_shelf", SIMPLE_DISK,
                new SimpleDiskConfiguration(
                        UniformFloat.of(Mth.sqrt(12), 5), // sqrt(12) is old static value
                        BlockStateProvider.simple(States.QUICKSOIL),
                        3
                ));

        public static final Holder<ConfiguredFeature<AetherLakeConfiguration, ?>> WATER_LAKE_CONFIGURED_FEATURE = register("water_lake", LAKE,
                AetherFeatureBuilders.lake(BlockStateProvider.simple(Blocks.WATER), BlockStateProvider.simple(AetherBlocks.AETHER_GRASS_BLOCK.get())));

        public static final Holder<ConfiguredFeature<SpringConfiguration, ?>> WATER_SPRING_CONFIGURED_FEATURE = register("water_spring", Feature.SPRING,
                AetherFeatureBuilders.spring(Fluids.WATER.defaultFluidState(), true, 4, 1, HolderSet.direct(Block::builtInRegistryHolder, AetherBlocks.HOLYSTONE.get(), AetherBlocks.AETHER_DIRT.get())));

        public static final Holder<ConfiguredFeature<OreConfiguration, ?>> ORE_AETHER_DIRT_CONFIGURED_FEATURE = register("aether_dirt_ore", Feature.ORE,
                new OreConfiguration(Tests.HOLYSTONE, States.AETHER_DIRT, 33));
        public static final Holder<ConfiguredFeature<OreConfiguration, ?>> ORE_ICESTONE_CONFIGURED_FEATURE = register("icestone_ore", Feature.ORE,
                new OreConfiguration(Tests.HOLYSTONE, States.ICESTONE, 16));
        public static final Holder<ConfiguredFeature<OreConfiguration, ?>> ORE_AMBROSIUM_CONFIGURED_FEATURE = register("ambrosium_ore", Feature.ORE,
                new OreConfiguration(Tests.HOLYSTONE, States.AMBROSIUM_ORE, 16));
        public static final Holder<ConfiguredFeature<OreConfiguration, ?>> ORE_ZANITE_CONFIGURED_FEATURE = register("zanite_ore", Feature.ORE,
                new OreConfiguration(Tests.HOLYSTONE, States.ZANITE_ORE, 8));
        public static final Holder<ConfiguredFeature<OreConfiguration, ?>> ORE_GRAVITITE_COMMON_CONFIGURED_FEATURE = register("gravitite_ore_common", Feature.ORE,
                new OreConfiguration(Tests.HOLYSTONE, States.GRAVITITE_ORE, 6, 0.9F));
        public static final Holder<ConfiguredFeature<OreConfiguration, ?>> ORE_GRAVITITE_DENSE_CONFIGURED_FEATURE = register("gravitite_ore_dense", Feature.ORE,
                new OreConfiguration(Tests.HOLYSTONE, States.GRAVITITE_ORE, 3, 0.5F));

        public static final Holder<ConfiguredFeature<RandomFeatureConfiguration, ?>> TREES_SKYROOT_AND_GOLDEN_OAK_CONFIGURED_FEATURE = register("trees_skyroot_and_golden_oak", Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(
                        PlacementUtils.inlinePlaced(GOLDEN_OAK_TREE_CONFIGURED_FEATURE, PlacementUtils.filteredByBlockSurvival(AetherBlocks.GOLDEN_OAK_SAPLING.get())), 0.01F)),
                        PlacementUtils.inlinePlaced(SKYROOT_TREE_CONFIGURED_FEATURE, PlacementUtils.filteredByBlockSurvival(AetherBlocks.SKYROOT_SAPLING.get()))));

        public static final Holder<ConfiguredFeature<RandomFeatureConfiguration, ?>> TREES_GOLDEN_OAK_AND_SKYROOT_CONFIGURED_FEATURE = register("trees_golden_oak_and_skyroot", Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(
                        PlacementUtils.inlinePlaced(SKYROOT_TREE_CONFIGURED_FEATURE, PlacementUtils.filteredByBlockSurvival(AetherBlocks.SKYROOT_SAPLING.get())), 0.1F)),
                        PlacementUtils.inlinePlaced(GOLDEN_OAK_TREE_CONFIGURED_FEATURE, PlacementUtils.filteredByBlockSurvival(AetherBlocks.GOLDEN_OAK_SAPLING.get()))));

        public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<ConfiguredFeature<FC, ?>> register(String name, F feature, FC configuration) {
            return BuiltinRegistries.registerExact(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(Aether.MODID, name).toString(), new ConfiguredFeature<>(feature, configuration));
        }
    }

    public static class PlacedFeatures {
        public static final Holder<PlacedFeature> COLD_AERCLOUD_PLACED_FEATURE = register("cold_aercloud", ConfiguredFeatures.COLD_AERCLOUD, AetherFeatureBuilders.createAercloudPlacements(128, 5));
        public static final Holder<PlacedFeature> BLUE_AERCLOUD_PLACED_FEATURE = register("blue_aercloud", ConfiguredFeatures.BLUE_AERCLOUD, AetherFeatureBuilders.createAercloudPlacements(96, 5));
        public static final Holder<PlacedFeature> GOLDEN_AERCLOUD_PLACED_FEATURE = register("golden_aercloud", ConfiguredFeatures.GOLDEN_AERCLOUD, AetherFeatureBuilders.createAercloudPlacements(160, 5));
        public static final Holder<PlacedFeature> PINK_AERCLOUD_PLACED_FEATURE = register("pink_aercloud", ConfiguredFeatures.PINK_AERCLOUD, AetherFeatureBuilders.createPinkAercloudPlacements(160, 7));

        public static final Holder<PlacedFeature> SKYROOT_GROVE_TREES_PLACED_FEATURE = register("skyroot_grove_trees_placed_feature", ConfiguredFeatures.TREES_SKYROOT_AND_GOLDEN_OAK_CONFIGURED_FEATURE,
                AetherFeatureBuilders.treePlacement(PlacementUtils.countExtra(2, 0.1F, 1)));

        public static final Holder<PlacedFeature> SKYROOT_FOREST_TREES_PLACED_FEATURE = register("skyroot_forest_trees_placed_feature", ConfiguredFeatures.TREES_SKYROOT_AND_GOLDEN_OAK_CONFIGURED_FEATURE,
                AetherFeatureBuilders.treePlacement(PlacementUtils.countExtra(7, 0.1F, 1)));

        public static final Holder<PlacedFeature> SKYROOT_THICKET_TREES_PLACED_FEATURE = register("skyroot_thicket_trees_placed_feature", ConfiguredFeatures.TREES_SKYROOT_AND_GOLDEN_OAK_CONFIGURED_FEATURE,
                AetherFeatureBuilders.treePlacement(PlacementUtils.countExtra(15, 0.1F, 1)));

        public static final Holder<PlacedFeature> GOLDEN_FOREST_TREES_PLACED_FEATURE = register("golden_forest_trees_placed_feature", ConfiguredFeatures.TREES_GOLDEN_OAK_AND_SKYROOT_CONFIGURED_FEATURE,
                AetherFeatureBuilders.treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));

        public static final Holder<PlacedFeature> CRYSTAL_ISLAND_PLACED_FEATURE = register("crystal_island", ConfiguredFeatures.CRYSTAL_ISLAND_CONFIGURED_FEATURE,
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(80), VerticalAnchor.absolute(120)),
                RarityFilter.onAverageOnceEvery(16));

        public static final Holder<PlacedFeature> HOLIDAY_TREE_PLACED_FEATURE = register("holiday_tree", ConfiguredFeatures.HOLIDAY_TREE_CONFIGURED_FEATURE,
                new HolidayFilter(),
                CountOnEveryLayerPlacement.of(1),
                RarityFilter.onAverageOnceEvery(48),
                PlacementUtils.filteredByBlockSurvival(AetherBlocks.SKYROOT_SAPLING.get()));

        public static final Holder<PlacedFeature> FLOWER_PATCH_PLACED_FEATURE = register("flower_patch", ConfiguredFeatures.FLOWER_PATCH_CONFIGURED_FEATURE,
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());

        public static final Holder<PlacedFeature> GRASS_PATCH_PLACED_FEATURE = register("grass_patch", ConfiguredFeatures.GRASS_PATCH_CONFIGURED_FEATURE,
                new ConfigFilter(AetherConfig.COMMON.generate_tall_grass),
                CountPlacement.of(2),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome());

        public static final Holder<PlacedFeature> TALL_GRASS_PATCH_PLACED_FEATURE = register("tall_grass_patch", ConfiguredFeatures.TALL_GRASS_PATCH_CONFIGURED_FEATURE,
                new ConfigFilter(AetherConfig.COMMON.generate_tall_grass),
                NoiseThresholdCountPlacement.of(-0.8D, 0, 7),
                RarityFilter.onAverageOnceEvery(32),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome());

        public static final Holder<PlacedFeature> QUICKSOIL_SHELF_PLACED_FEATURE = register("quicksoil_shelf", ConfiguredFeatures.QUICKSOIL_SHELF_CONFIGURED_FEATURE,
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                new ElevationAdjustment(UniformInt.of(-4, -2)),
                new ElevationFilter(47, 70),
                BlockPredicateFilter.forPredicate(BlockPredicate.matchesTag(AetherTags.Blocks.QUICKSOIL_CAN_GENERATE)));
        // FIXME once Terrain can go above 63 again, change 47 -> 63

        public static final Holder<PlacedFeature> WATER_LAKE_PLACED_FEATURE = register("water_lake", ConfiguredFeatures.WATER_LAKE_CONFIGURED_FEATURE,
                RarityFilter.onAverageOnceEvery(40),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome());

        public static final Holder<PlacedFeature> WATER_SPRING_PLACED_FEATURE = register("water_spring", ConfiguredFeatures.WATER_SPRING_CONFIGURED_FEATURE,
                CountPlacement.of(25),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(192)), BiomeFilter.biome());

        public static final Holder<PlacedFeature> ORE_AETHER_DIRT_PLACED_FEATURE = register("aether_dirt_ore", ConfiguredFeatures.ORE_AETHER_DIRT_CONFIGURED_FEATURE,
                AetherFeatureBuilders.commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.belowTop(0))));
        public static final Holder<PlacedFeature> ORE_ICESTONE_PLACED_FEATURE = register("icestone_ore", ConfiguredFeatures.ORE_ICESTONE_CONFIGURED_FEATURE,
                AetherFeatureBuilders.commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.belowTop(0))));
        public static final Holder<PlacedFeature> ORE_AMBROSIUM_PLACED_FEATURE = register("ambrosium_ore", ConfiguredFeatures.ORE_AMBROSIUM_CONFIGURED_FEATURE,
                AetherFeatureBuilders.commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.belowTop(0))));
        public static final Holder<PlacedFeature> ORE_ZANITE_PLACED_FEATURE = register("zanite_ore", ConfiguredFeatures.ORE_ZANITE_CONFIGURED_FEATURE,
                AetherFeatureBuilders.commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.belowTop(0))));
        public static final Holder<PlacedFeature> ORE_GRAVITITE_COMMON_PLACED_FEATURE = register("gravitite_ore_common", ConfiguredFeatures.ORE_GRAVITITE_COMMON_CONFIGURED_FEATURE,
                AetherFeatureBuilders.commonOrePlacement(7, HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.belowTop(0))));
        public static final Holder<PlacedFeature> ORE_GRAVITITE_DENSE_PLACED_FEATURE = register("gravitite_ore_dense", ConfiguredFeatures.ORE_GRAVITITE_DENSE_CONFIGURED_FEATURE,
                AetherFeatureBuilders.commonOrePlacement(3, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(0), VerticalAnchor.belowTop(0))));

        public static Holder<PlacedFeature> register(String name, Holder<? extends ConfiguredFeature<?, ?>> configuredFeature, List<PlacementModifier> placementModifiers) {
            return BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation(Aether.MODID, name).toString(), new PlacedFeature(Holder.hackyErase(configuredFeature), List.copyOf(placementModifiers)));
        }

        public static Holder<PlacedFeature> register(String name, Holder<? extends ConfiguredFeature<?, ?>> configuredFeature, PlacementModifier... placementModifiers) {
            return register(name, configuredFeature, List.of(placementModifiers));
        }
    }

    public static class States {
        public static final BlockState COLD_AERCLOUD = AetherBlocks.COLD_AERCLOUD.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState BLUE_AERCLOUD = AetherBlocks.BLUE_AERCLOUD.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState GOLDEN_AERCLOUD = AetherBlocks.GOLDEN_AERCLOUD.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState PINK_AERCLOUD = AetherBlocks.PINK_AERCLOUD.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);

        public static final BlockState SKYROOT_LOG = AetherBlocks.SKYROOT_LOG.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState SKYROOT_LEAVES = AetherBlocks.SKYROOT_LEAVES.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);

        public static final BlockState GOLDEN_OAK_LOG = AetherBlocks.GOLDEN_OAK_LOG.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState GOLDEN_OAK_LEAVES = AetherBlocks.GOLDEN_OAK_LEAVES.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);

        public static final BlockState CRYSTAL_LEAVES = AetherBlocks.CRYSTAL_LEAVES.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState CRYSTAL_FRUIT_LEAVES = AetherBlocks.CRYSTAL_FRUIT_LEAVES.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);

        public static final BlockState HOLIDAY_LEAVES = AetherBlocks.HOLIDAY_LEAVES.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState DECORATED_HOLIDAY_LEAVES = AetherBlocks.DECORATED_HOLIDAY_LEAVES.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState SNOW = Blocks.SNOW.defaultBlockState();
        public static final BlockState PRESENT = AetherBlocks.PRESENT.get().defaultBlockState();
        public static final BlockState AIR = Blocks.AIR.defaultBlockState();

        public static final BlockState PURPLE_FLOWER = AetherBlocks.PURPLE_FLOWER.get().defaultBlockState();
        public static final BlockState WHITE_FLOWER = AetherBlocks.WHITE_FLOWER.get().defaultBlockState();
        public static final BlockState BERRY_BUSH = AetherBlocks.BERRY_BUSH.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);

        public static final BlockState QUICKSOIL = AetherBlocks.QUICKSOIL.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);

        public static final BlockState AETHER_GRASS_BLOCK = AetherBlocks.AETHER_GRASS_BLOCK.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState AETHER_DIRT = AetherBlocks.AETHER_DIRT.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState HOLYSTONE = AetherBlocks.HOLYSTONE.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState ICESTONE = AetherBlocks.ICESTONE.get().defaultBlockState();
        public static final BlockState AMBROSIUM_ORE = AetherBlocks.AMBROSIUM_ORE.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true);
        public static final BlockState ZANITE_ORE = AetherBlocks.ZANITE_ORE.get().defaultBlockState();
        public static final BlockState GRAVITITE_ORE = AetherBlocks.GRAVITITE_ORE.get().defaultBlockState();
    }

    public static class Tests {
        public static final RuleTest HOLYSTONE = new TagMatchTest(AetherTags.Blocks.HOLYSTONE);
    }
}
