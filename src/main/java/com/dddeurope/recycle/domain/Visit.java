package com.dddeurope.recycle.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record Visit(Person person, List<FractionDropOff> drops) {

    public double calculatePrice() {
        return drops.stream()
            .mapToDouble(dropOff ->
                person.location().calculatePriceFor(dropOff.fractionType(), dropOff.weight()))
            .map(this::roundMonetaryValue)
            .sum();
    }

    private double roundMonetaryValue(double d) {
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
