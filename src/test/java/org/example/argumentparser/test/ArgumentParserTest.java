package org.example.argumentparser.test;

import org.example.argumentparser.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentParserTest {

    private Flag.RequiredFlagWithValue certFile;
    private Flag.OptionalFlagWithoutValue dard;
    private Flag.OptionalFlagWithDefaultValue frankie;
    private Flag.OptionalFlagWithValue levi;
    private PositionalArgument src;
    private PositionalArgument dst;
    private ArgumentParser parser;

    @BeforeEach
    public void setUp() {
        certFile = FlagBuilder.newBuilder("-c", "The certificate file.")
                .useLongName("--cert_file")
                .requireValue()
                .mustAppear()
                .build();

        dard = FlagBuilder.newBuilder("-d", "Darde Disco.")
                .useLongName("--dard")
                .build();

        frankie = FlagBuilder.newBuilder("-f", "Frankkkkiiieeee....")
                .useLongName("--frank")
                .requireValue()
                .useDefaultValue("Meow!")
                .build();

        levi = FlagBuilder.newBuilder("-l", "Levi's haddi location" /* not found 404 */)
                .useLongName("--levi")
                .requireValue()
                .build();

        src = new PositionalArgument("src", "source file location");
        dst = new PositionalArgument("destination", "destination file location");

        parser = ArgumentParserBuilder.newBuilder("haddis")
                .setProgramDescription("cool program")
                .addFlags(certFile, dard, levi, frankie)
                .addPositionalArguments(src, dst)
                .build();
    }

    @Test
    public void validInputs() {
        var args = new String[] {
                "--cert_file",
                "~/.ssh/cert_file",
                "~/source_file",
                "~/dest_folder",
        };
        parser.parse(args);



        assertEquals("~/.ssh/cert_file", certFile.getValue());
        assertEquals("~/source_file", src.getValue());
        assertEquals("~/dest_folder", dst.getValue());
        assertFalse(dard.hasAppeared());
        assertEquals(frankie.getValue(), "Meow!");
        assertTrue(levi.getValue().isEmpty());
    }

    @Test
    public void emptyInput() {
        assertThrows(IllegalStateException.class, () -> {
            parser.parse(new String[]{});
        });
    }

}