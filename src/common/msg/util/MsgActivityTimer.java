package common.msg.util;


/**
 * 接口调用
 *
 * @author junxiong.chen
 */
public class MsgActivityTimer {
    /**
     * 短信接口长链接，定时进行链路检查
     */
    public void executeInternal() {
        System.out.println("×××××××××××××开始链路检查××××××××××××××");
        int count = 0;
        boolean result = MsgContainer.activityTestISMG();
        while (!result) {
            count++;
            result = MsgContainer.activityTestISMG();
            if (count >= (MsgConfig.getConnectCount() - 1)) {//如果再次链路检查次数超过两次则终止连接
                break;
            }
        }
        System.out.println("×××××××××××××链路检查结束××××××××××××××");
    }

    public static void main(String[] args) {
        MsgActivityTimer msgActivityTimer = new MsgActivityTimer();
        msgActivityTimer.executeInternal();
    }
}
