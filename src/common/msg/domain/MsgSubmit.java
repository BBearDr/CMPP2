package common.msg.domain;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import common.msg.util.MsgUtils;

/**
 * Submit消息发送
 *
 * @author junxiong.chen
 * 2018-03-15
 */
public class MsgSubmit extends MsgHead {
    private static Logger logger = Logger.getLogger(MsgSubmit.class);
    private long msgId = 0;
    private byte pkTotal = 0x01;
    private byte pkNumber = 0x01;
    /**
     * 是否获取状态报告 0：不获取，1：获取
     */
    private byte registeredDelivery = 0x01;
    private byte msgLevel = 0x01;
    private String serviceId = "";
    /**
     * 通道向运营商传2，此处随意传
     */
    private byte feeUserType = 0x00;
    private String feeTerminalId = "";
    //	private byte feeTerminalType = 0x00;
    private byte tpPId = 0x00;
    private byte tpUdhi = 0x00;
    private byte msgFmt = 0x0f;
    private String msgSrc;
    /**
     * 运营商识别，可忽略此参数
     */
    private String feeType = "01";
    private String feeCode = "000000";
    // 暂不支持
    private String valIdTime = "";
    // 暂不支持
    private String atTime = "";
    // SP的服务代码或前缀为服务代码的长号码,
    // 网关将该号码完整的填到SMPP协议Submit_SM消息相应的source_addr字段，该号码最终在用户手机上显示为短消息的主叫号码。
    private String srcId;
    // 不支持群发
    private byte destUsrTl = 0x01;
    // 接收手机号码，
    private String destTerminalId;
    //	private byte destTerminalType = 0x00;// 真实号码
    private byte msgLength;
    private byte[] msgContent;
    // 点播业务使用的LinkID，非点播类业务的MT流程不使用该字段
    //	private String linkID = "";
    // 保留字段
    private String reserve = "00000000";

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        DataOutputStream dous = new DataOutputStream(bous);
        try {
            dous.writeInt(this.getTotalLength());
            dous.writeInt(this.getCommandId());
            dous.writeInt(this.getSequenceId());
            //Msg_Id 信息标识，由SP接入的短信网关本身产生，本处填空
            dous.writeLong(this.msgId);
            dous.writeByte(this.pkTotal);
            dous.writeByte(this.pkNumber);
            dous.writeByte(this.registeredDelivery);
            dous.writeByte(this.msgLevel);
            //Service_Id 业务标识，是数字、字母和符号的组合。
            MsgUtils.writeString(dous, this.serviceId, 10);
            dous.writeByte(this.feeUserType);
            MsgUtils.writeString(dous, this.feeTerminalId, 21);
//			dous.writeByte(this.feeTerminalType);//Fee_terminal_type 被计费用户的号码类型，0：真实号码；1：伪码
            dous.writeByte(this.tpPId);
            dous.writeByte(this.tpUdhi);
            dous.writeByte(this.msgFmt);
            MsgUtils.writeString(dous, this.msgSrc, 6);
            MsgUtils.writeString(dous, this.feeType, 2);
            MsgUtils.writeString(dous, this.feeCode, 6);
            MsgUtils.writeString(dous, this.valIdTime, 17);
            MsgUtils.writeString(dous, this.atTime, 17);
            MsgUtils.writeString(dous, this.srcId, 21);
            dous.writeByte(this.destUsrTl);
            MsgUtils.writeString(dous, this.destTerminalId, 21);
//			dous.writeByte(this.destTerminalType);//Dest_terminal_type 接收短信的用户的号码类型，0：真实号码；1：伪码
            dous.writeByte(this.msgLength);
            dous.write(this.msgContent);
            MsgUtils.writeString(dous, this.reserve, 8);
//			MsgUtils.writeString(dous,this.linkID,20);//点播业务使用的LinkID
            dous.close();
        } catch (IOException e) {
            logger.error("封装短信发送二进制数组失败。");
        }
        return bous.toByteArray();
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public byte getPkTotal() {
        return pkTotal;
    }

    public void setPkTotal(byte pkTotal) {
        this.pkTotal = pkTotal;
    }

    public byte getPkNumber() {
        return pkNumber;
    }

    public void setPkNumber(byte pkNumber) {
        this.pkNumber = pkNumber;
    }

    public byte getRegisteredDelivery() {
        return registeredDelivery;
    }

    public void setRegisteredDelivery(byte registeredDelivery) {
        this.registeredDelivery = registeredDelivery;
    }

    public byte getMsgLevel() {
        return msgLevel;
    }

    public void setMsgLevel(byte msgLevel) {
        this.msgLevel = msgLevel;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public byte getFeeUserType() {
        return feeUserType;
    }

    public void setFeeUserType(byte feeUserType) {
        this.feeUserType = feeUserType;
    }

    public String getFeeTerminalId() {
        return feeTerminalId;
    }

    public void setFeeTerminalId(String feeTerminalId) {
        this.feeTerminalId = feeTerminalId;
    }

    public byte getTpPId() {
        return tpPId;
    }

    public void setTpPId(byte tpPId) {
        this.tpPId = tpPId;
    }

    public byte getTpUdhi() {
        return tpUdhi;
    }

    public void setTpUdhi(byte tpUdhi) {
        this.tpUdhi = tpUdhi;
    }

    public byte getMsgFmt() {
        return msgFmt;
    }

    public void setMsgFmt(byte msgFmt) {
        this.msgFmt = msgFmt;
    }

    public String getMsgSrc() {
        return msgSrc;
    }

    public void setMsgSrc(String msgSrc) {
        this.msgSrc = msgSrc;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public String getFeeCode() {
        return feeCode;
    }

    public void setFeeCode(String feeCode) {
        this.feeCode = feeCode;
    }

    public String getValIdTime() {
        return valIdTime;
    }

    public void setValIdTime(String valIdTime) {
        this.valIdTime = valIdTime;
    }

    public String getAtTime() {
        return atTime;
    }

    public void setAtTime(String atTime) {
        this.atTime = atTime;
    }

    public String getSrcId() {
        return srcId;
    }

    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    public byte getDestUsrTl() {
        return destUsrTl;
    }

    public void setDestUsrTl(byte destUsrTl) {
        this.destUsrTl = destUsrTl;
    }

    public String getDestTerminalId() {
        return destTerminalId;
    }

    public void setDestTerminalId(String destTerminalId) {
        this.destTerminalId = destTerminalId;
    }

    public byte getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(byte msgLength) {
        this.msgLength = msgLength;
    }

    public byte[] getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(byte[] msgContent) {
        this.msgContent = msgContent;
    }

    public String getReserve() {
        return reserve;
    }

    public void setReserve(String reserve) {
        this.reserve = reserve;
    }
}
