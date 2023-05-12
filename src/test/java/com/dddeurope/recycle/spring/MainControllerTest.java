package com.dddeurope.recycle.spring;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.dddeurope.recycle.commands.CommandMessage;
import com.dddeurope.recycle.events.Event;
import com.dddeurope.recycle.events.EventMessage;
import com.dddeurope.recycle.events.FractionWasDropped;
import com.dddeurope.recycle.events.IdCardRegistered;
import com.dddeurope.recycle.events.IdCardScannedAtEntranceGate;
import com.dddeurope.recycle.events.IdCardScannedAtExitGate;
import com.dddeurope.recycle.events.PriceWasCalculated;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;

class MainControllerTest {

    private final MainController controller = new MainController();

    @Test
    public void scenario_1() {
        MainController.RecycleRequest request = toRequest(
            new IdCardRegistered("123", "Tony Stark", "Point Dume", "Malibu"),
            new IdCardScannedAtEntranceGate("123", "2023-02-10"),
            new IdCardScannedAtExitGate("123")
        );

        ResponseEntity<EventMessage> response = controller.handle(request);

        assertPriceWasCalculated(response, PriceWasCalculated::amount, 0.0);
    }

    @Test
    public void scenario_2() {
        MainController.RecycleRequest request = toRequest(
            new IdCardRegistered("123", "Tony Stark", "Point Dume", "Malibu"),
            new IdCardScannedAtEntranceGate("123", "2023-02-10"),
            new FractionWasDropped("123", "Construction waste", 71),
            new IdCardScannedAtExitGate("123")
        );

        ResponseEntity<EventMessage> response = controller.handle(request);

        assertPriceWasCalculated(response, PriceWasCalculated::amount, 10.65);
    }

    @Test
    public void scenario_3() {
        MainController.RecycleRequest request = toRequest(
            new IdCardRegistered("123", "Tony Stark", "Point Dume", "Malibu"),
            new IdCardScannedAtEntranceGate("123", "2023-02-10"),
            new FractionWasDropped("123", "Construction waste", 51),
            new FractionWasDropped("123", "Green waste", 23),
            new IdCardScannedAtExitGate("123")
        );

        ResponseEntity<EventMessage> response = controller.handle(request);

        assertPriceWasCalculated(response, PriceWasCalculated::amount, 9.72);
    }

    @Test
    public void scenario_4_1() {
        MainController.RecycleRequest request = toRequest(
            new IdCardRegistered("123", "Eric Cartman", "Point Dume", "South Park"),
            new IdCardScannedAtEntranceGate("123", "2023-02-10"),
            new FractionWasDropped("123", "Construction waste", 51),
            new FractionWasDropped("123", "Green waste", 23),
            new IdCardScannedAtExitGate("123")
        );

        ResponseEntity<EventMessage> response = controller.handle(request);

        assertPriceWasCalculated(response, PriceWasCalculated::amount, 0.0);
    }

    @Test
    public void scenario_4_2() {
        MainController.RecycleRequest request = toRequest(
            new IdCardRegistered("567", "Eric Cartman", "Point Dume", "South Park"),
            new IdCardScannedAtEntranceGate("567", "2023-02-10"),
            new FractionWasDropped("567", "Construction waste", 151),
            new FractionWasDropped("567", "Green waste", 73),
            new IdCardScannedAtExitGate("567")
        );

        ResponseEntity<EventMessage> response = controller.handle(request);

        assertPriceWasCalculated(response, PriceWasCalculated::cardId, "567");
        assertPriceWasCalculated(response, PriceWasCalculated::amount, 11.94);
    }

    private MainController.RecycleRequest toRequest(Event... events) {
        return new MainController.RecycleRequest(
            Arrays.stream(events)
                .map(event -> new EventMessage(UUID.randomUUID().toString(), event))
                .toList(), new CommandMessage()
        );
    }

    private <T> void assertPriceWasCalculated(ResponseEntity<EventMessage> handle, Function<PriceWasCalculated, T> producer, T expected) {
        assertThat(handle.getBody())
            .isInstanceOfSatisfying(EventMessage.class, event -> assertThat(event.getPayload())
                .isInstanceOfSatisfying(PriceWasCalculated.class, priceWasCalculated ->
                    assertThat(producer.apply(priceWasCalculated)).isEqualTo(expected)
                ));
    }

}
