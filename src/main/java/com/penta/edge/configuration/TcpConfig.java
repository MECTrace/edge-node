package com.penta.edge.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableIntegration
public class TcpConfig {

    @Value("${tcp.server.port}")
    private int tcpServerPort;




    @Bean
    public AbstractServerConnectionFactory connectionFactory() {
        CustomTcpSerializer customTcpSerializer = new CustomTcpSerializer();
        TcpNioServerConnectionFactory connectionFactory = new TcpNioServerConnectionFactory(tcpServerPort);
        connectionFactory.setSerializer(customTcpSerializer);
        connectionFactory.setDeserializer(customTcpSerializer);
        connectionFactory.setSingleUse(true);
        return connectionFactory;
    }

    @Bean
    public MessageChannel inboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel replyChannel() {
        return new DirectChannel();
    }

    @Bean
    public TcpInboundGateway inboundGateway(AbstractServerConnectionFactory connectionFactory, MessageChannel inboundChannel, MessageChannel replyChannel) {
        TcpInboundGateway tcpInboundGateway = new TcpInboundGateway();
        tcpInboundGateway.setConnectionFactory(connectionFactory);
        tcpInboundGateway.setRequestChannel(inboundChannel);
        tcpInboundGateway.setReplyChannel(replyChannel);
        return tcpInboundGateway;
    }


}
