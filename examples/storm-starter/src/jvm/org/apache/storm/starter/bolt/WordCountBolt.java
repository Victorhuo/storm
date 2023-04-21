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

    private ConcurrentHashMap<String, Object> sharedState;

    private Integer taskId;

    private static final Logger LOG = LoggerFactory.getLogger(WordCountTopologyNode.class);

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        String word = tuple.getString(0);
        Integer value = (Integer) sharedState.compute(word, (k, v) -> {
            if (v == null) {
                return 1;
            } else {
                return (Integer) v + 1;
            }
        });
        LOG.info("taskID : {} get value of {} : {}", taskId, tuple, value);

        collector.emit(new Values(word, value));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "count"));
    }

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context) {
        taskId = context.getThisTaskId();
        sharedState = context.getSharedState().setStateKey("WordCountBolt");
        if (sharedState == null) {
            LOG.info("sharedState is null");
        }
    }
}