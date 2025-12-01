package me.abdelaziz.aIAgent.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.abdelaziz.aIAgent.object.Structure;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class StructureHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Structure build(final Player player, final String request) {

        final Location loc = player.getLocation();
        final int[] center = {
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        };

        try {
            return mapper.readValue(GeminiHandler.sendBuildRequest(request, center), Structure.class);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to deserialize structure", e);
        }
    }
}
