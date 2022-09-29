/*
 * Copyright (C) 2022 Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import malpull.exceptions.SampleNotFoundException;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import okhttp3.Request;

/**
 * The class to interact with the VirusShare API
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class VirusShare extends GenericEndpoint implements IEndpoint {

    /**
     * Creates an instance of this object to interact with VirusShare
     *
     * @param apiKey the required API key to download files from VirusShare
     */
    public VirusShare(String apiKey) {
        //Sets the apiBase variable in the abstract GenericEndpoint class
        super("https://virusshare.com/apiv2/download?apikey=" + apiKey + "&hash=", "VirusShare");
    }

    /**
     * Gets a sample, as a byte array, from VirusShare
     *
     * @param hash the hash to look for
     * @return the sample as a byte array
     * @throws SampleNotFoundException if the hash is invalid or cannot be found
     * on VirusShare
     */
    @Override
    public byte[] getSample(String hash) throws SampleNotFoundException {
        //Checks if the given hash is valid
        if (hash == null || hash.isEmpty() || hash.isBlank()) {
            throw new SampleNotFoundException("The given hash is null, empty, or consists of only white space characters!");
        }

        try {
            String url = apiBase + hash;

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            byte[] zip = downloader.get(request);
            return unzipSample(zip);
        } catch (Exception e) {
            throw new SampleNotFoundException(e.getMessage());
        }
    }

    /**
     * Unzips a given zip archive
     *
     * @param rawZip the zip archive to unzip
     * @return the first file from the archive (which should be the only file in
     * the archive)
     * @throws IOException if the archive extraction fails
     */
    private byte[] unzipSample(byte[] rawZip) throws IOException {
        //Unzip file, password is "infected"
        String tempPath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "all_yara_rules.zip";
        List<byte[]> files = readZipArchive(rawZip, tempPath, "infected");
        if (files.isEmpty() == false) {
            return files.get(0);
        } else {
            throw new IOException("No files found within the zip archive!");
        }
    }

    /**
     * Reads a file based on the given header from the given ZIP file
     *
     * @param zipFile the ZIP file to read the file from
     * @param header the specific file to read from the ZIP archive
     * @return the raw bytes of the file that was read, or null if an error
     * occurred
     */
    private byte[] readFileFromZipArchive(ZipFile zipFile, FileHeader header) {
        try {
            ZipInputStream inputStream = zipFile.getInputStream(header);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int offset = -1;
            byte[] buff = new byte[1024];
            while ((offset = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, offset);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Reads the contents of the given ZIP archive (passed as a byte array). It
     * is temporarily written to the disk at the given path. The given password
     * is only used if the ZIP is encrypted, if it is not null nor empty. The
     * ZIP archive is removed from the disk prior to returning from this
     * function, regardless if an error occurs.
     *
     * @param zip the ZIP archive as a byte array
     * @param tempPath the temporary path to save the ZIP archive
     * @param password the password with which the ZIP archive is protected
     * @return a list of byte arrays, one for each of the ZIP files in the
     * archive
     * @throws IOException if the temporary path is not writeable, or if
     * something goes wrong with the ZIP archive extraction
     */
    private List<byte[]> readZipArchive(byte[] zip, String tempPath, String password) throws IOException {
        List<byte[]> files = new ArrayList<>();
        File localFile = new File(tempPath);
        localFile.getParentFile().mkdirs();
        Files.write(localFile.toPath(), zip); //overwrites if it exists, not thread safe when downloading the same data, unless a unique path is given

        try {
            ZipFile zipFile = new ZipFile(localFile);
            if (zipFile.isEncrypted() && password != null && password.isEmpty() == false) {
                zipFile.setPassword(password.toCharArray());
            }

            List<FileHeader> headers = zipFile.getFileHeaders();

            for (FileHeader header : headers) {
                byte[] bytes = readFileFromZipArchive(zipFile, header);

                //Ignore errors
                if (bytes == null) {
                    continue;
                }
                files.add(bytes);
            }

            //If all went well, delete the file
            localFile.delete();

            return files;
        } catch (ZipException e) {
            //Also delete the file if there is an error to avoid automated systems filling up over time
            localFile.delete();
            throw new IOException("Error whilst handling the (now deleted) ZIP archive:\n" + e.getMessage());
        }
    }
}
