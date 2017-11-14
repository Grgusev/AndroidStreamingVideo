

package com.daolab.daolabplayer.mediaproviders.ovp;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.daolab.daolabplayer.PKDrmParams;
import com.daolab.daolabplayer.PKLog;
import com.daolab.daolabplayer.PKMediaEntry;
import com.daolab.daolabplayer.PKMediaFormat;
import com.daolab.daolabplayer.PKMediaSource;
import com.daolab.daolabplayer.api.ovp.DaolabOvpErrorHelper;
import com.daolab.daolabplayer.api.ovp.DaolabOvpParser;
import com.daolab.daolabplayer.api.ovp.OvpConfigs;
import com.daolab.daolabplayer.api.ovp.model.DaolabBaseEntryListResponse;
import com.daolab.daolabplayer.api.ovp.model.DaolabEntryContextDataResult;
import com.daolab.daolabplayer.api.ovp.model.DaolabFlavorAsset;
import com.daolab.daolabplayer.api.ovp.model.FlavorAssetsFilter;
import com.daolab.daolabplayer.api.ovp.model.DaolabEntryType;
import com.daolab.daolabplayer.api.ovp.model.DaolabMediaEntry;
import com.daolab.daolabplayer.api.ovp.model.DaolabMetadata;
import com.daolab.daolabplayer.api.ovp.model.DaolabMetadataListResponse;
import com.daolab.daolabplayer.api.ovp.model.DaolabPlaybackContext;
import com.daolab.daolabplayer.api.ovp.model.DaolabPlaybackSource;
import com.daolab.daolabplayer.api.ovp.services.BaseEntryService;
import com.daolab.daolabplayer.api.ovp.services.MetaDataService;
import com.daolab.daolabplayer.mediaproviders.base.BECallableLoader;
import com.daolab.daolabplayer.mediaproviders.base.FormatsHelper;
import com.daolab.daolabplayer.mediaproviders.base.OnMediaLoadCompletion;
import com.daolab.netkit.connect.executor.APIOkRequestsExecutor;
import com.google.gson.JsonSyntaxException;
import com.daolab.netkit.connect.executor.RequestQueue;
import com.daolab.netkit.connect.request.MultiRequestBuilder;
import com.daolab.netkit.connect.request.RequestBuilder;
import com.daolab.netkit.connect.response.BaseResult;
import com.daolab.netkit.connect.response.ResponseElement;
import com.daolab.netkit.utils.Accessories;
import com.daolab.netkit.utils.ErrorElement;
import com.daolab.netkit.utils.OnRequestCompletion;
import com.daolab.netkit.utils.SessionProvider;
import com.daolab.daolabplayer.api.base.model.DaolabDrmPlaybackPluginData;
import com.daolab.daolabplayer.api.ovp.services.OvpService;
import com.daolab.daolabplayer.api.ovp.services.OvpSessionService;
import com.daolab.daolabplayer.mediaproviders.base.BEMediaProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by tehilarozin on 30/10/2016.
 */

public class DaolabOvpMediaProvider extends BEMediaProvider {

    private static final String TAG = DaolabOvpMediaProvider.class.getSimpleName();
    public static final boolean CanBeEmpty = true;

    private String entryId;
    private String uiConfId;
    private String referrer;

    private int maxBitrate;
    private Map<String, Object> flavorsFilter;


    public DaolabOvpMediaProvider() {
        super(DaolabOvpMediaProvider.TAG);
    }

    /**
     * MANDATORY! provides the baseUrl and the session token(ks) for the API calls.
     *
     * @param sessionProvider
     * @return
     */
    public DaolabOvpMediaProvider setSessionProvider(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
        return this;
    }

    /**
     *  NOT MANDATORY! The referrer url, to fetch the data for.
     *
     * @param referrer
     * @return
     */
    public DaolabOvpMediaProvider setReferrer(String referrer) {
        this.referrer = referrer;
        return this;
    }

    /**
     * MANDATORY! the entry id, to fetch the data for.
     *
     * @param entryId
     * @return
     */
    public DaolabOvpMediaProvider setEntryId(String entryId) {
        this.entryId = entryId;
        return this;
    }
    /**
     * optional parameter.
     * Defaults to {@link APIOkRequestsExecutor} implementation.
     *
     * @param executor
     * @return
     */
    public DaolabOvpMediaProvider setRequestExecutor(RequestQueue executor) {
        this.requestsExecutor = executor;
        return this;
    }

