/*
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Robin Yonathan <robin@robinyonathan.com>, 2017.
 */

/*
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Robin Yonathan <robin@robinyonathan.com>, 2017.
 */

package com.robinyonathan.blukutukhttp;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by Robin Yonathan on 14/08/17.
 */

public class ProgressRequestBody extends RequestBody {

    protected RequestBody mDelegate;
    protected Listener mListener;
    protected CountingSink mCountingSink;

    public ProgressRequestBody(RequestBody delegate, Listener listener) {
        mDelegate = delegate;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return mDelegate.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return mDelegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        mCountingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(mCountingSink);
        mDelegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink {
        private long bytesWritten = 0;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten += byteCount;
            mListener.onProgress((int) (100F * bytesWritten / contentLength()));
        }
    }

    public interface Listener {
        void onProgress(int progress);
    }
}
