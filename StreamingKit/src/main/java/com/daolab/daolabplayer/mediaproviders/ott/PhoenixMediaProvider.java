

package com.daolab.daolabplayer.mediaproviders.ott;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.daolab.daolabplayer.BEResponseListener;
import com.daolab.daolabplayer.PKLog;
import com.daolab.daolabplayer.PKMediaFormat;
import com.daolab.daolabplayer.api.phoenix.PhoenixErrorHelper;
import com.daolab.daolabplayer.api.phoenix.PhoenixParser;
import com.daolab.daolabplayer.api.phoenix.model.DaolabMediaAsset;
import com.daolab.daolabplayer.api.phoenix.model.DaolabPlaybackSource;
import com.daolab.daolabplayer.api.phoenix.services.AssetService;
import com.daolab.daolabplayer.mediaproviders.base.BECallableLoader;
import com.daolab.netkit.connect.executor.APIOkRequestsExecutor;
import com.google.gson.JsonParseException;
import com.daolab.netkit.connect.executor.RequestQueue;
import com.daolab.netkit.connect.request.MultiRequestBuilder;
import com.daolab.netkit.connect.request.RequestBuilder;
import com.daolab.netkit.connect.response.BaseResult;
import com.daolab.netkit.connect.response.ResponseElement;
import com.daolab.netkit.utils.Accessories;
import com.daolab.netkit.utils.ErrorElement;
import com.daolab.netkit.utils.OnRequestCompletion;
import com.daolab.netkit.utils.SessionProvider;
import com.daolab.daolabplayer.PKDrmParams;
import com.daolab.daolabplayer.PKMediaEntry;
import com.daolab.daolabplayer.PKMediaSource;
import com.daolab.daolabplayer.api.base.model.DaolabDrmPlaybackPluginData;
import com.daolab.daolabplayer.api.phoenix.APIDefines;
import com.daolab.daolabplayer.api.phoenix.model.DaolabPlaybackContext;
import com.daolab.daolabplayer.api.phoenix.services.OttUserService;
import com.daolab.daolabplayer.api.phoenix.services.PhoenixService;
import com.daolab.daolabplayer.mediaproviders.base.BEMediaProvider;
import com.daolab.daolabplayer.mediaproviders.base.FormatsHelper;
import com.daolab.daolabplayer.mediaproviders.base.OnMediaLoadCompletion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.daolab.daolabplayer.PKDrmParams.Scheme.PlayReadyCENC;
import static com.daolab.daolabplayer.PKDrmParams.Scheme.PlayReadyClassic;
import static com.daolab.daolabplayer.PKDrmParams.Scheme.WidevineCENC;
import static com.daolab.daolabplayer.PKDrmParams.Scheme.WidevineClassic;


/**
 * Created by tehilarozin on 27/10/2016.
 */

/*
* usages:
*
* by formats - request will fetch all available source, filter sources response according to requested formats list
*
* by mediaFile ids - request include the requests file ids and will fetch sources for those files only.
*
* mandatory fields: assetId, assetType, contextType
*
*
* */

public class PhoenixMediaProvider extends BEMediaProvider {

    private static final String TAG = "PhoenixMediaProvider";

    private static final boolean EnableEmptyKs = true;

    private MediaAsset mediaAsset;

    private BEResponseListener responseListener;

    private String referrer;

    private class MediaAsset {

        public String assetId;

        public APIDefines.DaolabAssetType assetType;

        public APIDefines.PlaybackContextType contextType;

        public List<String> formats;

        public List<String> mediaFileIds;

        public String protocol;

        public MediaAsset(){
        }

        public boolean hasFormats() {
            return formats != null && formats.size() > 0;
        }

        public boolean hasFiles() {
            return mediaFileIds != null && mediaFileIds.size() > 0;
        }
    }

    //!! add parameter for streamType - catchup/startOver/...

    public PhoenixMediaProvider() {
        super(PhoenixMediaProvider.TAG);
        this.mediaAsset = new MediaAsset();
    }

