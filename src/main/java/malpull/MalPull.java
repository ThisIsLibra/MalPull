/*
 * Copyright (C) 2020 Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
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
package malpull;

import endpoints.Koodous;
import endpoints.MalShare;
import endpoints.MalwareBazaar;
import exceptions.Error404NotFoundException;
import exceptions.Error429TooManyRequestsException;
import exceptions.HttpConnectionFailed;
import exceptions.SampleNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class is the main class of the project, and creates and calls all
 * objects. The project is set-up to have one malware repository service per
 * class, which is contacted via the service's API. The classes within the
 * <code>endpoints</code> package all correspond to a single service.
 *
 * The Downloader class, which resides in this package, functions as a wrapper
 * class around the OkHttp3 library that is being used to perform the HTTP
 * requests.
 *
 * To compile this project, use : mvn clean compile assembly:single
 *
 * @author Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
 */
public class MalPull {

    /**
     * A variable that is used to debug the program during development. If set
     * to true, the argument length check is skipped, and the parameters are
     * instantiated based on hard coded values within the main function.
     */
    private static final boolean DEBUG = false;

    public static void main(String[] args) {
        //Show the version information
        printVersionInformation();
        /**
         * If the length of the arguments is not equal to four, not all
         * information is provided. Additionally, the debug flag should be set
         * to false
         */
        if (args.length != 4 && !DEBUG) {
            //If that is the case, the usage should be printed
            printUsage();
            //Then the system should exit
            System.exit(0);
        }

        /**
         * Initialise local variables which are either set based on the given
         * arguments, or based on hard coded values when debugging
         */
        String keyMalShare;
        String keyKoodous;
        String hash;
        String path;

        if (DEBUG) {
            //The MalShare key to use during debugging
            keyMalShare = "";
            //The Koodous key to use during debugging
            keyKoodous = "";
            /**
             * Sample hashes to test with:
             *
             * Hash for MalShare: 78f29761f7f0f57a8f92e5f23d9e4d2d6465e848
             *
             * Hash for MalwareBazaar:
             * 094fd325049b8a9cf6d3e5ef2a6d4cc6a567d7d49c35f8bb8dd9e3c6acf3d78d
             *
             * Hash for Koodous: a24f14c4b95994d8440ed8f1c001a6135706716f
             */
            hash = "a24f14c4b95994d8440ed8f1c001a6135706716f";
            //The path to write the sample to
            path = "/home/user/test";
        } else {
            //Get the keys, hash, and path from the command line arguments
            keyMalShare = args[0];
            keyKoodous = args[1];
            hash = args[2];
            path = args[3];
        }

        /**
         * To get a sample, a try[ServiceName] function is called. This function
         * uses the respective service's endpoint class to check for the
         * availability of the sample. If it is present, it is download, after
         * which MalwarePuller shuts down to avoid using more API calls than
         * required.
         */
        tryMalShare(keyMalShare, hash, path);
        tryMalwareBazaar(hash, path);
        tryKoodous(keyKoodous, hash, path);
    }

    /**
     * Tries to get the sample from MalShare using the given API key. If it is
     * present, it is written to the given path and closes MalwarePuller.
     *
     * @param key the API key for MalShare
     * @param hash the sample's hash
     * @param path the path to write the sample to
     */
    private static void tryMalShare(String key, String hash, String path) {
        try {
            System.out.println("Searching MalShare...");
            byte[] output = new MalShare(key).getSample(hash);
            System.out.println("Sample found on MalShare!");
            System.out.println("Saving the sample to " + path);
            writeToDisk(output, path);
            System.out.println("Wrote sample to disk!");
            System.out.println("Exiting now");
            System.exit(0);
        } catch (HttpConnectionFailed ex) {
            System.out.println("An error occured when contacting MalShare, verify your own internet connection, as well as the uptime of MalShare!");
        } catch (SampleNotFoundException | Error404NotFoundException ex) {
            System.out.println("Sample not found on MalShare");
        } catch (Error429TooManyRequestsException ex) {
            System.out.println("MalShare reports that there are too many requests with this API key, try again later!");
        }
    }

    /**
     * Tries to get the sample from MalwareBazaar. If it is present, it is
     * written to the given path and closes MalwarePuller.
     *
     * @param hash the sample's hash
     * @param path the path to write the sample to
     */
    private static void tryMalwareBazaar(String hash, String path) {
        try {
            System.out.println("Searching MalwareBazaar...");
            byte[] output = new MalwareBazaar().getSample(hash);
            System.out.println("Sample found on MalwareBazaar!");
            System.out.println("Saving the sample to " + path);
            writeToDisk(output, path);
            System.out.println("Wrote sample to disk!");
            System.out.println("Exiting now");
            System.exit(0);
        } catch (HttpConnectionFailed ex) {
            System.out.println("An error occured when contacting MalwareBazaar, verify your own internet connection, as well as the uptime of MalwareBazaar!");
        } catch (SampleNotFoundException ex) {
            System.out.println("Sample not found on Malware Bazaar");
        }
    }

    /**
     * Tries to get the sample from Koodous using the given API key. If it is
     * present, it is written to the given path and closes MalwarePuller.
     *
     * @param key the API key for Koodous
     * @param hash the sample's hash
     * @param path the path to write the sample to
     */
    private static void tryKoodous(String key, String hash, String path) {
        try {
            System.out.println("Searching Koodous...");
            byte[] output = new Koodous(key).getSample(hash);
            System.out.println("Sample found on Koodous!");
            System.out.println("Saving the sample to " + path);
            writeToDisk(output, path);
            System.out.println("Wrote sample to disk!");
            System.out.println("Exiting now");
            System.exit(0);
        } catch (HttpConnectionFailed ex) {
            System.out.println("An error occured when contacting Koodous, verify your own internet connection, as well as the uptime of Koodous!");
        } catch (SampleNotFoundException | Error404NotFoundException ex) {
            System.out.println("Sample not found on Koodous");
        } catch (Error429TooManyRequestsException ex) {
            System.out.println("Koodous reports that there are too many requests with this API key, try again later!");
        }
    }

    /**
     * Writes the given byte array to the disk to the given location
     *
     * @param output the data to write to the disk
     * @param path the location to write the given data to
     */
    private static void writeToDisk(byte[] output, String path) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(output);
        } catch (IOException ex) {
            System.out.println("An error occured when writing the sample to \"" + path + "\". Verify your permissions and try again!");
        }
    }

    /**
     * Prints the version information, together with an additional newline
     */
    private static void printVersionInformation() {
        System.out.println("MalPull version 1.0-stable by Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]\n");
    }

    /**
     * Prints the program's usage
     */
    private static void printUsage() {
        System.out.println("This tool downloads a sample from MalShare, MalwareBazaar, and Koodous based on a given MD-5, SHA-1, or SHA-256 hash.");
        System.out.println("The sample is written to the given output directory. API Keys for both MalShare and Koodous are required.");
        System.out.println("Once the sample is found, it is downloaded and saved, after which MalwarePuller will exit.");
        System.out.println("");
        System.out.println("Sample usage of this program:");
        System.out.println("\t\tjava -jar /path/toMalwarePuller.jar malshare_api_key koodous_api_key sampleHash /path/to/save/the/sample/to");
    }

}
