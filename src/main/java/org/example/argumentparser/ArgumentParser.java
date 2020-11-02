package org.example.argumentparser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class ArgumentParser {

    private static final Pattern SHORT_FLAG_PATTERN = Pattern.compile("^-(?<argName>[a-zA-Z0-9])$");
    private static final Pattern LONG_FLAG_PATTERN = Pattern.compile("^--(?<argName>[a-zA-Z0-9_]{2,})$");

    private static final String INDENTATION = "    ";
    private final String programName;
    private final Optional<String> programDescription;
    private final List<PositionalArgument> positionalArguments;

    private final HashMap<String, Flag> shortNameMap = new HashMap<>();
    private final HashMap<String, Flag> longNameMap = new HashMap<>();


    public ArgumentParser(String programName, Optional<String> programDescription, Set<Flag> flags,
                          List<PositionalArgument> positionalArguments) {
        this.programName = programName;
        this.programDescription = programDescription;
        this.positionalArguments = positionalArguments;

        for (Flag flag : flags) {
            shortNameMap.put(flag.getShortName(), flag);
            if (flag.getLongName().isPresent()) {
                longNameMap.put(flag.getLongName().get(), flag);
            }
        }
    }

    public void parse(String[] args) {
        boolean printUsage = Arrays.stream(args)
                .anyMatch(arg -> arg.equals("-h") || arg.equals("--help"));
        if (printUsage) {
            System.out.println(usage());
            System.exit(0);
        }

        var currentPosArgIdx = 0;

        for (int i = 0; i < args.length; i++) {

            var arg  = args[i];

            Matcher shortNameMatcher = SHORT_FLAG_PATTERN.matcher(arg);
            Matcher longNameMatcher = LONG_FLAG_PATTERN.matcher(arg);
            if (shortNameMatcher.matches()){
                var shortName = shortNameMatcher.group("argName");
                if (!shortNameMap.containsKey(shortName)) {
                    throw new IllegalArgumentException("Invalid short name encountered: " + shortName);
                }
                var flag = shortNameMap.get(shortName);
                Optional<String> nextArg = (i+1) == args.length ? Optional.empty() : Optional.of(args[i+1]);
                if (parseFlag(shortName, flag, nextArg)) {
                    i++;
                }
            } else if (longNameMatcher.matches()) {
                var longName = longNameMatcher.group("argName");
                if (!longNameMap.containsKey(longName)) {
                    throw new IllegalArgumentException("Invalid short name encountered: " + longName);
                }
                var flag = longNameMap.get(longName);
                Optional<String> nextArg = (i+1) == args.length ? Optional.empty() : Optional.of(args[i+1]);
                if (parseFlag(longName, flag, nextArg)) {
                    i++;
                }
            } else {
                if (currentPosArgIdx == positionalArguments.size()) {
                    throw new IllegalArgumentException("Unrecognized positional argument with value: " + arg);
                }
                var posArg = positionalArguments.get(currentPosArgIdx++);
                posArg.setValue(arg);
            }
        }

        // validate pos args
        if (currentPosArgIdx != (positionalArguments.size())) {
            throw new IllegalStateException("Missing positional argument: "
                    + positionalArguments.get(currentPosArgIdx).getName());
        }

        // validate required flags
        var firstMissingRequiredFlag = getAllFlags().stream()
                .filter(flag -> isRequired(flag) && !flag.hasAppeared())
                .findFirst();

        if (firstMissingRequiredFlag.isPresent()) {
            var flag = firstMissingRequiredFlag.get();
            var flagName = flag.getLongName().orElse(flag.getShortName());
            throw new IllegalStateException("Missing required flag: " + flagName);
        }

        for (Flag flag : getAllFlags()) {
            flag.markParsed();
        }
        for (PositionalArgument positionalArgument : positionalArguments) {
            positionalArgument.markParsed();
        }
    }

    private boolean parseFlag(String inputName, Flag flag, Optional<String> nextArg) {
        flag.markAppeared();
        if ((requiresValue(flag))) {
            if (nextArg.isEmpty()) {
                throw new IllegalArgumentException("No flag value found for flag: " + inputName);
            }
            var flagValue = nextArg.get();
            if (flag instanceof Flag.RequiredFlagWithValue) {
                ((Flag.RequiredFlagWithValue) flag).setValue(flagValue);
            } else if (flag instanceof Flag.OptionalFlagWithValue) {
                ((Flag.OptionalFlagWithValue) flag).setValue(flagValue);
            } else if (flag instanceof Flag.OptionalFlagWithDefaultValue) {
                ((Flag.OptionalFlagWithDefaultValue) flag).setValue(flagValue);
            } else {
                throw new IllegalStateException("Internal Error: Unrecognized flag class "
                        + flag.getClass().getSimpleName());
            }
            return true;
        } else { // does not require value
            if (flag instanceof Flag.OptionalFlagWithoutValue) {
                flag.markAppeared();
            } else {
                throw new IllegalStateException("Internal Error: Unrecognized flag class "
                        + flag.getClass().getSimpleName());
            }
            return false;
        }
    }

    public String usage() {
        StringBuilder builder = new StringBuilder("USAGE: ");
        builder.append(programName).append(' ');

        List<Flag> optionalFlags = getAllFlags().stream()
                .filter(flag -> !(isRequired(flag)))
                .collect(toList());
        String optionalFlagsDesc = optionalFlags.stream()
                .map(flag -> '-' + flag.getShortName())
                .collect(joining(" "));
        if (!optionalFlagsDesc.isEmpty()) {
            builder.append('[').append(optionalFlagsDesc).append(']').append(' ');
        }

        List<Flag> requiredFlags = getAllFlags().stream()
                .filter(ArgumentParser::isRequired)
                .collect(toList());
        String requiredFlagsDesc = requiredFlags.stream()
                .map(flag -> '-' + flag.getShortName())
                .collect(joining(" "));
        if (!requiredFlagsDesc.isEmpty()) {
            builder.append(requiredFlagsDesc).append(' ');
        }

        for (PositionalArgument positionalArgument : positionalArguments) {
            builder.append(positionalArgument.getName()).append(' ');
        }

        if (programDescription.isPresent()) {
            builder.append('\n');
            builder.append(programDescription.get());
        }

        builder.append('\n').append('\n');

        Optional<Integer> maxLength = positionalArguments.stream()
                .map(positionalArgument -> positionalArgument.getName().length())
                .max(comparingInt(len -> len));
        if (maxLength.isPresent()) {
            builder.append("POSITIONAL ARGUMENTS\n");
            for (PositionalArgument positionalArgument : positionalArguments) {
                builder.append(INDENTATION).append(positionalArgument.getName());
                builder.append(" ".repeat(Math.max(0, maxLength.get() - positionalArgument.getName().length())));
                builder.append(" : ").append(positionalArgument.getDescription());
                builder.append('\n');
            }
            builder.append('\n');
        }

        builder.append("FLAGS\n");
        Flag.OptionalFlagWithoutValue helpPseudoFlag = new Flag.OptionalFlagWithoutValue("h",
                Optional.of("help"), "Print the program usage.");
        builder.append(flagUsage(helpPseudoFlag));
        for (Flag flag : requiredFlags) {
            builder.append(flagUsage(flag));
        }
        for (Flag flag : optionalFlags) {
            builder.append(flagUsage(flag));
        }
        return builder.toString();
    }

    private static String flagUsage(Flag flag) {
        StringBuilder builder = new StringBuilder();
        builder.append(INDENTATION)
                .append('-')
                .append(flag.getShortName());
        if (flag.getLongName().isPresent()) {
            builder.append(" --")
                    .append(flag.getLongName().get());
        }
        if (requiresValue(flag)) {
            builder.append(" <value>");
        }
        builder.append('\n')
                .append(INDENTATION)
                .append(INDENTATION)
                .append(" : [")
                .append(isRequired(flag) ? "Required" : "Optional")
                .append("] ");
        if (hasDefaultValue(flag)) {
            builder.append("(default=").append(((HasDefaultValue) flag).getDefaultValue()).append(") ");
        }
        builder.append(flag.getDescription())
                .append('\n');
        return builder.toString();
    }

    private Collection<Flag> getAllFlags() {
        return shortNameMap.values();
    }

    private static boolean hasDefaultValue(Flag flag) {
        return flag instanceof HasDefaultValue;
    }

    private static boolean requiresValue(Flag flag) {
        return flag instanceof RequiresValue;
    }

    private static boolean isRequired(Flag flag) {
        return flag instanceof FlagMustAppear;
    }
}
