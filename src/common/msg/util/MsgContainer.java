package common.msg.util;

import common.msg.domain.MsgCommand;
import common.msg.domain.MsgConnect;
import common.msg.domain.MsgHead;
import common.msg.domain.MsgSubmit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

/**
 * 短信接口容器，单例获得链接对象
 *
 * @author 张科伟
 * 2011-08-22 14:20
 */
public class MsgContainer {
    private static Log log = LogFactory.getLog(MsgContainer.class);
    private static Socket msgSocket;
    private static DataInputStream in;
    private static DataOutputStream out;

    public MsgContainer() {
    }

    public static DataInputStream getSocketDIS() {
        if (in == null || null == msgSocket || msgSocket.isClosed() || !msgSocket.isConnected()) {
            try {
                in = new DataInputStream(MsgContainer.getSocketInstance().getInputStream());
            } catch (IOException e) {
                in = null;
            }
        }
        return in;
    }

    public static DataOutputStream getSocketDOS() {
        if (out == null || null == msgSocket || msgSocket.isClosed() || !msgSocket.isConnected()) {
            try {
                out = new DataOutputStream(MsgContainer.getSocketInstance().getOutputStream());
            } catch (IOException e) {
                out = null;
            }
        }
        return out;
    }

    public static Socket getSocketInstance() {
        if (null == msgSocket || msgSocket.isClosed() || !msgSocket.isConnected()) {
            try {
                msgSocket = new Socket(MsgConfig.getIsmgIp(), MsgConfig.getIsmgPort());
                msgSocket.setKeepAlive(true);
                msgSocket.setSoTimeout(MsgConfig.getTimeOut());
                int count = 0;
                boolean result = connectISMG();
                while (!result) {
                    count++;
                    result = connectISMG();
                    if (count >= (MsgConfig.getConnectCount() - 1)) {//如果再次连接次数超过两次则终止连接
                        break;
                    }
                }//*/
            } catch (UnknownHostException e) {
                log.error("Socket链接短信网关端口号不正确：" + e.getMessage());
                //链接短信网关
            } catch (IOException e) {
                log.error("Socket链接短信网关失败：" + e.getMessage());
            }
        }
        return msgSocket;
    }

    /**
     * 创建Socket链接后请求链接ISMG
     *
     * @return
     */
    private static boolean connectISMG() {
        log.info("request link to ISMG...");
        MsgConnect connect = new MsgConnect();
        //消息总长度，级总字节数:消息头+消息主体
        connect.setTotalLength(12 + MsgCommand.CMPP_CONNECT_LEN);
        connect.setCommandId(MsgCommand.CMPP_CONNECT);
        connect.setSequenceId(MsgUtils.getSequence());
        //我们的企业代码
        connect.setSourceAddr(MsgConfig.getSpId());
        String timestamp = MsgUtils.getTimestamp();
        //md5(企业代码+密匙+时间戳)
        connect.setAuthenticatorSource(MsgUtils.getAuthenticatorSource(MsgConfig.getSpId(), MsgConfig.getSpSharedSecret(), timestamp));
        connect.setTimestamp(Integer.parseInt(timestamp));
        //版本号 高4bit为2，低4位为0
        connect.setVersion((byte) 0x20);
        List<byte[]> dataList = new ArrayList<byte[]>();
        dataList.add(connect.toByteArray());
        boolean result = cmppSender(dataList);
        return result;
    }

    /**
     * 对请求的数据进行发送
     *
     * @param dataList
     * @return
     */
    public static boolean cmppSender(List<byte[]> dataList) {
        CmppSender sender = new CmppSender(getSocketDOS(), getSocketDIS(), dataList);
        try {
            boolean success = sender.start();
            if (success) {
                log.info("request success！");
            } else {
                log.info("request error！");
            }
            return success;
        } catch (Exception e) {
            log.info("cmpp connect break, will be reload");
            try {
                out.close();
                in.close();
                out = null;
                in = null;
            } catch (IOException e1) {
                out = null;
                in = null;
            }
            return false;
        }
    }

