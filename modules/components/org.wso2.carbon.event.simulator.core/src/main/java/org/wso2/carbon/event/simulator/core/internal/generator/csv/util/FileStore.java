/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.simulator.core.internal.generator.csv.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;
import org.wso2.msf4j.formparam.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * FileStore keeps a record of all CSV files that have been uploaded to the system
 *
 * @see FileUploader
 */
public class FileStore {

    private static final Logger log = LoggerFactory.getLogger(FileStore.class);

    private static final FileStore fileStore = new FileStore();

    /**
     * Concurrent HashMap to hold the details of uploaded CSV files
     * It holds the data as key value pair
     * key: fileName
     * value: FileInfo which holds file information
     *
     * @see FileInfo
     */
    private ConcurrentHashMap<String, FileInfo> fileInfoMap = new ConcurrentHashMap<>();

    /**
     * FileStore() loads the name of csv files in '/tmp/eventSimulator' directory into file info map
     */
    private FileStore() {

        try {
            /*
            * create a directory called 'eventSimulator' inside tmp directory
            * if the directory already exists, load the details of all the files into the fileStore.
            * */
//          todo  use url dont upload
            boolean dirCreated = new File(Paths.get(System.getProperty("java.io.tmpdir"), FileUploader.DIRECTORY_NAME)
                    .toString()).mkdirs();

            if (dirCreated && log.isDebugEnabled()) {
                log.debug("Successfully created directory 'tmp/eventSimulator' ");
            }

            List<File> filesInFolder = Files.walk(Paths.get(System.getProperty("java.io.tmpdir"),
                    FileUploader.DIRECTORY_NAME)).filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".csv"))
                    .map(Path::toFile).collect(Collectors.toList());

            if (log.isDebugEnabled()) {
                log.debug("Retrieved files in temp directory " + Paths.get(System.getProperty("java.io.tmpdir"),
                        FileUploader.DIRECTORY_NAME).toString());
            }

            for (File file : filesInFolder) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setContentType("text/csv");
                fileInfo.setFileName(file.getName());
                fileInfoMap.put(file.getName(), fileInfo);
            }

            if (log.isDebugEnabled()) {
                log.debug("Initiated FileStore");
            }
        } catch (IOException e) {
            throw new SimulatorInitializationException("Error occurred when loading file names to filestore : ", e);
        }
    }

    /**
     * Method to return Singleton Object of FileStore
     *
     * @return fileStore
     */
    public static FileStore getFileStore() {
        return fileStore;
    }

    /**
     * Get the fileInfoMap which holds the details of uploaded CSV Files
     *
     * @return fileInfoMap
     */
    public ConcurrentHashMap<String, FileInfo> getFileInfoMap() {
        return fileInfoMap;
    }


    /**
     * Method to add file data into in memory
     *
     * @param fileInfo FileInfo Object which holds the details of file
     */
    public void addFile(FileInfo fileInfo) {
        fileInfoMap.put(fileInfo.getFileName(), fileInfo);
    }

    /**
     * Method to remove the file from in memory
     *
     * @param fileName File Name of uploaded CSV file
     * @throws IOException it throws IOException if anything occurred while
     *                     delete the file from temp directory and in memory
     */
    public void removeFile(String fileName) throws IOException {
        // delete the file from directory
        Files.deleteIfExists(Paths.get(System.getProperty("java.io.tmpdir"), FileUploader.DIRECTORY_NAME, fileName));
        //delete the file from in memory
        fileInfoMap.remove(fileName);
    }

    /**
     * Method to check whether the File Name  already exists in directory
     *
     * @param fileName File name of the file
     * @return true if exist false if not exist
     */
    public Boolean checkExists(String fileName) {
        return fileInfoMap.containsKey(fileName);
    }
}