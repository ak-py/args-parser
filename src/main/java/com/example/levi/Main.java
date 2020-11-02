package com.example.levi;

import org.example.argumentparser.ArgumentParserBuilder;
import org.example.argumentparser.FlagBuilder;
import org.example.argumentparser.PositionalArgument;

public class Main {
    public static void main(String[] args) {
        var certFile = FlagBuilder.newBuilder("-c", "The certificate file.")
                .useLongName("--cert_file")
                .requireValue()
                .mustAppear()
                .build();

        var dard = FlagBuilder.newBuilder("-d", "Darde.")
                .useLongName("--dard")
                .build();

        var frankie = FlagBuilder.newBuilder("-f", "Frankkkkiiieeee....")
                .useLongName("--frank")
                .requireValue()
                .useDefaultValue("Meow!")
                .build();

        var levi = FlagBuilder.newBuilder("-l", "Levi's haddi location" /* not found 404 */)
                .useLongName("--levi")
                .requireValue()
                .build();

        var src = new PositionalArgument("src", "source file location");
        var dst = new PositionalArgument("destination", "destination file location");

        var parser = ArgumentParserBuilder.newBuilder("haddis")
                .setProgramDescription("cool program")
                .addFlags(certFile, dard, levi, frankie)
                .addPositionalArguments(src, dst)
                .build();

        parser.parse(args);
    }
}
