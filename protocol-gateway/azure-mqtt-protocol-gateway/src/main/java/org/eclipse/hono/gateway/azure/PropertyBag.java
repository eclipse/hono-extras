/*******************************************************************************
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.hono.gateway.azure;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;

/**
 * A collection of methods for processing a <em>property-bag</em> in MQTT topics.
 *
 */
public final class PropertyBag {

    private final Map<String, List<String>> properties;
    private final String topicWithoutPropertyBag;

    private PropertyBag(final String topicWithoutPropertyBag, final Map<String, List<String>> properties) {
        this.properties = properties;
        this.topicWithoutPropertyBag = topicWithoutPropertyBag;
    }

    /**
     * Creates a property bag object from the given topic by retrieving all the properties from the
     * <em>property-bag</em>.
     *
     * @param topic The topic that the message has been published to.
     * @return The property bag object or {@code null} if no <em>property-bag</em> is set in the topic.
     * @throws NullPointerException if topic is {@code null}.
     */
    public static PropertyBag decode(final String topic) {

        Objects.requireNonNull(topic);

        final int index = topic.lastIndexOf("?");
        if (index > 0) {
            return new PropertyBag(
                    topic.substring(0, index),
                    new QueryStringDecoder(topic.substring(index)).parameters());
        }
        return new PropertyBag(topic, null);
    }

    /**
     * Creates a topic with the given properties as an URL-encoded property bag.
     *
     * @param baseTopic The topic to which the properties are appended.
     * @param properties The properties to encode into the result - may be {@code null}.
     * @return A topic string ending with the property bag or the base topic if no properties passed in.
     * @throws NullPointerException if the base topic is {@code null}.
     */
    public static String encode(final String baseTopic, final Map<String, Object> properties) {
        Objects.requireNonNull(baseTopic);

        if (properties == null) {
            return baseTopic;
        } else {
            final QueryStringEncoder queryStringEncoder = new QueryStringEncoder(baseTopic);
            properties.forEach((k, v) -> queryStringEncoder.addParam(k, v.toString()));
            return queryStringEncoder.toString();
        }
    }

    /**
     * Gets a property value from the <em>property-bag</em>.
     *
     * @param name The property name.
     * @return The property value or {@code null} if the property is not set.
     */
    public String getProperty(final String name) {
        return Optional.ofNullable(properties)
                .map(props -> props.get(name))
                .map(values -> values.get(0))
                .orElse(null);
    }

    /**
     * Gets an iterator iterating over the properties.
     *
     * @return The properties iterator.
     */
    public Iterator<Map.Entry<String, String>> getPropertyBagIterator() {
        return Optional.ofNullable(properties)
                .map(props -> props.entrySet().stream()
                        .map(entry -> (Map.Entry<String, String>) new AbstractMap.SimpleEntry<>(entry.getKey(),
                                entry.getValue() != null ? entry.getValue().get(0) : null))
                        .iterator())
                .orElse(Collections.emptyIterator());
    }

    /**
     * Returns the topic without the <em>property-bag</em>.
     *
     * @return The topic without the <em>property-bag</em>.
     */
    public String topicWithoutPropertyBag() {
        return topicWithoutPropertyBag;
    }

}
