package constants;

public final class Messages {

    public static final String FILE_NOT_UPLOADED = "The file not uploaded. Please load the file with ";
    public static final String HELP_MESSAGE = "Press \"Load file\" button to load the file with transport task.\n" +
            "Input file format: \n" +
            "<tariff 11><spaces>...<tariff 1n><spaces><production capacity 1>\n" +
            "...\n" +
            "<tariff m1><spaces>...<tariff mn><spaces><production capacity m>\n" +
            "<power consumption 1><spaces>...<power consumption n>\n" +
            "<spaces> -- this is a certain number of spaces;\n" +
            "<tariff ij> -- tariff for the transport of a unit of cargo from i production to j consumer.\n\n" +
            "Press \"Solve\" button to view the solution of the transport problem.\n" +
            "This program is capable of solving a general transport problem of an arbitrary type.";
    public static final String INVALID_FORMAT = "Invalid input file format.";
    public static final String FILE_NOT_LOADED = "Failed to load input file.";
}
