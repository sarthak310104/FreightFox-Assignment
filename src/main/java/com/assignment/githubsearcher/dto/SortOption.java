package com.assignment.githubsearcher.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SortOption {
    STARS("stars", "stars"),
    FORKS("forks", "forks"),
    UPDATED("updated", "lastUpdated");

    private final String value;
    private final String entityField;

    SortOption(String value, String entityField) {
        this.value = value;
        this.entityField = entityField;
    }

    @JsonValue
    public String getValue() { return value; }
    public String getEntityField() { return entityField; }

    @JsonCreator
    public static SortOption fromValue(String input) {
        for (SortOption option : values()) {
            if (option.value.equalsIgnoreCase(input)) {
                return option;
            }
        }
        throw new IllegalArgumentException(
                "Invalid sort '" + input + "'. Allowed values: stars, forks, updated");
    }
}