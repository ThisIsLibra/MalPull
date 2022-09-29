/*
 * Copyright (C) 2020 Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
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

import malpull.model.Arguments;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import malpull.exceptions.NoHashesFoundException;
import malpull.exceptions.NoServicesSetException;

/**
 * This class parses the given arguments into an Arguments object, thereby
 * simplifying the code in the main function, and splitting the sanity checks
 * from the rest of the code
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class ArgumentHandler {

    /**
     * Parses the arguments into an Arguments object.Stops MalPull upon the
     * occurrence of an exception and prints a help message for the user
     *
     * @param args the arguments to parse
     * @return the parsed arguments
     * @throws NoServicesSetException if none of the services are enabled
     * @throws NoHashesFoundException if no hashes are provided
     */
    public static Arguments handle(String[] args) throws NoServicesSetException, NoHashesFoundException {
        //A minimum of two arguments are required: the destination folder for the downloaded samples, and one or more hashes:
        //java -jar malpull.jar /path/to/write/samples/to hash1 hash2 hashN
        //Note that the keys file is required to be in the same folder as the JAR
        if (args.length < 2) {
            //If that is the case, the usage should be printed
            printUsage();
            //Then the system should exit
            System.exit(0);
        }

        String keysPath = null;
        //Get each key per line, sanity checks are performed within the loadFile function
        try {
            String baseFolder = new File(malpull.MalPull.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsolutePath();
            keysPath = baseFolder + File.separator + "keys.txt";
        } catch (URISyntaxException e) {
            System.out.println("The JAR's location cannot be obtained, check your privilege and try again!");
            System.exit(0);
        }

        List<String> keys = loadFile(keysPath);
        /**
         * Initialise all strings as null, only changing the value if it is
         * included in the keys list. Only if the value is not null, it should
         * be used later on
         */
        String threadsString = null;
        String koodousKey = null;
        String malwareBazaarKey = null;
        String malShareKey = null;
        String virusTotalKey = null;
        String triageKey = null;
        String virusShareKey = null;

        //Iterate through all keys, note that the endpoint prefix is case insensitive
        for (String key : keys) {
            if (key.toLowerCase().startsWith("threads=".toLowerCase())) {
                threadsString = key.substring("threads=".length(), key.length());
            } else if (key.toLowerCase().startsWith("koodous=".toLowerCase())) {
                koodousKey = key.substring("koodous=".length(), key.length());
            } else if (key.toLowerCase().startsWith("malwarebazaar=".toLowerCase())) {
                malwareBazaarKey = key.substring("malwarebazaar=".length(), key.length());
            } else if (key.toLowerCase().startsWith("malshare=".toLowerCase())) {
                malShareKey = key.substring("malshare=".length(), key.length());
            } else if (key.toLowerCase().startsWith("virustotal=".toLowerCase())) {
                virusTotalKey = key.substring("virustotal=".length(), key.length());
            } else if (key.toLowerCase().startsWith("triage=".toLowerCase())) {
                triageKey = key.substring("triage=".length(), key.length());
            } else if (key.toLowerCase().startsWith("virusshare=".toLowerCase())) {
                virusShareKey = key.substring("virusshare=".length(), key.length());
            }
        }

        int threadCount = -1;
        try {
            threadCount = Integer.parseInt(threadsString);
        } catch (Exception e) {
            System.out.println("Please provide a valid number for the thread count: " + args[0]);
            System.exit(0);
        }

        if (threadCount <= 0) {
            System.out.println("Please provide a non-negative non-zero number for the thread count within the keys file: " + threadCount);
            System.exit(0);
        }

        //Create a new set to store all hashes in, as a set cannot contain duplicate strings
        Set<String> hashes = new HashSet<>();

        //Add all hashes from the file into the set
        for (int i = 1; i < args.length; i++) {
            String hash = args[i];
            if (hash.isBlank() == false) {
                hashes.add(hash);
            }
        }

        //Get the current working directory of the invoker
        File path = null;

        try {
            path = new File(filterPath(args[0]));
        } catch (Exception e) {
            System.out.println("Please provide a valid path, the currently provided value is: " + args[0]);
            System.exit(0);
        }

        //If it does not exist, any and all missing folders are created
        if (path.exists() == false) {
            path.mkdirs();
        }

        //Return the parsed arguments
        return new Arguments(hashes, path.getAbsolutePath(), threadCount, koodousKey, malwareBazaarKey, malShareKey, virusTotalKey, triageKey, virusShareKey);
    }

    /**
     * Loads a file at the given path. The program exists if the file does not
     * exist, if the the given location is a folder, or if an IOException occurs
     *
     * @param path the path to load
     * @return the content of the file, one line per entry in the list
     */
    private static List<String> loadFile(String path) {
        //Create the output list
        List<String> output = new ArrayList<>();
        //Create the file object based on the given path
        path = filterPath(path);
        File file = new File(path);
        //Perform santiy checks
        if (file.isDirectory()) {
            System.out.println("The file at " + file.getAbsolutePath() + " is a directory!");
            System.exit(0);
        } else if (file.exists() == false) {
            System.out.println("The file at " + file.getAbsolutePath() + " does not exist!");
            System.exit(0);
        }

        //Read the file, line by line where one line contains one hash
        try ( BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.isBlank()) {
                    continue;
                }
                output.add(line);
            }
        } catch (IOException ex) {
            System.out.println("An exception occured when reading " + file.getAbsolutePath() + ":");
            System.exit(0);
        }
        return output;
    }

    /**
     * Replaces (if present) a starting tilde with the user's home directory
     *
     * @param path the path to check
     * @return the corrected path
     */
    private static String filterPath(String path) {
        if (path.startsWith("~")) {
            path = System.getProperty("user.home") + path.substring(1, path.length());
        }
        return path;
    }

    /**
     * Prints the program's usage
     */
    private static void printUsage() {
        System.out.println("This tool downloads samples from (and in order) Hatching Triage, Malware Bazaar, MalShare, VirusShare, VirusTotal, and Koodous, based on given MD-5, SHA-1, or SHA-256 hash(es).");
        System.out.println("The sample is written to the given output directory. API Keys for any of the used services is required.");
        System.out.println("Once all samples are downloaded, the hashes that couldn't be found will be listed.");
        System.out.println("For detailed information on the usage of MalPull, please visit https://maxkersten.nl/projects/malpull/#usage");
        System.out.println("");
        System.out.println("Sample usage of this program:");
        System.out.println("\t\tjava -jar /path/toMalPull.jar /path/to/write/samples/to hash1 hash2 hashN");
        System.out.println("Note that the keys.txt file is required to be in the same folder as MalPull's JAR!");
    }
}
