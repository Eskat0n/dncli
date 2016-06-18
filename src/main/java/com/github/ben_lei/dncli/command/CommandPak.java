package com.github.ben_lei.dncli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.github.ben_lei.dncli.converter.ByteCharacterConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by blei on 6/16/16.
 */
@Parameters(commandDescription = "DragonNest pak file extraction and compression.")
public class CommandPak {
    @Parameters(commandDescription = "Compresses a directory into a pak file.")
    public static class Compress {
        @Parameter(names = {"-i", "--input"}, description = "Input root \"\\\" directory.", converter = FileConverter.class, required = true)
        private File input;

        @Parameter(names = {"-o", "--output"}, description = "Output contents to provided file.", converter = FileConverter.class)
        private File output;

        @Parameter(names = {"-m", "--min"}, converter = ByteCharacterConverter.class,
            description = "Sets the min. size a compressed pak can be.")
        private Long min = 0L;

        @Parameter(names = {"-f", "--force"}, description = "Force overwrite files")
        private boolean force;
    }

    @Parameters(commandDescription = "Extracts DragonNest pak files to an output directory.")
    public static class Extract {
        @Parameter(description = "pakFiles...", required = true)
        private List<File> files = new ArrayList<>();

        @Parameter(names = {"-o", "--output"}, description = "Output directory to extract the paks.", converter = FileConverter.class, required = true)
        private File output;

        @Parameter(names = {"-j", "--javascript"}, description = "The filter JS file that should have a pakCompressFilter() function.",
            converter = FileConverter.class)
        private File filterFile;

        @Parameter(names = {"-f", "--force"}, description = "Force overwrite files")
        private boolean force;
    }

    @Parameters(commandDescription = "lists all files in pak")
    public static class Detail {
        @Parameter(description = "pakFiles...", required = true)
        private List<File> files = new ArrayList<>();
    }
}
