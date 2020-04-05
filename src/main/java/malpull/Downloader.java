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

import exceptions.Error404NotFoundException;
import exceptions.Error429TooManyRequestsException;
import exceptions.HttpConnectionFailed;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This class serves as a wrapper around the OkHttp3 client and exposes two
 * functions: <code>get(Request request)</code> and
 * <code>post(String url, RequestBody requestBody)</code>. This way, the classes
 * that contact the API endpoints can contain the logic for the API, and this
 * code can be reused within all services.
 *
 * @author Max 'Libra' Kersten [@LibraAnalysis, https://maxkersten.nl]
 */
public class Downloader {

    /**
     * The HTTP client that sends out the request
     */
    protected final OkHttpClient httpClient;

    /**
     * Creates the Downloader object, via which GET and POST requests can be
     * sent to a given address
     */
    public Downloader() {
        httpClient = new OkHttpClient();
    }

    /**
     * Sends a POST request to the given URL. The Request body should contain
     * the form data, if any is required.
     *
     * @param url the URL to contact
     * @param requestBody the request body with the form data
     * @return the data that the API returned in the form of a byte array
     * @throws HttpConnectionFailed if no HTTP connection can be established
     * (either because the machine that executes the JAR has no internet, or
     * because the host is unreachable)
     */
    public byte[] post(String url, RequestBody requestBody) throws HttpConnectionFailed {
        //Create the request object based on the given URL, where the given request body is also used
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        //Make the call
        try (Response response = httpClient.newCall(request).execute()) {
            //Check the return value
            if (!response.isSuccessful()) {
                throw new HttpConnectionFailed();
            }

            //Return response body
            return response.body().bytes();
        } catch (IOException e) {
            throw new HttpConnectionFailed();
        }
    }

    /**
     * Sends a GET request to the given URL
     *
     * @param request the request to send, which is already addressed to a
     * specific URL
     * @return the data that the API returned in the form of a byte array
     * @throws HttpConnectionFailed if no HTTP connection can be established
     * (either because the machine that executes the JAR has no internet, or
     * because the host is unreachable)
     * @throws Error404NotFoundException if the returned status code is 404 (Not
     * Found)
     * @throws Error429TooManyRequestsException if the returned status code is
     * 429 (Too Many Requests)
     */
    public byte[] get(Request request) throws HttpConnectionFailed, Error404NotFoundException, Error429TooManyRequestsException {
        //Make the call
        try (Response response = httpClient.newCall(request).execute()) {
            //Check the response code
            if (!response.isSuccessful()) {
                switch (response.code()) {
                    case 404:
                        throw new Error404NotFoundException();
                    case 429:
                        throw new Error429TooManyRequestsException();
                    default:
                        throw new HttpConnectionFailed();
                }
            }

            //Return response body
            return response.body().bytes();
        } catch (IOException e) {
            throw new HttpConnectionFailed();
        }
    }
}
