package org.example.argumentparser;


import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.function.Function.identity;

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

        public OptionalFlagWithValueBuilder<String> requireValue() {
            return new OptionalFlagWithValueBuilder<>(shortName, longName, description, identity());
        }

        public Flag.OptionalFlagWithoutValue build() {
            return new Flag.OptionalFlagWithoutValue(shortName, longName, description);
        }

        public OptionalFlagWithoutValueBuilder useLongName(String longName) {
            setLongName(longName);
            return this;
        }
    }

    public static class OptionalFlagWithValueBuilder<ValueType> extends FlagBuilder {
        private final Function<String, ValueType> converter;

        private OptionalFlagWithValueBuilder(String shortName, Optional<String> longName, String description,
                                             Function<String, ValueType> converter) {
            super(shortName, longName, description);
            this.converter = converter;
        }

        public  Flag.OptionalFlagWithValue<ValueType> build() {
            return new Flag.OptionalFlagWithValue<>(shortName, longName, description, converter);
        }

        public OptionalFlagWithDefaultValueBuilder<ValueType> useDefaultValue(ValueType defaultValue) {
            return new OptionalFlagWithDefaultValueBuilder<>(shortName, longName, description, defaultValue, converter);
        }

        public RequiredFlagWithValueBuilder<ValueType> mustAppear() {
            return new RequiredFlagWithValueBuilder<>(shortName, longName, description, converter);
        }

        public OptionalFlagWithValueBuilder<ValueType> useLongName(String longName) {
            setLongName(longName);
            return this;
        }

        public <NewValueType> OptionalFlagWithValueBuilder<NewValueType> useConverter(
                Function<String, NewValueType> converter) {
            return new OptionalFlagWithValueBuilder<>(shortName, longName, description, converter);
        }
    }

    public static class OptionalFlagWithDefaultValueBuilder<ValueType> extends FlagBuilder {
        private final ValueType defaultValue;
        private final Function<String, ValueType> converter;

        private OptionalFlagWithDefaultValueBuilder(String shortName, Optional<String> longName,
                                                    String description, ValueType defaultValue,
                                                    Function<String, ValueType> converter) {
            super(shortName, longName, description);
            this.defaultValue = defaultValue;
            this.converter = converter;
        }

        public Flag.OptionalFlagWithDefaultValue<ValueType> build() {
            return new Flag.OptionalFlagWithDefaultValue<>(shortName, longName, description, defaultValue, converter);
        }

        public OptionalFlagWithDefaultValueBuilder<ValueType> useLongName(String longName) {
            setLongName(longName);
            return this;
        }

        public <NewValueType> OptionalFlagWithDefaultValueBuilder<NewValueType> useConverterWithDefaultValue(
                NewValueType newDefaultValue, Function<String, NewValueType> converter) {
            return new OptionalFlagWithDefaultValueBuilder<>(shortName, longName, description, newDefaultValue, converter);

        }

    }

    public static class RequiredFlagWithValueBuilder<ValueType> extends FlagBuilder {

        private final Function<String, ValueType> converter;

        private RequiredFlagWithValueBuilder(String shortName, Optional<String> longName, String description,
                                             Function<String, ValueType> converter) {
            super(shortName, longName, description);
            this.converter = converter;
        }

        public Flag.RequiredFlagWithValue<ValueType> build() {
            return new Flag.RequiredFlagWithValue<>(shortName, longName, description, converter);
        }

        public RequiredFlagWithValueBuilder<ValueType> useLongName(String longName) {
            setLongName(longName);
            return this;
        }

        public <NewValueType> RequiredFlagWithValueBuilder<NewValueType> useConverter(
                Function<String, NewValueType> converter) {
            return new RequiredFlagWithValueBuilder<>(shortName, longName, description, converter);
        }

    }

}
