package tw.skyarrow.ehreader.util;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by SkyArrow on 2015/9/26.
 */
// From: http://nerds.weddingpartyapp.com/tech/2014/12/24/implementing-an-event-bus-with-rxjava-rxbus/
public class RxBus<T> {
    private final Subject<T, T> bus = new SerializedSubject<>(PublishSubject.create());

    public void send(T data){
        bus.onNext(data);
    }

    public Observable<T> tObservable(){
        return bus;
    }
}
