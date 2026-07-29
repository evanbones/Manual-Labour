package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.content.block.MillstoneStructuralBlock;
import com.evandev.manual_labour.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Constants.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile workstoneModel = models().withExistingParent("workstone", mcLoc("block/block"))
                .texture("particle", modLoc("block/workstone_top"))
                .texture("bottom", modLoc("block/workstone_bottom"))
                .texture("top", modLoc("block/workstone_top"))
                .texture("front", modLoc("block/workstone_front"))
                .texture("side", modLoc("block/workstone_side"))
                .element()
                .from(3, 0, 3).to(13, 12, 13)
                .face(Direction.DOWN).texture("#bottom").end()
                .face(Direction.UP).texture("#top").end()
                .face(Direction.NORTH).texture("#front").end()
                .face(Direction.SOUTH).texture("#front").end()
                .face(Direction.WEST).texture("#side").end()
                .face(Direction.EAST).texture("#side").end()
                .end();

        horizontalBlock(ModBlocks.WORKSTONE.get(), workstoneModel);
        simpleBlockItem(ModBlocks.WORKSTONE.get(), workstoneModel);

        ModelFile mortarModel = models().getExistingFile(modLoc("block/mortar"));
        horizontalBlock(ModBlocks.MORTAR.get(), mortarModel);
        simpleBlockItem(ModBlocks.MORTAR.get(), mortarModel);

        registerMillstone();
    }

    private void registerMillstone() {
        simpleBlock(ModBlocks.MILLSTONE.get(), models().getExistingFile(modLoc("block/millstone/base_center")));
        simpleBlock(ModBlocks.MILLSTONE_ROTOR.get(), models().getExistingFile(modLoc("block/millstone/rotor_static")));

        ModelFile topInvisible = models().getExistingFile(modLoc("block/millstone/top_invisible"));
        ModelFile baseN = models().getExistingFile(modLoc("block/millstone/base_n"));
        ModelFile baseS = models().getExistingFile(modLoc("block/millstone/base_s"));
        ModelFile baseE = models().getExistingFile(modLoc("block/millstone/base_e"));
        ModelFile baseW = models().getExistingFile(modLoc("block/millstone/base_w"));
        ModelFile baseNe = models().getExistingFile(modLoc("block/millstone/base_ne"));
        ModelFile baseNw = models().getExistingFile(modLoc("block/millstone/base_nw"));
        ModelFile baseSe = models().getExistingFile(modLoc("block/millstone/base_se"));
        ModelFile baseSw = models().getExistingFile(modLoc("block/millstone/base_sw"));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(ModBlocks.MILLSTONE_STRUCTURAL.get());
        builder.part().modelFile(topInvisible).addModel().condition(MillstoneStructuralBlock.TOP, true).end();

        builder.part().modelFile(baseN).addModel()
                .condition(MillstoneStructuralBlock.TOP, false).condition(MillstoneStructuralBlock.CORNER, false).condition(MillstoneStructuralBlock.FACING, Direction.SOUTH).end();
        builder.part().modelFile(baseS).addModel()
                .condition(MillstoneStructuralBlock.TOP, false).condition(MillstoneStructuralBlock.CORNER, false).condition(MillstoneStructuralBlock.FACING, Direction.NORTH).end();
        builder.part().modelFile(baseE).addModel()
                .condition(MillstoneStructuralBlock.TOP, false).condition(MillstoneStructuralBlock.CORNER, false).condition(MillstoneStructuralBlock.FACING, Direction.WEST).end();
        builder.part().modelFile(baseW).addModel()
                .condition(MillstoneStructuralBlock.TOP, false).condition(MillstoneStructuralBlock.CORNER, false).condition(MillstoneStructuralBlock.FACING, Direction.EAST).end();

        builder.part().modelFile(baseNe).addModel()
                .condition(MillstoneStructuralBlock.TOP, false).condition(MillstoneStructuralBlock.CORNER, true).condition(MillstoneStructuralBlock.FACING, Direction.SOUTH).end();
        builder.part().modelFile(baseNw).addModel()
                .condition(MillstoneStructuralBlock.TOP, false).condition(MillstoneStructuralBlock.CORNER, true).condition(MillstoneStructuralBlock.FACING, Direction.EAST).end();
        builder.part().modelFile(baseSe).addModel()
                .condition(MillstoneStructuralBlock.TOP, false).condition(MillstoneStructuralBlock.CORNER, true).condition(MillstoneStructuralBlock.FACING, Direction.WEST).end();
        builder.part().modelFile(baseSw).addModel()
                .condition(MillstoneStructuralBlock.TOP, false).condition(MillstoneStructuralBlock.CORNER, true).condition(MillstoneStructuralBlock.FACING, Direction.NORTH).end();
    }
}