    /**
     *  NOT MANDATORY! The referrer url, to fetch the data for.
     *
     * @param referrer
     * @return
     */
    public PhoenixMediaProvider setReferrer(String referrer) {
        this.referrer = referrer;
        return this;
    }

    /**
     * MANDATORY! provides the baseUrl and the session token(ks) for the API calls.
     *
     * @param sessionProvider
     * @return
     */
    public PhoenixMediaProvider setSessionProvider(@NonNull SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
        return this;
    }

    /**
     * MANDATORY! the media asset id, to fetch the data for.
     *
     * @param assetId
     * @return
     */
    public PhoenixMediaProvider setAssetId(@NonNull String assetId) {
        this.mediaAsset.assetId = assetId;
        return this;
    }

    /**
     * ESSENTIAL!! defines the playing asset group type
     * Defaults to - {@link APIDefines.DaolabAssetType#Media}
     *
     * @param assetType - can be one of the following types {@link APIDefines.DaolabAssetType}
     * @return
     */
    public PhoenixMediaProvider setAssetType(@NonNull APIDefines.DaolabAssetType assetType) {
        this.mediaAsset.assetType = assetType;
        return this;
    }

    /**
     * ESSENTIAL!! defines the playing context: Trailer, Catchup, Playback etc
     * Defaults to - {@link APIDefines.PlaybackContextType#Playback}
     *
     * @param contextType - can be one of the following types {@link APIDefines.PlaybackContextType}
     * @return
     */
    public PhoenixMediaProvider setContextType(@NonNull APIDefines.PlaybackContextType contextType) {
        this.mediaAsset.contextType = contextType;
        return this;
    }

    /**
     * OPTIONAL
     *
     * @param protocol - the desired protocol (http/https) for the playback sources
     * The default is null, which makes the provider filter by server protocol.
     * @return
     */
    public PhoenixMediaProvider setProtocol(@NonNull @HttpProtocol String protocol) {
        this.mediaAsset.protocol = protocol;
        return this;
    }

    /**
     * OPTIONAL
     * defines which of the sources to consider on {@link PKMediaEntry} creation.
     *
     * @param formats - 1 or more content format definition. can be: Hd, Sd, Download, Trailer etc
     * @return
     */
    public PhoenixMediaProvider setFormats(@NonNull String... formats) {
        this.mediaAsset.formats = new ArrayList<>(Arrays.asList(formats));
        return this;
    }



    /**
     * OPTIONAL - if not available all sources will be fetched
     * Provide a list of media files ids. will be used in the getPlaybackContext API request                                                                                                 .
     *
     * @param mediaFileIds - list of MediaFile ids to narrow sources fetching from API to
     *                     the specific files
     * @return
     */
    public PhoenixMediaProvider setFileIds(@NonNull String... mediaFileIds) {
        this.mediaAsset.mediaFileIds = new ArrayList<>(Arrays.asList(mediaFileIds));
        return this;
    }

    public PhoenixMediaProvider setResponseListener(BEResponseListener responseListener) {
        this.responseListener = responseListener;
        return this;
    }

    /**
     * OPTIONAL
     * Defaults to {@link APIOkRequestsExecutor} implementation.
     *
     * @param executor
     * @return
     */
    public PhoenixMediaProvider setRequestExecutor(@NonNull RequestQueue executor) {
        this.requestsExecutor = executor;
        return this;
    }


    protected Loader factorNewLoader(OnMediaLoadCompletion completion) {
        return new Loader(requestsExecutor, sessionProvider, mediaAsset, completion);
    }


    /**
     * Checks for non empty value on the mandatory parameters.
     *
     * @return - error in case of at least 1 invalid mandatory parameter.
     */
    @Override
    protected ErrorElement validateParams() {
        ErrorElement error = null;

        if (TextUtils.isEmpty(this.mediaAsset.assetId)) {
            error = ErrorElement.BadRequestError.addMessage(": Missing required parameter [assetId]");

        } else {

            //set Defaults if not provided:
            if (mediaAsset.assetType == null) {
                mediaAsset.assetType = APIDefines.DaolabAssetType.Media;
            }
            if (mediaAsset.contextType == null) {
                mediaAsset.contextType = APIDefines.PlaybackContextType.Playback;
            }
        }

        return error;
    }


