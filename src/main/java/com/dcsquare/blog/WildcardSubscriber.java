package com.dcsquare.blog;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dominik Obermaier
 */
public class WildcardSubscriber {

    private static final Logger log = LoggerFactory.getLogger(WildcardSubscriber.class);


    public static final String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";

    private static final String TOPIC_SUBSCRIPTION = "#";

    private static final int QUALITY_OF_SERVICE_LEVEL = 2;

    public void start() {
        log.info("Starting Wilcard subscriber");
        try {
            final String clientId = MqttClient.generateClientId();
            final MqttClient mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());

            mqttClient.setCallback(new SubscribeCallback(mqttClient));

            log.info("Connecting to {} with clientID {}", BROKER_URL, clientId);
            mqttClient.connect();

            //We subscribe to wildcard
            log.info("Subscribing to {}", TOPIC_SUBSCRIPTION);
            mqttClient.subscribe(TOPIC_SUBSCRIPTION, QUALITY_OF_SERVICE_LEVEL);

        } catch (MqttException e) {
            log.error("An unexpected error occured. Exiting", e);
            System.exit(1);
        }
    }

    public static void main(String... args) {
        final WildcardSubscriber subscriber = new WildcardSubscriber();
        subscriber.start();
    }

}
