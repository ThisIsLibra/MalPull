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

import concurrency.DownloadWorker;
import endpoints.IEndpoint;
import endpoints.Koodous;
import endpoints.MalShare;
import endpoints.MalwareBazaar;
import endpoints.Triage;
import endpoints.VirusTotal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 * To compile this project, use: mvn clean compile assembly:single
 *
 * @author Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
 */
public class MalPull {

    /**
     * A list of hashes that could not be downloaded, taken from the
     * deduplicated input
     */
    private static List<String> missingHashes = new ArrayList<>();

    /**
     * Build using:
     *
     * mvn clean compile assembly:single
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        //Get the start time
        long start = Instant.now().getEpochSecond();
        //Show the version information
        printVersionInformation();
        /**
         * Parse the arguments into a newly created object. If the arguments
         * cannot be parsed properly, the error message is displayed and MalPull
         * shuts down.
         */
        Arguments arguments = ArgumentHandler.handle(args);

        //Show the input back to the user, as this helps to avoid mistakes
        System.out.println("Read " + arguments.getHashes().size() + " hashes");
        System.out.println("Downloading will be done using " + arguments.getThreadCount() + " thread(s)");
        System.out.println("Output will be written to: " + arguments.getOutputPath());
        System.out.println("");

        //Get all hashes in deduplicated form
        Set<String> hashes = arguments.getHashes();

        //Get the thread count from the parsed arguments
        ExecutorService executor = Executors.newFixedThreadPool(arguments.getThreadCount());
        //Keep track of the count so each thread can print what number it had comapred to the total amount of downloads
        int count = 0;
        //Iterate through all hashes, adding an endpoint for each of the configured endpoints
        for (String hash : hashes) {
            count++;
            List<IEndpoint> endpoints = new ArrayList<>();
            if (arguments.getMalwareBazaarKey() != null) {
                IEndpoint malwareBazaar = new MalwareBazaar();
                endpoints.add(malwareBazaar);
            }
            if (arguments.getMalShareKey() != null) {
                IEndpoint malShare = new MalShare(arguments.getMalShareKey());
                endpoints.add(malShare);
            }
            if (arguments.getKoodousKey() != null) {
                IEndpoint koodous = new Koodous(arguments.getKoodousKey());
                endpoints.add(koodous);
            }
            if (arguments.getVirusTotalKey() != null) {
                IEndpoint virusTotal = new VirusTotal(arguments.getVirusTotalKey());
                endpoints.add(virusTotal);
            }
            if (arguments.getTriageKey() != null) {
                IEndpoint triage = new Triage(arguments.getTriageKey());
                endpoints.add(triage);
            }

            //Create a download worker for the hash, with all configured endpoints embedded
            DownloadWorker downloadWorker = new DownloadWorker(endpoints, arguments.getOutputPath(), hash, count, hashes.size());
            //Execute the download worker in the future, disregarding when specifically
            executor.execute(downloadWorker);
        }
        //Once all tasks are done, shut the executor down, meaning no new tasks can be added
        executor.shutdown();
        //Wait until the executor is terminated, which only happens when all downloads are finished
        while (!executor.isTerminated()) {
        }
        //Notify the user that all downloads are finished
        System.out.println("");
        System.out.println("All downloads finished! The sample number count is not always printed in ascending order, as the threads print the messages.");

        //If some hashes could not be found, these are printed
        if (missingHashes.size() > 0) {
            System.out.println("\n\nMissing " + missingHashes.size() + " hashes:");
            for (String missingHash : missingHashes) {
                System.out.println(missingHash);
            }
        }
        //Get the time since the start of the downloading
        String time = getDuration(start);
        //Display the time that the download process took
        System.out.println("\nDownloaded " + (hashes.size() - missingHashes.size()) + " samples in " + time + "!");
        //Exit the program explicitly, as it sometimes remains open in some edge cases
        System.exit(0);
    }

    /**
     * Gets the duration from the given starting point until the moment this
     * function is executed in the format of hh:mm:ss.
     *
     * @param start the time to start in seconds from epoch
     */
    private static String getDuration(long start) {
        //Get the end time
        long end = Instant.now().getEpochSecond();
        //Calculate the time difference
        long duration = end - start;

        //Calculate the amount of seconds
        long seconds = duration % 60;
        //Calculate the amount of minutes
        long minutes = (duration / 60) % 60;
        //Calculate the amount of hours
        long hours = (duration / (60 * 60)) % 24;
        //Format the times into a single string and return those
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * As the add function is not thread safe, this synchronised function is
     * used as a wrapper
     *
     * @param missingHash the hash to add
     */
    public static synchronized void addMissingHash(String missingHash) {
        missingHashes.add(missingHash);
    }

    /**
     * Prints the version information, together with an additional newline
     */
    private static void printVersionInformation() {
        System.out.println("MalPull version 1.2-stable by Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]\n");
    }
}
