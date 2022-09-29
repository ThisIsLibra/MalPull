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
package malpull.endpoints;

import java.io.IOException;
import malpull.exceptions.SampleNotFoundException;
import malshareapi.MalShareApi;

/**
 * This class is used to get a sample from the MalShare database via its API.
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class MalShare implements IEndpoint {

    /**
     * An instance of the MalShare API library
     */
    private MalShareApi api;

    /**
     * Creates an object to interact with the MalShare API
     *
     * @param key the MalShare API key which is required to use the API
     */
    public MalShare(String key) {
        api = new MalShareApi(key);
    }

    /**
     * Gets the endpoint name
     *
     * @return the name of the endpoint
     */
    @Override
    public String getName() {
        return "MalShare";
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
        try {
            return api.getFile(hash);
        } catch (IOException ex) {
            throw new SampleNotFoundException("Sample " + hash + " not found on MalShare!");
        }
    }
}
