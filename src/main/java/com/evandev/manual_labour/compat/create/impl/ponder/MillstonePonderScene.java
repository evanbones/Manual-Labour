package com.evandev.manual_labour.compat.create.impl.ponder;

import com.evandev.manual_labour.compat.create.impl.millstone.MillstoneBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class MillstonePonderScene {

    public static void millstone(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("millstone", "Grinding Items with the Millstone");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);

        BlockPos millstonePos = util.grid().at(2, 1, 2);
        Selection millstoneSelect = util.select().position(millstonePos);
        Selection structuralBase = util.select().fromTo(1, 1, 1, 3, 1, 3).substract(millstoneSelect);

        scene.world().showSection(structuralBase, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(millstoneSelect, Direction.DOWN);
        scene.idle(10);

        Vec3 millstoneTop = util.vector().topOf(millstonePos);
        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("The Millstone grinds items down when spun")
                .pointAt(millstoneTop)
                .placeNearTarget();
        scene.idle(70);

        scene.world().setKineticSpeed(millstoneSelect, 32);
        scene.effects().indicateSuccess(millstonePos);
        scene.idle(10);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .text("Standing on top will spin along with it")
                .pointAt(millstoneTop)
                .placeNearTarget();
        scene.idle(70);

        ItemStack wheat = new ItemStack(Items.WHEAT);
        Vec3 entitySpawn = util.vector().topOf(millstonePos.above(3));
        ElementLink<EntityElement> entity =
                scene.world().createItemEntity(entitySpawn, util.vector().of(0, 0.2, 0), wheat);
        scene.idle(18);
        scene.world().modifyEntity(entity, Entity::discard);
        scene.world().modifyBlockEntity(millstonePos, MillstoneBlockEntity.class,
                ms -> ms.inputInv.setStackInSlot(0, wheat.copy()));
        scene.idle(10);
        scene.overlay().showControls(millstoneTop, Pointing.DOWN, 30).withItem(wheat);
        scene.idle(7);

        scene.overlay().showText(50)
                .text("Throw or insert items on top to feed the Millstone")
                .pointAt(millstoneTop)
                .placeNearTarget();
        scene.idle(60);

        scene.world().modifyBlockEntity(millstonePos, MillstoneBlockEntity.class,
                ms -> ms.inputInv.setStackInSlot(0, ItemStack.EMPTY));

        scene.overlay().showText(60)
                .text("Once ground, right-click with an empty hand to collect the results")
                .pointAt(util.vector().blockSurface(millstonePos, Direction.WEST))
                .placeNearTarget();
        scene.idle(70);

        scene.world().setKineticSpeed(millstoneSelect, 0);
        scene.overlay().showText(70)
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .text("Spinning it too fast will cause it to stop grinding")
                .pointAt(millstoneTop)
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }
}
