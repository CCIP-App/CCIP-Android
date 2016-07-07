
package org.coscup.ccip.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Room {

    @SerializedName("room")
    @Expose
    private String room;
    @SerializedName("name")
    @Expose
    private String name;

    /**
     * 
     * @return
     *     The room
     */
    public String getRoom() {
        return room;
    }

    /**
     * 
     * @param room
     *     The room
     */
    public void setRoom(String room) {
        this.room = room;
    }

    /**
     * 
     * @return
     *     The name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    public void setName(String name) {
        this.name = name;
    }

}
