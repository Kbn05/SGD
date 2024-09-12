package com.kbnprojects.sgd.repository;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileManager extends Remote {
    byte[] downloadFile(String filePath) throws RemoteException;
    void uploadFile(byte[] fileData, String remoteFilePath) throws RemoteException;
    void deleteFile(String filePath) throws RemoteException;
    String[] listVersions(String filePath) throws RemoteException;
    byte[] recoverVersion(String filePath, String version) throws RemoteException;
    void saveVersion(byte[] fileData, String filePath) throws RemoteException;
}
