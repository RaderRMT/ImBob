package fr.rader.imbob.utils.errors;

import fr.rader.imbob.utils.LineReader;
import fr.rader.imbob.utils.StringUtils;
import fr.rader.imbob.Logger;

import java.io.File;

public class ErrorPrinter {

    private static final Logger logger = Logger.getInstance();

    /**
     * Print a nice error message to the user
     *
     * @param error the error (not formatted)
     */
    public static void printError(Error error) {
        printError(
                null,
                0,
                error
        );
    }

    /**
     * Print a nice error message to the user
     *
     * @param file source file
     * @param line line in the source that has the error
     * @param errorID the error id (please refer to {@link Error})
     * @param errorMessage the error message
     * @param solutions the possible solution(s) to fix the issue
     */
    public static void printError(File file, int line, String errorID, String errorMessage, String[] solutions) {
        printError(
                file,
                line,
                0,
                0,
                errorID,
                errorMessage,
                solutions
        );
    }

    /**
     * Print a nice error message to the user
     *
     * @param file source file
     * @param line line in the source that has the error
     * @param error the error (not formatted)
     */
    public static void printError(File file, int line, Error error) {
        printError(
                file,
                line,
                0,
                0,
                error
        );
    }

    /**
     * Print a nice error message to the user
     *
     * @param file source file
     * @param line line in the source that has the error
     * @param start start index for the underline
     * @param size size of the underline
     * @param error the error (not formatted)
     */
    public static void printError(File file, int line, int start, int size, Error error) {
        printError(
                file,
                line,
                start,
                size,
                error.getErrorID(),
                error.getErrorMessage(),
                error.getSolutions()
        );
    }

    /**
     * Print a nice error message to the user
     *
     * @param file source file
     * @param line line in the source that has the error
     * @param start start index for the underline
     * @param size size of the underline
     * @param errorID the error id (please refer to {@link Error})
     * @param errorMessage the error message
     * @param solutions the possible solution(s) to fix the issue
     */
    public static void printError(File file, int line, int start, int size, String errorID, String errorMessage, String[] solutions) {
        logger.error("Error " + errorID + ":");
        logger.error("");

        // if we give a file to this method,
        // we'll use it to print the line
        // that has the error
        if (file != null) {
            // we get the line
            String lineWithError = LineReader.getLine(file, line);
            // we return if we can't get the line
            if (lineWithError == null) {
                return;
            }

            logger.error(line + ":  " + lineWithError.trim());

            // we check if we have to underline it
            if (start != 0 && size != 0) {
                // we remove the leading spaces from the start index
                start -= StringUtils.leadingSpaces(lineWithError);

                // we build the underline
                StringBuilder underline = new StringBuilder("    ");
                for (int i = 0; i < start + size; i++) {
                    // as long as i is less than start + size,
                    // we add spaces to the underline to put it
                    // right under what we want to underline
                    underline.append((i < start) ? " " : "-");
                }

                // we print the underline
                logger.error(underline.toString());
            }
        }

        // we build the error message
        StringBuilder error = new StringBuilder("    ");
        for (int i = 0; i < start; i++) {
            error.append(" ");
        }

        // and we print it
        logger.error(error + "> " + errorMessage);

        // if the solutions array isn't empty,
        // we print the solutions
        if (!solutions[0].isEmpty()) {
            logger.error("");
            logger.error("Suggestion" + (solutions.length > 1 ? "s" : "") + ":");

            // print each solutions
            for (String solution : solutions) {
                logger.error("    > " + solution);
            }
        }
    }
}