    /**
     * 消息回复，或请求，例如链路检查、状态报告回复
     *
     * @param totalLength
     * @param commandId   消息类型
     * @return
     */
    public static boolean headISMG(int totalLength, int commandId) {
        MsgHead head = new MsgHead();
        //消息总长度，级总字节数:4+4+4(消息头)+(消息主体)
        head.setTotalLength(totalLength);
        head.setCommandId(commandId);
        head.setSequenceId(MsgUtils.getSequence());

        List<byte[]> dataList = new ArrayList<byte[]>();
        dataList.add(head.toByteArray());
        boolean result = cmppSender(dataList);
        log.info(" request ===>" + commandId);
        return result;
    }

    /**
     * 链路检查
     *
     * @return
     */
    public static boolean activityTestISMG() {
        return headISMG(12, MsgCommand.CMPP_ACTIVE_TEST);
    }

    /**
     * 拆除与ISMG的链接
     *
     * @return
     */
    public static boolean cancelISMG() {
        return headISMG(12, MsgCommand.CMPP_TERMINATE);
    }

    /**
     * 获取状态报告和回复信息
     *
     * @return
     */
    public static boolean deliverResp() {
        try {
            MsgHead head = new MsgHead();
            //消息总长度，级总字节数:消息头+消息主体
            head.setTotalLength(12 + MsgCommand.CMPP_DELIVER_LEN);
            //标识创建连接
            head.setCommandId(MsgCommand.CMPP_DELIVER);
            //序列，由我们指定
            head.setSequenceId(MsgUtils.getSequence());

            List<byte[]> dataList = new ArrayList<byte[]>();
            dataList.add(head.toByteArray());
            final CmppSender sender = new CmppSender(getSocketDOS(), getSocketDIS(), dataList);
            //获取状态5秒获取一次
            Thread.sleep(5 * 1000);
            sender.start();
            return true;
        } catch (Exception e) {
            try {
                out.close();
                in.close();
                out = null;
                in = null;
            } catch (IOException e1) {
                out = null;
                in = null;
            }
            log.error("deliver :" + e.getMessage());
            return false;
        }
    }

    /**
     * 短信发送，使用端口是7890，即长短信
     *
     * @param list
     * @return
     */
    public static boolean sendMsg(List<Map<String, String>> list, int sendType) {
        try {
            List<byte[]> dataList = new ArrayList<byte[]>();
            for (int i = 0; i < list.size(); i++) {
                Map<String, String> map = list.get(i);
                String msg = map.get("MESSAGE");
                String cusMsisdn = map.get("MOBILE");
                String url = map.get("URL");
                switch (sendType) {
                    case 1:
                        //短短信
                        if (msg.getBytes("utf-8").length < 140) {
                            sendShortMsg(msg, cusMsisdn, dataList);
                        } else {
                            sendLongMsg(msg, cusMsisdn, dataList);
                        }
                        break;
                    case 2:
                        int msgContent = 12 + 9 + 9 + url.getBytes("utf-8").length + 3 + msg.getBytes("utf-8").length + 3;
                        if (msgContent < 140) {
                            sendShortWapPushMsg(url, msg, cusMsisdn);
                        } else {
                            sendLongWapPushMsg(url, msg, cusMsisdn);
                        }
                        break;
                    default:
                        dataList = null;
                }
            }
            if (dataList.size() <= 0) {
                return false;
            }
            boolean result = cmppSender(dataList);
            int count = 0;
            while (!result) {
                count++;
                result = cmppSender(dataList);
                //如果再次连接次数超过两次则终止连接
                if (count >= (MsgConfig.getConnectCount() - 1)) {
                    break;
                }
            }
            return result;
        } catch (Exception e) {
            try {
                out.close();
            } catch (IOException e1) {
                out = null;
            }
            return false;
        }
    }

