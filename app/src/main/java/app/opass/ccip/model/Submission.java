package app.opass.ccip.model;

import android.content.Context;
import android.content.res.Resources;
import app.opass.ccip.R;
import app.opass.ccip.util.LocaleUtil;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Locale;

public class Submission {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("room")
    @Expose
    private String room;
    @SerializedName("start")
    @Expose
    private String start;
    @SerializedName("end")
    @Expose
    private String end;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("zh")
    @Expose
    private Zh zh;
    @SerializedName("en")
    @Expose
    private En en;
    @SerializedName("speakers")
    @Expose
    private List<Speaker> speakers = null;

    public static int getTypeString(String type) {
        switch (type) {
            case "K":
                return R.string.keynote;
            case "L":
                return R.string.lightning_talk;
            case "P":
                return R.string.panel_discussion;
            case "S":
                return R.string.short_talk;
            case "T":
                return R.string.talk;
            case "U":
                return R.string.unconf;
            default:
                throw new Resources.NotFoundException("Unexpected type symbol");
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SubmissionDetail getSubmissionDetail(Context context) {
        if (LocaleUtil.getCurrentLocale(context).getLanguage().equals(new Locale("zh").getLanguage())) {
            return getZh();
        } else {
            return getEn();
        }
    }

    public SubmissionDetail getZh() {
        return zh;
    }

    public void setZh(Zh zh) {
        this.zh = zh;
    }

    public SubmissionDetail getEn() {
        return en;
    }

    public void setEn(En en) {
        this.en = en;
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(List<Speaker> speakers) {
        this.speakers = speakers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Submission that = (Submission) o;

        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + room.hashCode();

        return result;
    }
}
