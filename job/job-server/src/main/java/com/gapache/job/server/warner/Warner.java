package com.gapache.job.server.warner;

/**
 * 失败告警接口
 *
 * @author HuSen
 * @since 2021/2/8 2:39 下午
 */
public interface Warner {

    /**
     * 发送警告接口
     * 由子类实现
     *
     * @param subject 标题
     * @param text    内容
     * @param to      收件人
     * @param from    发件人
     */
    void warning(String subject, String text, String to, String from);
}
