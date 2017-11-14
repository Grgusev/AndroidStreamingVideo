package com.daolab.netkit.connect.response;

import com.daolab.netkit.utils.ErrorElement;

/**
 */

public class BaseResult {
    public double executionTime;
    public ErrorElement error;

    public BaseResult() {
    }

    public BaseResult(ErrorElement error) {
        this.error = error;
    }
}
