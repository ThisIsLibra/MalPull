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

/**
 * The interface to use for all endpoints that are used
 *
 * @author Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
 */
public interface IEndpoint {

    /**
     * Gets the sample from the endpoint, based on the given hash
     *
     * @param hash the hash of the file to download
     * @return a byte[] that contains the file's data
     * @throws Exception in case any error occurs
     */
    public byte[] getSample(String hash) throws Exception;

    /**
     * Gets the name of the endpoint
     *
     * @return
     */
    public String getName();
}
