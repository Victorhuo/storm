package org.apache.storm.daemon.worker;

import java.util.List;
import org.apache.storm.utils.Utils;

public class TaskToExecutorGrouper {
    List<Integer> targets;

    public TaskToExecutorGrouper(List<Integer> targets) {
        this.targets = targets;
    }

    public static int objectToIndex(Object val, int numPartitions) {
        if (val == null) {
            return 0;
        }
        return Utils.toPositive(val.hashCode()) % numPartitions; // / 0 exception
    }

    public Integer chooseTasks(List<Object> values) {
        int i = objectToIndex(values.get(0), targets.size());
        return targets.get(i);
    }
}