    /**
     * optional parameter
     * will be used in media sources url
     *
     * @param uiConfId
     * @return
     */
    public DaolabOvpMediaProvider setUiConfId(String uiConfId) {
        this.uiConfId = uiConfId;
        return this;
    }

    @Override
    protected Loader factorNewLoader(OnMediaLoadCompletion completion) {
        return new Loader(requestsExecutor, sessionProvider, entryId, uiConfId, referrer, completion);
    }

    @Override
    protected ErrorElement validateParams() {
        return TextUtils.isEmpty(this.entryId) ?
                ErrorElement.BadRequestError.message(ErrorElement.BadRequestError + ": Missing required parameters, entryId") :
                null;
    }


    class Loader extends BECallableLoader {

        private String entryId;
        private String uiConfId;
        private String referrer;

        Loader(RequestQueue requestsExecutor, SessionProvider sessionProvider, String entryId, String uiConfId, String referrer, OnMediaLoadCompletion completion) {
            super(DaolabOvpMediaProvider.TAG + "#Loader", requestsExecutor, sessionProvider, completion);

            this.entryId = entryId;
            this.uiConfId = uiConfId;
            this.referrer = referrer;

            PKLog.v(TAG, loadId + ": construct new Loader");
        }

        @Override
        protected ErrorElement validateKs(String ks) {
            if (TextUtils.isEmpty(ks)) {
                if (CanBeEmpty) {
                    PKLog.w(TAG, "provided ks is empty, Anonymous session will be used.");
                } else {
                    return ErrorElement.BadRequestError.message(ErrorElement.BadRequestError + ": SessionProvider should provide a valid KS token");
                }
            }
            return null;
        }

        private RequestBuilder getEntryInfo(String baseUrl, String ks, int partnerId, String entryId, String referrer) {
            MultiRequestBuilder multiRequestBuilder = (MultiRequestBuilder) OvpService.getMultirequest(baseUrl, ks, partnerId)
                    .tag("entry-info-multireq");

            if (TextUtils.isEmpty(ks)) {
                multiRequestBuilder.add(OvpSessionService.anonymousSession(baseUrl, partnerId));

                ks = "{1:result:ks}";
            }

            return multiRequestBuilder.add(BaseEntryService.list(baseUrl, ks, entryId),
                    BaseEntryService.getPlaybackContext(baseUrl, ks, entryId, referrer),
                    MetaDataService.list(baseUrl, ks, entryId));
        }

        /**
         * Builds and passes to the executor, the multirequest for entry info and playback info fetching.
         *
         * @param ks
         * @throws InterruptedException
         */
        @Override
        protected void requestRemote(final String ks) throws InterruptedException {
            final RequestBuilder entryRequest = getEntryInfo(getApiBaseUrl(), ks, sessionProvider.partnerId(), entryId, referrer)
                    .completion(new OnRequestCompletion() {
                        @Override
                        public void onComplete(ResponseElement response) {
                            PKLog.v(TAG, loadId + ": got response to [" + loadReq + "]" + " isCanceled = " + isCanceled);
                            loadReq = null;

                            try {
                                onEntryInfoMultiResponse(ks, response, (OnMediaLoadCompletion) completion);
                            } catch (InterruptedException e) {
                                interrupted();
                            }
                        }
                    });

            synchronized (syncObject) {
                loadReq = requestQueue.queue(entryRequest.build());
                PKLog.d(TAG, loadId + ": request queued for execution [" + loadReq + "]");
            }

            if(!isCanceled()) {
                waitCompletion();
            }
        }

        private String getApiBaseUrl() {
            String sep = sessionProvider.baseUrl().endsWith("/") ? "" : "/";
            return sessionProvider.baseUrl() + sep + OvpConfigs.ApiPrefix;
        }

