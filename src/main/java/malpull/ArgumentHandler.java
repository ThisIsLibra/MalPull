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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class parses the given arguments into an Arguments object, thereby
 * simplifying the code in the main function, and splitting the sanity checks
 * from the rest of the code
 *
 * @author Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
 */
public class ArgumentHandler {

    /**
     * Parses the arguments into an Arguments object. Stops MalPull upon the
     * occurence of an exception and prints a help message for the user
     *
     * @param args the arguments to parse
     * @return the parsed arguments
     */
    public static Arguments handle(String[] args) {
        //Only 4 arguments are accepted:
        //java -jar malpull.jar threadCount /path/to/keys.txt /path/to/hashes.txt /path/to/write/samples/to
        if (args.length != 4) {
            //If that is the case, the usage should be printed
            printUsage();
            //Then the system should exit
            System.exit(0);
        }
        //Test if the thread count is a valid number
        try {
            Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("Please provide a valid number for the thread count: " + args[0]);
            System.exit(0);
        }
        //Get the thread count if its a valid number
        int threadCount = Integer.parseInt(args[0]);
        if (threadCount <= 0) {
            System.out.println("At least 1 thread is required!");
            System.exit(0);
        }

        //Get each key per line, sanity checks are performed within the loadFile function
        List<String> keys = loadFile(args[1]);
        /**
         * Initialise all strings as null, only changing the value if it is
         * included in the keys list. Only if the value is not null, it should
         * be used later on
         */
        String koodousKey = null;
        String malwareBazaarKey = null;
        String malShareKey = null;
        String virusTotalKey = null;
        //Iterate through all keys, note that the endpoint prefix is case insensitive
        for (String key : keys) {
            if (key.toLowerCase().startsWith("koodous=".toLowerCase())) {
                koodousKey = key.substring("koodous".length(), key.length());
            } else if (key.toLowerCase().startsWith("malwarebazaar=".toLowerCase())) {
                malwareBazaarKey = key.substring("malwarebazaar=".length(), key.length());
            } else if (key.toLowerCase().startsWith("malshare=".toLowerCase())) {
                malShareKey = key.substring("malshare=".length(), key.length());
            } else if (key.toLowerCase().startsWith("virustotal=".toLowerCase())) {
                virusTotalKey = key.substring("virustotal=".length(), key.length());
            }
        }

        //Create a new set to store all hashes in, as a set cannot contain duplicate strings
        Set<String> hashes = new HashSet<>();
        //Add all hashes from the file into the set, sanity checks are performed within the loadFile function
        hashes.addAll(loadFile(args[2]));

        //Get the output path as a file object. The path is filtered for the home symbol
        File path = new File(filterPath(args[3]));
        //If it does not exist, any missing folder is created
        if (path.exists() == false) {
            path.mkdirs();
        }

        //Return the parsed arguments
        return new Arguments(hashes, path.getAbsolutePath(), threadCount, koodousKey, malwareBazaarKey, malShareKey, virusTotalKey);
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
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                output.add(line);
            }
        } catch (IOException ex) {
            System.out.println("An exception occured when reading " + file.getAbsolutePath() + ":");
            Logger.getLogger(MalPull.class.getName()).log(Level.SEVERE, null, ex);
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
        System.out.println("This tool downloads samples from MalShare, MalwareBazaar, Koodous, and VirusTotal based on given MD-5, SHA-1, or SHA-256 hashes.");
        System.out.println("The sample is written to the given output directory. API Keys for any of the used services is required.");
        System.out.println("Once all samples are downloaded, the hashes that couldn't be found will be listed.");
        System.out.println("For detailed information on the usage of MalPull, please visit https://maxkersten.nl/wordpress/projects/malpull/#usage");
        System.out.println("");
        System.out.println("Sample usage of this program:");
        System.out.println("\t\tjava -jar /path/toMalPull.jar threadCount /path/to/keys.txt /path/to/hashes.txt /path/to/write/samples/to");
    }
}
