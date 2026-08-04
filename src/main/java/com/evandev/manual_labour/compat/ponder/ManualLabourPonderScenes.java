package com.evandev.manual_labour.compat.ponder;

import com.evandev.manual_labour.content.block.entity.MillstoneBlockEntity;
import com.evandev.manual_labour.content.block.entity.MortarBlockEntity;
import com.evandev.manual_labour.registry.ModItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ManualLabourPonderScenes {

    public static void mortar(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mortar", "Grinding and Mixing in the Mortar");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);

        BlockPos mortarPos = util.grid().at(2, 1, 2);
        scene.world().showSection(util.select().position(mortarPos), Direction.DOWN);
        scene.idle(10);

        Vec3 mortarTop = util.vector().topOf(mortarPos);
        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("The Mortar grinds and mixes items entirely by hand")
                .pointAt(mortarTop)
                .placeNearTarget();
        scene.idle(80);

        ItemStack wheat = new ItemStack(Items.WHEAT);
        Vec3 entitySpawn = util.vector().topOf(mortarPos.above(2));
        ElementLink<EntityElement> wheatEntity =
                scene.world().createItemEntity(entitySpawn, util.vector().of(0, 0.2, 0), wheat);
        scene.idle(15);
        scene.world().modifyEntity(wheatEntity, Entity::discard);
        scene.world().modifyBlockEntity(mortarPos, MortarBlockEntity.class,
                mortar -> mortar.insertFromPlayer(wheat.copy()));
        scene.idle(10);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("Right-click items onto it to add ingredients")
                .pointAt(mortarTop)
                .placeNearTarget();
        scene.idle(70);

        ItemStack pestle = new ItemStack(ModItems.PESTLE.get());
        ItemStack ladle = new ItemStack(ModItems.LADLE.get());
        scene.overlay().showControls(mortarTop, Pointing.DOWN, 40).rightClick().withItem(pestle);
        scene.idle(10);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Hold right-click with a Pestle to grind, or a Ladle to mix")
                .pointAt(mortarTop)
                .placeNearTarget();
        scene.idle(80);

        scene.effects().indicateSuccess(mortarPos);
        scene.idle(10);

        scene.overlay().showText(60)
                .text("Each tool wears down a little with every use")
                .pointAt(mortarTop)
                .placeNearTarget();
        scene.idle(70);

        scene.world().modifyBlockEntity(mortarPos, MortarBlockEntity.class,
                mortar -> mortar.placeDecorativeTool(pestle.copy(), true));
        scene.idle(15);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .text("Shift-right-click with a Pestle or Ladle to rest it on the Mortar")
                .pointAt(mortarTop)
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlockEntity(mortarPos, MortarBlockEntity.class, MortarBlockEntity::removeDecorativeTool);
        scene.idle(10);

        scene.overlay().showText(60)
                .text("Fluids can also be poured in or drawn out using buckets and bottles")
                .pointAt(mortarTop)
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }

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