    class Loader extends BECallableLoader {

        private MediaAsset mediaAsset;


        public Loader(RequestQueue requestsExecutor, SessionProvider sessionProvider, MediaAsset mediaAsset, OnMediaLoadCompletion completion) {
            super(PhoenixMediaProvider.TAG + "#Loader", requestsExecutor, sessionProvider, completion);

            this.mediaAsset = mediaAsset;

            PKLog.v(TAG, loadId + ": construct new Loader");
        }

        @Override
        protected ErrorElement validateKs(String ks) { // enable anonymous session creation
            return EnableEmptyKs || !TextUtils.isEmpty(ks) ? null :
                    ErrorElement.BadRequestError.message(ErrorElement.BadRequestError + ": SessionProvider should provide a valid KS token");
        }


        private RequestBuilder getPlaybackContextRequest(String baseUrl, String ks, String referrer, MediaAsset mediaAsset) {
            AssetService.DaolabPlaybackContextOptions contextOptions = new AssetService.DaolabPlaybackContextOptions(mediaAsset.contextType);
            if (mediaAsset.mediaFileIds != null) { // else - will fetch all available sources
                contextOptions.setMediaFileIds(mediaAsset.mediaFileIds);
            }

            // protocol will be added only if no protocol was give or http/https was set
            // for All no filter will be done via protocol and it will not be added to the request.
            if (mediaAsset.protocol == null) {
                contextOptions.setMediaProtocol(Uri.parse(baseUrl).getScheme());
            } else if (!HttpProtocol.All.equals(mediaAsset.protocol)) {
                contextOptions.setMediaProtocol(mediaAsset.protocol);
            }

            if (!TextUtils.isEmpty(referrer)) {
                contextOptions.setReferrer(referrer);
            }


            return AssetService.getPlaybackContext(baseUrl, ks, mediaAsset.assetId,
                    mediaAsset.assetType, contextOptions);
        }

        private RequestBuilder getRemoteRequest(String baseUrl, String ks, String referrer, MediaAsset mediaAsset) {

            if (TextUtils.isEmpty(ks)) {
                MultiRequestBuilder multiRequestBuilder = (MultiRequestBuilder) PhoenixService.getMultirequest(baseUrl, ks)
                        .tag("asset-play-data-multireq");
                String multiReqKs = "{1:result:ks}";
                return multiRequestBuilder.add(OttUserService.anonymousLogin(baseUrl, sessionProvider.partnerId(), null),
                        getPlaybackContextRequest(baseUrl, multiReqKs, referrer, mediaAsset));
            }

            return getPlaybackContextRequest(baseUrl, ks, referrer, mediaAsset);
        }

        /**
         * Builds and passes to the executor, the Asset info fetching request.
         *
         * @param ks
         * @throws InterruptedException
         */
        @Override
        protected void requestRemote(String ks) throws InterruptedException {
            final RequestBuilder requestBuilder = getRemoteRequest(getApiBaseUrl(), ks, referrer, mediaAsset)
                    .completion(new OnRequestCompletion() {
                        @Override
                        public void onComplete(ResponseElement response) {
                            PKLog.v(TAG, loadId + ": got response to [" + loadReq + "]");
                            loadReq = null;

                            try {
                                onAssetGetResponse(response);

                            } catch (InterruptedException e) {
                                interrupted();
                            }
                        }
                    });

            synchronized (syncObject) {
                loadReq = requestQueue.queue(requestBuilder.build());
                PKLog.d(TAG, loadId + ": request queued for execution [" + loadReq + "]");
            }

            if(!isCanceled()) {
                PKLog.v(TAG, loadId + " set waitCompletion");
                waitCompletion();
            } else {
                PKLog.v(TAG, loadId + " was canceled.");
            }
            PKLog.v(TAG, loadId + ": requestRemote wait released");
        }

