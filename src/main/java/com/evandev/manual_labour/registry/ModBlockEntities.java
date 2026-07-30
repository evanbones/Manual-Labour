package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.content.block.entity.MillstoneBlockEntity;
import com.evandev.manual_labour.content.block.entity.MortarBlockEntity;
import com.evandev.manual_labour.content.block.entity.WorkstoneBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WorkstoneBlockEntity>> WORKSTONE =
            BLOCK_ENTITIES.register("workstone", () -> BlockEntityType.Builder.of(WorkstoneBlockEntity::new, ModBlocks.WORKSTONE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MortarBlockEntity>> MORTAR =
            BLOCK_ENTITIES.register("mortar", () -> BlockEntityType.Builder.of(MortarBlockEntity::new, ModBlocks.MORTAR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MillstoneBlockEntity>> MILLSTONE =
            BLOCK_ENTITIES.register("millstone", () -> BlockEntityType.Builder.of(MillstoneBlockEntity::new, ModBlocks.MILLSTONE.get()).build(null));
}
