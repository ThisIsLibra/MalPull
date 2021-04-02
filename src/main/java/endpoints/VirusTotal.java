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
package endpoints;

import exceptions.SampleNotFoundException;
import okhttp3.Request;

/**
 * The class to get a sample from VirusTotal.
 *
 * @author Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
 */
public class VirusTotal extends GenericEndpoint implements IEndpoint {

    /**
     * The API key for VirusTotal
     */
    private String key;

    /**
     * Creates an object to interact with the VirusTotal endpoint, based on API
     * version 2
     *
     * @param key
     */
    public VirusTotal(String key) {
        super("https://www.virustotal.com/vtapi/v2/", "VirusTotal");
        this.key = key;
    }

    /**
     * Download the sample from the API
     *
     * @param hash the SHA-256 hash of the sample to download
     * @return the API's response, which is the raw file
     * @throws SampleNotFoundException if the sample cannot be found
     */
    @Override
    public byte[] getSample(String hash) throws SampleNotFoundException {
        //Create the URL
        String url = apiBase + "file/download?apikey=" + key + "&hash=" + hash;
        //Prepare the request with the API token
        Request request = new Request.Builder()
                .url(url)
                .build();
        //Return the value of the direct download link
        return downloader.get(request);
    }
}