        private String getApiBaseUrl() {
            return sessionProvider.baseUrl();
        }

        /**
         * Parse and create a {@link PKMediaEntry} object from the API response.
         *
         * @param response
         * @throws InterruptedException
         */
        private void onAssetGetResponse(final ResponseElement response) throws InterruptedException {
            ErrorElement error = null;
            PKMediaEntry mediaEntry = null;

            if (isCanceled()) {
                PKLog.v(TAG, loadId + ": i am canceled, exit response parsing ");
                return;
            }

            if(responseListener != null){
                responseListener.onResponse(response);
            }

            if (response != null && response.isSuccess()) {
                DaolabMediaAsset asset = null;

                try {
                    //**************************

                /* ways to parse the AssetInfo from response string:

                    1. <T> T PhoenixParser.parseObject: parse json string to a single object, according to a specific type - returns an object of the specific type
                            asset = PhoenixParser.parseObject(response.getResponse(), DaolabMediaAsset.class);

                    2. Object PhoenixParser.parse(String response, Class...types): parse json string according to 1 or more types (dynamic types array) - returns Object since can
                       be single or an array of objects. cast is needed, can be used for multiple response
                            asset = (DaolabMediaAsset) PhoenixParser.parse(response.getResponse(), DaolabMediaAsset.class);

                        in case of an error - the error will be passed over the returned object (should extend BaseResult) */

                    //*************************

                    PKLog.d(TAG, loadId + ": parsing response  [" + Loader.this.toString() + "]");
                    /* 3. <T> T PhoenixParser.parse(String response): parse json string to an object of dynamically parsed type.
                       type defined by the value of "objectType" property provided in the response objects, if type wasn't found or in
                       case of error object in the response, will be parsed to BaseResult object (error if occurred will be accessible from this object)*/

                    Object parsedResponses = PhoenixParser.parse(response.getResponse());
                    BaseResult playbackContextResult = parsedResponses instanceof BaseResult ? (BaseResult) parsedResponses : ((List<BaseResult>) parsedResponses).get(1);

                    if (playbackContextResult.error != null) {
                        //error = ErrorElement.LoadError.message("failed to get multirequest responses on load request for asset "+mediaAsset.assetId);
                        error = PhoenixErrorHelper.getErrorElement(playbackContextResult.error); // get predefined error if exists for this error code

                    } else {

                        DaolabPlaybackContext daolabPlaybackContext = (DaolabPlaybackContext) playbackContextResult;

                        if ((error = daolabPlaybackContext.hasError()) == null) { // check for error or unauthorized content

                            mediaEntry = ProviderParser.getMedia(mediaAsset.assetId,
                                    mediaAsset.formats != null ? mediaAsset.formats : mediaAsset.mediaFileIds,
                                    daolabPlaybackContext.getSources());

                            if (mediaEntry.getSources().size() == 0) { // makes sure there are sources available for play
                                error = ErrorElement.NotFound.message("Content can't be played due to lack of sources");
                            }
                        }
                    }
                } catch (JsonParseException | InvalidParameterException ex) {
                    error = ErrorElement.LoadError.message("failed parsing remote response: " + ex.getMessage());
                } catch (IndexOutOfBoundsException ex) {
                    error = ErrorElement.GeneralError.message("responses list doesn't contain the expected responses number: " + ex.getMessage());
                }

            } else {
                error = response != null && response.getError() != null ? response.getError() : ErrorElement.LoadError;
            }

            PKLog.i(TAG, loadId + ": load operation " + (isCanceled() ? "canceled" : "finished with " + (error == null ? "success" : "failure")));

            if (!isCanceled() && completion != null) {
                completion.onComplete(Accessories.buildResult(mediaEntry, error));
            }

            PKLog.w(TAG, loadId + " media load finished, callback passed...notifyCompletion");
            notifyCompletion();

        }
    }


    static class ProviderParser {

