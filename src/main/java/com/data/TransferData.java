package com.data;

import lombok.Data;

/**
 * @Description:
 * @Author: baozi
 * @Create: 2019-01-03 17:23
 */
@Data
public class TransferData {

    /**
     * 传输类型
     */
    private String type;

    /**
     * 业务数据
     */
    private String content;

    /**
     * 回馈消息
     */
    private String ack;
}
