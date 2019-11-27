package ro.infrasoft.infralib.io.filestore;

import ro.infrasoft.infralib.db.datasource.BaseDataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface FileStoreApi {
    /**
     * Verifica sa existe work dir pt unitatea data.
     *
     * @param unitId unit
     * @return true/false
     */
    public boolean checkWorkDir(int unitId);


    public void createWorkDir(int unitId);


    /**
     * Verifica sa existe root dir pt unitatea data.
     *
     * @param unitId unit
     * @return true/false
     */
    public boolean checkRootDir(int unitId);

    public void createRootDir(int unitId);

    /**
     * Verifica sa existe cache dir pt unitatea data.
     *
     * @param unitId unit
     * @return true/false
     */
    public boolean checkCacheDir(int unitId);

    public void createCacheDir(int unitId);

    /**
     * Stocheaza un fisier.
     */
    public void storeFile(File inputFile, int unitId, String fileId, int parcelNum, String version, String fileName, String password) throws Exception;

    /**
     * Intoarce un fisier din store.
     */
    public File retrieveFile(int unitId, String fileId, int parcelNum, String version, String fileName, String password) throws Exception;

    /**
     * Intoarce un fisier din store.
     */
    public File retrieveFileWithExtension(int unitId, String fileId, int parcelNum, String version, String fileName, String password, String extension) throws Exception;

    /**
     * Intoarce un fisier din store.
     */
    public File retrieveFile(int unitId, String fileId, int parcelNum, String version, String fileName, String password, final File zipOutputFile, final File outputFile) throws Exception;

    /**
     * Sterge fisierul dat.
     *
     * @param fileId    id fisier
     * @param parcelNum numar parcela
     * @throws Exception poate aruna exceptii
     */
    public void deleteFile(int unitId, int fileId, int parcelNum, String version, String fileName) throws Exception;


    public void moveFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception;

    public void moveFileDelete(int unitId, int fileId, int parcelNum, String version, String fileName) throws Exception;

    public void copyFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception;

    /**
     * Se face link la fisier.
     */
    public void linkFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception;

    /**
     * Se copiaza fisierul.
     * <p/>
     * <b>Nota, la copy, nu vrem sa rezolvam link, copiezi un link se copiaza link-ul nu fisierul.</b>
     */
    public void copyFile(int unitIdSource, int unitIdTarget, int fileIdSource, int fileIdTarget, int parcelNumSource, int parcelNumTarget, String versionSource, String versionTarget, String fileNameSource, String fileNameTarget) throws Exception;

    /**
     * Se face link la fisier.
     */
    public void linkFile(int unitIdSource, int unitIdTarget, int fileIdSource, int fileIdTarget, int parcelNumSource, int parcelNumTarget, String versionSource, String versionTarget, String fileNameSource, String fileNameTarget) throws Exception;

    /**
     * Se sterge cache pdf.
     */
    public void clearPdfCacheFile(int unitId, int fileId, int parcelNum, String version) throws Exception;


    /**
     * Sterge fisierul de work.
     *
     * @param inputFile fisierul
     * @throws Exception poate arunca exceptii
     */
    public void deleteWorkFile(File inputFile) throws Exception;

    /**
     * Decide min parcel.
     */
    public int getMinParcel(BaseDataSource db, int unitId);

    public void createParcelIfNotExists(int idUnitate, int numParcel) throws Exception;
}
