package com.dddeurope.recycle.domain;

import static com.dddeurope.recycle.domain.Location.DEFAULT;
import static com.dddeurope.recycle.domain.Location.SOUTH_PARK;

public record Person(String cardId, String city) {

    public Location location() {
        return switch (city) {
            case("South Park") -> SOUTH_PARK;
            default -> DEFAULT;
        };
    }

}
