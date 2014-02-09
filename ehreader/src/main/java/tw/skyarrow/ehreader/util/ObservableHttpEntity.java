package tw.skyarrow.ehreader.util;

import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by SkyArrow on 2014/2/9.
 */
// http://stackoverflow.com/a/19188010
public class ObservableHttpEntity implements HttpEntity {
    private HttpEntity entity;
    private OnWriteListener onWriteListener;

    public interface OnWriteListener {
        void onWrite(long totalSent);
    }

    public ObservableHttpEntity(HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean isRepeatable() {
        return entity.isRepeatable();
    }

    @Override
    public boolean isChunked() {
        return entity.isChunked();
    }

    @Override
    public long getContentLength() {
        return entity.getContentLength();
    }

    @Override
    public Header getContentType() {
        return entity.getContentType();
    }

    @Override
    public Header getContentEncoding() {
        return entity.getContentEncoding();
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return entity.getContent();
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        if (onWriteListener != null) {
            ObservableOutputStream stream = new ObservableOutputStream(outputStream);
            stream.setOnWriteListener(onWriteListener);
            entity.writeTo(stream);
        } else {
            entity.writeTo(outputStream);
        }
    }

    @Override
    public boolean isStreaming() {
        return entity.isStreaming();
    }

    @Override
    public void consumeContent() throws IOException {
        entity.consumeContent();
    }

    public OnWriteListener getOnWriteListener() {
        return onWriteListener;
    }

    public void setOnWriteListener(OnWriteListener onWriteListener) {
        this.onWriteListener = onWriteListener;
    }

    private class ObservableOutputStream extends ProxyOutputStream {
        private long totalSent = 0;
        private OnWriteListener onWriteListener;

        public ObservableOutputStream(OutputStream proxy) {
            super(proxy);
        }

        public void write(byte[] bts, int st, int end) throws IOException {
            totalSent += end;
            out.write(bts, st, end);

            if (onWriteListener != null) {
                onWriteListener.onWrite(totalSent);
            }
        }

        public OnWriteListener getOnWriteListener() {
            return onWriteListener;
        }

        public void setOnWriteListener(OnWriteListener listener) {
            this.onWriteListener = listener;
        }
    }
}