        /**
         * Parse and create a {@link PKMediaEntry} object from the multirequest call sent to the BE.
         *
         * @param ks
         * @param response
         * @param completion - A callback to pass the constructed {@link PKMediaEntry} object on.
         * @throws InterruptedException - in case the load operation canceled.
         */
        private void onEntryInfoMultiResponse(String ks, ResponseElement response, OnMediaLoadCompletion completion) throws InterruptedException {
            ErrorElement error = null;
            PKMediaEntry mediaEntry = null;


            if (isCanceled()) {
                PKLog.v(TAG, loadId + ": i am canceled, exit response parsing ");
                return;
            }

            if (response != null && response.isSuccess()) {

                try {
                    //parse multi response from request response

                /* in this option, in case of error response, the type of the parsed response will be BaseResult, and not the expected object type,
                   since we parse the type dynamically from the result and we get "DaolabAPIException" objectType */
                    List<BaseResult> responses = DaolabOvpParser.parse(response.getResponse());//, TextUtils.isEmpty(sessionProvider.getSessionToken()) ? 1 : 0, DaolabBaseEntryListResponse.class, DaolabEntryContextDataResult.class);
                /* in this option, responses types will always be as expected, and in case of an error, the error can be reached from the typed object, since
                * all response objects should extend BaseResult */
                    //  List<BaseResult> responses = (List<BaseResult>) DaolabOvpParser.parse(response.getResponse(), DaolabBaseEntryListResponse.class, DaolabEntryContextDataResult.class);
                    if (responses.size() == 0) {
                        error = ErrorElement.LoadError.message("failed to get responses on load requests");

                    } else {
                        // indexes should match the order of requests sent to the server.
                        int entryListResponseIdx = responses.size() > 3 ? 1 : 0;
                        int playbackResponseIdx = entryListResponseIdx + 1;
                        int metadataResponseIdx = playbackResponseIdx + 1;

                        if (responses.get(entryListResponseIdx).error != null) {
                            error = responses.get(entryListResponseIdx).error.addMessage("baseEntry/list request failed");
                        }
                        if (error == null && responses.get(playbackResponseIdx).error != null) {
                            error = responses.get(playbackResponseIdx).error.addMessage("baseEntry/getPlaybackContext request failed");
                        }

                        if (error == null) {
                            DaolabPlaybackContext daolabPlaybackContext = (DaolabPlaybackContext) responses.get(playbackResponseIdx);
                            DaolabMetadataListResponse metadataList = (DaolabMetadataListResponse) responses.get(metadataResponseIdx);

                            if ((error = daolabPlaybackContext.hasError()) == null) { // check for error or unauthorized content
                                mediaEntry = ProviderParser.getMediaEntry(sessionProvider.baseUrl(), ks, sessionProvider.partnerId() + "", uiConfId,
                                        ((DaolabBaseEntryListResponse) responses.get(entryListResponseIdx)).objects.get(0), daolabPlaybackContext, metadataList);

                                if (mediaEntry.getSources().size() == 0) { // makes sure there are sources available for play
                                    error = DaolabOvpErrorHelper.getErrorElement("NoFilesFound");
                                }
                            }
                        }
                    }
                } catch (JsonSyntaxException | InvalidParameterException ex) {
                    error = ErrorElement.LoadError.message("failed to create PKMediaEntry: " + ex.getMessage());
                } catch (IndexOutOfBoundsException ex) {
                    error = ErrorElement.GeneralError.message("responses list doesn't contain the expected responses number: " + ex.getMessage());
                }

            } else {
                error = response != null && response.getError() != null ? response.getError() : ErrorElement.LoadError;
            }

            PKLog.v(TAG, loadId + ": load operation " + (isCanceled() ? "canceled" : "finished with " + (error == null ? "success" : "failure: " + error)));


            if (!isCanceled() && completion != null) {
                completion.onComplete(Accessories.buildResult(mediaEntry, error));
            }

            notifyCompletion();

        }

    }

    private static class ProviderParser {

        /**
         * creates {@link PKMediaEntry} from entry's data and contextData
         *
         * @param baseUrl
         * @param entry
         * @param playbackContext
         * @return (in case of restriction on maxbitrate, filtering should be done by considering the flavors provided to the
         *source- if none meets the restriction, source should not be added to the mediaEntrys sources.)
         */
        public static PKMediaEntry getMediaEntry(String baseUrl, String ks, String partnerId, String uiConfId, DaolabMediaEntry entry,
                                                 DaolabPlaybackContext playbackContext, DaolabMetadataListResponse metadataList) throws InvalidParameterException {

            PKMediaEntry mediaEntry = new PKMediaEntry();
            ArrayList<DaolabPlaybackSource> daolabSources = playbackContext.getSources();
            List<PKMediaSource> sources;


            if (daolabSources != null && daolabSources.size() > 0) {
                sources = parseFromSources(baseUrl, ks, partnerId, uiConfId, entry, playbackContext);
            } else {
                sources = new ArrayList<>();
            }
            /*
            in case we need default sources creation:
            else {
                PKLog.e(TAG, "failed to receive sources to play");
                //throw new InvalidParameterException("Could not create sources for media entry");
                sources = parseFromFlavors(ks, partnerId, uiConfId, entry, playbackContext);
            }*/

            Map<String, String> metadata = parseMetadata(metadataList);

            return mediaEntry.setId(entry.getId()).setSources(sources)
                    .setDuration(entry.getMsDuration()).setMetadata(metadata)
                    .setName(entry.getName())
                    .setMediaType(MediaTypeConverter.toMediaEntryType(entry.getType()));
        }

