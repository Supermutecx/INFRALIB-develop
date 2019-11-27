package ro.infrasoft.infralib.io.filestore;

import ro.infrasoft.infralib.db.datasource.BaseDataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Clasa care se ocupa cu stocarea fisierelor.
 */
public final class FileStore {
    private static FileStoreApi fileStoreApi;

    private FileStore() {

    }

    public static boolean checkWorkDir(int unitId) {
        return fileStoreApi.checkWorkDir(unitId);
    }

    public static void createWorkDir(int unitId) {
        fileStoreApi.createWorkDir(unitId);
    }

    public static boolean checkRootDir(int unitId) {
        return fileStoreApi.checkRootDir(unitId);
    }

    public static void createRootDir(int unitId) {
        fileStoreApi.createRootDir(unitId);
    }

    public static boolean checkCacheDir(int unitId) {
        return fileStoreApi.checkCacheDir(unitId);
    }

    public static void createCacheDir(int unitId) {
        fileStoreApi.createCacheDir(unitId);
    }

    public static void storeFile(File inputFile, int unitId, String fileId, int parcelNum, String version, String fileName, String password) throws Exception {
        fileStoreApi.storeFile(inputFile, unitId, fileId, parcelNum, version, fileName, password);
    }

    public static File retrieveFile(int unitId, String fileId, int parcelNum, String version, String fileName, String password) throws Exception {
        return fileStoreApi.retrieveFile(unitId, fileId, parcelNum, version, fileName, password);
    }

    public static File retrieveFileWithExtension(int unitId, String fileId, int parcelNum, String version, String fileName, String password, String extension) throws Exception {
        return fileStoreApi.retrieveFileWithExtension(unitId, fileId, parcelNum, version, fileName, password, extension);
    }

    public static File retrieveFile(int unitId, String fileId, int parcelNum, String version, String fileName, String password, final File zipOutputFile, final File outputFile) throws Exception {
        return fileStoreApi.retrieveFile(unitId, fileId, parcelNum, version, fileName, password, zipOutputFile, outputFile);
    }

    public static void deleteFile(int unitId, int fileId, int parcelNum, String version, String fileName) throws Exception {
        fileStoreApi.deleteFile(unitId, fileId, parcelNum, version, fileName);
    }

    public static void moveFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception {
        fileStoreApi.moveFile(unitId, fileId, newFileId, parcelNum, version, fileName, newFileName);
    }

    public static void moveFileDelete(int unitId, int fileId, int parcelNum, String version, String fileName) throws Exception {
        fileStoreApi.moveFileDelete(unitId, fileId, parcelNum, version, fileName);
    }

    public static void copyFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception {
        fileStoreApi.copyFile(unitId, fileId, newFileId, parcelNum, version, fileName, newFileName);
    }

    public static void linkFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception {
        fileStoreApi.linkFile(unitId, fileId, newFileId, parcelNum, version, fileName, newFileName);
    }

    public static void copyFile(int unitIdSource, int unitIdTarget, int fileIdSource, int fileIdTarget, int parcelNumSource, int parcelNumTarget, String versionSource, String versionTarget, String fileNameSource, String fileNameTarget) throws Exception {
        fileStoreApi.copyFile(unitIdSource, unitIdTarget, fileIdSource, fileIdTarget, parcelNumSource, parcelNumTarget, versionSource, versionTarget, fileNameSource, fileNameTarget);
    }

    public static void linkFile(int unitIdSource, int unitIdTarget, int fileIdSource, int fileIdTarget, int parcelNumSource, int parcelNumTarget, String versionSource, String versionTarget, String fileNameSource, String fileNameTarget) throws Exception {
        fileStoreApi.linkFile(unitIdSource, unitIdTarget, fileIdSource, fileIdTarget, parcelNumSource, parcelNumTarget, versionSource, versionTarget, fileNameSource, fileNameTarget);
    }

    public static void clearPdfCacheFile(int unitId, int fileId, int parcelNum, String version) throws Exception {
        fileStoreApi.clearPdfCacheFile(unitId, fileId, parcelNum, version);
    }

    public static void deleteWorkFile(File inputFile) throws Exception {
        fileStoreApi.deleteWorkFile(inputFile);
    }

    public static FileStoreApi getFileStoreApi() {
        return fileStoreApi;
    }

    public static void setFileStoreApi(FileStoreApi fileStoreApi) {
        FileStore.fileStoreApi = fileStoreApi;
    }

    public static int getMinParcel(BaseDataSource db, int unitId){
        return fileStoreApi.getMinParcel(db, unitId);
    }
}
