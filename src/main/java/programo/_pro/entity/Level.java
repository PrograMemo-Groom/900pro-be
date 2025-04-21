package programo._pro.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(implementation = Level.class)
public enum Level {
    EASY, MEDIUM, HARD
}
