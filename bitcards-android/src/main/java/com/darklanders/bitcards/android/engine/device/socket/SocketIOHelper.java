package com.darklanders.bitcards.android.engine.device.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Chris on 30/04/2016.
 */
public class SocketIOHelper {

    public static final String CHARSET = "UTF-8";

    private ObjectMapper objectMapper;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public SocketIOHelper(Socket socket) throws IOException {
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.objectMapper = new ObjectMapper();
    }

    public InboundMessageType readInboundMessageType() throws IOException {
        int id = this.inputStream.read();
        if( id >= 0 ) {
            return InboundMessageType.fromId(id);
        } else {
            throw new EOFException();
        }
    }

    public void writeInboundMessageType(InboundMessageType inboundMessageType) throws IOException {
        this.outputStream.write(inboundMessageType.getId());
    }

    public InboundMessageScan readInboundMessageScan() throws IOException {
        return this.read(InboundMessageScan.class);
    }

    public void writeInboundMessageScan(InboundMessageScan inboundMessageScan) throws IOException {
        this.write(inboundMessageScan);
    }

    public InboundMessageDeviceData readInboundMessageDeviceData() throws IOException {
        return this.read(InboundMessageDeviceData.class);
    }

    public void writeInboundMessageDeviceData(InboundMessageDeviceData inboundMessageDeviceData) throws IOException {
        this.write(inboundMessageDeviceData);
    }

    public OutboundMessageType readOutboundMessageType() throws IOException {
        int id = this.inputStream.read();
        if( id >= 0 ) {
            return OutboundMessageType.fromId(id);
        } else {
            return null;
        }
    }

    public void writeOutboundMessageType(OutboundMessageType outboundMessageType) throws IOException {
        this.outputStream.write(outboundMessageType.getId());
    }

    public OutboundMessageInstruction readOutboundMessageInstruction() throws IOException {
        return this.read(OutboundMessageInstruction.class);
    }

    public void writeOutboundMessageInstruction(OutboundMessageInstruction outboundMessageInstruction) throws IOException {
        this.write(outboundMessageInstruction);
    }

    private <T> T read(Class<T> t) throws IOException {
        int length = this.inputStream.readInt();
        byte[] b = new byte[length];
        this.inputStream.readFully(b);
        String s = new String(b, CHARSET);
        return objectMapper.readValue(s, t);
    }

    private <T> void write(T t) throws IOException {
        String s = this.objectMapper.writeValueAsString(t);
        byte[] b = s.getBytes(CHARSET);
        this.outputStream.writeInt(b.length);
        this.outputStream.write(b);
    }

    public void stop() {
        IOUtils.closeQuietly(this.outputStream);
        IOUtils.closeQuietly(this.inputStream);
    }
}
