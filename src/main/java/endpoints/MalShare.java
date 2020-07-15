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
 * This class is used to get a sample from the MalShare database via its API.
 *
 * @author Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
 */
public class MalShare extends GenericEndpoint implements IEndpoint {

    /**
     * Creates an object to interact with the MalShare API
     *
     * @param key the MalShare API key which is required to use the API
     */
    public MalShare(String key) {
        //Sets the apiBase variable in the abstract GenericEndpoint class
        super("https://malshare.com/api.php?api_key=" + key + "&action=", "MalShare");
    }

    /**
     * This API call will return a byte array if the sample is found. If it is
     * not found, a plain text response that starts with "Sample not found by
     * hash" is given.
     *
     * @param hash the sample's hash
     * @return the URL to get the sample from
     */
    private String getDownloadUrl(String hash) {
        return apiBase + "getfile&hash=" + hash;
    }

    /**
     * Gets the sample from MalShare, if it is present. Throws an exception if
     * it is not present.
     *
     * @param hash the hash to look for
     * @return the sample as a byte array
     * @throws SampleNotFoundException if the sample cannot be found
     */
    @Override
    public byte[] getSample(String hash) throws SampleNotFoundException {
        //Gets the URL
        String url = getDownloadUrl(hash);
        //Create the request based on the URL
        Request request = new Request.Builder()
                .url(url)
                .build();
        //Get the result from the API
        byte[] result = downloader.get(request);
        //Convert the result into a new string to check what the result is
        String temp = new String(result);
        /**
         * If the sample is not present in the MalShare API, a plain text string
         * is returned. This string contains the "Sample not found by hash"
         * text. If string contains this text, the sample cannot be found.
         * Otherwise, the returned value is the raw file
         */
        if (temp.contains("Sample not found by hash")) {
            //If the sample cannot be found, an exception is thrown
            throw new SampleNotFoundException("Sample " + hash + " not found on MalShare!");
        }
        //Return the sample
        return result;
    }

}