    /**
     * 发送web push短信
     *
     * @param url       wap网址
     * @param desc      描述
     * @param cusMsisdn 短信
     * @return
     */
   /* public static boolean sendWapPushMsg(String url, String desc, String cusMsisdn) {
        try {
            int msgContent = 12 + 9 + 9 + url.getBytes("utf-8").length + 3 + desc.getBytes("utf-8").length + 3;
            if (msgContent < 140) {
                boolean result = sendShortWapPushMsg(url, desc, cusMsisdn);
                int count = 0;
                while (!result) {
                    count++;
                    result = sendShortWapPushMsg(url, desc, cusMsisdn);
                    if (count >= (MsgConfig.getConnectCount() - 1)) {//如果再次连接次数超过两次则终止连接
                        break;
                    }
                }
                return result;
            } else {
                boolean result = sendLongWapPushMsg(url, desc, cusMsisdn);
                int count = 0;
                while (!result) {
                    count++;
                    result = sendLongWapPushMsg(url, desc, cusMsisdn);
                    if (count >= (MsgConfig.getConnectCount() - 1)) {//如果再次连接次数超过两次则终止连接
                        break;
                    }
                }
                return result;
            }
        } catch (Exception e) {
            try {
                out.close();
            } catch (IOException e1) {
                out = null;
            }
            log.error("发送web push短信:" + e.getMessage());
            return false;
        }
    }*/

    /**
     * 发送短短信
     *
     * @return
     */
    private static List<byte[]> sendShortMsg(String msg, String cusMsisdn, List<byte[]> dataList) {
        try {
            int seq = MsgUtils.getSequence();
            byte[] msgByte = msg.getBytes("gb2312");
            MsgSubmit submit = new MsgSubmit();
            //12+8+1+1+1+1+10+1+21+1+1+1+6+2+6+17+17+21+1+21+1+8
            submit.setTotalLength(159 + msgByte.length);
            submit.setCommandId(MsgCommand.CMPP_SUBMIT);
            submit.setSequenceId(seq);
            submit.setPkTotal((byte) 0x01);
            submit.setPkNumber((byte) 0x01);
            submit.setRegisteredDelivery((byte) 0x00);
            submit.setMsgLevel((byte) 0x01);
            submit.setFeeUserType((byte) 0x02);
            submit.setFeeTerminalId("");
            submit.setTpPId((byte) 0x00);
            submit.setTpUdhi((byte) 0x00);
            submit.setMsgFmt((byte) 0x0f);
            submit.setMsgSrc(MsgConfig.getSpId());
            //+扩展码
            submit.setSrcId(MsgConfig.getSpCode()+"0258");
            submit.setDestTerminalId(cusMsisdn);
            submit.setMsgLength((byte) msgByte.length);
            submit.setMsgContent(msgByte);

            dataList.add(submit.toByteArray());
            log.info("to send mobile：" + cusMsisdn + ",content:"+msg+",SequenceId:" + seq);
            return dataList;
        } catch (Exception e) {
            try {
                out.close();
            } catch (IOException e1) {
                out = null;
            }
            log.error("发送短短信" + e.getMessage());
            return null;
        }
    }

