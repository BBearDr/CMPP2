package common.msg.util;

import common.msg.domain.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;


/**
 * @author junxiongchen
 * @date 2018/03/26
 */
public class Test {
    public static void main(String[] args) {

    }

   /* private byte[] getInData() throws IOException {
        try {
            int len = socketDIS.readInt();
            if (null != socketDIS && 0 != len) {
                byte[] data = new byte[len - 4];
                socketDIS.read(data);
                return data;
            } else {
                return null;
            }
        } catch (NullPointerException ef) {
            System.out.println("无流输入");
//            log.error("在本连结上接受字节消息:无流输入");
            return null;
        } catch (EOFException eof) {
            System.out.println(eof.getMessage());
//            log.error("在本连结上接受字节消息:"+eof.getMessage());
            return null;
        }
    }*/


    /*private void test1() {
*//*        boolean sendMsg = MsgContainer.sendMsg("【大街网】测试扩展码", "13264015025");
        System.out.println("send result:" + sendMsg);*//*
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    byte[] recv = new byte[0];
                    try {
                        recv = getInData();
                    } catch (IOException e) {
                     //   System.out.println("====" + e);
                    }
                    if (recv != null && recv.length > 8) {
                        // MsgHead msgHead = new MsgHead(recv);
                        MsgDeliver msgDeliver = new MsgDeliver(recv);
//                        List<byte[]> list = new ArrayList<byte[]>();
//                        list.add(recv);
//                        CmppSender cmppSender = new CmppSender(socketDOS,socketDIS,list);
                        System.out.println("<Msg_Id>:" + msgDeliver.getMsg_Id() + "<Dest_Id>" + msgDeliver.getDest_Id() + "<Service_Id>" + msgDeliver.getService_Id());
                        System.out.println("================================");
                        System.out.println("<TP_pid>:" + msgDeliver.getTP_pid() + "<TP_udhi>" + msgDeliver.getTP_udhi() + "<Msg_Fmt>" + msgDeliver.getMsg_Fmt());
                        System.out.println("================================");
                        System.out.println("<Src_terminal_Id>:" + msgDeliver.getSrc_terminal_Id() + "<Registered_Delivery>" + msgDeliver.getRegistered_Delivery() + "<Msg_Length>" + msgDeliver.getMsg_Length() + "<Dest_terminal_Id>" + msgDeliver.getDest_terminal_Id() + "<Msg_Content>" + msgDeliver.getMsg_Content());
                        System.out.println("================================");
                        System.out.println("<Msg_Id_report>:" + msgDeliver.getMsg_Id_report() + "<Stat>:" + msgDeliver.getStat() + "<Submit_time>" + msgDeliver.getSubmit_time() + "<Done_time>" + msgDeliver.getDone_time()
                                + "<Dest_terminal_Id>" + msgDeliver.getDest_terminal_Id() + "<SMSC_sequence>:" + msgDeliver.getSMSC_sequence()
                                + "<result>:" + msgDeliver.getResult());
                        MsgDeliverResp msgDeliverResp=new MsgDeliverResp();
                        msgDeliverResp.setTotalLength(12+8+4);
                        msgDeliverResp.setCommandId(MsgCommand.CMPP_DELIVER_RESP);
                        msgDeliverResp.setSequenceId(MsgUtils.getSequence());
                        msgDeliverResp.setMsg_Id(msgDeliver.getMsg_Id());
                        msgDeliverResp.setResult(msgDeliver.getResult());
                        try {
                            //进行回复
                        //    sendMsg(msgDeliverResp.toByteArray());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }*/

/*    private boolean sendMsg(byte[] data) throws Exception {
        try {
            socketDOS.write(data);
            socketDOS.flush();
            return true;
        } catch (NullPointerException ef) {
            System.out.println("在本连结上发送已打包后的消息的字节:无字节输入");
        }
        return false;
    }*/
}
