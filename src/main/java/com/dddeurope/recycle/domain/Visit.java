package com.dddeurope.recycle.domain;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

import com.dddeurope.recycle.events.PriceWasCalculated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public record Visit(Person person, List<DroppedFraction> drops) {

    public PriceWasCalculated calculatePrice() {
        double price = totalWeightByFractionType()
            .entrySet()
            .stream()
            .mapToDouble(entry -> person.location().calculatePriceFor(entry.getKey(), entry.getValue()))
            .map(this::roundMonetaryValue)
            .sum();
        return new PriceWasCalculated(person.cardId(), price, "EUR");
    }

    private double roundMonetaryValue(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Map<String, Integer> totalWeightByFractionType() {
        return drops.stream()
            .collect(groupingBy(
                DroppedFraction::fractionType,
                summingInt(DroppedFraction::weight)
            ));
    }
}
