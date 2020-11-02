package org.example.argumentparser;


import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FlagBuilder {

    private static final Pattern SHORT_NAME_PATTERN = Pattern.compile("^(-)?(?<argName>[a-zA-Z0-9])$");
    private static final Pattern LONG_NAME_PATTERN = Pattern.compile("^(--)?(?<argName>[a-zA-Z0-9_]{2,})$");

    protected final String shortName;
    protected Optional<String> longName;
    protected final String description;

    private FlagBuilder(String shortName, Optional<String> longName, String description) {
        this.shortName = shortName;
        this.longName = longName;
        this.description = description;
    }

    public static OptionalFlagWithoutValueBuilder newBuilder(String shortName, String description) {
        Matcher matcher = SHORT_NAME_PATTERN.matcher(shortName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid short name: " + shortName);
        }
        shortName = matcher.group("argName");

        if (shortName.equals("h")) {
            throw new IllegalArgumentException("Reserved short name: -h");
        }

        return new OptionalFlagWithoutValueBuilder(shortName, description, Optional.empty());
    }

    protected void setLongName(String longName) {
        Matcher matcher = LONG_NAME_PATTERN.matcher(longName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid long name: " + longName);
        }
        longName = matcher.group("argName");

        if (longName.equals("help")) {
            throw new IllegalArgumentException("Reserved long name: --help");
        }

        this.longName = Optional.of(longName);
    }

    public static class OptionalFlagWithoutValueBuilder extends FlagBuilder {

        private OptionalFlagWithoutValueBuilder(String shortName, String description, Optional<String> longName) {
            super(shortName, longName, description);
        }

        public OptionalFlagWithValueBuilder requireValue() {
            return new OptionalFlagWithValueBuilder(shortName, longName, description);
        }

        public Flag.OptionalFlagWithoutValue build() {
            return new Flag.OptionalFlagWithoutValue(shortName, longName, description);
        }

        public OptionalFlagWithoutValueBuilder useLongName(String longName) {
            setLongName(longName);
            return this;
        }
    }

    public static class OptionalFlagWithValueBuilder extends FlagBuilder {

        private OptionalFlagWithValueBuilder(String shortName, Optional<String> longName, String description) {
            super(shortName, longName, description);
        }

        public  Flag.OptionalFlagWithValue build() {
            return new Flag.OptionalFlagWithValue(shortName, longName, description);
        }

        public OptionalFlagWithDefaultValueBuilder useDefaultValue(String defaultValue) {
            return new OptionalFlagWithDefaultValueBuilder(shortName, longName, description, defaultValue);
        }

        public RequiredFlagWithValueBuilder mustAppear() {
            return new RequiredFlagWithValueBuilder(shortName, longName, description);
        }

        public OptionalFlagWithValueBuilder useLongName(String longName) {
            setLongName(longName);
            return this;
        }
    }

    public static class OptionalFlagWithDefaultValueBuilder extends FlagBuilder {
        private final String defaultValue;

        private OptionalFlagWithDefaultValueBuilder(String shortName, Optional<String> longName,
                                                    String description, String defaultValue) {
            super(shortName, longName, description);
            this.defaultValue = defaultValue;
        }

        public Flag.OptionalFlagWithDefaultValue build() {
            return new Flag.OptionalFlagWithDefaultValue(shortName, longName, description, defaultValue);
        }

        public OptionalFlagWithDefaultValueBuilder useLongName(String longName) {
            setLongName(longName);
            return this;
        }
    }

    public static class RequiredFlagWithValueBuilder extends FlagBuilder {

        private RequiredFlagWithValueBuilder(String shortName, Optional<String> longName, String description) {
            super(shortName, longName, description);
        }

        public Flag.RequiredFlagWithValue build() {
            return new Flag.RequiredFlagWithValue(shortName, longName, description);
        }

        public RequiredFlagWithValueBuilder useLongName(String longName) {
            setLongName(longName);
            return this;
        }

    }

}
