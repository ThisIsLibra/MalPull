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
package malpull.model;

import java.util.List;
import java.util.Map;

/**
 * A class to contain all download related results, which is returned once all
 * downloads have been completed
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class MalPullResult {

    private Map<String, String> downloadedSamples;
    private List<String> missingHashes;
    private String time;

    public MalPullResult(Map<String, String> downloadedSamples, List<String> missingHashes, String time) {
        this.downloadedSamples = downloadedSamples;
        this.missingHashes = missingHashes;
        this.time = time;
    }

    public Map<String, String> getDownloadedSamples() {
        return downloadedSamples;
    }

    public List<String> getMissingHashes() {
        return missingHashes;
    }

    public String getTime() {
        return time;
    }
}
