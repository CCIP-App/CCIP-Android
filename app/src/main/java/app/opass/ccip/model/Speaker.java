
package app.opass.ccip.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Speaker {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("zh")
    @Expose
    private Zh_ zh;
    @SerializedName("en")
    @Expose
    private En_ en;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public SpekaerDetail getZh() {
        return zh;
    }

    public void setZh(Zh_ zh) {
        this.zh = zh;
    }

    public SpekaerDetail getEn() {
        return en;
    }

    public void setEn(En_ en) {
        this.en = en;
    }

}
