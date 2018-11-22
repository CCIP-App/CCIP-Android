package app.opass.ccip.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Announcement {

    @SerializedName("_id")
    @Expose
    private Id id;
    @SerializedName("datetime")
    @Expose
    private Integer datetime;
    @SerializedName("msg_en")
    @Expose
    private String msgEn;
    @SerializedName("msg_zh")
    @Expose
    private String msgZh;
    @SerializedName("uri")
    @Expose
    private String uri;

    /**
     * @return The id
     */
    public Id getId() {
        return id;
    }

    /**
     * @param id The _id
     */
    public void setId(Id id) {
        this.id = id;
    }

    /**
     * @return The datetime
     */
    public Integer getDatetime() {
        return datetime;
    }

    /**
     * @param datetime The datetime
     */
    public void setDatetime(Integer datetime) {
        this.datetime = datetime;
    }

    /**
     * @return The msgEn
     */
    public String getMsgEn() {
        return msgEn;
    }

    /**
     * @param msgEn The msg_en
     */
    public void setMsgEn(String msgEn) {
        this.msgEn = msgEn;
    }

    /**
     * @return The msgZh
     */
    public String getMsgZh() {
        return msgZh;
    }

    /**
     * @param msgZh The msg_zh
     */
    public void setMsgZh(String msgZh) {
        this.msgZh = msgZh;
    }

    /**
     * @return The uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri The uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

}
