package lgvalle.com.fluxtodo.stores;

import java.util.ArrayList;
import java.util.List;

import lgvalle.com.fluxtodo.model.Todo;

/**
 * Created by estsarelv on 23/11/16.
 */

public class TodoState implements StoreState {

    private final List<Todo> todos;
    private Todo lastDeleted;

    public TodoState() {
        todos = new ArrayList<>();
    }

    public TodoState(TodoState other) {
        this.todos = other.todos;
        this.lastDeleted = other.lastDeleted;
    }

    public Todo getLastDeleted() {
        return lastDeleted;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public void setLastDeleted(Todo lastDeleted) {
        this.lastDeleted = lastDeleted;
    }

    @Override
    public String toString() {
        return "TodoState{" +
                "lastDeleted=" + lastDeleted +
                ", todos=" + todos +
                '}';
    }
}
