package app.opass.ccip.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class En implements SubmissionDetail {

    @SerializedName("subject")
    @Expose
    private String subject;
    @SerializedName("summary")
    @Expose
    private String summary;

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

}
