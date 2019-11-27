package ro.infrasoft.infralib.io.filestore;

public class FileLink {
    private int unitID;
    private int fileID;
    private int parcelNum;
    private String version;
    private String fileName;

    public FileLink() {
    }

    public FileLink(int unitID, int fileID, int parcelNum, String version, String fileName) {
        this.unitID = unitID;
        this.fileID = fileID;
        this.parcelNum = parcelNum;
        this.version = version;
        this.fileName = fileName;
    }

    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    public int getUnitID() {
        return unitID;
    }

    public void setUnitID(int unitID) {
        this.unitID = unitID;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getParcelNum() {
        return parcelNum;
    }

    public void setParcelNum(int parcelNum) {
        this.parcelNum = parcelNum;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
