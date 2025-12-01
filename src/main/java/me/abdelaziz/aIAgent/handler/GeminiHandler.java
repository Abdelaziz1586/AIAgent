package me.abdelaziz.aIAgent.handler;

import com.google.genai.Client;
import com.google.genai.types.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.util.*;
import java.util.stream.Collectors;

public final class GeminiHandler {

    private static Client client;
    private static GenerateContentConfig config;

    public static void initialize(final String apiKey) {

        client = new Client.Builder()
                .apiKey(apiKey)
                .build();

        config = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(
                        Part.fromText(loadSystemPrompt())
                ))
                .responseSchema(buildSchema())
                .responseMimeType("application/json")
                .build();

        sendEnumHandshake();
    }

    public static String sendBuildRequest(final String prompt, final int[] location) {

        final String finalPrompt = """
                %s
                
                META:
                {
                  "location": [%d, %d, %d]
                }
                """.formatted(
                prompt,
                location[0],
                location[1],
                location[2]
        );

        try {
            final GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash",
                            finalPrompt,
                            config
                    );

            return response.text();

        } catch (final Exception e) {
            throw new RuntimeException("Gemini build request failed", e);
        }
    }

    private static void sendEnumHandshake() {

        final String materials = Arrays.stream(Material.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        final String faces = Arrays.stream(BlockFace.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        final String handshakePrompt = """
                BLOCK MATERIAL ENUMS:
                [%s]
                
                FACE ENUMS:
                [%s]
                """.formatted(materials, faces);

        try {
            final GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash",
                            handshakePrompt,
                            config
                    );

            Bukkit.getLogger().info("Gemini handshake reply:");
            Bukkit.getLogger().info(response.text());

        } catch (final Exception e) {
            throw new RuntimeException("Gemini enum handshake failed", e);
        }
    }

    private static Schema buildSchema() {

        return Schema.builder()
                .type(Type.Known.OBJECT)
                .required(List.of("message", "location", "blocks"))
                .properties(Map.of(
                        "message", Schema.builder()
                                .type(Type.Known.STRING)
                                .build(),

                        "location", Schema.builder()
                                .type(Type.Known.ARRAY)
                                .items(Schema.builder()
                                        .type(Type.Known.NUMBER)
                                        .build())
                                .build(),

                        "blocks", Schema.builder()
                                .type(Type.Known.ARRAY)
                                .items(
                                        Schema.builder()
                                                .type(Type.Known.OBJECT)
                                                .required(List.of("type", "vector"))
                                                .properties(Map.of(
                                                        "type", Schema.builder()
                                                                .type(Type.Known.STRING)
                                                                .build(),

                                                        "face", Schema.builder()
                                                                .type(Type.Known.STRING)
                                                                .build(),

                                                        "vector", Schema.builder()
                                                                .type(Type.Known.ARRAY)
                                                                .items(
                                                                        Schema.builder()
                                                                                .type(Type.Known.NUMBER)
                                                                                .build()
                                                                )
                                                                .build()
                                                ))
                                                .build()
                                )
                                .build()
                ))
                .build();
    }

    private static String loadSystemPrompt() {

        return """
                Minecraft Builder AI – Core Instruction
                
                You are a professional Minecraft Builder/Crafter AI whose only job is to design structures and return them in a machine-readable JSON format.
                
                --------------------------------------------------
                
                INITIAL HANDSHAKE RULE (VERY IMPORTANT)
                
                The VERY FIRST message you receive will contain ONLY:
                - A list of allowed BLOCK MATERIAL enums.
                - A list of allowed FACE (direction) enums.
                
                This first message is NOT a build request.
                
                On this first message you must:
                1. Parse and store the enums internally.
                2. Return a confirmation JSON object ONLY.
                3. DO NOT output any blocks.
                4. DO NOT output a location field.
                
                Your response to the first message must be exactly:
                
                {
                  "message": "Enums received and stored successfully."
                }
                
                --------------------------------------------------
                
                NORMAL OPERATION (ALL FOLLOWING REQUESTS)
                
                After the enums have been received, you will then receive:
                
                - A user build request (what to build).
                - A META object containing:
                  - location: the center point of the structure in world coordinates.
                
                You must NOT assume anything that is not explicitly provided (no guessing materials, faces, or missing enums).
                Use only materials and face values that match the enums provided in the first message.
                
                --------------------------------------------------
                
                OUTPUT RULES (FOR BUILD REQUESTS ONLY)
                
                For all build responses, return exactly ONE valid JSON object using this structure:
                
                {
                  "message": "Short explanation of what was built",
                  "location": [x, y, z],
                  "blocks": [
                    {
                      "type": "BLOCK_ENUM",
                      "face": "FACE_ENUM",
                      "vector": [dx, dy, dz]
                    }
                  ]
                }
                
                --------------------------------------------------
                
                FIELD DEFINITIONS
                
                message:
                - A very short description of the finished structure.
                
                location:
                - Must be the exact location array received from META.
                - Do not modify, calculate, or shift this value.
                
                blocks:
                - A list of blocks composing the build.
                
                Each block entry contains:
                
                type:
                - A valid block type enum from the original materials list only.
                
                face:
                - A valid face enum from the original face list only.
                
                vector:
                - An offset relative to META location:
                  - [0,0,0] = block at the exact center location.
                  - [1,0,0] = one block east of center.
                  - [-2,1,3] = two west, one up, three south of center, etc.
                
                --------------------------------------------------
                
                BUILDING RULES
                
                1. All coordinates must be RELATIVE using vector offsets.
                2. Never output absolute world block coordinates.
                3. Blocks must form a complete logical structure (walls, floors, roofs must connect).
                4. Explanations must be minimal — only use the message field.
                5. No commentary outside JSON. Every response must be valid JSON only.
                
                --------------------------------------------------
                
                INTERPRETATION RULES
                
                - If a request is unclear, build the simplest version that satisfies it.
                - Do not invent mechanics, entities, or blocks.
                - Never reference materials or faces not included in the stored enum lists.
                - Build to real Minecraft scale (player ≈ 2 blocks tall, doors 2 blocks high, etc.).
                - The structure must be centered on the META location.
                
                --------------------------------------------------
                
                BLOCK ORIENTATION RULES
                
                1. The "face" field is REQUIRED only for blocks that support directional facing (e.g. stairs, doors, trapdoors, beds, furnaces, observers, etc.).
                
                2. For non-directional blocks (e.g. stone, cobblestone, glass, dirt, slabs, wool, concrete, etc.):
                   - DO NOT assign a real facing direction.
                   - ALWAYS DON'T ASSIGN IT
                
                3. Never invent orientation for blocks that do not use it.
                   If facing is meaningless to the block type, don't add the "face" key
                
                
                --------------------------------------------------
                
                MULTI-BLOCK SUPPORT RULES
                
                1. Any multi-block structure block (doors, beds, tall plants, etc.) MUST be placed so that its LOWER part is:
                   - On the structure’s ground level (y = 0 relative to META), OR
                   - Directly on top of a solid supporting block explicitly placed beneath it.
                
                2. NEVER place the lower part of a multi-block at a height where there is no physical support block below.
                
                3. The AI must always assume unsupported placement will cause block breakage and must avoid it.
                
                4. DOUBLE-HEIGHT BLOCK CLEARANCE (CRITICAL):
                   - When placing a 2-block high item (Doors, Sunflowers, Tall Grass) at vector [x, y, z]:
                     a. Output ONLY the bottom block entry at [x, y, z].
                     b. DO NOT output a block entry for the top half (no door_top).
                     c. DO NOT place ANY other block (no walls, no roof, no air) at [x, y+1, z].
                   - The coordinate [x, y+1, z] must remain completely NULL/UNDEFINED in the JSON output.\s
                   - This prevents the structure from overwriting the space the game needs to generate the top half of the door.
                
                --------------------------------------------------
                
                BEHAVIOR SUMMARY
                
                - First input = ENUM HANDSHAKE → confirm only.
                - Later inputs = BUILD REQUESTS → output full build JSON.
                - Translate words into accurate Minecraft structures.
                - Output JSON only — no markdown, no explanations.
                - Strictly obey provided enums and schema.
                - Always use vectors relative to the center location.
                """;
    }
}
