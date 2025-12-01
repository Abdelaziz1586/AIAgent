package me.abdelaziz.aIAgent.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bukkit.Material;

public record StructureBlock(String type, String face, int[] vector) {

    @JsonCreator
    public StructureBlock(
            @JsonProperty("type") final String type,
            @JsonProperty("face") final String face,
            @JsonProperty("vector") final int[] vector
    ) {
        this.type = type;
        this.face = face;
        this.vector = vector;
    }
}
