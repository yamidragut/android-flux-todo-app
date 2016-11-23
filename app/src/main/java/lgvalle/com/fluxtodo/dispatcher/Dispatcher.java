package lgvalle.com.fluxtodo.dispatcher;

import android.util.Log;

import java.util.ArrayList;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import lgvalle.com.fluxtodo.actions.Action;

/**
 * Created by lgvalle on 19/07/15.
 */
public class Dispatcher {

    private static final String TAG = Dispatcher.class.getSimpleName();

    private Flowable<Action> actionFlowable;
    private static Dispatcher instance;

    private final CompositeDisposable actionsDisposables = new CompositeDisposable();

    private static final ArrayList<Consumer<Action>> consumersList = new ArrayList<>();

    public static Dispatcher get() {
        if (instance == null) {
            instance = new Dispatcher();
        }
        return instance;
    }

    Dispatcher() {

    }

    public void subscribe(Consumer<Action> consumer) {
        consumersList.add(consumer);
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
        post(actionBuilder.build());
    }

    private boolean isEmpty(String type) {
        return type == null || type.isEmpty();
    }

    private void post(final Action action) {
//        actionFlowable = Flowable.just(action);

        for (Consumer<Action> consumer : consumersList) {
//            actionsDisposables.add(actionFlowable.subscribe(consumer));

            try {
                consumer.accept(action);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
