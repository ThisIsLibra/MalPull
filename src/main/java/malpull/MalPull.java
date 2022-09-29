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
package malpull;

import java.io.PrintStream;
import malpull.model.Arguments;
import malpull.endpoints.IEndpoint;
import malpull.endpoints.Koodous;
import malpull.endpoints.MalShare;
import malpull.endpoints.MalwareBazaar;
import malpull.endpoints.Triage;
import malpull.endpoints.VirusTotal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import malpull.endpoints.VirusShare;
import malpull.exceptions.NoArgumentsSetException;
import malpull.model.MalPullResult;

/**
 * This class can be used to use MalPull as a library in any project. The
 * constructors provide the option to log data. The download function requires
 * all parameters, which can be parsed via any front-end that is made, or
 * directly via code if this class is imported as a library.<br>
 * <br>
 * To compile this project as a single JAR, use:
 * <code>mvn clean compile assembly:single</code>. Install it in your local
 * Maven repository to use it as a library in other projects, or copy the code
 * into your project.
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class MalPull {

    /**
     * A mapping where the keys are equal to the user-provided hashes, and the
     * value per key is the endpoint the hash was downloaded from
     */
    private Map<String, String> downloadedSamples;

    /**
     * A list of hashes that could not be downloaded, taken from the
     * deduplicated input
     */
    private List<String> missingHashes;

    /**
     * The print stream to write the log to
     */
    private PrintStream outputStream;

    /**
     * Creates the MalPull object, automatically initialising the required
     * fields.
     *
     * @param outputStream the stream to write the logs to
     */
    public MalPull(PrintStream outputStream) {
        //Initialise the downloaded samples mapping
        downloadedSamples = new HashMap<>();
        //Initialise the missing hashes list
        missingHashes = new ArrayList<>();
        //Set the output stream
        this.outputStream = outputStream;
        log(getVersionInformation());
    }

    /**
     * Creates the MalPull object, automatically initialising the required
     * fields. This instance will not have a logger, as it is not provided in
     * the constructor
     */
    public MalPull() {
        //Initialise the downloaded samples mapping
        downloadedSamples = new HashMap<>();
        //Initialise the missing hashes list
        missingHashes = new ArrayList<>();
        //Set the output stream to null, thereby disabling it
        this.outputStream = null;
    }

    /**
     * Creates a download worker object, which can be scheduled with the thread
     * scheduler
     *
     * @param arguments the object that contains the API keys
     * @param hash the hash of the sample to use
     * @param count the number that corresponds with the hash (used in the
     * logging as <em>count/total</em>, where total is the total amount of
     * hashes in the arguments object
     * @return a list of download workers, ready to be scheduled
     */
    private DownloadWorker getDownloadWorker(Arguments arguments, String hash, int count) {
        //Create a list of endpoints
        List<IEndpoint> endpoints = new ArrayList<>();
        //Check all keys, and include endpoints for which keys are present
        if (arguments.getTriageKey() != null) {
            IEndpoint triage = new Triage(arguments.getTriageKey());
            endpoints.add(triage);
        }
        if (arguments.getMalwareBazaarKey() != null) {
            IEndpoint malwareBazaar = new MalwareBazaar();
            endpoints.add(malwareBazaar);
        }
        if (arguments.getMalShareKey() != null) {
            IEndpoint malShare = new MalShare(arguments.getMalShareKey());
            endpoints.add(malShare);
        }
        if (arguments.getVirusShareKey() != null) {
            IEndpoint virusShare = new VirusShare(arguments.getVirusShareKey());
            endpoints.add(virusShare);
        }
        if (arguments.getVirusTotalKey() != null) {
            IEndpoint virusTotal = new VirusTotal(arguments.getVirusTotalKey());
            endpoints.add(virusTotal);
        }
        if (arguments.getKoodousKey() != null) {
            IEndpoint koodous = new Koodous(arguments.getKoodousKey());
            endpoints.add(koodous);
        }

        //Create a download worker for the hash, with all configured endpoints embedded
        DownloadWorker worker = new DownloadWorker(this, endpoints, arguments.getOutputPath(), hash, count, arguments.getHashes().size());
        //Return the download worker
        return worker;
    }

    /**
     * Downloads all samples that correspond to the given hashes, using the
     * given services, to the given folder. Each sample is equal to the given
     * hash name. The logging is written to the stream that the constructor of
     * this object received. If none is given (or if it is equal to null), the
     * logging is not printed.<br>
     * <br>
     * This function can take a while to return, depending on the amount of
     * hashes that are provided, and the speed of the enabled services. Take
     * note of this!
     *
     * @param arguments the arguments to use
     * @return the download result, providing insight into all downloaded
     * samples, all missing hashes, and the time it took
     * @throws NoArgumentsSetException if the given argument object is null
     */
    public MalPullResult download(Arguments arguments) throws NoArgumentsSetException {
        //Check if the arguments are set
        if (arguments == null) {
            throw new NoArgumentsSetException("The arguments need to  be set prior to calling the download function");
        }

        /**
         * The validity of the arguments are checked upon the creation of the
         * arguments object, meaning that the presence of the object indicates
         * that the values can be trusted
         */
        //Get the start time
        long start = Instant.now().getEpochSecond();

        //Get the thread count from the parsed arguments
        ExecutorService executor = Executors.newFixedThreadPool(arguments.getThreadCount());

        int count = 0;
        //Iterate over all hashes
        for (String hash : arguments.getHashes()) {
            //Increase the count, which indicates is used in the logging to display the progress with respect to the total amount of hashes
            count++;
            //Get the download worker for the given hash
            DownloadWorker worker = getDownloadWorker(arguments, hash, count);
            //Schedule the worker
            executor.execute(worker);
        }

        //Once all workers are created, shut the executor down, meaning no new tasks can be added
        executor.shutdown();
        //Wait until the executor is terminated, which only happens when all downloads are finished
        while (!executor.isTerminated()) {
        }

        //Get the time since the downloading started
        String time = getDuration(start);

        //Return the result to the caller
        return new MalPullResult(downloadedSamples, missingHashes, time);
    }

    /**
     * Gets the duration from the given starting point until the moment this
     * function is executed in the format of <code>hhh:mm:ss</code>.
     *
     * @param start the time to start in seconds from epoch
     */
    private String getDuration(long start) {
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
        return String.format("%03d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * A thread safe function to write data to the given output stream
     *
     * @param message the message to log
     */
    protected synchronized void log(String message) {
        if (outputStream == null) {
            return;
        }
        outputStream.println(message);
    }

    /**
     * A thread safe function to add a hash (and the endpoint it was downloaded
     * from) to the mapping
     *
     * @param hash the hash to add
     * @param endpoint the endpoint where the hash was downloaded from
     */
    protected synchronized void addDownloadedHash(String hash, String endpoint) {
        downloadedSamples.put(hash, endpoint);
    }

    /**
     * A thread safe function to add a missing hash to the list
     *
     * @param missingHash the hash to add
     */
    protected synchronized void addMissingHash(String missingHash) {
        missingHashes.add(missingHash);
    }

    /**
     * Gets the version information, together with an additional newline
     *
     * @return the version information
     */
    public String getVersionInformation() {
        return "MalPull version 1.4-stable by Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]\n";
    }
}
