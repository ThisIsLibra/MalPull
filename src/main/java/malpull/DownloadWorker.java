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

import malpull.endpoints.IEndpoint;
import malpull.exceptions.SampleNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import malpull.MalPull;

/**
 * A worker class that extends the runnable interface, meaning it can be
 * executed as a thread. This class tries to download the given sample from any
 * of the endpoints in the list. If it fails, the hash is added to the missing
 * hashes list in the main class.
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class DownloadWorker implements Runnable {

    /**
     * The instance of MalPull to connect back to
     */
    private MalPull malPull;

    /**
     * The list of endpoints iterate through in an attempt to download the hash
     */
    private List<IEndpoint> endpoints;

    /**
     * The path to write the file to if it is found
     */
    private String path;

    /**
     * The hash of the sample to download
     */
    private String hash;

    /**
     * The number of this worker in the queue, used to print if the sample can
     * be downloaded to display the progress to the user
     */
    private int count;

    /**
     * The total amount of samples that are in the queue, used to display the
     * progress to the user
     */
    private int total;

    /**
     * Creates a worker object, which can be queued for the thread pool
     *
     * @param malPull the MalPull instance to connect back to
     * @param endpoints the list of endpoints to attempt to download from
     * @param path the location to write the file to the disk
     * @param hash the hash to look for
     * @param count the queue number of this worker, remains unchanged after
     * creation
     * @param total the total number of samples to be downloaded
     */
    public DownloadWorker(MalPull malPull, List<IEndpoint> endpoints, String path, String hash, int count, int total) {
        this.malPull = malPull;
        this.endpoints = endpoints;
        this.path = path;
        this.hash = hash;
        this.count = count;
        this.total = total;
    }

    /**
     * Downloads the sample to the given location. If the sample cannot be
     * found, the hash is added to the list of missing hashes, which is printed
     * at the end.
     */
    @Override
    public void run() {
        try {
            //Add the hash to the file name
            String filePath = path + File.separator + hash;
            //The boolean to check if the sample has been downloaded
            boolean isDownloaded = false;
            //Iterate through all the endpoints
            for (IEndpoint endpoint : endpoints) {
                //Try to dowload the file
                try {
                    /**
                     * If it is already downloaded, the iteration loop should be
                     * broken to avoid more API calls than are required, and to
                     * move on to the next worker in the queue
                     */
                    if (isDownloaded) {
                        break;
                    }
                    //Get the sample from the endpoint
                    byte[] output = endpoint.getSample(hash);
                    //If the output is not zero bytes
                    if (output.length > 0) {
                        //The file is written to disk
                        writeToDisk(output, filePath);
                        //A message is printed for the user
                        malPull.log("(" + count + " / " + total + ") Wrote " + output.length + " bytes to " + filePath + " from " + endpoint.getName());
                        //Add the hash to the log
                        malPull.addDownloadedHash(hash, endpoint.getName());
                        //The boolean is set to true, causing the next iteration to break out of the loop
                        isDownloaded = true;
                    }
                } catch (SampleNotFoundException ex) {
                    /**
                     * The exception message can be ignored, as failure to
                     * download the sample results in the missing hash, but only
                     * if none of the configured endpoints has the hash
                     */
                }
            }
            //If the sample is not downloaded after the loop, it is missing
            if (isDownloaded == false) {
                //This method is thread safe
                malPull.addMissingHash(hash);
                malPull.log("(" + count + " / " + total + ") Added \"" + hash + "\" to the missing hashes");
            }
        } catch (Exception ex) {
            String message = "(" + count + " / " + total + ") An error occured when downloading " + hash + ":\n" + ex.getMessage();
            malPull.log(message);
        }
    }

    /**
     * Writes the given byte array to the disk to the given location
     *
     * @param output the data to write to the disk
     * @param path the location to write the given data to
     */
    private void writeToDisk(byte[] output, String path) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(output);
        } catch (IOException ex) {
            malPull.log("An error occured when writing the sample to \"" + path + "\". Check the permissions and try again! Error:\n" + ex.getMessage());
        }
    }
}
