package common.msg.domain;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * CMPP 链接响应
 * @author junxiong.chen
 */
public class MsgConnectResp extends MsgHead {
    private static Logger logger = Logger.getLogger(MsgConnectResp.class);
    //响应状态状态 0：正确 1：消息结构错 2：非法源地址 3：认证错 4：版本太高 5~ ：其他错误
    private int status;
    //响应状态状态 0：正确 1：消息结构错 2：非法源地址 3：认证错 4：版本太高 5~ ：其他错误
    private String statusStr;
    private byte[] authenticatorISMG;
    //	服务器支持的最高版本号，对于3.0的版本，高4bit为3，低4位为0
    private byte version;

    public MsgConnectResp(byte[] data) {
        if (data.length == MsgCommand.CMPP_CONNECT_RESR_LEN) {
            ByteArrayInputStream bins = new ByteArrayInputStream(data);
            DataInputStream dins = new DataInputStream(bins);
            try {
                this.setTotalLength(data.length + 4);
                this.setCommandId(dins.readInt());
                this.setSequenceId(dins.readInt());
                this.setStatus(dins.readByte());
                byte[] aiByte = new byte[16];
                dins.read(aiByte);
                this.authenticatorISMG = aiByte;
                this.version = dins.readByte();
                dins.close();
                bins.close();
            } catch (IOException e) {
            }
        } else {
            logger.info("链接至IMSP,解析数据包出错，包长度不一致。长度为:" + data.length);
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
        switch (status) {
            case 0:
                statusStr = "正确";
                break;
            case 1:
                statusStr = "消息结构错";
                break;
            case 2:
                statusStr = "非法源地址";
                break;
            case 3:
                statusStr = "认证错";
                break;
            case 4:
                statusStr = "版本太高";
                break;
            case 5:
                statusStr = "其他错误";
                break;
            default:
                statusStr = status + ":未知";
                break;
        }
    }

    public byte[] getAuthenticatorISMG() {
        return authenticatorISMG;
    }

    public void setAuthenticatorISMG(byte[] authenticatorISMG) {
        this.authenticatorISMG = authenticatorISMG;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public String getStatusStr() {
        return statusStr;
    }
}
