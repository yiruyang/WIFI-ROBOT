package com.dsrobot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

public class MjpegInputStream extends DataInputStream {
    private final byte[] SOI_MARKER = {(byte) 0xFF, (byte) 0xD8};
    private final byte[] EOF_MARKER = {(byte) 0xFF, (byte) 0xD9};
    private final String CONTENT_LENGTH = "Content-Length";
    private final static int HEADER_MAX_LENGTH = 100;
    private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
    private int mContentLength = -1;
    private static MjpegInputStream mis = null;

    public static MjpegInputStream read(String url) {
        HttpResponse res;
        DefaultHttpClient httpclient = new DefaultHttpClient(); //创建http客户端，才能调用它的各种方法
        httpclient.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, 500);/*设置超时时间*/
        try {
            res = httpclient.execute(new HttpGet(URI.create(url)));
            mis = new MjpegInputStream(res.getEntity().getContent());
            return mis;
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * 获得创建的mjpegInputsteam流
     *
     * @return
     */
    public static MjpegInputStream getInstance() {
        if (mis != null)
            return mis;
        return null;
    }

    /**
     * 因为mpjeginputstream继承了datainputstream 所以可以调用mpjeginputstream的关闭流方法
     */
    public static void closeInstance() {
        try {
            if (null != mis) {
                mis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mis = null;
    }

    public MjpegInputStream(InputStream in) {
        super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
    }

    private int getEndOfSeqeunce(DataInputStream in, byte[] sequence)
            throws IOException {
        int seqIndex = 0;
        byte c;
        for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte();
            if (c == sequence[seqIndex]) {
                seqIndex++;
                if (seqIndex == sequence.length)
                    return i + 1;
            } else
                seqIndex = 0;
        }
        return -1;
    }

    private int getStartOfSequence(DataInputStream in, byte[] sequence)
            throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }

    private int parseContentLength(byte[] headerBytes) throws IOException,
            NumberFormatException {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        props.load(headerIn);
        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }

    public Bitmap readMjpegFrame() throws IOException {
        mark(FRAME_MAX_LENGTH);
        int headerLen = getStartOfSequence(this, SOI_MARKER);
        reset();
        byte[] header = new byte[headerLen];
        readFully(header);
        try {
            mContentLength = parseContentLength(header);
        } catch (NumberFormatException nfe) {
            mContentLength = getEndOfSeqeunce(this, EOF_MARKER);
        }
        reset();
        byte[] frameData = new byte[mContentLength];
        skipBytes(headerLen);
        readFully(frameData);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
    }
}
