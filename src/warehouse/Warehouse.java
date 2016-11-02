package warehouse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Warehouse {
    private List<Container> containerList;
    private int size;
    private Lock lock;//добавлен лок

    public Warehouse(int size) {
        containerList = new ArrayList<Container>(size);
        lock = new ReentrantLock();
        this.size = size;
    }

    public boolean addContainer(List<Container> containers) {
        lock.lock();//ограничен вход в секцию
        boolean result = false;
        try {
            if (containerList.size() + containers.size() <= size) {
                result = containerList.addAll(containers);
            }
        } finally {
            lock.unlock();//возвращаем возможность входа в секцию
        }
        return result;
    }

    public List<Container> getContainer(int amount) {
        lock.lock();//ограничен вход в секцию
        try {
            if (containerList.size() >= amount) {
                List<Container> cargo = new ArrayList<Container>(containerList.subList(0, amount));
                containerList.removeAll(cargo);
                return cargo;
            }
        } finally {
            lock.unlock();//возвращаем возможность входа в секцию
        }
        return null;
    }

    public int getSize() {
        return size;
    }

    public int getRealSize() {
        return containerList.size();
    }

    public int getFreeSize() {
        return size - containerList.size();
    }

    public Lock getLock() {
        return lock;
    }

    //добавлены
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Warehouse warehouse = (Warehouse) o;

        if (getSize() != warehouse.getSize()) return false;
        if (containerList != null ? !containerList.equals(warehouse.containerList) : warehouse.containerList != null)
            return false;
        return getLock() != null ? getLock().equals(warehouse.getLock()) : warehouse.getLock() == null;

    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + getSize();
        result = 31 * result + (getLock() != null ? getLock().hashCode() : 0);
        return result;
    }
}
