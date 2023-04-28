package org.apache.storm.daemon.worker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.storm.executor.Executor;
import org.apache.storm.tuple.AddressedTuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.TupleImpl;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class WorkerStateTransfer {
    private Map<String, List<Integer>> componentToTasks;
    private ArrayList<Integer> localTasks;
    private List<Integer> comTasks;
    private static final Logger LOG = LoggerFactory.getLogger(WorkerStateTransfer.class);
    private Executor executor;
    private Integer targetTask;
    private String preKey;
    private Object preValue;

    public WorkerStateTransfer(Map<String, List<Integer>> componentToTasks,
                               ArrayList<Integer> localTasks, Executor executor) {
        this.componentToTasks = componentToTasks;
        this.localTasks = localTasks;
        this.executor = executor;
        Map<String, Map<String, Fields>> comToFields = this.executor.getWorkerTopologyContext().getComponentFields();
        comToFields.get(executor.getComponentId()).put("sharestream", new Fields("word", "count"));
        LOG.info("flag1: {}", comToFields);
    }

    // 1 1, 4, 7, 10, 13, 16, 19, 22, 25, 28
    // 2 2, 5, 8, 11, 14, 17, 20, 23, 26
    // 3 3, 6, 9, 12, 15, 18, 21, 24, 27
    private void sendStateToTargetTask(String key, Object value, Integer targetTask) {
        List<Object> values = new Values(key, value);
        TupleImpl t = new TupleImpl(executor.getWorkerTopologyContext(), values, executor.getComponentId(), 0, "sharestream", null);
        AddressedTuple addressedTuple = new AddressedTuple(targetTask, t);
        LOG.info("transfered state to task {}:{}", targetTask, t.getValues());
        // executor.getExecutorTransfer().tryTransfer(addressedTuple, null);
    }

    public void transferState(String componentId, ConcurrentHashMap<String, Object> sharedState) {
        if (sharedState == null) {
            return ;
        }
        comTasks =  componentToTasks.get(componentId);
        LOG.info("before transfering states sharedstate size: {}", sharedState.size());
        for (Iterator<Entry<String, Object>> iter = sharedState.entrySet().iterator(); iter.hasNext(); ) {
            Entry<String, Object> entry = iter.next();
            sharedState.compute(entry.getKey(), (k, v) -> fun(k, v));
            // sent preK,perV to targetTask;
            if (preKey != null && preValue != null && targetTask != null) {
                sendStateToTargetTask(preKey, preValue, targetTask);
                preKey = null;
                preValue = null;
                targetTask = null;
            }
        }
        LOG.info("transfer all states now sharedstate size: {}", sharedState.size());
    }

    private Object fun(String k, Object v) {
        int targetWorkerIndex = Utils.toPositive(k.hashCode()) % comTasks.size();
        if (!localTasks.contains(comTasks.get(targetWorkerIndex))) {
            targetTask = comTasks.get(targetWorkerIndex);
            preValue = v;
            preKey = k;
            return null;
        }
        return v;
    }
}
