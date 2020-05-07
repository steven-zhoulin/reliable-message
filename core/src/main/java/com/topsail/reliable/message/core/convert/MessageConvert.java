package com.topsail.reliable.message.core.convert;

import com.topsail.reliable.message.core.entity.event.AccountChangeEvent;
import com.topsail.reliable.message.core.util.JsonUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author Steven
 * @date 2020-04-30
 */
public class MessageConvert {

    public static final AccountChangeEvent from(Message message) {
        String messageString = new String((byte[]) message.getPayload());
        AccountChangeEvent accountChangeEvent = JsonUtils.decode(messageString, AccountChangeEvent.class);
        return accountChangeEvent;
    }

    public static final AccountChangeEvent from(MessageExt messageExt) {
        String jsonString = new String(messageExt.getBody());
        AccountChangeEvent accountChangeEvent = JsonUtils.decode(jsonString, AccountChangeEvent.class);
        return accountChangeEvent;
    }

    public static final Message<String> from(AccountChangeEvent accountChangeEvent) {
        String jsonString = JsonUtils.encode(accountChangeEvent);
        /** 这里将 事务Id 作为 业务 Key */
        Message<String> message = MessageBuilder.withPayload(jsonString).setHeader("KEYS", accountChangeEvent.getTransactionId()).build();
        return message;
    }

}
