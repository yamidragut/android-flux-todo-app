package lgvalle.com.fluxtodo.dispatcher;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import lgvalle.com.fluxtodo.actions.Action;

/**
 * Created by lgvalle on 19/07/15.
 */
public class Dispatcher {

    private static final String TAG = Dispatcher.class.getSimpleName();

    private final PublishProcessor<Action> publishProcessor = PublishProcessor.create();
    private static Dispatcher instance;

    private final CompositeDisposable actionsDisposables = new CompositeDisposable();

    public static Dispatcher get() {
        if (instance == null) {
            instance = new Dispatcher();
        }
        return instance;
    }

    Dispatcher() { }

    public void subscribe(Consumer<Action> consumer) {
        actionsDisposables.add(publishProcessor.subscribe(consumer));
    }

    public void unsubscribeAll() {
        actionsDisposables.clear();
    }

    public void dispatch(String type, Object... data) {
        if (isEmpty(type)) {
            throw new IllegalArgumentException("Type must not be empty");
        }

        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("Data must be a valid list of key,value pairs");
        }

        Action.Builder actionBuilder = Action.type(type);
        int i = 0;
        while (i < data.length) {
            String key = (String) data[i++];
            Object value = data[i++];
            actionBuilder.bundle(key, value);
        }

        dispatch(actionBuilder.build());
    }

    public void dispatch(final Action action) {
        publishProcessor.onNext(action);
    }

    private boolean isEmpty(String type) {
        return type == null || type.isEmpty();
    }

}
