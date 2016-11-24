package lgvalle.com.fluxtodo.stores;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.reactivex.functions.Consumer;
import lgvalle.com.fluxtodo.actions.Action;
import lgvalle.com.fluxtodo.actions.TodoActions;
import lgvalle.com.fluxtodo.dispatcher.Dispatcher;
import lgvalle.com.fluxtodo.model.Todo;

/**
 * Created by lgvalle on 02/08/15.
 */
public class TodoStore extends Store {

    private static TodoStore instance;

    protected TodoStore() {
        subscribeToActions();
    }

    public static TodoStore get() {
        if (instance == null) {
            instance = new TodoStore();
        }
        return instance;
    }

    public List<Todo> getTodos() {
        return ((TodoState) getState()).getTodos();
    }

    public boolean canUndo() {
        return ((TodoState) getState()).getLastDeleted() != null;
    }

    @Override
    public TodoState initState() {
        return new TodoState();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void subscribeToActions() {

        Dispatcher.get().subscribe(TodoActions.TODO_CREATE, new Consumer<Action>() {
            @Override
            public void accept(Action action) throws Exception {
                String text = ((String) action.getData().get(TodoActions.KEY_TEXT));
                updateState(create(((TodoState) getState()), text));
            }
        });

        Dispatcher.get().subscribe(TodoActions.TODO_DESTROY, new Consumer<Action>() {
            @Override
            public void accept(Action action) throws Exception {
                long id = ((long) action.getData().get(TodoActions.KEY_ID));
                updateState(destroy(((TodoState) getState()), id));
            }
        });

        Dispatcher.get().subscribe(TodoActions.TODO_UNDO_DESTROY, new Consumer<Action>() {
            @Override
            public void accept(Action action) throws Exception {
                updateState(undoDestroy(((TodoState) getState())));
            }
        });

        Dispatcher.get().subscribe(TodoActions.TODO_COMPLETE, new Consumer<Action>() {
            @Override
            public void accept(Action action) throws Exception {
                long id = ((long) action.getData().get(TodoActions.KEY_ID));
                updateState(updateComplete(((TodoState) getState()), id, true));
            }
        });

        Dispatcher.get().subscribe(TodoActions.TODO_UNDO_COMPLETE, new Consumer<Action>() {
            @Override
            public void accept(Action action) throws Exception {
                long id = ((long) action.getData().get(TodoActions.KEY_ID));
                updateState(updateComplete(((TodoState) getState()), id, false));
            }
        });

        Dispatcher.get().subscribe(TodoActions.TODO_DESTROY_COMPLETED, new Consumer<Action>() {
            @Override
            public void accept(Action action) throws Exception {
                updateState(destroyCompleted(((TodoState) getState())));
            }
        });

        Dispatcher.get().subscribe(TodoActions.TODO_DESTROY_NOT_COMPLETED, new Consumer<Action>() {
            @Override
            public void accept(Action action) throws Exception {
                updateState(destroyNotCompleted(((TodoState) getState())));
            }
        });

        Dispatcher.get().subscribe(TodoActions.TODO_TOGGLE_COMPLETE_ALL, new Consumer<Action>() {
            @Override
            public void accept(Action action) throws Exception {
                updateState(updateCompleteAll(((TodoState) getState())));
            }
        });

    }

    private void updateState(TodoState newState) {
        if (newState != null) {
            setState(newState);
        }
    }

    private TodoState destroyCompleted(TodoState state) {
        TodoState newState = new TodoState(state);
        Iterator<Todo> iter = newState.getTodos().iterator();
        while (iter.hasNext()) {
            Todo todo = iter.next();
            if (todo.isComplete()) {
                iter.remove();
            }
        }

        newState.setLastDeleted(null);

        return newState;
    }

    private TodoState destroyNotCompleted(TodoState state) {
        TodoState newState = new TodoState(state);
        Iterator<Todo> iter = newState.getTodos().iterator();
        while (iter.hasNext()) {
            Todo todo = iter.next();
            if (!todo.isComplete()) {
                iter.remove();
            }
        }

        newState.setLastDeleted(null);

        return newState;
    }

    private TodoState updateCompleteAll(TodoState state) {
        TodoState newState;
        if (areAllComplete(state)) {
            newState = updateAllComplete(state, false);
        } else {
            newState = updateAllComplete(state, true);
        }

        newState.setLastDeleted(null);

        return newState;
    }

    private boolean areAllComplete(TodoState state) {
        for (Todo todo : state.getTodos()) {
            if (!todo.isComplete()) {
                return false;
            }
        }
        return true;
    }

    private TodoState updateAllComplete(TodoState state, boolean complete) {
        TodoState newState = new TodoState(state);
        for (Todo todo : newState.getTodos()) {
            todo.setComplete(complete);
        }

        newState.setLastDeleted(null);

        return newState;
    }

    private TodoState updateComplete(TodoState state, long id, boolean complete) {
        TodoState newState = new TodoState(state);
        Todo todo = getById(newState.getTodos(), id);
        if (todo != null) {
            todo.setComplete(complete);
            return newState;
        }

        newState.setLastDeleted(null);

        return null;
    }

    private TodoState undoDestroy(TodoState state) {
        if (state.getLastDeleted() != null) {
            TodoState newState = addElement(state, state.getLastDeleted().clone());
            newState.setLastDeleted(null);
            return newState;
        }

        return null;
    }

    private TodoState create(TodoState state, String text) {
        long id = System.currentTimeMillis();
        Todo todo = new Todo(id, text);
        return addElement(state,todo);
    }

    private TodoState destroy(TodoState state, long id) {
        TodoState newState = new TodoState(state);
        Iterator<Todo> iter = state.getTodos().iterator();
        while (iter.hasNext()) {
            Todo todo = iter.next();
            if (todo.getId() == id) {
                newState.setLastDeleted(todo.clone());
                setState(newState);
                iter.remove();
                break;
            }
        }

        return newState;
    }

    private Todo getById(List<Todo> todoList, long id) {
        Iterator<Todo> iter = todoList.iterator();
        while (iter.hasNext()) {
            Todo todo = iter.next();
            if (todo.getId() == id) {
                return todo;
            }
        }
        return null;
    }


    private TodoState addElement(TodoState state, Todo clone) {
        TodoState newState = new TodoState(state);
        newState.getTodos().add(clone);
        Collections.sort(newState.getTodos());
        newState.setLastDeleted(null);
        return newState;
    }

}
