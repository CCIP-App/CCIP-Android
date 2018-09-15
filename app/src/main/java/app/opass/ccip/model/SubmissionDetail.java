package app.opass.ccip.model;

public interface SubmissionDetail {
    String subject = null;
    String summary = null;

    String getSubject();

    void setSubject(String subject);

    String getSummary();

    void setSummary(String summary);
}
