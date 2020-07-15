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

import java.util.Set;

/**
 * The argument class, which contains all relevant information from the parsed
 * arguments
 *
 * @author Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
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
     * The API key for Malware Bazaar is not required, meaning any value will enable downloading from this service
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
     * Creates an object that contains all parsed arguments
     * @param hashes all loaded hashes
     * @param outputPath the folder to write the downloads to
     * @param threadCount the amount of threads to use
     * @param koodousKey the API key for Koodous
     * @param malwareBazaarKey the API key for Malware Bazaar
     * @param malShareKey the API key for MalShare
     * @param virusTotalKey  the API key for VirusTotal
     */
    public Arguments(Set<String> hashes, String outputPath, int threadCount, String koodousKey, String malwareBazaarKey, String malShareKey, String virusTotalKey) {
        this.hashes = hashes;
        this.outputPath = outputPath;
        this.threadCount = threadCount;
        this.koodousKey = koodousKey;
        this.malwareBazaarKey = malwareBazaarKey;
        this.malShareKey = malShareKey;
        this.virusTotalKey = virusTotalKey;
    }

    /**
     * Gets all loaded hashes without duplicates
     * @return all loaded hashes
     */
    public Set<String> getHashes() {
        return hashes;
    }

    /**
     * Get the output folder where the downloads should be written to
     * @return the path to the output folder
     */
    public String getOutputPath() {
        return outputPath;
    }

    /**
     * Get the given thread count to use when downloading samples
     * @return the thread count
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * Get the API key of Koodous, can be null if the API key is not used
     * @return the API key
     */
    public String getKoodousKey() {
        return koodousKey;
    }

    /**
     * The API key of Malware Bazaar does not exist, so this value is either null or not null. Exclusion from the keys file means the service wont be used
     * @return a value to see if this endpoint is to be used
     */
    public String getMalwareBazaarKey() {
        return malwareBazaarKey;
    }

    /**
     * Gets the API key of MalShare, can be null if the API key is not used
     * @return the API key
     */
    public String getMalShareKey() {
        return malShareKey;
    }

    /**
     * The API key of VirusTotal, can be null if the API key is not used
     * @return the API key
     */
    public String getVirusTotalKey() {
        return virusTotalKey;
    }
}
