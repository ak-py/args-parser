package org.example.argumentparser;

import java.util.*;

public class ArgumentParserBuilder {

    private final String programName;
    private final Set<Flag> flags = new HashSet<>();
    private final List<PositionalArgument> positionalArguments = new ArrayList<>();
    private Optional<String> programDescription = Optional.empty();

    private ArgumentParserBuilder(String programName) {
        this.programName = programName;
    }

    public static ArgumentParserBuilder newBuilder(String programName) {
        return new ArgumentParserBuilder(programName);
    }

    public ArgumentParser build() {
        return new ArgumentParser(programName, programDescription, flags, positionalArguments);
    }

    public ArgumentParserBuilder setProgramDescription(String programDescription) {
        this.programDescription = Optional.of(programDescription);
        return this;
    }

    public ArgumentParserBuilder addFlag(Flag flag) {
        if (flag.isParsed()) {
            throw new IllegalArgumentException("Provided Flag is already parsed.");
        }
        boolean shortNameAlreadyRegistered = flags.stream()
                .anyMatch(f -> f.getShortName().equals(flag.getShortName()));
        boolean longNameAlreadyRegistered = flags.stream()
                .anyMatch(f -> f.getLongName().isPresent() && flag.getLongName().isPresent()
                        && f.getLongName().get().equals(flag.getLongName().get()));
        if (shortNameAlreadyRegistered || longNameAlreadyRegistered) {
            throw new IllegalStateException("Provided flag is already registered.");
        }
        flags.add(flag);
        return this;
    }

    public ArgumentParserBuilder addFlags(Flag ...flags) {
        for (Flag flag : flags) {
            addFlag(flag);
        }
        return this;
    }

    public ArgumentParserBuilder addPositionalArgument(PositionalArgument arg) {
        if (arg.isParsed()) {
            throw new IllegalArgumentException("Positional Argument provided is already parsed.");
        }
        if (positionalArguments.contains(arg)) {
            throw new IllegalStateException("Positional Argument already registered.");
        }
        positionalArguments.add(arg);
        return this;
    }

    public ArgumentParserBuilder addPositionalArguments(PositionalArgument ...args) {
        for (PositionalArgument arg : args) {
            addPositionalArgument(arg);
        }
        return this;
    }

}
