package org.apache.storm.starter;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SharedStateBolt extends BaseRichBolt {
    private Map<String, Integer> sharedState;
    private OutputCollector collector;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.sharedState = (ConcurrentHashMap<String, Integer>) context.getResource("sharedState");

    }

    @Override
    public void execute(Tuple tuple) {
        String word = tuple.getStringByField("word");
        Integer count = sharedState.getOrDefault(word, 0);
        count++;
        sharedState.put(word, count);
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

}