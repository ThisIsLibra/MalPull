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
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class is used to get a sample from the Koodous database via its API.
 *
 * @author Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
 */
public class Koodous extends GenericEndpoint implements IEndpoint {

    /**
     * The API key that is used to interact with the MalShare API
     */
    private String key;

    /**
     * Creates an object to interact with the Koodous API
     *
     * @param key the MalShare API key which is required to use the API
     */
    public Koodous(String key) {
        //Sets the apiBase variable in the abstract GenericEndpoint class
        super("https://koodous.com/api/apks", "Koodous");
        //Sets the key variable
        this.key = key;
    }

    /**
     * Gets the sample from Koodous, if it is present. Throws an exception if it
     * is not present.
     *
     * @param hash the hash to look for
     * @return the sample as a byte array
     * @throws SampleNotFoundException if the sample cannot be found
     */
    @Override
    public byte[] getSample(String hash) throws SampleNotFoundException {
        //Get the SHA-256 hash via the search function of the API, as only SHA-256 hashes can be used when downloading a sample
        String sha256Hash = getSha256Hash(hash);
        //Return the API's response
        return download(sha256Hash);
    }

    /**
     * Get the SHA-256 hash based upon a given MD-5, SHA-1, or SHA-256 hash. The
     * download API of Koodous only accepts SHA-256 hashes when queried.
     *
     * @param hash the hash to query the back-end for, and obtain the SHA-256
     * hash of the sample
     * @return the SHA-256 hash of the given sample
     * @throws HttpConnectionFailed if no connection can be made from the
     * current machine, or to the given host
     * @throws SampleNotFoundException if the sample cannot be found
     * @throws Error404NotFoundException if the target returns a 404 status code
     * @throws Error429TooManyRequestsException if the target returns a 429
     */
    private String getSha256Hash(String hash) throws SampleNotFoundException {
        //Create the url
        String url = apiBase + "?search=" + hash + "&page=%1&page_size=%100";
        //Create the requested, based on the given URL and with the required header token
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Token " + key)
                .build();
        //Get the result from the get request as a string
        String result = new String(downloader.get(request));
        //Convert the string in a JSON object
        JSONObject jsonObject = new JSONObject(result);
        //Get the count based on the search result
        int count = jsonObject.optInt("count", 0);
        if (count == 0) {
            //If there are no hits, the sample is not present
            throw new SampleNotFoundException("Sample " + hash + "  not found on Koodous!");
        }
        //Get the results if there are any
        JSONArray results = jsonObject.getJSONArray("results");
        //Return the SHA-256 hash of the first hit
        return results.getJSONObject(0).getString("sha256");
    }

    /**
     * Download the sample from the API
     *
     * @param hash the SHA-256 hash of the sample to download
     * @return the API's response, which is the raw file
     * @throws HttpConnectionFailed if no connection can be made from the
     * current machine, or to the given host
     * @throws Error404NotFoundException if the target returns a 404 status code
     * @throws Error429TooManyRequestsException if the target returns a 429
     */
    private byte[] download(String hash) throws SampleNotFoundException {
        //Create the URL
        String url = apiBase + "/" + hash + "/download";
        //Prepare the request with teh API token
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Token " + key)
                .build();
        //Get the result from the API as a string
        String temp = new String(downloader.get(request));
        //Convert the string into a JSON object, and get the direct download URL
        String directUrl = new JSONObject(temp).getString("download_url");
        //Reset the request to the direct download URL
        request = new Request.Builder()
                .url(directUrl)
                .addHeader("Authorization", "Token " + key)
                .build();
        //Return the value of the direct download link
        return downloader.get(request);
    }
}
