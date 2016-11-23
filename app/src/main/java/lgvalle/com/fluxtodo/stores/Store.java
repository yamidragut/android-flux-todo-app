package lgvalle.com.fluxtodo.stores;

import lgvalle.com.fluxtodo.actions.Action;
import lgvalle.com.fluxtodo.actions.ActionsCreator;
import lgvalle.com.fluxtodo.dispatcher.Dispatcher;

/**
 * Created by lgvalle on 02/08/15.
 */
public abstract class Store {

    final private ActionsCreator actionsCreator;

    protected Store(Dispatcher dispatcher) {
        actionsCreator = ActionsCreator.get(dispatcher);
    }

    void emitStoreChange() {
        actionsCreator.updateUi();
    }

    public abstract void onAction(Action action);

}
