
package org.sitcon.ccip.model;

import android.content.res.Resources;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.sitcon.ccip.R;

public class Submission {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("speaker")
    @Expose
    private Speaker speaker;
    @SerializedName("start")
    @Expose
    private String start;
    @SerializedName("end")
    @Expose
    private String end;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("room")
    @Expose
    private String room;
    @SerializedName("community")
    @Expose
    private String community;
    @SerializedName("subject")
    @Expose
    private String subject;
    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("sli.do")
    @Expose
    private String slido;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public Speaker getSpeaker() {
        return speaker;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
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

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSlido() {
        return slido;
    }

    public void setSlido(String slido) {
        this.slido = slido;
    }


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
