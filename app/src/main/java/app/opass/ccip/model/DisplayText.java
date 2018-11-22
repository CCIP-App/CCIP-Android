package app.opass.ccip.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DisplayText {

    @SerializedName("en-US")
    @Expose
    private String enUS;
    @SerializedName("zh-TW")
    @Expose
    private String zhTW;

    public String getEnUS() {
        return enUS;
    }

    public void setEnUS(String enUS) {
        this.enUS = enUS;
    }

    public String getZhTW() {
        return zhTW;
    }

    public void setZhTW(String zhTW) {
        this.zhTW = zhTW;
    }

}
