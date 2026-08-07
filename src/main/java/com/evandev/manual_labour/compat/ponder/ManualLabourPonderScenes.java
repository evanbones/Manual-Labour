package com.evandev.manual_labour.compat.ponder;

import com.evandev.manual_labour.content.block.entity.MortarBlockEntity;
import com.evandev.manual_labour.registry.ModItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ManualLabourPonderScenes {

    public static void mortar(SceneBuilder scene, SceneBuildingUtil util) {
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
                .text("Fluids can also be poured in or drawn out with a bucket")
                .pointAt(mortarTop)
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }
}
