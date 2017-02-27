
package org.sitcon.ccip.model;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Attendee {

    @SerializedName("_id")
    @Expose
    private Id id;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("attr")
    @Expose
    private JsonElement attr;
    @SerializedName("first_use")
    @Expose
    private Integer firstUse;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("scenarios")
    @Expose
    private List<Scenario> scenarios = new ArrayList<Scenario>();

    /**
     * 
     * @return
     *     The id
     */
    public Id getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The _id
     */
    public void setId(Id id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The token
     */
    public String getToken() {
        return token;
    }

    /**
     * 
     * @param token
     *     The token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 
     * @return
     *     The userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 
     * @param userId
     *     The user_id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 
     * @return
     *     The attr
     */
    public JsonElement getAttr() {
        return attr;
    }

    /**
     * 
     * @param attr
     *     The attr
     */
    public void setAttr(JsonElement attr) {
        this.attr = attr;
    }

    public Integer getFirstUse() {
        return firstUse;
    }

    public void setFirstUse(Integer firstUse) {
        this.firstUse = firstUse;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The scenarios
     */
    public List<Scenario> getScenarios() {
        return scenarios;
    }

    /**
     * 
     * @param scenarios
     *     The scenarios
     */
    public void setScenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

}
