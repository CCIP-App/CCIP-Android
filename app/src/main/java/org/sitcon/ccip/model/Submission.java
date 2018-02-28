
package org.sitcon.ccip.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Submission {

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

    public static String getTypeString(String type) {
        return "";
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

        if (!start.equals(that.start) || !end.equals(that.end)) {
            return false;
        }

        return room.equals(that.room);
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + room.hashCode();

        return result;
    }
}
