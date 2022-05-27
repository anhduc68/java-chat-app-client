/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi_controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import model.User;
public interface IUser extends Remote{
    public int update( User user )throws RemoteException;
     public int isExist( String username) throws RemoteException;
     public int updateImg(User user, File f) throws RemoteException, FileNotFoundException;
     public byte[] retriveImg(User user)  throws RemoteException;
     public User getUserByID(int id) throws RemoteException;
}
