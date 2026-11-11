package com.subocol.taller.agenda.config;

import com.subocol.taller.agenda.event.EstadoOrdenCambiadoEvent;
import com.subocol.taller.agenda.event.OrdenCerradaEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.exchange:taller.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue-ordenes:agenda.ordenes.queue}")
    private String queueOrdenesName;

    @Bean
    public TopicExchange tallerExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue agendaOrdenesQueue() {
        return QueueBuilder.durable(queueOrdenesName).build();
    }

    @Bean
    public Binding bindingOrdenes(Queue agendaOrdenesQueue, TopicExchange tallerExchange) {
        return BindingBuilder.bind(agendaOrdenesQueue).to(tallerExchange).with("orden.#");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.subocol.taller.ordenes.event.EstadoOrdenCambiadoEvent", EstadoOrdenCambiadoEvent.class);
        idClassMapping.put("com.subocol.taller.ordenes.event.OrdenCerradaEvent", OrdenCerradaEvent.class);
        typeMapper.setIdClassMapping(idClassMapping);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}

