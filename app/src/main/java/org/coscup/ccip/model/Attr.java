
package org.coscup.ccip.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Attr {

    @SerializedName("diet")
    @Expose
    private String diet;

    /**
     * 
     * @return
     *     The diet
     */
    public String getDiet() {
        return diet;
    }

    /**
     * 
     * @param diet
     *     The diet
     */
    public void setDiet(String diet) {
        this.diet = diet;
    }

}
