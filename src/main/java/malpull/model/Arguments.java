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
package malpull.model;

import java.util.Set;
import malpull.exceptions.NoHashesFoundException;
import malpull.exceptions.NoServicesSetException;

/**
 * The argument class, which contains all relevant information that MalPull
 * requires
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class Arguments {

    /**
     * The set that contains all loaded hashes
     */
    private Set<String> hashes;

    /**
     * The folder to write all samples to
     */
    private String outputPath;

    /**
     * The amount of threads to use, minimally one
     */
    private int threadCount;

    /**
     * The API key for Koodous
     */
    private String koodousKey;

    /**
     * The API key for Malware Bazaar is not required, meaning any value will
     * enable downloading from this service
     */
    private String malwareBazaarKey;

    /**
     * The API key for MalShare
     */
    private String malShareKey;

    /**
     * The API key for VirusTotal
     */
    private String virusTotalKey;

    /**
     * The API key for Triage
     */
    private String triageKey;

    /**
     * Creates an object that contains all parsed arguments
     *
     * @param hashes all loaded hashes, cannot be null or empty
     * @param outputPath the folder to write the downloads to
     * @param threadCount the amount of threads to use, a value of zero or lower
     * will set the count to 1
     * @param koodousKey the API key for Koodous, null if the service is not to
     * be used
     * @param malwareBazaarKey the API key for Malware Bazaar, null if the
     * service is not to be used
     * @param malShareKey the API key for MalShare, null if the service is not
     * to be used
     * @param virusTotalKey the API key for VirusTotal, null if the service is
     * not to be used
     * @param triageKey the API key for Triage, null if the service is not to be
     * used
     * @throws NoServicesSetException if all of the API key strings are null at
     * the same time, meaning no services will be reached
     * @throws NoHashesFoundException if the given set of hashes is null or
     * empty
     */
    public Arguments(Set<String> hashes, String outputPath, int threadCount, String koodousKey, String malwareBazaarKey, String malShareKey, String virusTotalKey, String triageKey) throws NoServicesSetException, NoHashesFoundException {
        this.hashes = hashes;
        this.outputPath = outputPath;
        this.threadCount = threadCount;
        this.koodousKey = koodousKey;
        this.malwareBazaarKey = malwareBazaarKey;
        this.malShareKey = malShareKey;
        this.virusTotalKey = virusTotalKey;
        this.triageKey = triageKey;

        if (koodousKey == null
                && malwareBazaarKey == null
                && malShareKey == null
                && virusTotalKey == null
                && triageKey == null) {
            throw new NoServicesSetException("No services have been set in the arguments object, as all API keys are null!");
        }

        if (hashes == null || hashes.isEmpty()) {
            throw new NoHashesFoundException("No hashes are provided when creating the arguments object, given that the list is either null or empty!");
        }

        if (this.threadCount <= 0) {
            this.threadCount = 1;
        }
    }

    /**
     * Gets all loaded hashes without duplicates
     *
     * @return all loaded hashes
     */
    public Set<String> getHashes() {
        return hashes;
    }

    /**
     * Get the output folder where the downloads should be written to
     *
     * @return the path to the output folder
     */
    public String getOutputPath() {
        return outputPath;
    }

    /**
     * Get the given thread count to use when downloading samples
     *
     * @return the thread count
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * Get the API key of Koodous, can be null if the API key is not used
     *
     * @return the API key
     */
    public String getKoodousKey() {
        return koodousKey;
    }

    /**
     * The API key of Malware Bazaar does not exist, so this value is either
     * null or not null. Exclusion from the keys file means the service wont be
     * used
     *
     * @return a value to see if this endpoint is to be used
     */
    public String getMalwareBazaarKey() {
        return malwareBazaarKey;
    }

    /**
     * Gets the API key of MalShare, can be null if the API key is not used
     *
     * @return the API key
     */
    public String getMalShareKey() {
        return malShareKey;
    }

    /**
     * The API key of VirusTotal, can be null if the API key is not used
     *
     * @return the API key
     */
    public String getVirusTotalKey() {
        return virusTotalKey;
    }

    /**
     * The API key of Triage, can be null if the API key is not used
     *
     * @return the API key
     */
    public String getTriageKey() {
        return triageKey;
    }
}
