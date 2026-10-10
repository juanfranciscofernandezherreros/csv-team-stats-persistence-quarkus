package com.example.csvteamstats.domain.validation;

import com.example.csvteamstats.domain.model.TeamStats;
import java.util.Optional;

/** Validates persistence constraints as business input, before reaching PostgreSQL. */
public class TeamStatsFunctionalValidator {
    public Optional<Failure> validate(TeamStats value) {
        if (value == null) {
            return Optional.of(new Failure("REQUIRED_FIELD_EMPTY", "teamStats must not be null"));
        }
        return required(value.getSourceEventId(), "sourceEventId", 255)
                .or(() -> required(value.getMatchId(), "matchId", 128))
                .or(() -> required(value.getPeriod(), "period", 64))
                .or(() -> required(value.getCategory(), "category", 128))
                .or(() -> required(value.getMetric(), "metric", 255))
                .or(() -> required(value.getHomeTeam(), "homeTeam", 255))
                .or(() -> required(value.getAwayTeam(), "awayTeam", 255));
    }

    private Optional<Failure> required(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return Optional.of(new Failure("REQUIRED_FIELD_EMPTY", field + " must not be blank"));
        }
        if (value.length() > maxLength) {
            return Optional.of(new Failure(
                    "FIELD_TOO_LONG", field + " length " + value.length() + " exceeds " + maxLength));
        }
        return Optional.empty();
    }

    public record Failure(String reasonCode, String reasonDetail) {}
}