    /**
     * 发送长短信
     *
     * @return
     */
    private static List<byte[]> sendLongMsg(String msg, String cusMsisdn, List<byte[]> dataList) {
        try {
            byte[] allByte = msg.getBytes("iso-10646-ucs-2");
//			byte[] allByte=msg.getBytes("UTF-16BE");
            int msgLength = allByte.length;
            int maxLength = 140;
            int msgSendCount = msgLength % (maxLength - 6) == 0 ? msgLength / (maxLength - 6) : msgLength / (maxLength - 6) + 1;
            //短信息内容头拼接
            byte[] msgHead = new byte[6];
            Random random = new Random();
            random.nextBytes(msgHead);
            msgHead[0] = 0x05;
            msgHead[1] = 0x00;
            msgHead[2] = 0x03;
            msgHead[4] = (byte) msgSendCount;
            msgHead[5] = 0x01;
            int seqId = MsgUtils.getSequence();
            for (int i = 0; i < msgSendCount; i++) {
                //msgHead[3]=(byte)MsgUtils.getSequence();
                msgHead[5] = (byte) (i + 1);
                byte[] needMsg = null;
                //消息头+消息内容拆分
                if (i != msgSendCount - 1) {
                    int start = (maxLength - 6) * i;
                    int end = (maxLength - 6) * (i + 1);
                    needMsg = MsgUtils.getMsgBytes(allByte, start, end);
                } else {
                    int start = (maxLength - 6) * i;
                    int end = allByte.length;
                    needMsg = MsgUtils.getMsgBytes(allByte, start, end);
                }
                int subLength = needMsg.length + msgHead.length;
                byte[] sendMsg = new byte[needMsg.length + msgHead.length];
                System.arraycopy(msgHead, 0, sendMsg, 0, 6);
                System.arraycopy(needMsg, 0, sendMsg, 6, needMsg.length);
                MsgSubmit submit = new MsgSubmit();
                //12+8+1+1+1+1+10+1+21+1+1+1+6+2+6+17+17+21+1+21+1+8
                submit.setTotalLength(159 + subLength);
                submit.setCommandId(MsgCommand.CMPP_SUBMIT);
                submit.setSequenceId(seqId);
                submit.setPkTotal((byte) msgSendCount);
                submit.setPkNumber((byte) (i + 1));
                submit.setRegisteredDelivery((byte) 0x00);
                submit.setMsgLevel((byte) 0x01);
                submit.setFeeUserType((byte) 0x02);
                submit.setFeeTerminalId("");
                submit.setTpPId((byte) 0x00);
                submit.setTpUdhi((byte) 0x01);
                submit.setMsgFmt((byte) 0x08);
                submit.setMsgSrc(MsgConfig.getSpId());
                submit.setSrcId(MsgConfig.getSpCode());
                submit.setDestTerminalId(cusMsisdn);
                submit.setMsgLength((byte) subLength);
                submit.setMsgContent(sendMsg);
                dataList.add(submit.toByteArray());
            }
            log.info("向" + cusMsisdn + "下发长短信，序列号为:" + seqId);
            return dataList;
        } catch (Exception e) {
            try {
                out.close();
            } catch (IOException e1) {
                out = null;
            }
            log.error("发送长短信" + e.getMessage());
            return null;
        }
    }


