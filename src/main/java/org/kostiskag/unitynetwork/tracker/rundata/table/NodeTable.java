package org.kostiskag.unitynetwork.tracker.rundata.table;

import org.kostiskag.unitynetwork.tracker.rundata.entry.NodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.entry.RedNodeEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NodeTable<N extends NodeEntry> {
    protected static final int TIMEOUT_SECONDS = 5;
    protected final List<N> list;
    protected final Lock orb = new ReentrantLock(true);

    protected NodeTable() {
        list = new ArrayList<N>();
    }

    /**
    WE USED to have synchronized methods for table access but this
    approach was found to be ineffective as one calling thread was
    frequently in the need to call several methods before
    making a complete and meaningful action on the table like ex.
    1. check if bn named "pakis" exists
    2. if yes get me its instance

    with sync methods there was no guarantee that after method 1 another thread wouldn't
    have deleted "pakis" before 2 was called!

    SO NOW, the caller gets the orb, does his action by calling one or several methods
    gives the orb back when he completes his action!

    This method also lets the caller use Optionals and streams as by their nature there was no
    guarantee for which point in time the caller would decide to consume one!

    To improve matters further and to be sure the caller owns the orb before calling
    anything, he has to also pass it as argument in the calling method!

    every caller should do :
    try {
        Lock l = aquireLock();
        findSmth(lock, args);
        findAnotherSmth(lock, args);
    } catch interupted {
        log("unbelivable!!!");
    } finally {
        readLock.unlock();
    }

    The inner method on its turn has to validate the lock to ensure it was not called from
    a caller without having a lock

    public int getSmth(Lock lock, String name) throws InterruptedException {
        validateLock(lock);
        do stuff...
        return ...
    }
	**/
    public Lock aquireLock() throws InterruptedException {
        orb.tryLock(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return orb;
    }

    /**
     * To be used inside a finally block
     */
    public void releaseLock() {
        orb.unlock();
    }

    /**
     * To be called by all inner methods
     * Ensures the lock is called before attempting to call a method
     *
     * @param lock
     * @throws InterruptedException
     */
    protected void validateLock(Lock lock) throws InterruptedException {
        if (lock != orb) {
            throw new InterruptedException("the given lock is not the BNtable's orb!");
        }
    }

    //accessors!
    public int getSize(Lock lock) throws InterruptedException {
        validateLock(lock);
        return list.size();
    }

    public boolean isOnline(Lock lock, N toBeChecked) throws InterruptedException {
        return getOptionalNodeEntry(lock, toBeChecked).isPresent();
    }

    public boolean isOnline(Lock lock, String hostname) throws InterruptedException {
        return getOptionalNodeEntry(lock, hostname).isPresent();
    }

    public Optional<N> getOptionalNodeEntry(Lock lock, N toBeChecked) throws InterruptedException {
        validateLock(lock);
        return list.stream()
                .filter(n -> n.equals(toBeChecked))
                .findFirst();
    }

    public Optional<N> getOptionalNodeEntry(Lock lock, String hostname) throws InterruptedException {
        validateLock(lock);
        return list.stream()
                .filter(n -> n.getHostname().equals(hostname))
                .findFirst();
    }

    public N getNodeEntry(Lock lock, N toBeChecked) throws IllegalAccessException, InterruptedException {
        validateLock(lock);
        Optional<N> e = getOptionalNodeEntry(lock, toBeChecked);
        if (e.isPresent()) {
            return e.get();
        }
        throw new IllegalAccessException("the given node was not found on table "+toBeChecked);
    }

    public N getNodeEntry(Lock lock, String hostname) throws InterruptedException, IllegalAccessException {
        validateLock(lock);
        Optional<N> n = getOptionalNodeEntry(lock, hostname);
        if (n.isPresent()) {
            return n.get();
        }
        throw new IllegalAccessException("the given node was not found on table "+hostname);
    }

}
