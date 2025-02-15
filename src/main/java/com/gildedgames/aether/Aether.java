package com.gildedgames.aether;

import com.gildedgames.aether.client.registry.AetherParticleTypes;
import com.gildedgames.aether.client.registry.AetherSoundEvents;
import com.gildedgames.aether.common.block.entity.IncubatorBlockEntity;
import com.gildedgames.aether.common.block.util.dispenser.DispenseDartBehavior;
import com.gildedgames.aether.common.block.entity.AltarBlockEntity;
import com.gildedgames.aether.common.block.entity.FreezerBlockEntity;
import com.gildedgames.aether.common.registry.*;
import com.gildedgames.aether.common.registry.worldgen.AetherBiomes;
import com.gildedgames.aether.common.registry.worldgen.AetherFoliagePlacerTypes;
import com.gildedgames.aether.common.registry.worldgen.AetherNoiseGeneratorSettings;
import com.gildedgames.aether.common.registry.worldgen.AetherTreeDecoratorTypes;
import com.gildedgames.aether.common.world.gen.placement.PlacementModifiers;
import com.gildedgames.aether.core.AetherConfig;
import com.gildedgames.aether.core.data.*;
import com.gildedgames.aether.core.network.AetherPacketHandler;
import com.gildedgames.aether.core.resource.CombinedResourcePack;
import com.gildedgames.aether.core.util.SunAltarWhitelist;
import com.gildedgames.aether.core.util.TriviaReader;
import net.minecraft.SharedConstants;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.resource.PathResourcePack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;

import java.nio.file.Path;
import java.util.List;

