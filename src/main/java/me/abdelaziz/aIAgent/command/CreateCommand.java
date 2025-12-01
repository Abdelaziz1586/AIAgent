package me.abdelaziz.aIAgent.command;

import me.abdelaziz.aIAgent.AIAgent;
import me.abdelaziz.aIAgent.handler.StructureHandler;
import me.abdelaziz.aIAgent.object.StructureBlock;
import me.abdelaziz.aIAgent.object.Structure;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Bed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public final class CreateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, final String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (args.length == 0) {
            player.sendMessage("/create <prompt>");
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(AIAgent.getInstance(), () -> {
            final Structure structure = StructureHandler.build(player, String.join(" ", Arrays.copyOfRange(args, 0, args.length)));

            build(player.getWorld(), structure);

            player.sendMessage(structure.message());
        });
        return true;
    }

    private static void build(final World world, final Structure structure) {
        Bukkit.getScheduler().runTask(AIAgent.getInstance(), () -> {
            final Location middle = toLocation(world, structure.location());

            final Iterator<StructureBlock> iterator = structure.blocks().iterator();
            Bukkit.getScheduler().runTaskTimer(AIAgent.getInstance(), task -> {
                if (iterator.hasNext())
                    placeBlock(middle, iterator.next());

                if (!iterator.hasNext())
                    task.cancel();
            }, 0L, 1L);
        });
    }

    private static Location toLocation(final World world, final int[] location) {
        return new Location(
                world,
                location[0],
                location[1],
                location[2]
        );
    }

    private static void placeBlock(final Location middle, final StructureBlock structureBlock) {
        try {
            final Material material = Material.valueOf(structureBlock.type());
            final BlockFace blockFace = structureBlock.face() == null ? null : BlockFace.valueOf(structureBlock.face());

            final Block block = addVectorToLocation(middle, structureBlock.vector()).getBlock();

            block.setType(material, true);

            BlockData data = block.getBlockData();

            if (blockFace != null && data instanceof Directional directional) {
                directional.setFacing(blockFace);
                block.setBlockData(directional);
                data = block.getBlockData();
            }

            if (data instanceof Bisected bisected) {
                if (bisected.getHalf() == Bisected.Half.BOTTOM) {
                    final Block topBlock = block.getRelative(BlockFace.UP);

                    topBlock.setType(material, false);

                    if (topBlock.getBlockData() instanceof Bisected topBisected) {
                        topBisected.setHalf(Bisected.Half.TOP);

                        if (topBisected instanceof Directional topDir && data instanceof Directional bottomDir)
                            topDir.setFacing(bottomDir.getFacing());

                        topBlock.setBlockData(topBisected, false);
                    }
                }
            }

            if (data instanceof Bed bed) {
                bed.setPart(Bed.Part.FOOT);
                block.setBlockData(bed);

                final BlockFace facing = bed.getFacing();
                final Block headBlock = block.getRelative(facing);

                headBlock.setType(material, false);

                if (headBlock.getBlockData() instanceof Bed headBed) {
                    headBed.setPart(Bed.Part.HEAD);
                    headBed.setFacing(facing);
                    headBlock.setBlockData(headBed, false);
                }
            }

            block.getWorld().spawnParticle(
                    Particle.BLOCK,
                    block.getLocation().add(0.5, 0.5, 0.5),
                    20, 0.2, 0.2, 0.2,
                    material.createBlockData()
            );

            block.getWorld().playSound(
                    block.getLocation(),
                    material.createBlockData().getSoundGroup().getPlaceSound(),
                    1.0f, 1.0f
            );

        } catch (final IllegalArgumentException ignored) {
        }
    }


    private static Location addVectorToLocation(final Location location, final int[] vector) {
        return location.clone().add(
                vector[0],
                vector[1],
                vector[2]
        );
    }
}
