package com.dddeurope.recycle.spring;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.dddeurope.recycle.commands.CommandMessage;
import com.dddeurope.recycle.events.EventMessage;
import com.dddeurope.recycle.events.IdCardRegistered;
import com.dddeurope.recycle.events.IdCardScannedAtEntranceGate;
import com.dddeurope.recycle.events.IdCardScannedAtExitGate;
import com.dddeurope.recycle.events.PriceWasCalculated;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

class MainControllerTest {

    private final MainController controller = new MainController();

    @Test
    public void shouldNotChargeWithoutDropoff() {
        // events
        MainController.RecycleRequest request = new MainController.RecycleRequest(
            List.of(
                new EventMessage("f54013d5-c3cb-44a2-9ae1-ed27f5d2663a", new IdCardRegistered("123", "Tony Stark", "Point Dume", "Malibu")),
                new EventMessage("381a119a-e2be-4535-af6f-6d530ad35639", new IdCardScannedAtEntranceGate("123", "2023-02-10")),
                new EventMessage("a5de3ee6-6ea3-4107-a741-adbb58bb806b", new IdCardScannedAtExitGate("123"))
            ), new CommandMessage()
        );

        ResponseEntity<EventMessage> handle = controller.handle(request);

        assertThat(handle.getBody())
            .isInstanceOfSatisfying(EventMessage.class, (event) -> {
                assertThat(event.getPayload())
                    .isInstanceOfSatisfying(PriceWasCalculated.class, priceWasCalculated ->
                        assertThat(priceWasCalculated.amount()).isEqualTo(0.0)
                    );
            });
    }
}
