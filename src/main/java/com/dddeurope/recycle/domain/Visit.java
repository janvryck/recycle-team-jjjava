package com.dddeurope.recycle.domain;

import com.dddeurope.recycle.events.PriceWasCalculated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record Visit(Person person, List<DroppedFraction> drops) {

    public PriceWasCalculated calculatePrice() {
        double price = drops.stream()
            .mapToDouble(drop -> person.location().calculatePriceFor(drop.fractionType(), drop.weight()))
            .map(this::roundMonetaryValue)
            .sum();
        return new PriceWasCalculated(person.cardId(), price, "EUR");
    }

    private double roundMonetaryValue(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