        public static PKMediaEntry getMedia(String assetId, final List<String> sourcesFilter, ArrayList<DaolabPlaybackSource> playbackSources) {

            PKMediaEntry mediaEntry = new PKMediaEntry();
            mediaEntry.setId("" + assetId);
            mediaEntry.setName(null);

            // until the response will be delivered in the right order:
            playbackSourcesSort(sourcesFilter, playbackSources);

            ArrayList<PKMediaSource> sources = new ArrayList<>();

            long maxDuration = 0;

            if (playbackSources != null) {

                // if provided, only the "formats" matching MediaFiles should be parsed and added to the PKMediaEntry media sources
                for (DaolabPlaybackSource playbackSource : playbackSources) {

                    boolean inSourceFilter = sourcesFilter != null &&
                            (sourcesFilter.contains(playbackSource.getType()) ||
                                    sourcesFilter.contains(playbackSource.getId()+""));

                    if (sourcesFilter != null && !inSourceFilter) { // if specific formats/fileIds were requested, only those will be added to the sources.
                        continue;
                    }

                    PKMediaFormat mediaFormat = FormatsHelper.getPKMediaFormat(playbackSource.getFormat(), playbackSource.hasDrmData());

                    if (mediaFormat == null) {
                        continue;
                    }

                    PKMediaSource pkMediaSource = new PKMediaSource()
                            .setId(playbackSource.getId() + "")
                            .setUrl(playbackSource.getUrl())
                            .setMediaFormat(mediaFormat);

                    List<DaolabDrmPlaybackPluginData> drmData = playbackSource.getDrmData();
                    if (drmData != null) {
                        List<PKDrmParams> drmParams = new ArrayList<>();
                        for (DaolabDrmPlaybackPluginData drm : drmData) {
                            drmParams.add(new PKDrmParams(drm.getLicenseURL(), getScheme(drm.getScheme())));
                        }
                        pkMediaSource.setDrmData(drmParams);
                    }

                    sources.add(pkMediaSource);
                    maxDuration = Math.max(playbackSource.getDuration(), maxDuration);
                }
            }
            return mediaEntry.setDuration(maxDuration).setSources(sources).setMediaType(MediaTypeConverter.toMediaEntryType(""));
        }

        //TODO: check why we get all sources while we asked for 4 specific formats

        // needed to sort the playback source result to be in the same order as in the requested list.
        private static void playbackSourcesSort(final List<String> sourcesFilter, ArrayList<DaolabPlaybackSource> playbackSources) {
            Collections.sort(playbackSources, new Comparator<DaolabPlaybackSource>() {
                @Override
                public int compare(DaolabPlaybackSource o1, DaolabPlaybackSource o2) {

                    int valueIndex1 = -1;
                    int valueIndex2 = -1;
                    if(sourcesFilter != null) {
                        valueIndex1 = sourcesFilter.indexOf(o1.getType());
                        if (valueIndex1 == -1) {
                            valueIndex1 = sourcesFilter.indexOf(o1.getId() + "");
                            valueIndex2 = sourcesFilter.indexOf(o2.getId() + "");
                        } else {
                            valueIndex2 = sourcesFilter.indexOf(o2.getType());
                        }
                    }
                    return valueIndex1 - valueIndex2;
                }
            });
        }
    }

    public static PKDrmParams.Scheme getScheme(String scheme) {

        switch (scheme) {
            case "WIDEVINE_CENC":
                return WidevineCENC;
            case "PLAYREADY_CENC":
                return PlayReadyCENC;
            case "WIDEVINE":
                return WidevineClassic;
            case "PLAYREADY":
                return PlayReadyClassic;
            default:
                return null;
        }
    }

    static class MediaTypeConverter {

        public static PKMediaEntry.MediaEntryType toMediaEntryType(String mediaType) {
            switch (mediaType) {
                default:
                    return PKMediaEntry.MediaEntryType.Unknown;
            }
        }
    }

    @StringDef({HttpProtocol.Http, HttpProtocol.Https, HttpProtocol.All})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HttpProtocol {
        public static final String Http = "http";       // only http sources
        public static final String Https = "https";     // only https sources
        public static final String All = "all";         // do not filter by protocol
    }

}
