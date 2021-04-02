/*
 * Copyright (C) 2021 Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The class that is used to get a sample from Triage
 *
 * @author Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
 */
public class Triage extends GenericEndpoint implements IEndpoint {

    /**
     * The API key for Triage
     */
    private String key;

    /**
     * The HTTP client that sends out the request
     */
    protected final OkHttpClient httpClient;

    /**
     * Creates an object to interact with the Triage endpoint
     *
     * @param key
     */
    public Triage(String key) {
        super("https://api.tria.ge/v0/", "Triage");
        this.key = key;
        httpClient = new OkHttpClient();
    }

    /**
     * Retrieves the sample identifier from Triage that is required to download
     * the sample
     *
     * @param hash SHA-256 hash of the sample to download
     * @return sample identifier assigned by Triage
     * @throws SampleNotFoundException if the sample cannot be found for any
     * reason whatsoever
     */
    public String getSampleId(String hash) throws SampleNotFoundException {
        //Create the URL
        String url = apiBase + "search?query=" + hash;
        //Prepare the request with the API key
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + key)
                .build();
        //Execute the request
        try (Response response = httpClient.newCall(request).execute()) {
            //Check if the request was successful
            if (response.isSuccessful()) {
                //Parse the JSON response and extract the identifier
                JSONObject object = new JSONObject(response.body().string());
                JSONArray array = object.getJSONArray("data");
                return array.getJSONObject(0).getString("id");
            } else {
                throw new SampleNotFoundException("Sample not present on Triage!");
            }
        } catch (Exception ex) {
            throw new SampleNotFoundException("An exception occured when getting the sample id for Triage!");
        }
    }

    /**
     * Download the sample from the API
     *
     * @param hash SHA-256 hash of the sample to download
     * @return the API's response, which is the raw file
     * @throws SampleNotFoundException if the sample cannot be found
     */
    public byte[] getSample(String hash) throws SampleNotFoundException {
        //Create the URL
        String sampleId = getSampleId(hash);
        String url = apiBase + "samples/" + sampleId + "/sample";
        //Prepare the request with the API key
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + key)
                .build();
        //Return the value of the direct download link
        return downloader.get(request);
    }
}
