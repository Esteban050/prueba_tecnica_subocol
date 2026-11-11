package com.subocol.taller.notificaciones.config;

import com.subocol.taller.notificaciones.event.CitaAgendadaEvent;
import com.subocol.taller.notificaciones.event.CitaCanceladaEvent;
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

    @Value("${app.rabbitmq.queue-citas:notificaciones.citas.queue}")
    private String queueCitasName;

    @Bean
    public TopicExchange tallerExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue notificacionesCitasQueue() {
        return QueueBuilder.durable(queueCitasName).build();
    }

    @Bean
    public Binding bindingCitas(Queue notificacionesCitasQueue, TopicExchange tallerExchange) {
        return BindingBuilder.bind(notificacionesCitasQueue).to(tallerExchange).with("cita.#");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.subocol.taller.agenda.event.CitaAgendadaEvent", CitaAgendadaEvent.class);
        idClassMapping.put("com.subocol.taller.agenda.event.CitaCanceladaEvent", CitaCanceladaEvent.class);
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

