package programo._pro.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(implementation = Color.class)
public enum Color {
    RED, YELLOW, GREEN, BLUE, PINK, ORANGE
}
