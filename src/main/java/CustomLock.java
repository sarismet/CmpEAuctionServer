import java.util.ArrayList;

public class CustomLock {
    Boolean islock = false;
    public synchronized Boolean lock(){
        if(islock){
            return false;
        }
        islock = true;
        return true;
    }
    public synchronized void unlock(){
            islock = false;
            notify();
    }
}
