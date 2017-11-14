package com.daolab.netkit.connect.response;

import com.daolab.netkit.utils.ErrorElement;

/**
 */

public class PrimitiveResult extends BaseResult {
    private String result = null;

    public PrimitiveResult(String result) {
        super();
        this.result = result;
    }

    public PrimitiveResult() {
        super();
    }

    public PrimitiveResult(ErrorElement error) {
        super(error);
    }

    public String getResult() {
        return result;
    }

    public PrimitiveResult result(String result){
        this.result = result;
        return this;
    }

    public PrimitiveResult error(ErrorElement error) {
        this.error = error;
        return this;
    }
}
