package common.msg.domain;

import java.io.*;

import org.apache.log4j.Logger;

/**
 * 
 * 链路检查消息结构定义
 * @author junxiong.chen
 */
public class MsgActiveTestResp extends MsgHead {
	private static Logger logger=Logger.getLogger(MsgActiveTestResp.class);
	private byte reserved;

    public MsgActiveTestResp() {
    }

    public MsgActiveTestResp(byte[] data){
		if(data.length>1){
			ByteArrayInputStream bins=new ByteArrayInputStream(data);
			DataInputStream dins=new DataInputStream(bins);
			try {
				this.setTotalLength(data.length+4);
				this.setCommandId(dins.readInt());
				this.setSequenceId(dins.readInt());
				this.reserved=dins.readByte();
				dins.close();
				bins.close();
			} catch (IOException e){
                logger.info("MsgActiveTestResp is throw IOException error");
            }
		}else{
			logger.info("链路检查,解析数据包出错，包长度不一致。长度为:"+data.length);
		}
	}
    @Override
    public byte[] toByteArray(){
        ByteArrayOutputStream bous=new ByteArrayOutputStream();
        DataOutputStream dous=new DataOutputStream(bous);
        try {
            dous.writeInt(this.getTotalLength());
            dous.writeInt(this.getCommandId());
            dous.writeInt(this.getSequenceId());
            dous.writeByte(this.reserved);
            dous.close();
        } catch (IOException e) {
            logger.error("封装链接二进制数组失败。");
        }
        return bous.toByteArray();
    }
	public byte getReserved() {
		return reserved;
	}

	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
}
