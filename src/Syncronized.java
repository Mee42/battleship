public class Syncronized<T> {
    private T t;
    private final Lock lock = new Lock();
    private static class Lock{}

    Syncronized(T t){
        this.t = t;
    }
    T get(){
        synchronized (lock){
            return t;
        }
    }
    void set(T t){
        synchronized (lock){
            this.t = t;
        }
    }
    interface Mapper<A>{ A map(A a);}
    void getAndSet(Mapper<T> mapper){
        synchronized (lock){
            t = mapper.map(t);
        }
    }
}
