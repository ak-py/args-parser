package org.example.argumentparser;

import java.util.Optional;

public class PositionalArgument {

    private final String name;
    private final String description;
    private boolean parsed = false;
    private Optional<String> value = Optional.empty();

    public PositionalArgument(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getValue() {
        if (!parsed) {
            throw new IllegalStateException("Flag not parsed yet. Make sure you have called " +
                    ArgumentParser.class.getSimpleName() + ".parse().");
        }
        if (value.isEmpty()){
            throw new IllegalStateException("Internal Error");
        }
        return value.get();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setValue(String value) {
        this.value = Optional.of(value);
    }

    void markParsed() {
        parsed = true;
    }

    boolean isParsed() {
        return parsed;
    }

}