        private static Map<String, String> parseMetadata(DaolabMetadataListResponse metadataList) {
            Map<String, String> metadata = new HashMap<>();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException("Failed to create DocumentBuilder", e);
            }

            if (metadataList != null && metadataList.objects != null && metadataList.objects.size() > 0) {
                for (DaolabMetadata metadataItem : metadataList.objects) {
                    extractMetadata(builder, metadataItem.xml, metadata);
                }
            }

            return metadata;
        }

        private static void extractMetadata(DocumentBuilder builder, String xml, Map<String, String> metadataMap) {
            InputSource is = new InputSource(new StringReader(xml));
            Document doc;
            try {
                doc = builder.parse(is);
            } catch (SAXException | IOException e) {
                Log.e(TAG, "extractMetadata: XML parsing failed", e);
                return;
            }

            Node rootElement = doc.getDocumentElement();
            if ("metadata".equals(rootElement.getNodeName())) {
                for (Node node = rootElement.getFirstChild(); node != null; node = node.getNextSibling()) {
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Node text = node.getFirstChild();
                        if (text != null) {
                            metadataMap.put(node.getNodeName(), text.getNodeValue());
                        }
                    }
                }
            }
        }

        /**
         * Parse PKMediaSource objects from the getPlaybackContext API response.
         * Goes over the sources and creates for each supported source (supported format) a correlating
         * PKMediaSource item, initiate with the relevant data.
         *
         * @param baseUrl         - baseUrl for the playing source construction
         * @param ks              - if not empty, will be added to the playing url path
         * @param partnerId
         * @param uiConfId        - if not empty, will be added to the playing url path
         * @param entry
         * @param playbackContext - the response object of the "baseEntry/getPlaybackContext" API.
         * @return - list of PKMediaSource created from sources list
         */
        @NonNull
        private static List<PKMediaSource> parseFromSources(String baseUrl, String ks, String partnerId, String uiConfId, DaolabMediaEntry entry, DaolabPlaybackContext playbackContext) {
            ArrayList<PKMediaSource> sources = new ArrayList<>();

            //-> create PKMediaSource-s according to sources list provided in "getContextData" response
            for (DaolabPlaybackSource playbackSource : playbackContext.getSources()) {

                if (!FormatsHelper.validateFormat(playbackSource)) { // only validated formats will be added to the sources.
                    continue;
                }

                String playUrl = null;
                PKMediaFormat mediaFormat = FormatsHelper.getPKMediaFormat(playbackSource.getFormat(), playbackSource.hasDrmData());

                // in case playbackSource doesn't have flavors we don't need to build the url and we'll use the provided one.
                if (playbackSource.hasFlavorIds()) {

                    String baseProtocol = null;
                    try {
                        baseProtocol = new URL(baseUrl).getProtocol();

                    } catch (MalformedURLException e) {
                        PKLog.e(TAG, "Provided base url is wrong");
                        baseProtocol = OvpConfigs.DefaultHttpProtocol;
                    }

                    PlaySourceUrlBuilder playUrlBuilder = new PlaySourceUrlBuilder()
                            .setBaseUrl(baseUrl)
                            .setEntryId(entry.getId())
                            .setFlavorIds(playbackSource.getFlavorIds())
                            .setFormat(playbackSource.getFormat())
                            .setKs(ks)
                            .setPartnerId(partnerId)
                            .setUiConfId(uiConfId)
                            .setProtocol(playbackSource.getProtocol(baseProtocol)); //get protocol from base url

                    String extension = "";
                    //-> find out what should be the extension: if playbackSource format doesn't have mapped value, mediaFormat is null,
                    //->  the extension will be fetched from the flavorAssets.
                    if (mediaFormat == null) {
                        // filter the flavors that the playbackSource supports
                        List<DaolabFlavorAsset> flavorAssets = FlavorAssetsFilter.filter(playbackContext.getFlavorAssets(), "id", playbackSource.getFlavorIdsList());
                        if (flavorAssets.size() > 0) {
                            extension = flavorAssets.get(0).getFileExt();
                        }
                    } else {
                        extension = mediaFormat.pathExt;
                    }

                    playUrlBuilder.setExtension(extension);

                    playUrl = playUrlBuilder.build();

                } else {
                    playUrl = playbackSource.getUrl();
                }

                if (playUrl == null) {
                    PKLog.w(TAG, "failed to create play url from source, discarding source:" + (entry.getId() + "_" + playbackSource.getDeliveryProfileId()) + ", " + playbackSource.getFormat());
                    continue;
                }

                PKMediaSource pkMediaSource = new PKMediaSource().setUrl(playUrl).setId(entry.getId() + "_" + playbackSource.getDeliveryProfileId()).setMediaFormat(mediaFormat);
                //-> sources with multiple drm data are split to PKMediaSource per drm
                List<DaolabDrmPlaybackPluginData> drmData = playbackSource.getDrmData();
                if (drmData != null) {
                    List<PKDrmParams> drmParams = new ArrayList<>();
                    for (DaolabDrmPlaybackPluginData drm : drmData) {
                        drmParams.add(new PKDrmParams(drm.getLicenseURL(), getScheme(drm.getScheme())));
                    }
                    pkMediaSource.setDrmData(drmParams);
                }

                sources.add(pkMediaSource);

            }

            return sources;
        }

        @NonNull
        static List<PKMediaSource> parseFromFlavors(String ks, String partnerId, String uiConfId, DaolabMediaEntry entry, DaolabEntryContextDataResult contextData) {

            ArrayList<PKMediaSource> sources = new ArrayList<>();

            if (contextData != null) {
                //-> filter a list for flavors correspond to the list of "flavorParamsId"s received on the entry data response.
                List<DaolabFlavorAsset> matchingFlavorAssets = FlavorAssetsFilter.filter(contextData.getFlavorAssets(), "flavorParamsId", entry.getFlavorParamsIdsList());

                //-> construct a string of "ids" from the filtered DaolabFlavorAsset list.
                StringBuilder flavorIds = new StringBuilder(matchingFlavorAssets.size() > 0 ? matchingFlavorAssets.get(0).getId() : "");
                for (int i = 1; i < matchingFlavorAssets.size(); i++) {
                    flavorIds.append(",").append(matchingFlavorAssets.get(i).getId());
                }

                if (flavorIds.length() > 0) {
                    //-> create PKMediaSource for every predefine extension:
                    //Collection<PKMediaFormat> extensions = FormatsHelper.getSupportedExtensions();

                    for (Map.Entry<FormatsHelper.StreamFormat, PKMediaFormat> mediaFormatEntry : FormatsHelper.getSupportedFormats().entrySet()/*extensions*/) {
                        String formatName = mediaFormatEntry.getKey().formatName;//FormatsHelper.getFormatNameByMediaFormat(mediaFormat);
                        String playUrl = new PlaySourceUrlBuilder()
                                .setEntryId(entry.getId())
                                .setFlavorIds(flavorIds.toString())
                                .setKs(ks)
                                .setPartnerId(partnerId)
                                .setUiConfId(uiConfId)
                                .setExtension(mediaFormatEntry.getValue().pathExt)
                                .setFormat(formatName).build();

                        PKMediaSource mediaSource = new PKMediaSource().setId(entry.getId() + "_" + mediaFormatEntry.getValue().pathExt).setMediaFormat(mediaFormatEntry.getValue());
                        mediaSource.setUrl(playUrl);
                        sources.add(mediaSource);
                    }
                }
            }

            return sources;
        }

    }


    public static class MediaTypeConverter {

        public static PKMediaEntry.MediaEntryType toMediaEntryType(DaolabEntryType type) {
            switch (type) {
                case MEDIA_CLIP:
                    return PKMediaEntry.MediaEntryType.Vod;
                case LIVE_STREAM:
                    return PKMediaEntry.MediaEntryType.Live;
                default:
                    return PKMediaEntry.MediaEntryType.Unknown;
            }
        }
    }

    public static PKDrmParams.Scheme getScheme(String name) {

        switch (name) {
            case "drm.WIDEVINE_CENC":
                return PKDrmParams.Scheme.WidevineCENC;
            case "drm.PLAYREADY_CENC":
                return PKDrmParams.Scheme.PlayReadyCENC;
            case "widevine.WIDEVINE":
                return PKDrmParams.Scheme.WidevineClassic;
            default:
                return null;
        }
    }

}

