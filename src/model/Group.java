
package model;

import java.io.Serializable;


public class Group implements Serializable{
    int id;
    String gr_name;

    public Group() {
    }

    public Group(int id, String gr_name) {
        this.id = id;
        this.gr_name = gr_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGr_name() {
        return gr_name;
    }

    public void setGr_name(String gr_name) {
        this.gr_name = gr_name;
    }
    
}
