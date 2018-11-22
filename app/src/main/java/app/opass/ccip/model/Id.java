package app.opass.ccip.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Id {

    @SerializedName("$oid")
    @Expose
    private String $oid;

    /**
     * @return The $oid
     */
    public String get$oid() {
        return $oid;
    }

    /**
     * @param $oid The $oid
     */
    public void set$oid(String $oid) {
        this.$oid = $oid;
    }

}
