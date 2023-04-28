/**
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

package org.apache.storm.starter;

import java.util.ArrayList;

import org.apache.storm.metric.LoggingMetricsConsumer;
import org.apache.storm.starter.bolt.WordCountBolt;
import org.apache.storm.starter.spout.RandomSentenceSpout;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.ConfigurableTopology;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

/**
 * This topology demonstrates Storm's stream groupings and multilang
 * capabilities.
 */
public class WordCountTopology extends ConfigurableTopology {
    public static void main(String[] args) throws Exception {
        ConfigurableTopology.start(new WordCountTopology(), args);
    }

    @Override
    protected int run(String[] args) throws Exception {

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new RandomSentenceSpout(), 3);

        builder.setBolt("count", new WordCountBolt(), 6).shuffleGrouping("spout");

        conf.setDebug(false);

        String topologyName = "word-count";

        conf.setNumWorkers(3);
        ArrayList<String> sharedStateComponent = new ArrayList<String>();
        sharedStateComponent.add("count");
        conf.put("sharedStateComponent", sharedStateComponent);
        conf.registerMetricsConsumer(LoggingMetricsConsumer.class);
        if (args != null && args.length > 0) {
            topologyName = args[0];
        }
        return submit(topologyName, conf, builder);
    }
    //    public static class SplitSentence extends BaseBasicBolt {
    //
    //        public void execute(Tuple tuple, BasicOutputCollector collector) {
    //            String sentence = tuple.getStringByField("word");
    //            String[] words = sentence.split(" ");
    //
    //            for (String word : words) {
    //                collector.emit(new Values(word));
    //            }
    //        }
    //
    //        @Override
    //        public void declareOutputFields(OutputFieldsDeclarer declarer) {
    //            declarer.declare(new Fields("word"));
    //        }
    //
    //    }
}
