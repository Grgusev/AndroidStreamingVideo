

package com.daolab.daolabplayer.mediaproviders.mock;

import android.content.Context;

import com.daolab.daolabplayer.MediaEntryProvider;
import com.daolab.daolabplayer.PKLog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.daolab.netkit.connect.response.ResultElement;
import com.daolab.netkit.utils.CallableLoader;
import com.daolab.netkit.utils.ErrorElement;
import com.daolab.netkit.utils.OnCompletion;
import com.daolab.daolabplayer.PKMediaEntry;
import com.daolab.daolabplayer.PKMediaFormat;
import com.daolab.daolabplayer.PKMediaSource;
import com.daolab.daolabplayer.mediaproviders.base.OnMediaLoadCompletion;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.daolab.daolabplayer.PKMediaEntry.MediaEntryType.Unknown;

/**
 * Created by tehilarozin on 06/11/2016.
 */

public class MockMediaProvider implements MediaEntryProvider {

    public static final String TAG = "MockMediaProvider";

    private JsonObject inputJson;
    private String inputFile;
    private String id;
    private Context context;

    private Future<Void> currentLoad;
    private ExecutorService exSvc;
    private Object syncObject = new Object();

    public MockMediaProvider(JsonObject inputJson, String id) {
        this.inputJson = inputJson;
        this.id = id;
        exSvc = Executors.newSingleThreadExecutor();//?? do i need this?
    }

    public MockMediaProvider(String inputFile, Context context, String id) {
        this.inputFile = inputFile;
        this.id = id;
        this.context = context;
        exSvc = Executors.newSingleThreadExecutor();//?? do i need this?
    }

    public MockMediaProvider id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public void load(final OnMediaLoadCompletion completion) {
        synchronized (syncObject) {
            cancel();
            currentLoad = exSvc.submit(new MockMediaProvider.Loader(completion));
        }
    }

    @Override
    public synchronized void cancel() {
        if (currentLoad != null && !currentLoad.isDone() && !currentLoad.isCancelled()) {
            PKLog.i(TAG, "has running load operation, canceling current load operation - " + currentLoad.toString());
            currentLoad.cancel(true);
        } else {
            PKLog.i(TAG, (currentLoad != null ? currentLoad.toString() : "") + ": no need to cancel operation," + (currentLoad == null ? "operation is null" : (currentLoad.isDone() ? "operation done" : "operation canceled")));
        }
    }

    /**
     * reads entries Json input from a file. the data is "saved" in the inputJson member.
     * next load on the same {@link MockMediaProvider} object will not read the file again, but
     * use the inputJson member, and search for the "id" entry.
     *
     * @return - The Media entry identified by "id"
     * @throws IOException
     */
    private PKMediaEntry getFromFile() throws IOException {
        JsonParser parser = new JsonParser();

        if(context != null){
            JsonReader jsonReader = new JsonReader(new InputStreamReader(context.getAssets().open(inputFile)));
            inputJson = parser.parse(jsonReader).getAsJsonObject();
        } else {
            inputJson = parser.parse(new FileReader(inputFile)).getAsJsonObject();
        }

        return getFromJson(inputJson);
    }

    private PKMediaEntry getFromJson(JsonObject data)  throws JsonSyntaxException{
        if (data.has(id)) { // data holds multiple entry objects
            return MockMediaParser.parseMedia(data.getAsJsonObject(id));

        } else if (data.has("id") && data.getAsJsonPrimitive("id").getAsString().equals(id)) { // data is the actual inputJson object
            return MockMediaParser.parseMedia(data);
        }
        return null;
    }


    static class MockMediaParser {

        static PKMediaEntry parseMedia(JsonObject mediaObject) throws JsonSyntaxException {
            PKMediaEntry mediaEntry =  new Gson().fromJson(mediaObject, PKMediaEntry.class);
            if (mediaEntry.getMediaType() == null) {
                mediaEntry.setMediaType(Unknown);
            }
            List<PKMediaSource> mediaSources = mediaEntry.getSources();
            for (PKMediaSource mediaSource : mediaSources) {
                mediaSource.setMediaFormat(PKMediaFormat.valueOfUrl(mediaSource.getUrl()));
            }
            return mediaEntry;
        }



    }


    class Loader extends CallableLoader<Void> {

        ErrorElement error = null;
        PKMediaEntry mediaEntry = null;

        Loader(OnCompletion completion) {
            super(MockMediaProvider.TAG, completion);
        }


        @Override
        protected Void load() throws InterruptedException {
            //parse Json input to MediaEntry
            try {
                mediaEntry = inputJson != null ? getFromJson(inputJson) : getFromFile();

            } catch (IOException | JsonSyntaxException ex) {
                error = ErrorElement.LoadError;
                mediaEntry = null;
            }

            if (mediaEntry == null) {
                error = ErrorElement.NotFound;
            }

            if (completion != null) {
                final ErrorElement finalError = error;
                final PKMediaEntry finalEntry = mediaEntry;
                completion.onComplete(new ResultElement<PKMediaEntry>() {

                    @Override
                    public PKMediaEntry getResponse() {
                        return finalEntry;
                    }

                    @Override
                    public boolean isSuccess() {
                        return finalEntry != null;
                    }

                    @Override
                    public ErrorElement getError() {
                        return finalError;
                    }
                });
            }
            return null;
        }

        @Override
        protected synchronized void cancel() {

        }
    }
}
