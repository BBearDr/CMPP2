package common.msg.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import common.msg.domain.MsgActiveTestResp;
import common.msg.domain.MsgCommand;
import common.msg.domain.MsgConnectResp;
import common.msg.domain.MsgDeliver;
import common.msg.domain.MsgDeliverResp;
import common.msg.domain.MsgHead;
import common.msg.domain.MsgSubmitResp;

/**
 * 启动一个线程去接收和发送数据，如果队列处理完毕就关闭线程
 *
 * @author junxiong.chen
 */
public class CmppSender {
    private static Log log = LogFactory.getLog(CmppSender.class);
    //需要发出的二进制数据队列
    private List<byte[]> sendData = new ArrayList<byte[]>();
    //需要接受的二进制队列
    private List<byte[]> getData = new ArrayList<byte[]>();
    private DataOutputStream out;
    private DataInputStream in;

    public CmppSender(DataOutputStream out, DataInputStream in, List<byte[]> sendData) {
        super();
        this.sendData = sendData;
        this.out = out;
        this.in = in;
    }

    public boolean start() throws Exception {
        boolean success = false;
        if (out != null && null != sendData) {
            for (byte[] data : sendData) {
                sendMsg(data);
                byte[] returnData = getInData();
                if (null != returnData) {
                    getData.add(returnData);
                }
            }
        }
        if (in != null && null != getData) {
            for (byte[] data : getData) {
                if (data.length >= 8) {
                    MsgHead head = new MsgHead(data);
                    switch (head.getCommandId()) {
                        case MsgCommand.CMPP_CONNECT_RESP:
                            MsgConnectResp connectResp = new MsgConnectResp(data);
                            if (connectResp.getStatus() == 0) {
                                success = true;
                            }
                            log.info("CMPP_CONNECT_RESP,status :" + connectResp.getStatus() + " 序列号：" + connectResp.getSequenceId());
                            break;
                        case MsgCommand.CMPP_ACTIVE_TEST_RESP:
                            MsgActiveTestResp activeResp = new MsgActiveTestResp(data);
                            log.info("CMPP_ACTIVE_TEST_RESP短信网关与短信网关进行连接检查" + " 序列号：" + activeResp.getSequenceId());
                            break;
                        case MsgCommand.CMPP_SUBMIT_RESP:
                            MsgSubmitResp submitResp = new MsgSubmitResp(data);
                            if (submitResp.getResult() == 0) {
                                success = true;
                            }
                            log.info("CMPP_SUBMIT_RESP,stauts : " + submitResp.getResult() + " 序列号：" + submitResp.getSequenceId() + " msgId：" + submitResp.getMsgId());
                            break;
                        case MsgCommand.CMPP_TERMINATE_RESP:
                            log.info("拆除与ISMG的链接" + " 序列号：" + head.getSequenceId());
                            break;
                        case MsgCommand.CMPP_CANCEL_RESP:
                            log.info("CMPP_CANCEL_RESP 序列号：" + head.getSequenceId());
                            break;
                        case MsgCommand.CMPP_CANCEL:
                            log.info("CMPP_CANCEL 序列号：" + head.getSequenceId());
                            break;
                        case MsgCommand.CMPP_DELIVER:
                            MsgDeliver msgDeliver = new MsgDeliver(data);
                            if (msgDeliver.getResult() == 0) {
                                if (msgDeliver.getRegistered_Delivery() == 0) {
                                    log.info("<Msg_Id>:" + msgDeliver.getMsg_Id() + "<Dest_Id>" + msgDeliver.getDest_Id() + "<SequenceId>:" + head.getSequenceId() + "<Dest_terminal_Id>:" + msgDeliver.getDest_terminal_Id() + "<Msg_Content>:" + msgDeliver.getMsg_Content() + "<Src_terminal_Id>:" + msgDeliver.getSrc_terminal_Id() + "<Registered_Delivery>" + msgDeliver.getRegistered_Delivery() + "<Msg_Length>" + msgDeliver.getMsg_Length());
                                } else {
                                    log.info("<Msg_Id_report>:" + msgDeliver.getMsg_Id_report() + "<Stat>:" + msgDeliver.getStat()
                                            + "<Submit_time>" + msgDeliver.getSubmit_time() + "<Done_time>" + msgDeliver.getDone_time()
                                            + "<Dest_terminal_Id>" + msgDeliver.getDest_terminal_Id() + "<SMSC_sequence>:" + msgDeliver.getSMSC_sequence()
                                            + "<result>:" + msgDeliver.getResult());
                                }
                            } else {
                                log.info("CMPP_DELIVER 序列号：" + head.getSequenceId());
                            }
                            MsgDeliverResp msgDeliverResp = new MsgDeliverResp();
                            msgDeliverResp.setTotalLength(12 + 8 + 4);
                            msgDeliverResp.setCommandId(MsgCommand.CMPP_DELIVER_RESP);
                            msgDeliverResp.setSequenceId(MsgUtils.getSequence());
                            msgDeliverResp.setMsg_Id(msgDeliver.getMsg_Id());
                            msgDeliverResp.setResult(msgDeliver.getResult());
                            //进行回复
                            sendMsg(msgDeliverResp.toByteArray());
                            break;
                        case MsgCommand.CMPP_DELIVER_RESP:
                            log.info("CMPP_DELIVER_RESP 序列号：" + head.getSequenceId());
                            break;
                        case MsgCommand.CMPP_QUERY:
                            log.info("CMPP_QUERY 序列号：" + head.getSequenceId());
                            break;
                        case MsgCommand.CMPP_QUERY_RESP:
                            log.info("CMPP_QUERY_RESP 序列号：" + head.getSequenceId());
                            break;
                        case MsgCommand.CMPP_TERMINATE:
                            log.info("CMPP_TERMINATE 序列号：" + head.getSequenceId());
                            break;
                        case MsgCommand.CMPP_CONNECT:
                            log.info("CMPP_CONNECT 序列号：" + head.getSequenceId());
                            break;
                        case MsgCommand.CMPP_ACTIVE_TEST:
                            log.info("CMPP_ACTIVE_TEST 序列号：" + head.getSequenceId());
                            MsgActiveTestResp msgActiveTestResp = new MsgActiveTestResp();
                            msgActiveTestResp.setTotalLength(12 + 8 + 4);
                            msgActiveTestResp.setCommandId(MsgCommand.CMPP_ACTIVE_TEST_RESP);
                            msgActiveTestResp.setSequenceId(MsgUtils.getSequence());
                            msgActiveTestResp.setReserved((byte) 1);
                            //进行回复
                            sendMsg(msgActiveTestResp.toByteArray());
                            break;
                        case MsgCommand.CMPP_SUBMIT:
                            log.info("CMPP_SUBMIT 序列号：" + head.getSequenceId());
                            break;
                        default:
                            log.error("无法解析IMSP返回的包结构：包长度为" + head.getTotalLength());
                            break;
                    }
                }
            }
        }
        return success;
    }

    public List<byte[]> getGetData() {
        return getData;
    }

    /**
     * 在本连结上发送已打包后的消息的字节
     *
     * @param data:要发送消息的字节
     */
    private boolean sendMsg(byte[] data) throws Exception {
        try {
            out.write(data);
            out.flush();
            return true;
        } catch (NullPointerException ef) {
            log.error("在本连结上发送已打包后的消息的字节:无字节输入");
        }
        return false;
    }

    private byte[] getInData() throws IOException {
        try {
            int len = in.readInt();
            if (null != in && 0 != len) {
                byte[] data = new byte[len - 4];
                in.read(data);
                return data;
            } else {
                return null;
            }
        } catch (NullPointerException ef) {
            log.error("在本连结上接受字节消息:无流输入");
            return null;
        } catch (EOFException eof) {
            log.error("在本连结上接受字节消息:" + eof.getMessage());
            return null;
        }
    }
}
