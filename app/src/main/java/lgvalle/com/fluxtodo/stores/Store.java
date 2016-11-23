package lgvalle.com.fluxtodo.stores;

import android.support.annotation.NonNull;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import lgvalle.com.fluxtodo.actions.Action;
import lgvalle.com.fluxtodo.actions.ActionsCreator;
import lgvalle.com.fluxtodo.dispatcher.Dispatcher;


public abstract class Store {

    private final PublishProcessor<StoreState> publishProcessor = PublishProcessor.create();
    private StoreState state;


    /**
     * Observable state.
     */
    public Flowable<StoreState> getFlowable() {
        return publishProcessor;
    }

    /**
     * Current TodoState
     */
    @NonNull
    public final StoreState getState() {
        if (state == null) {
            setState(initState());
        }
        return state;
    }

    protected void setState(StoreState newState) {
        if (state != null && state.equals(newState)) {
            return;
        }

        state = newState;
        publishProcessor.onNext(state);
    }

    public abstract StoreState initState();
    public abstract void onAction(Action action);

}
