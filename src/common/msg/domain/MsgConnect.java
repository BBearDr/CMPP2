package common.msg.domain;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import common.msg.util.MsgUtils;

/**
 * CMPP 连接
 *
 * @author junxiong.chen
 */
public class MsgConnect extends MsgHead {
    private static Logger logger = Logger.getLogger(MsgConnect.class);
    //源地址，此处为SP_Id，即SP的企业代码。
    private String sourceAddr;
    private byte[] authenticatorSource;
    private byte version;
    private int timestamp;

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        DataOutputStream dous = new DataOutputStream(bous);
        try {
            dous.writeInt(this.getTotalLength());
            dous.writeInt(this.getCommandId());
            dous.writeInt(this.getSequenceId());
            MsgUtils.writeString(dous, this.sourceAddr, 6);
            dous.write(authenticatorSource);
            dous.writeByte(version);
            dous.writeInt(timestamp);
            dous.close();
        } catch (IOException e) {
            logger.error("封装链接二进制数组失败。");
        }
        return bous.toByteArray();
    }

    public String getSourceAddr() {
        return sourceAddr;
    }

    public void setSourceAddr(String sourceAddr) {
        this.sourceAddr = sourceAddr;
    }

    public byte[] getAuthenticatorSource() {
        return authenticatorSource;
    }

    public void setAuthenticatorSource(byte[] authenticatorSource) {
        this.authenticatorSource = authenticatorSource;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
