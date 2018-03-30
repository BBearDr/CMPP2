package common.msg.domain;

/**
 * 短信命令代码标识
 * 链接请求、终止连接请求、提交短信请求、长链接激活等
 *
 * @author 张科伟
 * 2011-08-22 11:34
 */
public interface MsgCommand {
    /**
     * CMPP_CONNECT请求连接
     */
    int CMPP_CONNECT = 0x00000001;
    /**
     * 请求连接应答
     */
    int CMPP_CONNECT_RESP = 0x80000001;
    /**
     * 终止连接
     */
    int CMPP_TERMINATE = 0x00000002;
    /**
     * 终止连接应答
     */
    int CMPP_TERMINATE_RESP = 0x80000002;
    /**
     * 提交短信
     */
    int CMPP_SUBMIT = 0x00000004;
    /**
     * 提交短信应答
     */
    int CMPP_SUBMIT_RESP = 0x80000004;
    /**
     * 短信下发
     */
    int CMPP_DELIVER = 0x00000005;
    /**
     * 下发短信应答
     */
    int CMPP_DELIVER_RESP = 0x80000005;
    /**
     * 发送短信状态查询
     */
    int CMPP_QUERY = 0x00000006;
    /**
     * 发送短信状态查询应答
     */
    int CMPP_QUERY_RESP = 0x80000006;
    /**
     * 删除短信
     */
    int CMPP_CANCEL = 0x00000007;
    /**
     * 删除短信应答
     */
    int CMPP_CANCEL_RESP = 0x80000007;
    /**
     * 激活测试
     */
    int CMPP_ACTIVE_TEST = 0x00000008;
    /**
     * 激活测试应答
     */
    int CMPP_ACTIVE_TEST_RESP = 0x80000008;
    /**
     * 连接消息长度
     */
    int CMPP_CONNECT_LEN = 6 + 16 + 1 + 4;
    /**
     * 连接响应data长度
     */
    int CMPP_CONNECT_RESR_LEN = 8 + 1 + 16 + 1;
    /**
     * 状态响应data
     */
    int CMPP_DELIVER_LEN = 8 + 21 + 10 + 1 + 1 + 1 + 21 + 1 + 1 + 8;
    /**
     * 短信提交状态data
     */
    int CMPP_SUBMIT_RESP_LEN = 8 + 8 + 1;

}
