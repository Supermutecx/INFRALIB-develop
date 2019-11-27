package ro.infrasoft.infralib.io.filestore;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.jasypt.salt.ZeroSaltGenerator;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;
import ro.infrasoft.infralib.db.datasource.BaseDataSource;
import ro.infrasoft.infralib.db.type.DbType;
import ro.infrasoft.infralib.settings.Settings;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Date;
import java.util.zip.ZipEntry;

/**
 * Clasa care se ocupa cu stocarea fisierelor.
 */
@SuppressWarnings({"TryFinallyCanBeTryWithResources", "ResultOfMethodCallIgnored"})
public final class DiskFileStoreApi implements FileStoreApi {
    private static final String SEP = File.separator;

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
        boolean ok = false;
        try {
            File unitFolder = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId);
            ok = unitFolder.exists();
        } catch (Exception e) {
            // ignored - false
        }
        return ok;
    }

    @Override
    public void createRootDir(int unitId) {
        if (!checkRootDir(unitId)) {
            try {
                File workDir = new File(Settings.get("file_store", "root_dir"));
                if (workDir.exists()) {
                    File unitX = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId);
                    unitX.mkdir();
                } else {
                    workDir.mkdir();
                    File unitX = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId);
                    unitX.mkdir();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        // Se face rost de parcela
        File parcel = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum);

        // Daca parcela nu exista, se creaza
        if (!parcel.exists())
            parcel.mkdir();

        // Se face rost de fisierul in care se va salva
        File outputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + fileId + "_" + version);
        if (outputFile.exists())
            throw new Exception("OUTPUT_FILE_EXISTS");

        // Se initializeaza criptarea
        StandardPBEByteEncryptor binaryEncryptor = new StandardPBEByteEncryptor();
        ZeroSaltGenerator sg = new ZeroSaltGenerator();

        binaryEncryptor.setSaltGenerator(sg);
        binaryEncryptor.setPassword(password);

        FileInputStream fis = null;
        FileChannel inputChannel = null;
        FileOutputStream fos = null;
        FileChannel outputChannel = null;

        try {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(Integer.valueOf(Settings.get("file_store", "byte_buffer")));
            ByteBuffer cryptByteBuffer = ByteBuffer.allocateDirect(Integer.valueOf(Settings.get("file_store", "byte_buffer")) + 8);

            // Se citeste fisierul de input
            fis = new FileInputStream(inputFile);
            inputChannel = fis.getChannel();


            // Se initializeaza fisierul de output
            fos = new FileOutputStream(outputFile);
            outputChannel = fos.getChannel();

            // Se citeste din sursa, se cripteaza si se pune in fisierul temporar
            int bytes = inputChannel.read(byteBuffer);
            while (bytes != -1) {
                byteBuffer.flip();

                // Se face criptarea
                byte[] barr = new byte[byteBuffer.remaining()];
                byteBuffer.get(barr, 0, barr.length);
                byte[] encrBytes = binaryEncryptor.encrypt(barr);
                cryptByteBuffer.clear();
                cryptByteBuffer.put(encrBytes);
                cryptByteBuffer.flip();
                outputChannel.write(cryptByteBuffer);
                byteBuffer.clear();
                bytes = inputChannel.read(byteBuffer);
            }
        } finally {
            if(inputChannel!=null){
                inputChannel.close();
            }
            if(outputChannel!=null){
                outputChannel.close();
            }
            if(fis!=null){
                fis.close();
            }
            if(fos!=null){
                fos.close();
            }
        }

        // acum facem arhiva
        try {
            File toZip = outputFile;
            File zipFile = new File(outputFile.getAbsoluteFile().getAbsolutePath() + ".zip");
            FileSource fs = new FileSource(toZip.getName(), toZip);
            ZipEntrySource[] entries = new ZipEntrySource[]{fs};
            ZipUtil.pack(entries, zipFile);
            toZip.delete();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw (new Exception(e));
        }
    }

    @Override
    public File retrieveFile(int unitId, String fileId, int parcelNum, String version, String fileName, String password) throws Exception {
        boolean zipped = false;
        File inputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + fileId + "_" + version);

        // in caz ca e link, rezolva fisierul real
        inputFile = rezolveLinkIfNeeded(inputFile);

        if (!inputFile.exists()) {
            // incercam zip
            final File zipFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".zip");
            if (!zipFile.exists())
                throw new Exception("FILE_NOT_EXIST" + ":" + parcelNum + ":" + fileId + ":" + version);
            else {
                // unzip
                final File zipOutputFile = new File(Settings.get("file_store", "work_dir") + SEP + "unit_" + unitId + SEP + "file_" + fileId + "_" + version + "_" + new Date().getTime());
                ZipUtil.iterate(zipFile, new ZipEntryCallback() {
                    @Override
                    public void process(InputStream in, ZipEntry zipEntry) throws IOException {
                        ZipUtil.unpackEntry(zipFile, zipEntry.getName(), zipOutputFile);
                    }
                });

                inputFile = zipOutputFile;
                zipped = true;
            }
        }

        // Se initializeaza criptarea
        StandardPBEByteEncryptor binaryEncryptor = new StandardPBEByteEncryptor();
        ZeroSaltGenerator sg = new ZeroSaltGenerator();

        binaryEncryptor.setSaltGenerator(sg);
        binaryEncryptor.setPassword(password);

        FileInputStream fis = null;
        FileChannel inputChannel = null;
        File outputFile = null;
        FileOutputStream fos = null;
        FileChannel outputChannel = null;

        try {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(Integer.valueOf(Settings.get("file_store", "byte_buffer")) + 8);
            ByteBuffer cryptByteBuffer = ByteBuffer.allocateDirect(Integer.valueOf(Settings.get("file_store", "byte_buffer")));

            // Se citeste fisierul de input
            fis = new FileInputStream(inputFile);
            inputChannel = fis.getChannel();


            // Se initializeaza fisierul de output
            String outputName = "download_file_" + new Date().getTime() + "_" + Thread.currentThread().getId();
            outputFile = new File(Settings.get("file_store", "work_dir") + "unit_" + unitId + SEP + outputName);
            fos = new FileOutputStream(outputFile);
            outputChannel = fos.getChannel();

            // Se citeste din sursa, se decripteaza si se pune in fisierul temporar
            int bytes = inputChannel.read(byteBuffer);
            while (bytes != -1) {
                byteBuffer.flip();

                // Se face criptarea
                byte[] barr = new byte[byteBuffer.remaining()];
                byteBuffer.get(barr, 0, barr.length);
                byte[] encrBytes = binaryEncryptor.decrypt(barr);
                cryptByteBuffer.clear();
                cryptByteBuffer.put(encrBytes);
                cryptByteBuffer.flip();
                outputChannel.write(cryptByteBuffer);
                byteBuffer.clear();
                bytes = inputChannel.read(byteBuffer);
            }
        } finally {
            inputChannel.close();
            outputChannel.close();
            fis.close();
            fos.close();
        }

        return outputFile;
    }

    @Override
    public File retrieveFileWithExtension(int unitId, String fileId, int parcelNum, String version, String fileName, String password, String extension) throws Exception {
        boolean zipped = false;
        File inputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + fileId + "_" + version);

        // in caz ca e link, rezolva fisierul real
        inputFile = rezolveLinkIfNeeded(inputFile);

        if (!inputFile.exists()) {
            // incercam zip
            final File zipFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".zip");
            if (!zipFile.exists())
                throw new Exception("FILE_NOT_EXIST" + ":" + parcelNum + ":" + fileId + ":" + version);
            else {
                // unzip
                final File zipOutputFile = new File(Settings.get("file_store", "work_dir") + SEP + "unit_" + unitId + SEP + "file_" + fileId + "_" + version + "_" + new Date().getTime());
                ZipUtil.iterate(zipFile, new ZipEntryCallback() {
                    @Override
                    public void process(InputStream in, ZipEntry zipEntry) throws IOException {
                        ZipUtil.unpackEntry(zipFile, zipEntry.getName(), zipOutputFile);
                    }
                });

                inputFile = zipOutputFile;
                zipped = true;
            }
        }

        // Se initializeaza criptarea
        StandardPBEByteEncryptor binaryEncryptor = new StandardPBEByteEncryptor();
        ZeroSaltGenerator sg = new ZeroSaltGenerator();

        binaryEncryptor.setSaltGenerator(sg);
        binaryEncryptor.setPassword(password);

        FileInputStream fis = null;
        FileChannel inputChannel = null;
        File outputFile = null;
        FileOutputStream fos = null;
        FileChannel outputChannel = null;

        try {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(Integer.valueOf(Settings.get("file_store", "byte_buffer")) + 8);
            ByteBuffer cryptByteBuffer = ByteBuffer.allocateDirect(Integer.valueOf(Settings.get("file_store", "byte_buffer")));

            // Se citeste fisierul de input
            fis = new FileInputStream(inputFile);
            inputChannel = fis.getChannel();


            // Se initializeaza fisierul de output
            String outputName = "download_file_" + new Date().getTime() + "_" + Thread.currentThread().getId() + "." + extension;
            outputFile = new File(Settings.get("file_store", "work_dir") + "unit_" + unitId + SEP + outputName);
            fos = new FileOutputStream(outputFile);
            outputChannel = fos.getChannel();

            // Se citeste din sursa, se decripteaza si se pune in fisierul temporar
            int bytes = inputChannel.read(byteBuffer);
            while (bytes != -1) {
                byteBuffer.flip();

                // Se face criptarea
                byte[] barr = new byte[byteBuffer.remaining()];
                byteBuffer.get(barr, 0, barr.length);
                byte[] encrBytes = binaryEncryptor.decrypt(barr);
                cryptByteBuffer.clear();
                cryptByteBuffer.put(encrBytes);
                cryptByteBuffer.flip();
                outputChannel.write(cryptByteBuffer);
                byteBuffer.clear();
                bytes = inputChannel.read(byteBuffer);
            }
        } finally {
            inputChannel.close();
            outputChannel.close();
            fis.close();
            fos.close();
        }

        return outputFile;
    }

    @Override
    public File retrieveFile(int unitId, String fileId, int parcelNum, String version, String fileName, String password, final File zipOutputFile, final File outputFile) throws Exception {
        boolean zipped = false;
        File inputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + fileId + "_" + version);

        // in caz ca e link, rezolva fisierul real
        inputFile = rezolveLinkIfNeeded(inputFile);

        if (!inputFile.exists()) {
            // incercam zip
            final File zipFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".zip");
            if (!zipFile.exists())
                throw new Exception("FILE_NOT_EXIST" + ":" + parcelNum + ":" + fileId + ":" + version);
            else {
                // unzip
                ZipUtil.iterate(zipFile, new ZipEntryCallback() {
                    @Override
                    public void process(InputStream in, ZipEntry zipEntry) throws IOException {
                        ZipUtil.unpackEntry(zipFile, zipEntry.getName(), zipOutputFile);
                    }
                });

                inputFile = zipOutputFile;
                zipped = true;
            }
        }

        // Se initializeaza criptarea
        StandardPBEByteEncryptor binaryEncryptor = new StandardPBEByteEncryptor();
        ZeroSaltGenerator sg = new ZeroSaltGenerator();

        binaryEncryptor.setSaltGenerator(sg);
        binaryEncryptor.setPassword(password);

        FileInputStream fis = null;
        FileChannel inputChannel = null;
        FileOutputStream fos = null;
        FileChannel outputChannel = null;

        try {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(Integer.valueOf(Settings.get("file_store", "byte_buffer")) + 8);
            ByteBuffer cryptByteBuffer = ByteBuffer.allocateDirect(Integer.valueOf(Settings.get("file_store", "byte_buffer")));

            // Se citeste fisierul de input
            fis = new FileInputStream(inputFile);
            inputChannel = fis.getChannel();


            // Se initializeaza fisierul de output
            String outputName = "download_file_" + new Date().getTime() + "_" + Thread.currentThread().getId();
            fos = new FileOutputStream(outputFile);
            outputChannel = fos.getChannel();

            // Se citeste din sursa, se decripteaza si se pune in fisierul temporar
            int bytes = inputChannel.read(byteBuffer);
            while (bytes != -1) {
                byteBuffer.flip();

                // Se face criptarea
                byte[] barr = new byte[byteBuffer.remaining()];
                byteBuffer.get(barr, 0, barr.length);
                byte[] encrBytes = binaryEncryptor.decrypt(barr);
                cryptByteBuffer.clear();
                cryptByteBuffer.put(encrBytes);
                cryptByteBuffer.flip();
                outputChannel.write(cryptByteBuffer);
                byteBuffer.clear();
                bytes = inputChannel.read(byteBuffer);
            }
        } finally {
            inputChannel.close();
            outputChannel.close();
            fis.close();
            fos.close();
        }

        return outputFile;
    }

    @Override
    public void deleteFile(int unitId, int fileId, int parcelNum, String version, String fileName) throws Exception {
        File inputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + fileId + "_" + version);
        if (!inputFile.exists()) {
            // incercam zip
            final File zipFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".zip");
            if (!zipFile.exists()) {

                // incercam shortcut
                final File shortcutFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".dmslink");
                if (!shortcutFile.exists()) {
                    throw new Exception("FILE_NOT_EXIST" + ":" + parcelNum + ":" + fileId + ":" + version);
                } else {
                    inputFile = shortcutFile;
                }
            } else {
                inputFile = zipFile;
            }
        }

        inputFile.delete();
    }

    @Override
    public void moveFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception {

        // Se face rost de fisierul de input si output
        File inputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + fileId + "_" + version);
        boolean zipped = false;
        boolean shortcut = false;
        if (!inputFile.exists()) {
            // incercam zip
            final File zipFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".zip");
            if (!zipFile.exists()) {

                // incercam shortcut
                final File shortcutFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".dmslink");
                if (!shortcutFile.exists()) {
                    throw new Exception("FILE_NOT_EXIST" + ":" + parcelNum + ":" + fileId + ":" + version);
                } else {
                    inputFile = shortcutFile;
                    shortcut = true;
                }
            } else {
                inputFile = zipFile;
                zipped = true;
            }
        }

        String finalExt = "";
        if (zipped)
            finalExt = ".zip";
        if (shortcut)
            finalExt = ".dmslink";

        File outputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + newFileId + "_" + version + finalExt);

        // se muta
        Files.move(inputFile, outputFile);
        inputFile.delete();
    }

    @Override
    public void moveFileDelete(int unitId, int fileId, int parcelNum, String version, String fileName) throws Exception {
        // Se face rost de fisierul de input
        File inputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + fileId + "_" + version);
        boolean zipped = false;
        boolean shortcut = false;
        if (!inputFile.exists()) {
            // incercam zip
            final File zipFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".zip");
            if (!zipFile.exists()) {

                // incercam shortcut
                final File shortcutFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".dmslink");
                if (!shortcutFile.exists()) {
                    throw new Exception("FILE_NOT_EXIST" + ":" + parcelNum + ":" + fileId + ":" + version);
                } else {
                    inputFile = shortcutFile;
                    shortcut = true;
                }
            } else {
                inputFile = zipFile;
                zipped = true;
            }
        }

        String finalExt = "";
        if (zipped)
            finalExt = ".zip";
        if (shortcut)
            finalExt = ".dmslink";

        // incercam directorul de delete de pe unitatea curenta, daca nu exista in cream
        File deletedFolder = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "deleted");
        if (!deletedFolder.exists()) {
            deletedFolder.mkdir();
        }

        // locatia unde se va muta fisierul
        File outputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "deleted" + SEP + "file_" + fileId + "_" + version);

        // se muta
        Files.move(inputFile, outputFile);
        inputFile.delete();
    }

    @Override
    public void copyFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception {

        // Se face rost de fisierul de input si output
        File inputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + fileId + "_" + version);
        boolean zipped = false;
        boolean shortcut = false;
        if (!inputFile.exists()) {
            // incercam zip
            final File zipFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".zip");
            if (!zipFile.exists()) {

                // incercam shortcut
                final File shortcutFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".dmslink");
                if (!shortcutFile.exists()) {
                    throw new Exception("FILE_NOT_EXIST" + ":" + parcelNum + ":" + fileId + ":" + version);
                } else {
                    inputFile = shortcutFile;
                    shortcut = true;
                }
            } else {
                inputFile = zipFile;
                zipped = true;
            }
        }

        String finalExt = "";
        if (zipped)
            finalExt = ".zip";
        if (shortcut)
            finalExt = ".dmslink";

        File outputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + newFileId + "_" + 1 + finalExt);

        // se copiaza
        Files.copy(inputFile, outputFile);
    }

    @Override
    public void linkFile(int unitId, int fileId, int newFileId, int parcelNum, String version, String fileName, String newFileName) throws Exception {

        // Se face rost de fisierul de input si output
        File inputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + fileId + "_" + version);
        File outputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitId + SEP + "parcel_" + parcelNum + SEP + "file_" + newFileId + "_" + 1 + ".dmslink");

        // se face link
        makeSimpleLinkFile(inputFile, outputFile);
    }

    @Override
    public void copyFile(int unitIdSource, int unitIdTarget, int fileIdSource, int fileIdTarget, int parcelNumSource, int parcelNumTarget, String versionSource, String versionTarget, String fileNameSource, String fileNameTarget) throws Exception {

        // Se face rost de fisierul de input si output
        File inputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitIdSource + SEP + "parcel_" + parcelNumSource + SEP + "file_" + fileIdSource + "_" + versionSource);
        boolean zipped = false;
        boolean shortcut = false;
        if (!inputFile.exists()) {
            // incercam zip
            final File zipFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".zip");
            if (!zipFile.exists()) {

                // incercam shortcut
                final File shortcutFile = new File(inputFile.getAbsoluteFile().getAbsolutePath() + ".dmslink");
                if (!shortcutFile.exists()) {
                    throw new Exception("FILE_NOT_EXIST" + ":" + parcelNumSource + ":" + fileIdSource + ":" + versionSource);
                } else {
                    inputFile = shortcutFile;
                    shortcut = true;
                }
            } else {
                inputFile = zipFile;
                zipped = true;
            }
        }

        String finalExt = "";
        if (zipped)
            finalExt = ".zip";
        if (shortcut)
            finalExt = ".dmslink";

        File outputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitIdTarget + SEP + "parcel_" + parcelNumTarget + SEP + "file_" + fileIdTarget + "_" + versionTarget + finalExt);

        // se copiaza
        Files.copy(inputFile, outputFile);
    }

    @Override
    public void linkFile(int unitIdSource, int unitIdTarget, int fileIdSource, int fileIdTarget, int parcelNumSource, int parcelNumTarget, String versionSource, String versionTarget, String fileNameSource, String fileNameTarget) throws Exception {

        // Se face rost de fisierul de input si output
        File inputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitIdSource + SEP + "parcel_" + parcelNumSource + SEP + "file_" + fileIdSource + "_" + versionSource);
        File outputFile = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + unitIdTarget + SEP + "parcel_" + parcelNumTarget + SEP + "file_" + fileIdTarget + "_" + versionTarget + ".dmslink");

        // se face link
        makeSimpleLinkFile(inputFile, outputFile);
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

    private File rezolveLinkIfNeeded(File inputFile) {
        // daca exista si nu e null
        if (inputFile != null) {

            File linkFile = new File(inputFile.getAbsolutePath() + ".dmslink");

            if (linkFile.exists()) {

                // daca e shortcut
                if (isSimpleFileLink(linkFile)) {
                    Path targetPath = null;
                    try {
                        File targetFile = getSimpleFileLink(linkFile);

                        // chemaa recursiv cu link-ul gasit (poate si asta e link)
                        return rezolveLinkIfNeeded(targetFile);
                    } catch (IOException e) {
                        // nu a reusit sa il citeasca ca link, ramane asa
                    }
                }
            }
        }

        // default intoarce file
        return inputFile;
    }

    private void makeSimpleLinkFile(File inputFile, File linkFile) throws FileNotFoundException {
        if (inputFile != null) {
            try (PrintWriter printWriter = new PrintWriter(linkFile)) {
                printWriter.print(inputFile.getAbsolutePath());
            }
        }
    }

    private File getSimpleFileLink(File link) throws IOException {
        File output = null;

        if (link != null && link.exists()) {
            String fileUrl = FileUtils.readFileToString(link);

            if (fileUrl != null && !fileUrl.isEmpty()) {
                output = new File(fileUrl);
            }
        }

        return output;
    }

    private boolean isSimpleFileLink(File file) {
        // fast logic
        if (file == null)
            return false;

        if (!file.exists())
            return false;

        if (file.getAbsolutePath().endsWith(".dmslink"))
            return true;

        return false;
    }

    @Override
    public int getMinParcel(BaseDataSource db, int unitId) {
        int numPerParcel = 0;

        try {
            String strNumPerParcel = Settings.get("file_store", "num_per_parcel");
            numPerParcel = Integer.valueOf(strNumPerParcel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (numPerParcel == 0){
            numPerParcel = 250;
        }

        int minParcel = 0;
        String strMinParcel = null;
        try {
            strMinParcel = db.sql("select get_min_parcel(" + numPerParcel + ") mp from dual",
                    "select $[SCHEMA_NAME].get_min_parcel(" + numPerParcel + ") mp from dual", DbType.SQL_SERVER,
                    "select $[SCHEMA_NAME].get_min_parcel(" + numPerParcel + ") mp from dual", DbType.MYSQL).get("mp");
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

        if (minParcel == 0){
            minParcel = 1;
        }

        return minParcel;
    }

    @Override
    public void createParcelIfNotExists(int idUnitate, int numParcel) throws Exception {
        File parcel = new File(Settings.get("file_store", "root_dir") + SEP + "unit_" + idUnitate + SEP + "parcel_" + numParcel);

        // Daca parcela nu exista, se creaza
        if (!parcel.exists())
            parcel.mkdirs();
    }
}
