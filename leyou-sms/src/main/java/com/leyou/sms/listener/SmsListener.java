package com.leyou.sms.listener;


import com.aliyuncs.exceptions.ClientException;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.util.Map;

@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {
    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties smsProperties;
    //接收 rabbit 发过来的消息队列
    @RabbitListener(bindings = @QueueBinding(
            value= @Queue(value="leyou.sms.queue",durable="true" ),
            exchange =@Exchange(value="leyou.sms.exchange",ignoreDeclarationExceptions ="true",
                    type= ExchangeTypes.TOPIC),
            key={"verify.code.sms"}
    ))
    public  void  sendSms(Map<String,String> msg) throws ClientException {
       if(CollectionUtils.isEmpty(msg)){
           return ;
       }
     String phone = msg.get("phone");
     String code = msg.get("code");
     if(StringUtils.isNoneBlank(phone)  && StringUtils.isNoneBlank(code)){


       // SendSmsResponse sendSms(String phone, String code, String signName, String template)
        this.smsUtils.sendSms(phone,code,this.smsProperties.getSignName(),this.smsProperties.getVerifyCodeTemplate());
     }
    }
}
