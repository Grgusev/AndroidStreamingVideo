

package com.daolab.daolabplayer.mediaproviders.base;

import android.support.annotation.NonNull;

import com.daolab.daolabplayer.MediaEntryProvider;
import com.daolab.daolabplayer.PKLog;
import com.daolab.daolabplayer.PKMediaEntry;
import com.daolab.netkit.connect.executor.APIOkRequestsExecutor;
import com.daolab.netkit.connect.executor.RequestQueue;
import com.daolab.netkit.utils.Accessories;
import com.daolab.netkit.utils.ErrorElement;
import com.daolab.netkit.utils.SessionProvider;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by tehilarozin on 06/12/2016.
 */

public abstract class BEMediaProvider implements MediaEntryProvider {

    public static final int MaxThreads = 3;
    private ExecutorService loadExecutor;
    protected RequestQueue requestsExecutor;
    protected SessionProvider sessionProvider;
    private LoaderFuture currentLoad;
    protected final Object syncObject = new Object();

    protected String tag = "BEMediaProvider";

    private static class LoaderFuture {

        OnMediaLoadCompletion loadCompletion;
        Future<Void> submittedTask;

        LoaderFuture(@NonNull Future<Void> task, OnMediaLoadCompletion completion) {
            this.submittedTask = task;
            this.loadCompletion = completion;
        }

        boolean isDone() {
            return submittedTask.isDone();
        }

        boolean isCancelled() {
            return submittedTask.isCancelled();
        }

        public boolean cancel(boolean allowInterruption) {
            if (submittedTask != null && !submittedTask.isDone() && !submittedTask.isCancelled()) {
                submittedTask.cancel(allowInterruption);
                return true;
            }
            return false;
        }
    }

    protected BEMediaProvider(String tag){
        this.requestsExecutor = APIOkRequestsExecutor.getSingleton();
        this.requestsExecutor.enableLogs(false);
        loadExecutor = Executors.newFixedThreadPool(MaxThreads);//TODO - once multi load execution will be supported will be changed to newFixedThreadExecutor or alike
        this.tag = tag;
    }

    protected abstract ErrorElement validateParams();

    protected abstract Callable<Void> factorNewLoader(OnMediaLoadCompletion completion);

    /**
     * Activates the providers data fetching process.
     * According to previously provided arguments, a request is built and passed to the remote server.
     * Fetching flow can ended with {@link PKMediaEntry} object if succeeded or with {@link ErrorElement} if failed.
     *
     * @param completion - a callback for handling the result of data fetching flow.
     */
    @Override
    public void load(final OnMediaLoadCompletion completion) {

        ErrorElement error = validateParams();
        if (error != null) {
            if (completion != null) {
                completion.onComplete(Accessories.<PKMediaEntry>buildResult(null, error));
            }
            return;
        }

        //!- in case load action is in progress and new load is activated, prev request will be canceled
        if (currentLoad != null && currentLoad.cancel(true)) {
            if (currentLoad.loadCompletion != null) {
                currentLoad.loadCompletion.onComplete(Accessories.<PKMediaEntry>buildResult(null, ErrorElement.CanceledRequest));
            }
        }
        synchronized (syncObject) {
            currentLoad = new LoaderFuture(loadExecutor.submit(factorNewLoader(completion)), completion);
            PKLog.v(tag, "new loader started " + currentLoad.toString());
        }
    }

    @Override
    public void cancel() {
        synchronized (syncObject) {
            if (currentLoad != null && !currentLoad.isDone() && !currentLoad.isCancelled()) {
                PKLog.v(tag, "has running load operation, canceling current load operation - " + currentLoad.toString());
                currentLoad.cancel(true);
            } else {
                //for DEBUG: PKLog.v(tag, (currentLoad != null ? currentLoad.toString() : "") + ": no need to cancel operation," + (currentLoad == null ? "operation is null" : (currentLoad.isDone() ? "operation done" : "operation canceled")));
            }
        }
    }

}
