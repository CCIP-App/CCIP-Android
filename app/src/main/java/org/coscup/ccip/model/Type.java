
package org.coscup.ccip.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Type {

    @SerializedName("type")
    @Expose
    private Integer type;
    @SerializedName("nameen")
    @Expose
    private String nameen;
    @SerializedName("namezh")
    @Expose
    private String namezh;

    /**
     * 
     * @return
     *     The type
     */
    public Integer getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The nameen
     */
    public String getNameen() {
        return nameen;
    }

    /**
     * 
     * @param nameen
     *     The nameen
     */
    public void setNameen(String nameen) {
        this.nameen = nameen;
    }

    /**
     * 
     * @return
     *     The namezh
     */
    public String getNamezh() {
        return namezh;
    }

    /**
     * 
     * @param namezh
     *     The namezh
     */
    public void setNamezh(String namezh) {
        this.namezh = namezh;
    }

}
