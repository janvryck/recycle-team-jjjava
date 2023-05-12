package com.dddeurope.recycle.spring;

import com.dddeurope.recycle.commands.CommandMessage;
import com.dddeurope.recycle.domain.FractionDropOff;
import com.dddeurope.recycle.domain.Person;
import com.dddeurope.recycle.domain.Visit;
import com.dddeurope.recycle.events.EventMessage;
import com.dddeurope.recycle.events.FractionWasDropped;
import com.dddeurope.recycle.events.IdCardRegistered;
import com.dddeurope.recycle.events.PriceWasCalculated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MainController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    @GetMapping("/validate")
    public String validate() {
        return "Hi!";
    }

    @PostMapping("/handle-command")
    public ResponseEntity<EventMessage> handle(@RequestBody RecycleRequest request) {
        LOGGER.info("Incoming Request: {}", request.asString());

        var visit = new Visit(extractPersonFrom(request.history), extractDropOffs(request.history));

        var message = new EventMessage("todo", new PriceWasCalculated("123", visit.calculatePrice(), "EUR"));

        return ResponseEntity.ok(message);
    }

    private Person extractPersonFrom(List<EventMessage> history) {
        return history.stream()
            .filter(event -> IdCardRegistered.class.getSimpleName().equals(event.getType()))
            .map(EventMessage::getPayload)
            .map(IdCardRegistered.class::cast)
            .map(event -> new Person(event.cardId(), event.city()))
            .findFirst()
            .orElseThrow();
    }

    private List<FractionDropOff> extractDropOffs(List<EventMessage> history) {
        return history.stream()
            .filter(event -> FractionWasDropped.class.getSimpleName().equals(event.getType()))
            .map(evt -> (FractionWasDropped) evt.getPayload())
            .map(evt -> new FractionDropOff(
                evt.cardId(), evt.fractionType(), evt.weight()
            ))
            .toList();
    }

    public record RecycleRequest(List<EventMessage> history, CommandMessage command) {

        public String asString() {
            var historyAsString = history.stream()
                .map(EventMessage::toString)
                .collect(Collectors.joining("\n\t"));

            return String.format("%n%s %nWith History\n\t%s", command, historyAsString);
        }
    }
}
