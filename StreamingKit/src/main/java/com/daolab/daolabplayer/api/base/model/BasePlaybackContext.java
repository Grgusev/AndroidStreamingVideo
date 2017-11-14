

package com.daolab.daolabplayer.api.base.model;

import com.google.gson.annotations.SerializedName;
import com.daolab.netkit.connect.response.BaseResult;
import com.daolab.netkit.utils.ErrorElement;

import java.util.ArrayList;

/**
 * Created by tehilarozin on 02/11/2016.
 */

public class BasePlaybackContext extends BaseResult {

    ArrayList<DaolabRuleAction> actions;
    ArrayList<DaolabAccessControlMessage> messages;


    public ArrayList<DaolabAccessControlMessage> getMessages() {
        return messages;
    }

    public ArrayList<DaolabRuleAction> getActions() {
        return actions;
    }



    public ErrorElement hasError() {
        ErrorElement error = null;

        if (hasBlockedAction() && messages != null) {
            // in case we'll want to gather errors or priorities message, loop over messages. Currently returns the first error

            for (DaolabAccessControlMessage message : messages) {
                error = getErrorElement(message);
                if (error != null) {
                    break;
                }
            }
        }
        return error;
    }

    protected ErrorElement getErrorElement(DaolabAccessControlMessage message) {
        return  null;
    }

    public boolean hasBlockedAction() {
        if (actions != null) {
            for (DaolabRuleAction rule : actions) {
                if (rule != null && DaolabRuleAction.DaolabRuleActionType.BLOCK.equals(rule.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class DaolabRuleAction extends BaseResult{
        String objectType;
        DaolabRuleActionType type;

        public DaolabRuleAction() {
        }

        public DaolabRuleActionType getType() {
            return type;
        }


        public enum DaolabRuleActionType {
            DRM_POLICY("drm.DRM_POLICY"),
            @SerializedName(value = "1", alternate = {"BLOCK"})
            BLOCK("1"),
            @SerializedName("2")
            PREVIEW("2"),
            @SerializedName("3")
            LIMIT_FLAVORS("3"),
            @SerializedName("4")
            ADD_TO_STORAGE("4"),
            @SerializedName("5")
            LIMIT_DELIVERY_PROFILES("5"),
            @SerializedName("6")
            SERVE_FROM_REMOTE_SERVER("6"),
            @SerializedName("7")
            REQUEST_HOST_REGEX("7"),
            @SerializedName("8")
            LIMIT_THUMBNAIL_CAPTURE("8");

            public String value;

            DaolabRuleActionType(String value) {
                this.value = value;
            }
        }
    }

    public static class DaolabAccessControlDrmPolicyAction extends DaolabRuleAction {
        int policyId;

        public DaolabAccessControlDrmPolicyAction() {
            super();
            objectType = "DaolabAccessControlDrmPolicyAction";
        }
    }

    public static class DaolabAccessControlLimitDeliveryProfilesAction extends DaolabRuleAction {
        String deliveryProfileIds;
        boolean isBlockedList;

        public DaolabAccessControlLimitDeliveryProfilesAction() {
            super();
            objectType = "DaolabAccessControlLimitDeliveryProfilesAction";
        }
    }

    public static class DaolabAccessControlMessage {
        String message;
        String code;

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
