package org.zkoss.ganttz.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * This class contains the information of a task container. It can be modified
 * and notifies of the changes to the interested parties. <br/>
 * Created at Jul 1, 2009
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class TaskContainerBean extends TaskBean {

    private static <T> List<T> removeNulls(Collection<T> elements) {
        ArrayList<T> result = new ArrayList<T>();
        for (T e : elements) {
            if (e != null) {
                result.add(e);
            }
        }
        return result;
    }

    private static <T> T getSmallest(Collection<T> elements,
            Comparator<T> comparator) {
        List<T> withoutNulls = removeNulls(elements);
        if (withoutNulls.isEmpty())
            throw new IllegalArgumentException("at least one required");
        T result = null;
        for (T element : withoutNulls) {
            result = result == null ? element : (comparator.compare(result,
                    element) < 0 ? result : element);
        }
        return result;
    }

    private static <T extends Comparable<? super T>> T getSmallest(
            Collection<T> elements) {
        return getSmallest(elements, new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }
        });
    }

    private static <T extends Comparable<? super T>> T getBiggest(
            Collection<T> elements) {
        return getSmallest(elements, new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                return o2.compareTo(o1);
            }
        });
    }

    private List<TaskBean> tasks = new ArrayList<TaskBean>();

    public void add(TaskBean task) {
        tasks.add(task);
    }

    public List<TaskBean> getTasks() {
        return tasks;
    }

    public Date getSmallestBeginDateFromChildren() {
        if (tasks.isEmpty())
            return getBeginDate();
        return getSmallest(getStartDates());
    }

    private List<Date> getStartDates() {
        ArrayList<Date> result = new ArrayList<Date>();
        for (TaskBean taskBean : tasks) {
            result.add(taskBean.getBeginDate());
        }
        return result;
    }

    private List<Date> getEndDates() {
        ArrayList<Date> result = new ArrayList<Date>();
        for (TaskBean taskBean : tasks) {
            result.add(taskBean.getEndDate());
        }
        return result;
    }

    public Date getBiggestDateFromChildren() {
        if (tasks.isEmpty())
            return getEndDate();
        return getBiggest(getEndDates());
    }

}