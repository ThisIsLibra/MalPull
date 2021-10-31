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

import malpull.network.MalPullConnector;

/**
 * This abstract class contains the shared code base for all endpoints, which is
 * done to re-use code.
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public abstract class GenericEndpoint {

    /**
     * The downloader wrapper class that is used to make HTTP requests
     */
    protected MalPullConnector downloader;

    /**
     * The name of the endpoint
     */
    public String name;

    /**
     * The base URL of the API, to which specific API actions can be appended
     */
    protected String apiBase;

    public GenericEndpoint(String apiBase, String name) {
        //Sets the apiBase variable
        this.apiBase = apiBase;
        //Initialises the downloader class
        downloader = new MalPullConnector();
        //Sets the name variable
        this.name = name;
    }

    /**
     * Gets the endpoint name
     *
     * @return the name of the endpoint
     */
    public String getName() {
        return name;
    }
}
