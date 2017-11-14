

package com.daolab.daolabplayer.api.ovp;

import com.daolab.netkit.utils.ErrorElement;
import com.daolab.netkit.utils.RestrictionError;

/**
 * @hide
 */

public class DaolabOvpErrorHelper {

    public static ErrorElement getErrorElement(String code, String message){
        switch (code){
            /*case "SCHEDULED_RESTRICTED":
            case "COUNTRY_RESTRICTED":*/
            case "NoFilesFound":
                return ErrorElement.NotFound.message("Content can't be played due to lack of sources");

            default:
                String messageCode = code;
                if (!"".equals(messageCode)) {
                    messageCode += ":";
                }
                return new RestrictionError(messageCode + message, RestrictionError.Restriction.NotAllowed);
        }
    }

    public static ErrorElement getErrorElement(String code) {
        return getErrorElement(code, null);
    }
}
