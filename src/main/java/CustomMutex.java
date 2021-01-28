import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class CustomMutex implements Lock {

    ArrayList<String> readingThreadIds;
    CustomLock mylock;
    String purpose;
    public CustomMutex(String purpose){
        this.mylock = new CustomLock();
        this.purpose = purpose;
        readingThreadIds = new ArrayList<String>();
    }

    public void requestCS () {
        while (!this.mylock.lock());
        while (this.readingThreadIds.size()>0){
            System.out.println("readingThreadIds is present");
        }
    }

    public void releaseCS () {
        this.mylock.unlock();
    }
    public void requestReading(String uuid) {
        readingThreadIds.add(uuid);
    }

    public void releaseReading(String uuid) {
        readingThreadIds.remove(readingThreadIds.indexOf(uuid));
    }
    @Override
    public void lock() {

    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {

    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
