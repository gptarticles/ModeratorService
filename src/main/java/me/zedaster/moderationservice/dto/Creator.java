package me.zedaster.moderationservice.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Creator {
    /**
     * User ID of the creator
     */
    private long id;

    /**
     * Username of the creator
     */
    private String name;
}
