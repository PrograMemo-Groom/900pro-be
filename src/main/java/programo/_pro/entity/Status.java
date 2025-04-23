package programo._pro.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(implementation = Status.class)
public enum Status {
    IN_PROGRESS, COMPLETED, ABSENT
}
