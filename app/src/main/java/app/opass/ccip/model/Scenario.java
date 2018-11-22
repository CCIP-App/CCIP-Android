package app.opass.ccip.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Scenario {

    @SerializedName("display_text")
    @Expose
    private DisplayText displayText;
    @SerializedName("order")
    @Expose
    private Integer order;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("available_time")
    @Expose
    private Integer availableTime;
    @SerializedName("attr")
    @Expose
    private JsonElement attr;
    @SerializedName("expire_time")
    @Expose
    private Integer expireTime;
    @SerializedName("countdown")
    @Expose
    private Integer countdown;
    @SerializedName("used")
    @Expose
    private Integer used;
    @SerializedName("disabled")
    @Expose
    private String disabled;

    public DisplayText getDisplayText() {
        return displayText;
    }

    public void setDisplayText(DisplayText displayText) {
        this.displayText = displayText;
    }

    /**
     * @return The order
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * @param order The order
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The availableTime
     */
    public Integer getAvailableTime() {
        return availableTime;
    }

    /**
     * @param availableTime The available_time
     */
    public void setAvailableTime(Integer availableTime) {
        this.availableTime = availableTime;
    }

    /**
     * @return The attr
     */
    public JsonElement getAttr() {
        return attr;
    }

    /**
     * @param attr The attr
     */
    public void setAttr(JsonElement attr) {
        this.attr = attr;
    }

    /**
     * @return The expireTime
     */
    public Integer getExpireTime() {
        return expireTime;
    }

    /**
     * @param expireTime The expire_time
     */
    public void setExpireTime(Integer expireTime) {
        this.expireTime = expireTime;
    }

    /**
     * @return The countdown
     */
    public Integer getCountdown() {
        return countdown;
    }

    /**
     * @param countdown The countdown
     */
    public void setCountdown(Integer countdown) {
        this.countdown = countdown;
    }

    /**
     * @return The used
     */
    public Integer getUsed() {
        return used;
    }

    /**
     * @param used The used
     */
    public void setUsed(Integer used) {
        this.used = used;
    }

    /**
     * @return The disabled
     */
    public String getDisabled() {
        return disabled;
    }

    /**
     * @param disabled The used
     */
    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }

}
