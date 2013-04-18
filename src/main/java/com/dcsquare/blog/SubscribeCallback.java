/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANYc KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * imitations under the License.
 */

package com.dcsquare.blog;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * @author Dominik Obermaier
 */
public class SubscribeCallback implements MqttCallback {

    private static final Logger log = LoggerFactory.getLogger(WildcardSubscriber.class);

    private static final long RECONNECT_INTERVAL = TimeUnit.SECONDS.toMillis(10);

    private static final String JDBC_URL = "jdbc:mysql://localhost:8889/mqtt_messages?user=root&password=root";

    private static final String SQL_INSERT = "INSERT INTO `Messages` (`message`,`topic`,`quality_of_service`) VALUES (?,?,?)";


    private final MqttClient mqttClient;

    private PreparedStatement statement;

    public SubscribeCallback(final MqttClient mqttClient) {
        this.mqttClient = mqttClient;

        try {
            final Connection conn = DriverManager.getConnection(JDBC_URL);
            statement = conn.prepareStatement(SQL_INSERT);
        } catch (SQLException e) {
            log.error("Could not connect to database. Exiting", e);
            System.exit(1);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        try {
            log.info("Connection lost. Trying to reconnect");

            //Let's try to reconnect
            mqttClient.connect();

        } catch (MqttException e) {
            log.error("", e);
            try {
                Thread.sleep(RECONNECT_INTERVAL);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
            }
            connectionLost(e);
        }
    }

    @Override
    public void messageArrived(MqttTopic topic, MqttMessage message) throws Exception {
        log.debug("Message arrived with QoS {} on Topic {}. Message size: {} bytes", message.getQos(), topic.getName(), message.getPayload().length);

        try {
            statement.setBytes(1, message.getPayload());
            statement.setString(2, topic.getName());
            statement.setInt(3, message.getQos());

            //Ok, let's persist to the database
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error while inserting", e);
        }

    }

    @Override
    public void deliveryComplete(MqttDeliveryToken token) {
        //no-op
    }
}