@Mod(Aether.MODID)
@Mod.EventBusSubscriber(modid = Aether.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Aether
{
    public static final String MODID = "aether";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Path DIRECTORY = FMLPaths.CONFIGDIR.get().resolve("aether");

    public static TriviaReader TRIVIA_READER;

    public Aether() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::curiosSetup);
        modEventBus.addListener(this::dataSetup);
        modEventBus.addListener(this::packSetup);

        DeferredRegister<?>[] registers = {
                AetherBlocks.BLOCKS,
                AetherEntityTypes.ENTITIES,
                AetherEffects.EFFECTS,
                AetherItems.ITEMS,
                AetherParticleTypes.PARTICLES,
                AetherPOI.POI,
                AetherSoundEvents.SOUNDS,
                AetherContainerTypes.CONTAINERS,
                AetherBlockEntityTypes.BLOCK_ENTITIES,
                AetherRecipes.RECIPE_SERIALIZERS,
                AetherLootModifiers.GLOBAL_LOOT_MODIFIERS,
                AetherBiomes.BIOMES,
                AetherFoliagePlacerTypes.FOLIAGE_PLACERS,
                AetherTreeDecoratorTypes.TREE_DECORATORS
        };

        for (DeferredRegister<?> register : registers) {
            register.register(modEventBus);
        }

        AetherBlocks.registerWoodTypes();

        DIRECTORY.toFile().mkdirs();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AetherConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AetherConfig.CLIENT_SPEC);

        TRIVIA_READER = new TriviaReader();
    }

    @SubscribeEvent //This is not actually for registering RecipeSerializers.
    public static void register(RegistryEvent.Register<RecipeSerializer<?>> event) {
        SunAltarWhitelist.initialize();

        AetherLoot.init();
        AetherAdvancements.init();
        PlacementModifiers.init();
        AetherRecipes.RecipeTypes.init();
        AetherRecipeBookTypes.init();
        AetherNoiseGeneratorSettings.init();
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AetherPacketHandler.register();

            AetherBlocks.registerPots();
            AetherBlocks.registerFlammability();
            AetherBlocks.registerFreezables();

            AetherEntityTypes.registerSpawnPlacements();

            AetherItems.registerAbilities();

            registerDispenserBehaviors();
            registerCauldronInteractions();
            registerComposting();
            registerFuels();
        });
    }

    public void curiosSetup(InterModEnqueueEvent event) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("aether_pendant").icon(new ResourceLocation(Aether.MODID, "gui/slots/pendant")).hide().build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("aether_cape").icon(new ResourceLocation(Aether.MODID, "gui/slots/cape")).hide().build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("aether_ring").icon(new ResourceLocation(Aether.MODID, "gui/slots/ring")).size(2).hide().build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("aether_shield").icon(new ResourceLocation(Aether.MODID, "gui/slots/shield")).hide().build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("aether_gloves").icon(new ResourceLocation(Aether.MODID, "gui/slots/gloves")).hide().build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("aether_accessory").icon(new ResourceLocation(Aether.MODID, "gui/slots/misc")).size(2).hide().build());
    }

    public void dataSetup(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        if (event.includeClient()) {
            generator.addProvider(new AetherBlockStateData(generator, helper));
            generator.addProvider(new AetherItemModelData(generator, helper));
            generator.addProvider(new AetherLanguageData(generator));
            generator.addProvider(new AetherSoundData(generator, helper));
        }
        if (event.includeServer()) {
            generator.addProvider(new AetherRecipeData(generator));
            generator.addProvider(new AetherLootTableData(generator));
            generator.addProvider(new AetherLootModifierData(generator));
            AetherBlockTagData blockTags = new AetherBlockTagData(generator, helper);
            generator.addProvider(blockTags);
            generator.addProvider(new AetherItemTagData(generator, blockTags, helper));
            generator.addProvider(new AetherEntityTagData(generator, helper));
            generator.addProvider(new AetherFluidTagData(generator, helper));
            generator.addProvider(new AetherDimensionTagData(generator, helper));
            generator.addProvider(new AetherAdvancementData(generator, helper));
            generator.addProvider(new AetherWorldData(generator));
        }
    }

    public void packSetup(AddPackFindersEvent event) {
        setupReleasePack(event);
        setupBetaPack(event);
        setupCTMFixPack(event);
    }

    private void setupReleasePack(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            Path resourcePath = ModList.get().getModFileById(Aether.MODID).getFile().findResource("packs/classic_125");
            PathResourcePack pack = new PathResourcePack(ModList.get().getModFileById(Aether.MODID).getFile().getFileName() + ":" + resourcePath, resourcePath);
            createCombinedPack(event, resourcePath, pack, "builtin/aether_125_art", "Aether 1.2.5 Textures", "The classic look of the Aether from 1.2.5");
        }
    }

    private void setupBetaPack(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            Path resourcePath = ModList.get().getModFileById(Aether.MODID).getFile().findResource("packs/classic_b173");
            PathResourcePack pack = new PathResourcePack(ModList.get().getModFileById(Aether.MODID).getFile().getFileName() + ":" + resourcePath, resourcePath);
            createCombinedPack(event, resourcePath, pack, "builtin/aether_b173_art", "Aether b1.7.3 Textures", "The original look of the Aether from b1.7.3");
        }
    }

    private void createCombinedPack(AddPackFindersEvent event, Path sourcePath, PathResourcePack pack, String name, String title, String description) {
        Path baseResourcePath = ModList.get().getModFileById(Aether.MODID).getFile().findResource("packs/classic_base");
        PathResourcePack basePack = new PathResourcePack(ModList.get().getModFileById(Aether.MODID).getFile().getFileName() + ":" + baseResourcePath, baseResourcePath);
        List<PathResourcePack> mergedPacks = List.of(pack, basePack);
        event.addRepositorySource((packConsumer, packConstructor) ->
                packConsumer.accept(Pack.create(
                        name, false,
                        () -> new CombinedResourcePack(name, title, new PackMetadataSection(new TextComponent(description), PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())), mergedPacks, sourcePath),
                        packConstructor, Pack.Position.TOP, PackSource.BUILT_IN)
                ));
    }

    private void setupCTMFixPack(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES && ModList.get().isLoaded("ctm")) {
            Path resourcePath = ModList.get().getModFileById(Aether.MODID).getFile().findResource("packs/ctm_fix");
            PathResourcePack pack = new PathResourcePack(ModList.get().getModFileById(Aether.MODID).getFile().getFileName() + ":" + resourcePath, resourcePath);
            event.addRepositorySource((packConsumer, packConstructor) ->
                packConsumer.accept(packConstructor.create(
                        "builtin/aether_ctm_fix", new TextComponent("Aether CTM Fix"), true, () -> pack,
                        new PackMetadataSection(new TextComponent("Fixes Quicksoil Glass Panes when using CTM"), PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())),
                        Pack.Position.TOP, PackSource.BUILT_IN, false)
                ));
        }
    }

    private void registerDispenserBehaviors() {
        AetherDispenseBehaviors.DEFAULT_FIRE_CHARGE_BEHAVIOR = DispenserBlock.DISPENSER_REGISTRY.get(Items.FIRE_CHARGE);
        AetherDispenseBehaviors.DEFAULT_FLINT_AND_STEEL_BEHAVIOR = DispenserBlock.DISPENSER_REGISTRY.get(Items.FLINT_AND_STEEL);
        DispenserBlock.registerBehavior(AetherItems.GOLDEN_DART.get(), new DispenseDartBehavior(AetherItems.GOLDEN_DART));
        DispenserBlock.registerBehavior(AetherItems.POISON_DART.get(), new DispenseDartBehavior(AetherItems.POISON_DART));
        DispenserBlock.registerBehavior(AetherItems.ENCHANTED_DART.get(), new DispenseDartBehavior(AetherItems.ENCHANTED_DART));
        DispenserBlock.registerBehavior(AetherItems.LIGHTNING_KNIFE.get(), AetherDispenseBehaviors.DISPENSE_LIGHTNING_KNIFE_BEHAVIOR);
        DispenserBlock.registerBehavior(AetherItems.HAMMER_OF_NOTCH.get(), AetherDispenseBehaviors.DISPENSE_NOTCH_HAMMER_BEHAVIOR);
        DispenserBlock.registerBehavior(AetherItems.SKYROOT_WATER_BUCKET.get(), AetherDispenseBehaviors.SKYROOT_BUCKET_DISPENSE_BEHAVIOR);
		DispenserBlock.registerBehavior(AetherItems.SKYROOT_BUCKET.get(), AetherDispenseBehaviors.SKYROOT_BUCKET_PICKUP_BEHAVIOR);
        DispenserBlock.registerBehavior(AetherItems.AMBROSIUM_SHARD.get(), AetherDispenseBehaviors.DISPENSE_AMBROSIUM_BEHAVIOR);
        DispenserBlock.registerBehavior(AetherItems.SWET_BALL.get(), AetherDispenseBehaviors.DISPENSE_SWET_BALL_BEHAVIOR);
        DispenserBlock.registerBehavior(Items.FIRE_CHARGE, AetherDispenseBehaviors.DISPENSE_FIRE_CHARGE_BEHAVIOR);
        DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, AetherDispenseBehaviors.DISPENSE_FLINT_AND_STEEL);
    }

    private void registerCauldronInteractions() {
        CauldronInteraction.EMPTY.put(AetherItems.SKYROOT_WATER_BUCKET.get(), AetherCauldronInteractions.FILL_WATER);
        CauldronInteraction.WATER.put(AetherItems.SKYROOT_WATER_BUCKET.get(), AetherCauldronInteractions.FILL_WATER);
        CauldronInteraction.LAVA.put(AetherItems.SKYROOT_WATER_BUCKET.get(), AetherCauldronInteractions.FILL_WATER);
        CauldronInteraction.POWDER_SNOW.put(AetherItems.SKYROOT_WATER_BUCKET.get(), AetherCauldronInteractions.FILL_WATER);
        CauldronInteraction.WATER.put(AetherItems.SKYROOT_BUCKET.get(), AetherCauldronInteractions.EMPTY_WATER);
        CauldronInteraction.WATER.put(AetherItems.LEATHER_GLOVES.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.put(AetherItems.RED_CAPE.get(), AetherCauldronInteractions.CAPE);
        CauldronInteraction.WATER.put(AetherItems.BLUE_CAPE.get(), AetherCauldronInteractions.CAPE);
        CauldronInteraction.WATER.put(AetherItems.YELLOW_CAPE.get(), AetherCauldronInteractions.CAPE);
    }

    private void registerComposting() {
        ComposterBlock.add(0.3F, AetherBlocks.SKYROOT_LEAVES.get());
        ComposterBlock.add(0.3F, AetherBlocks.SKYROOT_SAPLING.get());
        ComposterBlock.add(0.3F, AetherBlocks.GOLDEN_OAK_LEAVES.get());
        ComposterBlock.add(0.3F, AetherBlocks.GOLDEN_OAK_SAPLING.get());
        ComposterBlock.add(0.3F, AetherBlocks.CRYSTAL_LEAVES.get());
        ComposterBlock.add(0.3F, AetherBlocks.CRYSTAL_FRUIT_LEAVES.get());
        ComposterBlock.add(0.3F, AetherBlocks.HOLIDAY_LEAVES.get());
        ComposterBlock.add(0.3F, AetherBlocks.DECORATED_HOLIDAY_LEAVES.get());
        ComposterBlock.add(0.3F, AetherItems.BLUE_BERRY.get());
        ComposterBlock.add(0.5F, AetherItems.ENCHANTED_BERRY.get());
        ComposterBlock.add(0.5F, AetherBlocks.BERRY_BUSH.get());
        ComposterBlock.add(0.5F, AetherBlocks.BERRY_BUSH_STEM.get());
        ComposterBlock.add(0.65F, AetherBlocks.WHITE_FLOWER.get());
        ComposterBlock.add(0.65F, AetherBlocks.PURPLE_FLOWER.get());
        ComposterBlock.add(0.65F, AetherItems.WHITE_APPLE.get());
    }

    private void registerFuels() {
        AltarBlockEntity.addItemEnchantingTime(AetherItems.AMBROSIUM_SHARD.get(), 500);
        FreezerBlockEntity.addItemFreezingTime(AetherBlocks.ICESTONE.get(), 500);
        IncubatorBlockEntity.addItemIncubatingTime(AetherBlocks.AMBROSIUM_TORCH.get(), 1000);
    }
}
