package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.content.block.MillstoneStructuralBlock;
import com.evandev.manual_labour.content.block.MillstoneStructure;
import com.evandev.manual_labour.registry.ModBlocks;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import net.minecraft.core.BlockPos;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PonderSchematicProvider implements DataProvider {
    private static final int BASE_PLATE_SIZE = 5;
    private static final BlockPos CENTER = new BlockPos(2, 1, 2);

    private final PackOutput.PathProvider pathProvider;

    public PonderSchematicProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "ponder");
    }

    private static Map<BlockPos, BlockState> baseFloor() {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        BlockState concrete = Blocks.WHITE_CONCRETE.defaultBlockState();
        BlockState snow = Blocks.SNOW_BLOCK.defaultBlockState();
        for (int x = 0; x < BASE_PLATE_SIZE; x++) {
            for (int z = 0; z < BASE_PLATE_SIZE; z++) {
                BlockState state = ((x + z) % 2 == 0) ? concrete : snow;
                blocks.put(new BlockPos(x, 0, z), state);
            }
        }
        return blocks;
    }

    private static Map<BlockPos, BlockState> mortarScene() {
        Map<BlockPos, BlockState> blocks = baseFloor();
        blocks.put(CENTER, ModBlocks.MORTAR.get().defaultBlockState());
        return blocks;
    }

    private static Map<BlockPos, BlockState> millstoneScene() {
        Map<BlockPos, BlockState> blocks = baseFloor();
        blocks.put(CENTER, ModBlocks.MILLSTONE.get().defaultBlockState());
        for (BlockPos offset : MillstoneStructure.ALL_OFFSETS) {
            BlockState structural = ModBlocks.MILLSTONE_STRUCTURAL.get().defaultBlockState()
                    .setValue(DirectionalBlock.FACING, MillstoneStructure.baseFacing(offset))
                    .setValue(MillstoneStructuralBlock.CORNER, MillstoneStructure.isCorner(offset));
            blocks.put(CENTER.offset(offset), structural);
        }
        return blocks;
    }

    private static CompoundTag buildStructureTag(Map<BlockPos, BlockState> blocks) {
        List<BlockState> palette = new ArrayList<>();
        ListTag blockList = new ListTag();

        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            int paletteIndex = palette.indexOf(state);
            if (paletteIndex < 0) {
                paletteIndex = palette.size();
                palette.add(state);
            }

            CompoundTag blockTag = new CompoundTag();
            ListTag posTag = new ListTag();
            posTag.add(IntTag.valueOf(pos.getX()));
            posTag.add(IntTag.valueOf(pos.getY()));
            posTag.add(IntTag.valueOf(pos.getZ()));
            blockTag.put("pos", posTag);
            blockTag.putInt("state", paletteIndex);
            blockList.add(blockTag);

            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        ListTag paletteTag = new ListTag();
        for (BlockState state : palette) {
            paletteTag.add(NbtUtils.writeBlockState(state));
        }

        ListTag sizeTag = new ListTag();
        sizeTag.add(IntTag.valueOf(maxX + 1));
        sizeTag.add(IntTag.valueOf(maxY + 1));
        sizeTag.add(IntTag.valueOf(maxZ + 1));

        CompoundTag tag = new CompoundTag();
        tag.put("size", sizeTag);
        tag.put("entities", new ListTag());
        tag.put("blocks", blockList);
        tag.put("palette", paletteTag);
        return NbtUtils.addCurrentDataVersion(tag);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.allOf(
                writeSchematic(output, "mortar", mortarScene()),
                writeSchematic(output, "millstone", millstoneScene())
        );
    }

    private CompletableFuture<?> writeSchematic(CachedOutput output, String name, Map<BlockPos, BlockState> blocks) {
        return CompletableFuture.runAsync(() -> {
            try {
                CompoundTag tag = buildStructureTag(blocks);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                HashingOutputStream hashing = new HashingOutputStream(Hashing.sha1(), bytes);
                NbtIo.writeCompressed(tag, hashing);
                Path path = pathProvider.file(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), "nbt");
                output.writeIfNeeded(path, bytes.toByteArray(), hashing.hash());
            } catch (IOException e) {
                Constants.LOG.error("Failed to write ponder schematic " + name, e);
            }
        });
    }

    @Override
    public String getName() {
        return "Ponder Schematics";
    }
}
