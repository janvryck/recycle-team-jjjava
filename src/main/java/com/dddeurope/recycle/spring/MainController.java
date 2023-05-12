package com.dddeurope.recycle.spring;

import com.dddeurope.recycle.commands.CommandMessage;
import com.dddeurope.recycle.events.EventMessage;
import com.dddeurope.recycle.events.FractionWasDropped;
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

        var message = new EventMessage("todo", new PriceWasCalculated("123", calculatePrice(request.history), "EUR"));

        return ResponseEntity.ok(message);
    }

    private static double calculatePrice(List<EventMessage> history) {
        return history.stream()
            .filter(event -> FractionWasDropped.class.getSimpleName().equals(event.getType()))
            .map(evt -> (FractionWasDropped) evt.getPayload())
            .mapToDouble(event -> event.weight() * priceFor(event.fractionType()))
            .map(MainController::roundMonetaryValue)
            .sum()
            ;
    }

    private static double roundMonetaryValue(double d) {
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static double priceFor(String fractionType) {
        return switch (fractionType) {
            case "Construction waste" -> 0.15;
            case "Green waste" -> 0.09;
            default -> throw new IllegalArgumentException("Unknown fraction " + fractionType);
        };
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
