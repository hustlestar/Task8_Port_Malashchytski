package port;

import org.apache.log4j.Logger;
import ship.Ship;
import warehouse.Container;
import warehouse.Warehouse;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Port {
    private final static Logger logger = Logger.getRootLogger();

    private BlockingQueue<Berth> berthList;
    private volatile Warehouse portWarehouse;//добавлен волатайл
    private ConcurrentMap<Ship, Berth> usedBerths; // заменен на многопоточный
    private Lock lock;//добавлен лок

    public Warehouse getPortWarehouse() {
        return portWarehouse;
    }

    public Port(int berthSize, int warehouseSize) {
        portWarehouse = new Warehouse(warehouseSize);
        berthList = new ArrayBlockingQueue<Berth>(berthSize);
        for (int i = 0; i < berthSize; i++) {
            berthList.add(new Berth(i+1, portWarehouse));
        }
        usedBerths = new ConcurrentHashMap<Ship, Berth>();
        lock = new ReentrantLock();//многоразовый замок
        logger.debug("Порт создан.");
    }

    public void setContainersToWarehouse(List<Container> containerList) {
        portWarehouse.addContainer(containerList);
    }

    public boolean lockBerth(Ship ship) {
        lock.lock();//закрываем вход в секцию
        Berth berth;
        try {
            if (!berthList.isEmpty()) {
                berth = berthList.take();
                usedBerths.put(ship, berth);
                logger.info("Корабль " + ship.getName() + " у причала №" + berth.getId());
                if (berthList.isEmpty()) {
                    logger.debug("В порту нет пустых причалов");
                }
            } else {
                logger.debug("Для корабля " + ship.getName() + " нет пустых причалов");
                return false;
            }
        } catch (InterruptedException e) {
            logger.debug("Кораблю " + ship.getName() + " отказано в швартовке.");
            return false;
        } finally {
            lock.unlock();//открываем доступ к секции
        }
        return true;
    }


    public boolean unlockBerth(Ship ship) {
        lock.lock();//закрываем вход в секцию
        Berth berth = usedBerths.get(ship);

        try {
            berthList.put(berth);
            usedBerths.remove(ship);
            logger.info("Корабль " + ship.getName() + " покинул причал №" + berth.getId());
            if (!berthList.isEmpty()) {
                logger.debug("В порту есть пустые причалы и контейнеры - " + portWarehouse.getRealSize() );
            }
        } catch (InterruptedException e) {
            logger.debug("Корабль " + ship.getName() + " не смог отшвартоваться.");
            return false;
        } finally {
            lock.unlock();//открываем доступ к секции
        }
        return true;
    }

    public Berth getBerth(Ship ship) throws PortException {

        Berth berth = usedBerths.get(ship);
        if (berth == null) {
            throw new PortException("Try to use Berth without blocking.");
        }
        return berth;
    }

    //добавлены
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Port port = (Port) o;

        if (berthList != null ? !berthList.equals(port.berthList) : port.berthList != null) return false;
        if (getPortWarehouse() != null ? !getPortWarehouse().equals(port.getPortWarehouse()) : port.getPortWarehouse() != null)
            return false;
        if (usedBerths != null ? !usedBerths.equals(port.usedBerths) : port.usedBerths != null) return false;
        return lock != null ? lock.equals(port.lock) : port.lock == null;

    }

    @Override
    public int hashCode() {
        return lock != null ? lock.hashCode() : 0;
    }
}
