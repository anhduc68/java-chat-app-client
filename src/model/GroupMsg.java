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
public class GroupMsg implements Serializable{
    private int user_id;
    private int gr_id;
    String msg;

    public GroupMsg() {
    }

    public GroupMsg(int user_id, int gr_id, String msg) {
        this.user_id = user_id;
        this.gr_id = gr_id;
        this.msg = msg;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getGr_id() {
        return gr_id;
    }

    public void setGr_id(int gr_id) {
        this.gr_id = gr_id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    
}