    /**
     * 发送web push 短短信
     *
     * @param url       wap网址
     * @param desc      描述
     * @param cusMsisdn 短信
     * @return
     */
    private static List<byte[]> sendShortWapPushMsg(String url, String desc, String cusMsisdn) {
        try {
            //length 12
            byte[] szWapPushHeader1 = {0x0B, 0x05, 0x04, 0x0B, (byte) 0x84, 0x23, (byte) 0xF0, 0x00, 0x03, 0x03, 0x01, 0x01};
            //length 9
            byte[] szWapPushHeader2 = {0x29, 0x06, 0x06, 0x03, (byte) 0xAE, (byte) 0x81, (byte) 0xEA, (byte) 0x8D, (byte) 0xCA};
            //length 9
            byte[] szWapPushIndicator = {0x02, 0x05, 0x6A, 0x00, 0x45, (byte) 0xC6, 0x08, 0x0C, 0x03};
            //去除了http://前缀的UTF8编码的Url地址"的二进制编码
            byte[] szWapPushUrl = url.getBytes("utf-8");
            //length 3
            byte[] szWapPushDisplayTextHeader = {0x00, 0x01, 0x03};
            //想在手机上显示的关于这个URL的文字说明,UTF8编码的二进制
            byte szMsg[] = desc.getBytes("utf-8");
            //length 3
            byte[] szEndOfWapPush = {0x00, 0x01, 0x01};
            int msgLength = 12 + 9 + 9 + szWapPushUrl.length + 3 + szMsg.length + 3;
            int seq = MsgUtils.getSequence();
            MsgSubmit submit = new MsgSubmit();
            submit.setTotalLength(12 + 8 + 1 + 1 + 1 + 1 + 10 + 1 + 32 + 1 + 1 + 1 + 1 + 6 + 2 + 6 + 17 + 17 + 21 + 1 + 32 + 1 + 1 + msgLength + 20);
            submit.setCommandId(MsgCommand.CMPP_SUBMIT);
            submit.setSequenceId(seq);
            submit.setPkTotal((byte) 0x01);
            submit.setPkNumber((byte) 0x01);
            submit.setRegisteredDelivery((byte) 0x00);
            submit.setMsgLevel((byte) 0x01);
            submit.setFeeUserType((byte) 0x00);
            submit.setFeeTerminalId("");
            submit.setTpPId((byte) 0x00);
            submit.setTpUdhi((byte) 0x01);
            submit.setMsgFmt((byte) 0x04);
            submit.setMsgSrc(MsgConfig.getSpId());
            submit.setSrcId(MsgConfig.getSpCode());
            submit.setDestTerminalId(cusMsisdn);
            submit.setMsgLength((byte) msgLength);
            byte[] sendMsg = new byte[12 + 9 + 9 + szWapPushUrl.length + 3 + szMsg.length + 3];
            System.arraycopy(szWapPushHeader1, 0, sendMsg, 0, 12);
            System.arraycopy(szWapPushHeader2, 0, sendMsg, 12, 9);
            System.arraycopy(szWapPushIndicator, 0, sendMsg, 12 + 9, 9);
            System.arraycopy(szWapPushUrl, 0, sendMsg, 12 + 9 + 9, szWapPushUrl.length);
            System.arraycopy(szWapPushDisplayTextHeader, 0, sendMsg, 12 + 9 + 9 + szWapPushUrl.length, 3);
            System.arraycopy(szMsg, 0, sendMsg, 12 + 9 + 9 + szWapPushUrl.length + 3, szMsg.length);
            System.arraycopy(szEndOfWapPush, 0, sendMsg, 12 + 9 + 9 + szWapPushUrl.length + 3 + szMsg.length, 3);
            submit.setMsgContent(sendMsg);
            List<byte[]> dataList = new ArrayList<byte[]>();
            dataList.add(submit.toByteArray());
            log.info("向" + cusMsisdn + "下发web push短短信，序列号为:" + seq);
            return dataList;
        } catch (Exception e) {
            try {
                out.close();
            } catch (IOException e1) {
                out = null;
            }
            log.error("发送web push短短信" + e.getMessage());
            return null;
        }
    }

