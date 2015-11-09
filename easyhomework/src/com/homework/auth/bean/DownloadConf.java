package com.homework.auth.bean;

public class DownloadConf {
private String platform;
private String market;
private String latestVersion;
private int fileSize;
private String md5;
private String url;
private String note;
private String noteCn;
private String noteCnTr;
private String noteEn;
private String noteEs;

public String getPlatform() {
	return platform;
}
public void setPlatform(String platform) {
	this.platform = platform;
}
public String getMarket() {
	return market;
}
public void setMarket(String market) {
	this.market = market;
}
public int getFileSize() {
	return fileSize;
}
public void setFileSize(int fileSize) {
	this.fileSize = fileSize;
}
public String getMd5() {
	return md5;
}
public void setMd5(String md5) {
	this.md5 = md5;
}
public String getUrl() {
	return url;
}
public void setUrl(String url) {
	this.url = url;
}
public String getNote() {
	return note;
}
public void setNote(String note) {
	this.note = note;
}
public String getLatestVersion() {
	return latestVersion;
}
public void setLatestVersion(String latestVersion) {
	this.latestVersion = latestVersion;
}
public String getNoteCn() {
	return noteCn;
}
public void setNoteCn(String noteCn) {
	this.noteCn = noteCn;
}
public String getNoteCnTr() {
	return noteCnTr;
}
public void setNoteCnTr(String noteCnTr) {
	this.noteCnTr = noteCnTr;
}
public String getNoteEn() {
	return noteEn;
}
public void setNoteEn(String noteEn) {
	this.noteEn = noteEn;
}
public String getNoteEs() {
	return noteEs;
}
public void setNoteEs(String noteEs) {
	this.noteEs = noteEs;
}

}
