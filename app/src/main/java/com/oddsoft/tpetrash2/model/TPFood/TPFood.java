
package com.oddsoft.tpetrash2.model.TPFood;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TPFood {

    @SerializedName("result")
    @Expose
    private Result result;

    /**
     * 
     * @return
     *     The result
     */
    public Result getResult() {
        return result;
    }

    /**
     * 
     * @param result
     *     The result
     */
    public void setResult(Result result) {
        this.result = result;
    }

}
