package org.example.argumentparser;

import java.util.Optional;

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

    public static class RequiredFlagWithValue extends Flag implements RequiresValue, FlagMustAppear {
        private Optional<String> value = Optional.empty();

        RequiredFlagWithValue(String shortName, Optional<String> longName, String description) {
            super(shortName, longName, description);
        }

        void setValue(String value) {
            this.value = Optional.of(value);
        }

        public String getValue() {
            assertParsed();
            if (value.isEmpty()) {
                throw new IllegalStateException("Internal Error");
            }
            return value.get();
        }
    }

    public static class OptionalFlagWithValue extends Flag implements RequiresValue {

        private Optional<String> value = Optional.empty();

        OptionalFlagWithValue(String shortName, Optional<String> longName, String description) {
            super(shortName, longName, description);
        }

        void setValue(String value) {
            this.value = Optional.of(value);
        }

        public Optional<String> getValue() {
            assertParsed();
            return value;
        }
    }

    public static class OptionalFlagWithDefaultValue extends Flag implements RequiresValue, HasDefaultValue {
        private final String defaultValue;
        private String value;

        OptionalFlagWithDefaultValue(String shortName, Optional<String> longName,
                                     String description, String defaultValue) {
            super(shortName, longName, description);
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            assertParsed();
            return value;
        }

        @Override
        public String getDefaultValue() {
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
