/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.storm.starter.bolt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.storm.metric.api.CountMetric;
import org.apache.storm.metric.api.MeanReducer;
import org.apache.storm.metric.api.ReducedMetric;
import org.apache.storm.starter.WordCountTopologyNode;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordCountBolt extends BaseBasicBolt {

    private transient CountMetric countMetric;
    private transient ReducedMetric latencyMetric;
    private ConcurrentHashMap<String, Object> sharedState;

    private Integer taskId;

    private static final Logger LOG = LoggerFactory.getLogger(WordCountTopologyNode.class);

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        String word = tuple.getStringByField("word");
        Integer count;
        long startTime = System.currentTimeMillis();
        if (tuple.contains("count")) {
            count = tuple.getIntegerByField("count");
            LOG.info("taskId get sharedData {},{}:{}", taskId, word, count);
        } else {
            this.countMetric.incr();
            count = 1;
        }

        Integer value = (Integer) sharedState.compute(word, (k, v) -> {
            if (v == null) {
                return count;
            } else {
                return (Integer) v + count;
            }
        });
        long latency = System.currentTimeMillis() - startTime;
        this.latencyMetric.update(latency);
        LOG.info("taskID : {} get value of {} : {}", taskId, tuple, value);

        collector.emit(new Values(word, value));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "value"));
    }

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context) {
        this.countMetric = new CountMetric();
        this.latencyMetric = new ReducedMetric(new MeanReducer());
        context.registerMetric("execute_count", countMetric, 10);
        context.registerMetric("latency_metric", latencyMetric, 10);
        taskId = context.getThisTaskId();
        sharedState = context.getSharedState().setStateKey(taskId);
        if (sharedState == null) {
            LOG.info("sharedState is null");
        }
    }
}