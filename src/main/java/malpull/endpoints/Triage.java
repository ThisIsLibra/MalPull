/*
 * Copyright (C) 2021 Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
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
package malpull.endpoints;

import java.io.IOException;
import malpull.exceptions.SampleNotFoundException;
import triageapi.TriageApi;
import triageapi.model.SearchResult;

/**
 * The class that is used to get a sample from Triage
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class Triage implements IEndpoint {

    /**
     * An instance of the Triage API client
     */
    private TriageApi api;

    /**
     * Creates an object to interact with the Triage endpoint
     *
     * @param key the API key for Triage
     */
    public Triage(String key) {
        api = new TriageApi(key, false);
    }

    /**
     * Gets the endpoint name
     *
     * @return the name of the endpoint
     */
    @Override
    public String getName() {
        return "Triage";
    }

    /**
     * Download the sample from the API
     *
     * @param hash SHA-256 hash of the sample to download
     * @return the API's response, which is the raw file
     * @throws SampleNotFoundException if the sample cannot be found
     */
    @Override
    public byte[] getSample(String hash) throws SampleNotFoundException {
        String id = null;
        try {
            SearchResult result = api.search(hash, 1);
            if (result.isEmpty() == false
                    && result.getSearchResults().isEmpty() == false) {
                id = result.getSearchResults().get(0).getId();
                byte[] downloadSample = api.downloadSample(id);
                if (downloadSample.length > 0) {
                    return downloadSample;
                } else {
                    throw new SampleNotFoundException("The sample download for \"" + hash + "\" failed on Triage!");
                }
            }
            throw new SampleNotFoundException("The search for \"" + hash + "\" failed on Triage!");
        } catch (IOException e) {
            throw new SampleNotFoundException("The search or download for \"" + hash + "\" failed on Triage!");
        }
    }
}
