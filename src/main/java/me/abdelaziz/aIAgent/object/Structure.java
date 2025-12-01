package me.abdelaziz.aIAgent.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Structure(String message, int[] location, List<StructureBlock> blocks) {

    @JsonCreator
    public Structure(
            @JsonProperty("message") final String message,
            @JsonProperty("location") final int[] location,
            @JsonProperty("blocks") final List<StructureBlock> blocks
    ) {
        this.message = message;
        this.location = location;
        this.blocks = blocks;
    }
}
