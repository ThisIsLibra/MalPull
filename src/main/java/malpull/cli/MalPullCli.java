/*
 * Copyright (C) 2021 Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package malpull.cli;

import malpull.MalPull;
import malpull.exceptions.NoArgumentsSetException;
import malpull.model.Arguments;
import malpull.exceptions.NoHashesFoundException;
import malpull.exceptions.NoServicesSetException;
import malpull.model.MalPullResult;

/**
 * This class is the command-line interface wrapper for the MalPull object.<br>
 * <br>
 * Build using: <code>mvn clean compile assembly:single</code>
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class MalPullCli {

    /**
     * The main function of the program, providing a CLI to use. The
     * ArgumentHandler class provides help regarding the CLI arguments that are
     * required.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        //Create the MalPull instance, using the standard system output printstream to print the logging
        MalPull malPull = new MalPull(System.out);

        /**
         * Parse the arguments into a newly created object. If the arguments
         * cannot be parsed properly, the error message is displayed and MalPull
         * shuts down.
         */
        try {
            //Parse the arguments
            Arguments arguments = ArgumentHandler.handle(args);

            System.out.println("The available platforms are:");
            for (String platform : arguments.getAvailablePlatforms()) {
                System.out.println("\t" + platform);
            }
            System.out.println("");

            //Show the input to the user, as this helps to avoid mistakes
            System.out.println("Read " + arguments.getHashes().size() + " hashes");
            System.out.println("Downloading will be done using " + arguments.getThreadCount() + " thread(s)");
            System.out.println("Output will be written to: " + arguments.getOutputPath());
            System.out.println("");

            //Start the downloading, where the progress will be printed to the standard output by the threads
            MalPullResult result = malPull.download(arguments);

            //Notify the user that all downloads are finished
            System.out.println("\nAll downloads finished! The sample number count is not always printed in ascending order, as the threads print the messages.");

            //If some hashes could not be found, these are printed
            if (result.getMissingHashes().isEmpty() == false) {
                System.out.println("\n\nMissing " + result.getMissingHashes().size() + " hashes:");
                for (String missingHash : result.getMissingHashes()) {
                    System.out.println(missingHash);
                }
            }
            //Display the time that the download process took
            System.out.println("\nDownloaded " + result.getDownloadedSamples().size() + " samples in " + result.getTime() + "!");
            //Exit the program explicitly, as it sometimes remains open in some edge cases
            System.exit(0);
        } catch (NoServicesSetException | NoHashesFoundException | NoArgumentsSetException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
