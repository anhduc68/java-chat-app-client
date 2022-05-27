/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;

/**
 *
 * @author Dieu Dai Hiep
 */
public class ObjectWrapper implements Serializable {

    private static final long serialVersionUID = 166666L;
    public static final int LOGIN = 1;
    public static final int REPLY_LOGIN = 2;
    public static final int SIGNUP = 3;
    public static final int REPLY_SIGNUP = 4;
    public static final int SEARCH_USER_BY_NAME = 5;
    public static final int REPLY_SEARCH_USER = 6;
    public static final int ADD_FRIEND_REQUEST = 7;
    public static final int REPLY_ADDFRIEND_REQUEST = 8;
    public static final int SHOW_FRIEND_REQUEST = 9;
    public static final int REPLY_SHOW_FRIEND_REQUEST = 10;
    public static final int ADD_FRIEND = 11;
    public static final int REPLY_ADD_FRIEND = 12;
    public static final int SHOW_LIST_FRIEND = 13;
    public static final int REPLY_SHOW_LIST_FRIEND = 14;
    public static final int NOTIFY_TO_USER = 15;
    public static final int LIST_ONLINE_FRIEND = 16;
    public static final int SEND_MESSAGE = 17;
    public static final int SHOW_LIST_MESSAGE = 18;
    public static final int REPLY_SHOW_LIST_MESSAGE = 19;
    public static final int SELECT_USER_MESSAGE_TO = 20;
    public static final int UPDATE_STATUS = 21;
    public static final int UPDATE_NEWS = 22;
    public static final int ADD_GROUP = 23;
    public static final int REPLY_ADD_GROUP = 24;
    public static final int JOIN_GROUP = 25;
    public static final int REPLY_JOIN_GROUP = 26;
    public static final int SHOW_LIST_GROUP = 27;
    public static final int REPLY_SHOW_LIST_GROUP = 28;
    public static final int SEND_GROUP_MESSAGE = 29;
    public static final int REPLY_SEND_GROUP_MESSAGE = 30;
    public static final int SHOW_LIST_GROUP_MESSAGE = 31;
    public static final int REPLY_SHOW_LIST_GROUP_MESSAGE = 32;
    public static final int SELECT_GROURP_MESSAGE_TO = 33;
    public static final int REPLY_GET_USER = 34;
    public static final int QUIT = 99;

    private int performative;
    private Object data;

    public ObjectWrapper() {
    }

    public ObjectWrapper(int performative, Object data) {
        this.performative = performative;
        this.data = data;
    }

    public int getPerformative() {
        return performative;
    }

    public void setPerformative(int performative) {
        this.performative = performative;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
