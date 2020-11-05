package org.example.argumentparser;

import java.util.Optional;
import java.util.function.Function;

public abstract class Flag {

    private final String shortName;
    private final Optional<String> longName;
    private final String description;
    private boolean parsed = false;
    protected boolean appeared = false;

    Flag(String shortName, Optional<String> longName, String description) {
        this.shortName = shortName;
        this.longName = longName;
        this.description = description;
    }

    public String getShortName() {
        return shortName;
    }

    public Optional<String> getLongName() {
        return longName;
    }

    public String getDescription() {
        return description;
    }

    protected void assertParsed() {
        if (!parsed) {
            throw new IllegalStateException("Flag not parsed yet. Make sure you have called " +
                    ArgumentParser.class.getSimpleName() + ".parse().");
        }
    }

    void markAppeared() {
        appeared = true;
    }

    void markParsed() {
        parsed = true;
    }

    boolean isParsed() {
        return parsed;
    }

    boolean hasAppeared() {
        return appeared;
    }

    public static class RequiredFlagWithValue<ValueType> extends Flag implements RequiresValue<ValueType>,
            FlagMustAppear {
        private final Function<String, ValueType> converter;
        private Optional<ValueType> value = Optional.empty();

        RequiredFlagWithValue(String shortName, Optional<String> longName, String description,
                              Function<String, ValueType> converter) {
            super(shortName, longName, description);
            this.converter = converter;
        }

        void setRawValue(String value) {
            this.value = Optional.of(converter.apply(value));
        }

        public ValueType getValue() {
            assertParsed();
            if (value.isEmpty()) {
                throw new IllegalStateException("Internal Error");
            }
            return value.get();
        }
    }

    public static class OptionalFlagWithValue<ValueType> extends Flag implements RequiresValue<ValueType> {
        private final Function<String, ValueType> converter;
        private Optional<ValueType> value = Optional.empty();

        OptionalFlagWithValue(String shortName, Optional<String> longName, String description,
                              Function<String, ValueType> converter) {
            super(shortName, longName, description);
            this.converter = converter;
        }

        void setRawValue(String value) {
            this.value = Optional.of(converter.apply(value));
        }

        public Optional<ValueType> getValue() {
            assertParsed();
            return value;
        }
    }

    public static class OptionalFlagWithDefaultValue<ValueType> extends Flag implements RequiresValue<ValueType>,
            HasDefaultValue<ValueType> {
        private final Function<String, ValueType> converter;
        private final ValueType defaultValue;
        private ValueType value;

        OptionalFlagWithDefaultValue(String shortName, Optional<String> longName,
                                     String description, ValueType defaultValue, Function<String, ValueType> converter) {
            super(shortName, longName, description);
            this.converter = converter;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        void setRawValue(String value) {
            this.value = converter.apply(value);
        }

        public ValueType getValue() {
            assertParsed();
            return value;
        }

        @Override
        public ValueType getDefaultValue() {
            return defaultValue;
        }
    }

    public static class OptionalFlagWithoutValue extends Flag {
        OptionalFlagWithoutValue(String shortName, Optional<String> longName, String description) {
            super(shortName, longName, description);
        }

        @Override
        public boolean hasAppeared() {
            assertParsed();
            return super.hasAppeared();
        }
    }
}
