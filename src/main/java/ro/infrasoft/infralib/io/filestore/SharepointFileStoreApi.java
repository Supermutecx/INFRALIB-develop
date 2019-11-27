package ro.infrasoft.infralib.io.filestore;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.apache.commons.io.FileUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import ro.infrasoft.infralib.db.datasource.BaseDataSource;
import ro.infrasoft.infralib.db.type.DbType;
import ro.infrasoft.infralib.settings.Settings;
import ro.infrasoft.infralib.ssl.TrustAllStrategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Clasa care se ocupa cu stocarea fisierelor.
 */
@SuppressWarnings({"TryFinallyCanBeTryWithResources", "ResultOfMethodCallIgnored"})
public final class SharepointFileStoreApi implements FileStoreApi {
    private static final String SEP = File.separator;

    private String url;
    private String username;
    private String password;
    private CloseableHttpClient client;
    private String formDigest;
    private DateTime formDigestExpireDateTime;
    private SimpleDateFormat digestDateFormat;
    private int formDigestTimeoutSeconds;
    private ObjectMapper mapper;

    public SharepointFileStoreApi() {
        // format data primit e similar cu 08 Feb 2018 16:00:26 -0000
        digestDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
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
        String formDigest = getFormDigest();
        createParcelIfNotExists(unitId, parcelNum);

        String storageFileName = getStorageFileName(fileName, fileId, version);

        boolean fileExists = fileExists(unitId, parcelNum, storageFileName);

        if (fileExists)
            throw new Exception("OUTPUT_FILE_EXISTS");

        CloseableHttpResponse response = null;
        try {
            HttpPost request = new HttpPost(buildUrl("_api/web/GetFolderByServerRelativeUrl('/unit_" + unitId + "_parcel_" + parcelNum + "')/Files/Add(url='" + encodeUrlParam(storageFileName) + "',overwrite=true)"));
            request.setHeader("Accept", "application/json;odata=verbose");
            request.setHeader("X-RequestDigest", formDigest);
            request.setEntity(new FileEntity(inputFile));

            // executam si citim raspunsul
            response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();

            // citim rezultatul
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder resultBuilder = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                resultBuilder.append(line);
            }
            String result = resultBuilder.toString();

            if (responseCode != 200 && responseCode != 201) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Cod raspuns ").append(responseCode).append(" la storeFile. ").append(result);
                throw new Exception(errorMessage.toString());
            }
        } finally {
            if (response != null)
                EntityUtils.consume(response.getEntity());

            if (response != null) {
                try {
                    response.close();
                } catch (Exception ignore) {
                }
            }
        }
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
        String formDigest = getFormDigest();

        String storageFileName = getStorageFileNameTryShortcut(unitId, parcelNum, fileName, String.valueOf(fileId), version);
        CloseableHttpResponse response = null;

        try {
            HttpPost request = new HttpPost(buildUrl("_api/web/GetFileByServerRelativeUrl('/unit_" + unitId + "_parcel_" + parcelNum + "/" + encodeUrlParam(storageFileName) + "')"));
            request.setHeader("X-RequestDigest", formDigest);
            request.setHeader("X-HTTP-Method", "DELETE");
            request.setHeader("IF-MATCH", "*");

            // executam si citim raspunsul
            response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();

            // citim rezultatul
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder resultBuilder = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                resultBuilder.append(line);
            }
            String result = resultBuilder.toString();

            if (responseCode != 200) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Cod raspuns ").append(responseCode).append(" la deleteFile. ").append(result);
                throw new Exception(errorMessage.toString());
            }
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (Exception ignore) {
                }

                if (response != null) {
                    try {
                        response.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        }
    }

    @Override
    public void moveFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception {
        String formDigest = getFormDigest();
        createParcelIfNotExists(unitId, parcelNum);

        String storageFileName = getStorageFileNameTryShortcut(unitId, parcelNum, fileName, String.valueOf(fileId), version);
        String storageFileNameTarget = getStorageFileName(storageFileName, String.valueOf(newFileId), version);

        boolean fileExists = fileExists(newFileId, parcelNum, storageFileNameTarget);

        if (fileExists)
            throw new Exception("OUTPUT_FILE_EXISTS");

        CloseableHttpResponse response = null;
        try {
            HttpPost request = new HttpPost(buildUrl("_api/web/GetFileByServerRelativeUrl('/unit_" + unitId + "_parcel_" + parcelNum + "/" + encodeUrlParam(storageFileName) + "')/moveto(newUrl='/unit_" + unitId + "_parcel_" + parcelNum + "/" + encodeUrlParam(storageFileNameTarget) + "',flags=1)"));
            request.setHeader("Accept", "application/json;odata=verbose");
            request.setHeader("X-RequestDigest", formDigest);

            // executam si citim raspunsul
            response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();

            // citim rezultatul
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder resultBuilder = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                resultBuilder.append(line);
            }
            String result = resultBuilder.toString();

            if (responseCode != 200 && responseCode != 201) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Cod raspuns ").append(responseCode).append(" la moveFile. ").append(result);
                throw new Exception(errorMessage.toString());
            }
        } finally {
            if (response != null)
                EntityUtils.consume(response.getEntity());

            if (response != null) {
                try {
                    response.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    @Override
    public void moveFileDelete(int unitId, int fileId, int parcelNum, String version, String fileName) throws Exception {
        String formDigest = getFormDigest();
        createDeletedParcelIfNotExists(unitId);

        String storageFileName = getStorageFileNameTryShortcut(unitId, parcelNum, fileName, String.valueOf(fileId), version);

        CloseableHttpResponse response = null;
        try {
            HttpPost request = new HttpPost(buildUrl("_api/web/GetFileByServerRelativeUrl('/unit_" + unitId + "_parcel_" + parcelNum + "/" + encodeUrlParam(storageFileName) + "')/moveto(newUrl='/unit_" + unitId + "_deleted" + "/" + encodeUrlParam(storageFileName) + "',flags=1)"));
            request.setHeader("Accept", "application/json;odata=verbose");
            request.setHeader("X-RequestDigest", formDigest);

            // executam si citim raspunsul
            response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();

            // citim rezultatul
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder resultBuilder = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                resultBuilder.append(line);
            }
            String result = resultBuilder.toString();

            if (responseCode != 200 && responseCode != 201) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Cod raspuns ").append(responseCode).append(" la moveFileDelete. ").append(result);
                throw new Exception(errorMessage.toString());
            }
        } finally {
            if (response != null)
                EntityUtils.consume(response.getEntity());

            if (response != null) {
                try {
                    response.close();
                } catch (Exception ignore) {
                }
            }
        }
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
        innerCreateParcelIfNotExist(idUnitate, "parcel_" + numParcel);
    }

    public void initConnection() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(new TrustAllStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), new NoopHostnameVerifier());

        RequestConfig localConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM))
                .build();

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new NTCredentials(username+":"+password));

        client = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultRequestConfig(localConfig)
                .build();
    }

    private void createDeletedParcelIfNotExists(int idUnitate) throws Exception {
        innerCreateParcelIfNotExist(idUnitate, "deleted");
    }

    private boolean parcelExists(int idUnitate, int numParcel) {
        return innerParcelExists(idUnitate, "parcel_" + numParcel);
    }

    private boolean deletedParcelExists(int idUnitate) {
        return innerParcelExists(idUnitate, "deleted");
    }

    private void innerCreateParcelIfNotExist(int idUnitate, String ext) throws Exception {
        if (!innerParcelExists(idUnitate, ext)) {
            String formDigest = getFormDigest();

            CloseableHttpResponse response = null;
            try {
                HttpPost request = new HttpPost(buildUrl("_api/web/lists"));
                request.setHeader("Accept", "application/json;odata=verbose");
                request.setHeader("Content-Type", "application/json;odata=verbose");
                request.setHeader("Content-Type", "application/json;odata=verbose");
                request.setHeader("X-RequestDigest", formDigest);

                request.setEntity(new StringEntity("" +
                        "{ '__metadata': { 'type': 'SP.List' }, " +
                        "'AllowContentTypes': true, " +
                        "'BaseTemplate': 101, " +
                        "'ContentTypesEnabled': true, " +
                        "'Description': 'Documenta parcel document library for unit " + idUnitate + " and with suffix " + ext + "', " +
                        "'Title': 'unit_" + idUnitate + "_" + ext + "' }"));

                // executam si citim raspunsul
                response = client.execute(request);
                int responseCode = response.getStatusLine().getStatusCode();

                // citim rezultatul
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder resultBuilder = new StringBuilder();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    resultBuilder.append(line);
                }
                String result = resultBuilder.toString();

                if (responseCode != 200 && responseCode != 201) {
                    StringBuilder errorMessage = new StringBuilder();
                    errorMessage.append("Cod raspuns ").append(responseCode).append(" la createParcelIfNotExists. ").append(result);
                    throw new Exception(errorMessage.toString());
                }
            } finally {
                if (response != null)
                    EntityUtils.consume(response.getEntity());

                if (response != null) {
                    try {
                        response.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        }
    }

    private boolean innerParcelExists(int idUnitate, String ext) {
        boolean exists = false;

        CloseableHttpResponse response = null;
        try {
            try {
                HttpGet request = new HttpGet(buildUrl("_api/web/GetFolderByServerRelativeUrl('/unit_" + idUnitate + "_" + ext + "')"));

                // add header
                request.setHeader("Accept", "application/json;odata=verbose");

                // executam si citim raspunsul
                response = client.execute(request);
                int responseCode = response.getStatusLine().getStatusCode();

                // citim rezultatul
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder resultBuilder = new StringBuilder();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    resultBuilder.append(line);
                }
                String result = resultBuilder.toString();

                if (responseCode != 200) {
                    StringBuilder errorMessage = new StringBuilder();
                    errorMessage.append("Cod raspuns ").append(responseCode).append(" la parcelExist. ").append(result);
                    throw new Exception(errorMessage.toString());
                }

                exists = true;
            } catch (Exception e) {
                exists = false;
            }
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (Exception ignore) {
                }

                if (response != null) {
                    try {
                        response.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        }

        return exists;
    }

    private boolean fileExists(int idUnitate, int numParcel, String fileName) {
        boolean exists = false;

        CloseableHttpResponse response = null;
        try {
            try {
                HttpGet request = new HttpGet(buildUrl("_api/lists/getbytitle('unit_" + idUnitate + "_parcel_" + numParcel + "')/Files('" + encodeUrlParam(fileName) + "')"));

                // add header
                request.setHeader("Accept", "application/json;odata=verbose");

                // executam si citim raspunsul
                response = client.execute(request);
                int responseCode = response.getStatusLine().getStatusCode();

                // citim rezultatul
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder resultBuilder = new StringBuilder();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    resultBuilder.append(line);
                }
                String result = resultBuilder.toString();

                if (responseCode != 200) {
                    StringBuilder errorMessage = new StringBuilder();
                    errorMessage.append("Cod raspuns ").append(responseCode).append(" la fileExists. ").append(result);
                    throw new Exception(errorMessage.toString());
                }

                exists = true;
            } catch (Exception e) {
                exists = false;
            }
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (Exception ignore) {
                }

                if (response != null) {
                    try {
                        response.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        }

        return exists;
    }

    private String getFormDigest() throws Exception {
        DateTime now = new DateTime();
        if (formDigest != null && !formDigest.trim().isEmpty() && formDigestExpireDateTime != null &&
                now.plusMinutes(5).isBefore(formDigestExpireDateTime)) {
            return formDigest;
        }

        CloseableHttpResponse response = null;
        try {
            HttpPost request = new HttpPost(buildUrl("_api/contextinfo"));

            // add header
            request.setHeader("Accept", "application/json;odata=verbose");

            // executam si citim raspunsul
            response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();

            // citim rezultatul
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder resultBuilder = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                resultBuilder.append(line);
            }
            String result = resultBuilder.toString();

            if (responseCode != 200) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Cod raspuns ").append(responseCode).append(" la getFormDigest. ").append(result);
                throw new Exception(errorMessage.toString());
            }

            if (result == null || result.trim().isEmpty()) {
                throw new Exception("Rezultat gol la getFormDigest.");
            }

            ReadContext ctx = JsonPath.parse(result);
            String digestAndDate = ctx.read("$.d.GetContextWebInformation.FormDigestValue");
            if (digestAndDate == null || digestAndDate.trim().isEmpty()) {
                throw new Exception("Nu pot citi digest din json la getFormDigest.");
            }
            formDigest = digestAndDate;

            formDigestTimeoutSeconds = ctx.read("$.d.GetContextWebInformation.FormDigestTimeoutSeconds");

            // ca sa initializeze data de expirare a digestului cu data lui sharepoint
            /*
            if (!digestAndDate.contains(",")){
                throw new Exception("Format digest invalid la getFormDigest.");
            }

            String[] digestAndDateArr = digestAndDate.split(",");
            if (digestAndDateArr.length < 2){
                throw new Exception("Format digest invalid la getFormDigest.");
            }

            String digest = digestAndDateArr[0].trim();
            String dateInStr = digestAndDateArr[1].trim();

            Date digestDate = digestDateFormat.parse(dateInStr.trim());
            if (digestDate == null){
                throw new Exception("Nu se poate parsa digest date la getFormDigest.");
            }

            formDigestExpireDateTime = new DateTime(digestDate).plusSeconds(this.formDigestTimeoutSeconds);
             */

            // altfel initializeaza data de expirare a digestului cu data lui DMS
            formDigestExpireDateTime = new DateTime().plusSeconds(formDigestTimeoutSeconds);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (Exception ignore) {
                }

                if (response != null) {
                    try {
                        response.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        }

        return formDigest;
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
        CloseableHttpResponse response = null;

        try {
            HttpGet request = new HttpGet(buildUrl("_api/web/GetFileByServerRelativeUrl('/unit_" + fileLink.getUnitID() + "_parcel_" + fileLink.getParcelNum() + "/" + encodeUrlParam(storageFileName) + "')/$value"));

            // executam si citim raspunsul
            response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode != 200) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Cod raspuns ").append(responseCode).append(" la retrieveFile. ");
                throw new Exception(errorMessage.toString());
            }

            try (InputStream is = response.getEntity().getContent(); OutputStream os = new FileOutputStream(outputFile)) {
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
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (Exception ignore) {
                }

                if (response != null) {
                    try {
                        response.close();
                    } catch (Exception ignore) {
                    }
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
        String formDigest = getFormDigest();
        createParcelIfNotExists(unitIdTarget, parcelNumTarget);

        String storageFileName = getStorageFileNameTryShortcut(unitIdSource, parcelNumSource, fileNameSource, String.valueOf(fileIdSource), versionSource);
        String storageFileNameTarget = getStorageFileName(storageFileName, String.valueOf(fileIdTarget), versionTarget);

        boolean fileExists = fileExists(fileIdTarget, parcelNumTarget, storageFileNameTarget);

        if (fileExists)
            throw new Exception("OUTPUT_FILE_EXISTS");

        CloseableHttpResponse response = null;
        try {
            HttpPost request = new HttpPost(buildUrl("_api/web/GetFileByServerRelativeUrl('/unit_" + unitIdSource + "_parcel_" + parcelNumSource + "/" + encodeUrlParam(storageFileName) + "')/copyto(strNewUrl='/unit_" + unitIdTarget + "_parcel_" + parcelNumTarget + "/" + encodeUrlParam(storageFileNameTarget) + "',bOverWrite=true)"));
            request.setHeader("Accept", "application/json;odata=verbose");
            request.setHeader("X-RequestDigest", formDigest);

            // executam si citim raspunsul
            response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();

            // citim rezultatul
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder resultBuilder = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                resultBuilder.append(line);
            }
            String result = resultBuilder.toString();

            if (responseCode != 200 && responseCode != 201) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Cod raspuns ").append(responseCode).append(" la copyFile. ").append(result);
                throw new Exception(errorMessage.toString());
            }
        } finally {
            if (response != null)
                EntityUtils.consume(response.getEntity());

            if (response != null) {
                try {
                    response.close();
                } catch (Exception ignore) {
                }
            }
        }
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

    private String encodeUrlParam(String param) {
        if (param == null) {
            return param;
        }

        String ret = param;

        try {
            ret = URLEncoder.encode(param, "UTF-8");
        } catch (Exception ignore) {
        }

        return ret;
    }

    private String buildUrl(String suffix) {
        if (url == null || url.trim().isEmpty()) {
            url = "";
        }

        String retUrl = url;
        if (!retUrl.endsWith("/")) {
            retUrl += "/";
        }

        if (suffix == null) {
            return retUrl;
        }

        retUrl += suffix.trim();

        return retUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