    /**
     * 发送web push 长短信
     *
     * @param url       wap网址
     * @param desc      描述
     * @param cusMsisdn 短信
     * @return
     */
    private static List<byte[]> sendLongWapPushMsg(String url, String desc, String cusMsisdn) {
        try {
            List<byte[]> dataList = new ArrayList<byte[]>();
            //length 12
            byte[] wdp = {0x0B, 0x05, 0x04, 0x0B, (byte) 0x84, 0x23, (byte) 0xF0, 0x00, 0x03, 0x03, 0x01, 0x01};
            //需要拆分的部分
            //length 9
            byte[] wsp = {0x29, 0x06, 0x06, 0x03, (byte) 0xAE, (byte) 0x81, (byte) 0xEA, (byte) 0x8D, (byte) 0xCA};
            //length 9
            byte[] szWapPushIndicator = {0x02, 0x05, 0x6A, 0x00, 0x45, (byte) 0xC6, 0x08, 0x0C, 0x03};
            //去除了http://前缀的UTF8编码的Url地址"的二进制编码
            byte[] szWapPushUrl = url.getBytes("utf-8");
            //length 3
            byte[] szWapPushDisplayTextHeader = {0x00, 0x01, 0x03};
            //想在手机上显示的关于这个URL的文字说明,UTF8编码的二进制
            byte szMsg[] = desc.getBytes("utf-8");
            //length 3
            byte[] szEndOfWapPush = {0x00, 0x01, 0x01};
            byte[] allByte = new byte[9 + 9 + szWapPushUrl.length + 3 + szMsg.length + 3];

            System.arraycopy(wsp, 0, allByte, 0, 9);
            System.arraycopy(szWapPushIndicator, 0, allByte, 9, 9);
            System.arraycopy(szWapPushUrl, 0, allByte, 18, szWapPushUrl.length);
            System.arraycopy(szWapPushDisplayTextHeader, 0, allByte, 18 + szWapPushUrl.length, 3);
            System.arraycopy(szMsg, 0, allByte, 18 + szWapPushUrl.length + 3, szMsg.length);
            System.arraycopy(szEndOfWapPush, 0, allByte, 18 + szWapPushUrl.length + 3 + szMsg.length, 3);
            int msgMax = 140;
            int msgCount = allByte.length % (msgMax - wdp.length) == 0 ? allByte.length / (msgMax - wdp.length) : allByte.length / (msgMax - wdp.length) + 1;
            wdp[10] = (byte) msgCount;
            int seqId = MsgUtils.getSequence();
            for (int i = 0; i < msgCount; i++) {
                wdp[11] = (byte) (i + 1);
                byte[] needMsg = null;
                //消息头+消息内容拆分
                if (i != msgCount - 1) {
                    int start = (msgMax - wdp.length) * i;
                    int end = (msgMax - wdp.length) * (i + 1);
                    needMsg = MsgUtils.getMsgBytes(allByte, start, end);
                } else {
                    int start = (msgMax - wdp.length) * i;
                    int end = allByte.length;
                    needMsg = MsgUtils.getMsgBytes(allByte, start, end);
                }
                int msgLength = needMsg.length + wdp.length;
                MsgSubmit submit = new MsgSubmit();
                submit.setTotalLength(12 + 8 + 1 + 1 + 1 + 1 + 10 + 1 + 32 + 1 + 1 + 1 + 1 + 6 + 2 + 6 + 17 + 17 + 21 + 1 + 32 + 1 + 1 + msgLength + 20);
                submit.setCommandId(MsgCommand.CMPP_SUBMIT);
                submit.setSequenceId(seqId);
                submit.setPkTotal((byte) msgCount);
                submit.setPkNumber((byte) (i + 1));
                submit.setRegisteredDelivery((byte) 0x00);
                submit.setMsgLevel((byte) 0x01);
                submit.setFeeUserType((byte) 0x00);
                submit.setFeeTerminalId("");
                submit.setTpPId((byte) 0x00);
                submit.setTpUdhi((byte) 0x01);
                submit.setMsgFmt((byte) 0x04);
                submit.setMsgSrc(MsgConfig.getSpId());
                submit.setSrcId(MsgConfig.getSpCode());
                submit.setDestTerminalId(cusMsisdn);
                submit.setMsgLength((byte) msgLength);
                byte[] sendMsg = new byte[wdp.length + needMsg.length];
                System.arraycopy(wdp, 0, sendMsg, 0, wdp.length);
                System.arraycopy(needMsg, 0, sendMsg, wdp.length, needMsg.length);
                submit.setMsgContent(sendMsg);
                dataList.add(submit.toByteArray());
            }
            log.info("向" + cusMsisdn + "下发web pus长短信，序列号为:" + seqId);
            return dataList;
        } catch (Exception e) {
            try {
                out.close();
            } catch (IOException e1) {
                out = null;
            }
            log.error("发送web push长短信" + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("MESSAGE", "【大街网】测试验证码");
        map.put("MOBILE","18732190340");
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        list.add(map);
        boolean result = MsgContainer.sendMsg(list, 1);
        System.out.println(result);
/*        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    boolean result = MsgContainer.deliverResp();
                }
            }
        }).start();*/
  /*      boolean result = MsgContainer.deliverResp();
        System.out.println(result);*/
    }
}
