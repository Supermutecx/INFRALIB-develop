package ro.infrasoft.infralib.io.filestore;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.S3Object;
import com.emc.vipr.services.s3.ViPRS3Client;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import ro.infrasoft.infralib.db.datasource.BaseDataSource;
import ro.infrasoft.infralib.db.type.DbType;
import ro.infrasoft.infralib.settings.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Clasa care se ocupa cu stocarea fisierelor.
 */
@SuppressWarnings({"TryFinallyCanBeTryWithResources", "ResultOfMethodCallIgnored"})
public final class EcsS3FilestoreApi implements FileStoreApi {
    private static final String SEP = File.separator;

    private String url;
    private String accessKey;
    private String secretKey;
    private String namespace;
    private ViPRS3Client client;
    private ObjectMapper mapper;

    public EcsS3FilestoreApi() {
        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
    }

    @Override
    public boolean checkWorkDir(int unitId) {
        boolean ok = false;
        try {
            File unitFolder = new File(Settings.get("file_store", "work_dir") + SEP + "unit_" + unitId);
            ok = unitFolder.exists();
        } catch (Exception e) {
            // ignored - false
        }
        return ok;
    }

    @Override
    public void createWorkDir(int unitId) {
        if (!checkWorkDir(unitId)) {
            try {
                File workDir = new File(Settings.get("file_store", "work_dir"));
                if (workDir.exists()) {
                    File unitX = new File(Settings.get("file_store", "work_dir") + SEP + "unit_" + unitId);
                    unitX.mkdir();
                } else {
                    workDir.mkdir();
                    File unitX = new File(Settings.get("file_store", "work_dir") + SEP + "unit_" + unitId);
                    unitX.mkdir();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean checkRootDir(int unitId) {
        return true; // nu se foloseste
    }

    @Override
    public void createRootDir(int unitId) {
        // nu se foloseste
    }

    @Override
    public boolean checkCacheDir(int unitId) {
        boolean ok = false;
        try {
            File unitFolder = new File(Settings.get("file_store", "cache_dir") + SEP + "unit_" + unitId);
            ok = unitFolder.exists();
        } catch (Exception e) {
            // ignored - false
        }
        return ok;
    }

    @Override
    public void createCacheDir(int unitId) {
        if (!checkCacheDir(unitId)) {
            try {
                File workDir = new File(Settings.get("file_store", "cache_dir"));
                if (workDir.exists()) {
                    File unitX = new File(Settings.get("file_store", "cache_dir") + SEP + "unit_" + unitId);
                    unitX.mkdir();
                } else {
                    workDir.mkdir();
                    File unitX = new File(Settings.get("file_store", "cache_dir") + SEP + "unit_" + unitId);
                    unitX.mkdir();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void storeFile(File inputFile, int unitId, String fileId, int parcelNum, String version, String fileName, String password) throws Exception {
        createParcelIfNotExists(unitId, parcelNum);

        String storageFileName = getStorageFileName(fileName, fileId, version);

        boolean fileExists = fileExists(unitId, parcelNum, storageFileName);

        if (fileExists)
            throw new Exception("OUTPUT_FILE_EXISTS");

        client.putObject("unit-" + unitId + "-parcel-" + parcelNum, storageFileName, inputFile);
    }

    @Override
    public File retrieveFile(int unitId, String fileId, int parcelNum, String version, String fileName, String password) throws Exception {
        String outputName = "download_file_" + new Date().getTime() + "_" + Thread.currentThread().getId();
        File outputFile = new File(Settings.get("file_store", "work_dir") + SEP + "unit_" + unitId + SEP + outputName);
        return innerRetrieveFile(unitId, fileId, parcelNum, version, fileName, password, outputFile, true);
    }

    @Override
    public File retrieveFileWithExtension(int unitId, String fileId, int parcelNum, String version, String fileName, String password, String extension) throws Exception {
        String outputName = "download_file_" + new Date().getTime() + "_" + Thread.currentThread().getId() + "." + extension;
        File outputFile = new File(Settings.get("file_store", "work_dir") + SEP + "unit_" + unitId + SEP + outputName);
        return innerRetrieveFile(unitId, fileId, parcelNum, version, fileName, password, outputFile, true);
    }

    @Override
    public File retrieveFile(int unitId, String fileId, int parcelNum, String version, String fileName, String password, final File zipOutputFile, final File outputFile) throws Exception {
        return innerRetrieveFile(unitId, fileId, parcelNum, version, fileName, password, outputFile, true);
    }

    @Override
    public void deleteFile(int unitId, int fileId, int parcelNum, String version, String fileName) throws Exception {
        String storageFileName = getStorageFileNameTryShortcut(unitId, parcelNum, fileName, String.valueOf(fileId), version);

        client.deleteObject("unit-" + unitId + "-parcel-" + parcelNum, storageFileName);
    }

    @Override
    public void moveFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception {
        createParcelIfNotExists(unitId, parcelNum);

        String storageFileName = getStorageFileNameTryShortcut(unitId, parcelNum, fileName, String.valueOf(fileId), version);
        String storageFileNameTarget = getStorageFileName(storageFileName, String.valueOf(newFileId), version);

        boolean fileExists = fileExists(newFileId, parcelNum, storageFileNameTarget);

        if (fileExists)
            throw new Exception("OUTPUT_FILE_EXISTS");

        innerCopyFile(unitId, unitId, fileId, newFileId, parcelNum, parcelNum, version, version, fileName, newFileName);
        deleteFile(unitId, fileId, parcelNum, version, fileName);
    }

    @Override
    public void moveFileDelete(int unitId, int fileId, int parcelNum, String version, String fileName) throws Exception {
        createDeletedParcelIfNotExists(unitId);

        String storageFileName = getStorageFileNameTryShortcut(unitId, parcelNum, fileName, String.valueOf(fileId), version);

        client.copyObject("unit-" + unitId + "-parcel-" + parcelNum, storageFileName, "unit-" + unitId + "-deleted", storageFileName);
        deleteFile(unitId, fileId, parcelNum, version, fileName);
    }

    @Override
    public void copyFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception {
        innerCopyFile(unitId, unitId, fileId, newFileId, parcelNum, parcelNum, version, "1", fileName, newFileName);
    }

    @Override
    public void linkFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception {
        innerLinkFile(unitId, unitId, fileId, newFileId, parcelNum, parcelNum, version, "1", fileName, newFileName);
    }

    @Override
    public void copyFile(int unitIdSource, int unitIdTarget, int fileIdSource, int fileIdTarget, int parcelNumSource, int parcelNumTarget, String versionSource, String versionTarget, String fileNameSource, String fileNameTarget) throws Exception {
        innerCopyFile(unitIdSource, unitIdTarget, fileIdSource, fileIdTarget, parcelNumSource, parcelNumTarget, versionSource, versionTarget, fileNameSource, fileNameTarget);
    }

    @Override
    public void linkFile(int unitIdSource, int unitIdTarget, int fileIdSource, int fileIdTarget, int parcelNumSource, int parcelNumTarget, String versionSource, String versionTarget, String fileNameSource, String fileNameTarget) throws Exception {
        innerLinkFile(unitIdSource, unitIdTarget, fileIdSource, fileIdTarget, parcelNumSource, parcelNumTarget, versionSource, versionTarget, fileNameSource, fileNameTarget);
    }

    @Override
    public void clearPdfCacheFile(int unitId, int fileId, int parcelNum, String version) throws Exception {
        try {
            File inputFile = new File(Settings.get("file_store", "cache_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + fileId + "_" + version + ".pdf");
            if (inputFile.exists()) {
                inputFile.delete();
            }
        } catch (Throwable th) {
            //TODO - LOG
        }
    }

    @Override
    public void deleteWorkFile(File inputFile) throws Exception {
        if (!inputFile.exists())
            throw new Exception("FILE_NOT_EXIST");

        inputFile.delete();
    }

    private FileLink rezolveLinkIfNeeded(FileLink fileLink, String password) throws Exception {
        String fileNameOnly = getFileNameOnly(fileLink.getFileName());
        String storageFileName = getStorageFileName(fileNameOnly + ".dmslink", String.valueOf(fileLink.getFileID()), fileLink.getVersion());

        boolean fileExists = fileExists(fileLink.getUnitID(), fileLink.getParcelNum(), storageFileName);

        if (fileExists) {
            File shortcut = retrieveFileShortcut(fileLink.getUnitID(), String.valueOf(fileLink.getFileID()), fileLink.getParcelNum(), fileLink.getVersion(), storageFileName, password);
            if (shortcut != null) {
                fileLink = getSimpleFileLink(fileLink, shortcut);
                return rezolveLinkIfNeeded(fileLink, password);
            }
        }

        return fileLink;
    }

    private FileLink getSimpleFileLink(FileLink fileLink, File shortcut) throws IOException {
        if (shortcut != null && shortcut.exists()) {
            String fileJson = FileUtils.readFileToString(shortcut);
            if (fileJson != null && !fileJson.isEmpty()) {
                fileLink = mapper.readValue(fileJson, FileLink.class);
            }
        }

        return fileLink;
    }

    @Override
    public int getMinParcel(BaseDataSource db, int unitId) {
        int sizePerParcelInMb = 0;

        try {
            String strSizePerParcelInMb = Settings.get("file_store", "size_per_parcel_in_mb");
            sizePerParcelInMb = Integer.valueOf(strSizePerParcelInMb);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sizePerParcelInMb == 0) {
            sizePerParcelInMb = 5000;
        }

        int minParcel = 0;
        String strMinParcel = null;
        try {
            strMinParcel = db.sql("select get_min_parcel_size(" + sizePerParcelInMb + ") mp from dual",
                    "select $[SCHEMA_NAME].get_min_parcel_size(" + sizePerParcelInMb + ") mp from dual", DbType.SQL_SERVER,
                    "select $[SCHEMA_NAME].get_min_parcel_size(" + sizePerParcelInMb + ") mp from dual", DbType.MYSQL).get("mp");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (strMinParcel != null) {
            try {
                minParcel = Integer.valueOf(strMinParcel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (minParcel == 0) {
            minParcel = 1;
        }

        return minParcel;
    }

    @Override
    public void createParcelIfNotExists(int idUnitate, int numParcel) throws Exception {
        innerCreateParcelIfNotExist(idUnitate, "parcel-" + numParcel);
    }

    public void initConnection() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        AWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        client = new ViPRS3Client(url, creds);
        client.setNamespace(namespace);

        S3ClientOptions options = new S3ClientOptions();
        options.setPathStyleAccess(true);
        client.setS3ClientOptions(options);
    }

    private void createDeletedParcelIfNotExists(int idUnitate) throws Exception {
        innerCreateParcelIfNotExist(idUnitate, "deleted");
    }

    private boolean parcelExists(int idUnitate, int numParcel) {
        return innerParcelExists(idUnitate, "parcel-" + numParcel);
    }

    private boolean deletedParcelExists(int idUnitate) {
        return innerParcelExists(idUnitate, "deleted");
    }

    private void innerCreateParcelIfNotExist(int idUnitate, String ext) throws Exception {
        if (!innerParcelExists(idUnitate, ext)) {
            client.createBucket("unit-" + idUnitate + "-" + ext);
        }
    }

    private boolean innerParcelExists(int idUnitate, String ext) {
        return client.doesBucketExist("unit-" + idUnitate + "-" + ext);
    }

    private boolean fileExists(int idUnitate, int numParcel, String fileName) {
        boolean exists = false;

        S3Object file = null;
        try {
            try {
                file = client.getObject("unit-" + idUnitate + "-parcel-" + numParcel, fileName);
            } catch (Throwable th) {
                exists = false;
            }

            if (file != null) {
                exists = true;
            }
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    String message = e.getMessage();
                    if (message == null) {
                        if (e.getCause() != null && e.getCause().getMessage() != null) {
                            message = e.getCause().getMessage();
                        } else {
                            message = "unknown error.";
                        }
                    }
                    System.out.println(message);
                    e.printStackTrace();
                }
            }
        }

        return exists;
    }

    private File retrieveFileShortcut(int unitId, String fileId, int parcelNum, String version, String fileName, String password) throws Exception {
        String outputName = "download_file_shortcut_" + new Date().getTime() + "_" + Thread.currentThread().getId();
        File outputFile = new File(Settings.get("file_store", "work_dir") + SEP + "unit_" + unitId + SEP + outputName);
        return innerRetrieveFile(unitId, fileId, parcelNum, version, fileName, password, outputFile, false);
    }

    private File innerRetrieveFile(int unitId, String fileId, int parcelNum, String version, String fileName, String password, File outputFile, boolean followShortcut) throws Exception {
        // in caz ca e link, rezolva fisierul real
        FileLink fileLink = new FileLink(unitId, Integer.valueOf(fileId), parcelNum, version, fileName);
        if (followShortcut) {
            fileLink = rezolveLinkIfNeeded(fileLink, password);
        }

        String storageFileName = getStorageFileName(fileLink.getFileName(), String.valueOf(fileLink.getFileID()), fileLink.getVersion());

        S3Object s3Object = null;

        try {
            s3Object = client.getObject("unit-" + unitId + "-parcel-" + parcelNum, storageFileName);
            if (s3Object == null) {
                throw new Exception("S3 Object is null.");
            }

            try (InputStream is = s3Object.getObjectContent(); OutputStream os = new FileOutputStream(outputFile)) {
                final ReadableByteChannel inputChannel = Channels.newChannel(is);
                final WritableByteChannel outputChannel = Channels.newChannel(os);

                try {
                    final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
                    while (inputChannel.read(buffer) != -1) {
                        buffer.flip();
                        outputChannel.write(buffer);
                        buffer.compact();
                    }
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        outputChannel.write(buffer);
                    }
                } finally {
                    if (inputChannel != null)
                        inputChannel.close();

                    if (outputChannel != null)
                        outputChannel.close();
                }
            }
        } finally {
            if (s3Object != null) {
                try {
                    s3Object.close();
                } catch (IOException e) {
                    String message = e.getMessage();
                    if (message == null) {
                        if (e.getCause() != null && e.getCause().getMessage() != null) {
                            message = e.getCause().getMessage();
                        } else {
                            message = "unknown error.";
                        }
                    }
                    System.out.println(message);
                    e.printStackTrace();
                }
            }
        }

        return outputFile;
    }

    private void innerLinkFile(int unitIdSource, int unitIdTarget, int fileIdSource, int fileIdTarget, int parcelNumSource, int parcelNumTarget, String versionSource, String versionTarget, String fileNameSource, String fileNameTarget) throws Exception {
        FileLink fileLink = new FileLink(unitIdSource, fileIdSource, parcelNumSource, versionSource, fileNameSource);
        String fileLinkJson = mapper.writeValueAsString(fileLink);

        String shortcutFileName = getFileNameOnly(fileNameTarget) + ".dmslink";
        File workFile = new File(Settings.get("file_store", "work_dir") + SEP + "unit_" + unitIdTarget + SEP + shortcutFileName);

        try (PrintWriter printWriter = new PrintWriter(workFile)) {
            printWriter.print(fileLinkJson);
        }

        storeFile(workFile, unitIdTarget, String.valueOf(fileIdTarget), parcelNumTarget, versionTarget, shortcutFileName, Settings.get("application", "password"));
    }

    private void innerCopyFile(int unitIdSource, int unitIdTarget, int fileIdSource, int fileIdTarget, int parcelNumSource, int parcelNumTarget, String versionSource, String versionTarget, String fileNameSource, String fileNameTarget) throws Exception {
        createParcelIfNotExists(unitIdTarget, parcelNumTarget);

        String storageFileName = getStorageFileNameTryShortcut(unitIdSource, parcelNumSource, fileNameSource, String.valueOf(fileIdSource), versionSource);
        String storageFileNameTarget = getStorageFileName(storageFileName, String.valueOf(fileIdTarget), versionTarget);

        boolean fileExists = fileExists(fileIdTarget, parcelNumTarget, storageFileNameTarget);

        if (fileExists)
            throw new Exception("OUTPUT_FILE_EXISTS");

        client.copyObject("unit-" + unitIdSource + "-parcel-" + parcelNumSource, storageFileName, "unit-" + unitIdTarget + "-parcel-" + parcelNumTarget, storageFileNameTarget);
    }

    private String getFileExtension(String fileName) {
        String ext = "";
        if (fileName != null && fileName.contains(".")) {
            int index = fileName.lastIndexOf(".");
            if (index != -1 && index < fileName.length()) {
                ext = fileName.substring(index + 1);
            }
        }

        if (ext == null || ext.trim().isEmpty()) {
            ext = "bin";
        }
        return ext;
    }

    private String getFileNameOnly(String fileName) {
        String nameOnly = fileName;
        if (fileName != null && fileName.contains(".")) {
            int index = fileName.lastIndexOf(".");
            if (index != -1) {
                nameOnly = fileName.substring(0, index);
            }
        }

        return nameOnly;
    }

    private String getStorageFileName(String fileName, String fileId, String version) {
        String extension = getFileExtension(fileName);
        return "file_" + fileId + "_" + version + "." + extension;
    }

    private String getShortcutFileName(String fileName, String fileId, String version) {
        return "file_" + fileId + "_" + version + ".dmslink";
    }

    private String getStorageFileNameTryShortcut(int idUnitate, int numParcel, String fileName, String fileId, String version) {
        String storageFileName = getStorageFileName(fileName, fileId, version);
        String shortcutFileName = getShortcutFileName(fileName, fileId, version);
        if (fileExists(idUnitate, numParcel, storageFileName)) {
            return storageFileName;
        } else if (fileExists(idUnitate, numParcel, shortcutFileName)) {
            return shortcutFileName;
        }

        return storageFileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
