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
public class Message implements Serializable{
    private int from_id;
    private int to_id;
    private String msg;

    public Message() {
    }

    public Message(int from_id, int to_id, String msg) {
        this.from_id = from_id;
        this.to_id = to_id;
        this.msg = msg;
    }

    public int getFrom_id() {
        return from_id;
    }

    public void setFrom_id(int from_id) {
        this.from_id = from_id;
    }

    public int getTo_id() {
        return to_id;
    }

    public void setTo_id(int to_id) {
        this.to_id = to_id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

   
    
   
    @Override
    public String toString(){
        return from_id+" "+to_id+" "+msg+"\n";
    }
}
