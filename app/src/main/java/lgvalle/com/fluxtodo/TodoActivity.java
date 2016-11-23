package lgvalle.com.fluxtodo;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;
import lgvalle.com.fluxtodo.actions.Action;
import lgvalle.com.fluxtodo.actions.ActionsCreator;
import lgvalle.com.fluxtodo.dispatcher.Dispatcher;
import lgvalle.com.fluxtodo.stores.StoreState;
import lgvalle.com.fluxtodo.stores.TodoStore;

public class TodoActivity extends AppCompatActivity {

    @BindView(R.id.main_input) EditText mainInput;
    @BindView(R.id.main_layout) ViewGroup mainLayout;
    @BindView(R.id.main_checkbox) CheckBox mainCheck;

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private TodoStore todoStore;
    private TodoRecyclerAdapter listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDependencies();
        setupView();
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get();
        actionsCreator = ActionsCreator.get(dispatcher);
        todoStore = TodoStore.get();

        subscribe();
    }

    private void subscribe() {
        todoStore.getFlowable().subscribe(new Consumer<StoreState>() {
            @Override
            public void accept(StoreState storeState) throws Exception {
                updateUI();
            }
        });
    }

    private void setupView() {
        ButterKnife.bind(this);

        RecyclerView mainList = (RecyclerView) findViewById(R.id.main_list);
        mainList.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new TodoRecyclerAdapter(actionsCreator);
        mainList.setAdapter(listAdapter);
    }

    @OnClick (R.id.main_add)
    public void onAddClick() {
        addTodo();
        resetMainInput();
    }

    @OnClick (R.id.main_checkbox)
    public void onCheckboxClick() {
        checkAll();
    }

    @OnClick (R.id.main_clear_completed)
    public void onClearCompletedClick() {
        clearCompleted();
        resetMainCheck();
    }

    @OnClick (R.id.main_clear_not_completed)
    public void onClearNotCompletedClick() {
        clearNotCompleted();
        resetMainCheck();
    }

    private void updateUI() {
        listAdapter.setItems(todoStore.getTodos());

        if (todoStore.canUndo()) {
            Snackbar snackbar = Snackbar.make(mainLayout, "Element deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actionsCreator.undoDestroy();
                }
            });
            snackbar.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dispatcher.unsubscribeAll();
    }

    private void addTodo() {
        if (validateInput()) {
            actionsCreator.create(getInputText());
        }
    }

    private void checkAll() {
        actionsCreator.toggleCompleteAll();
    }

    private void clearCompleted() {
        actionsCreator.destroyCompleted();
    }

    private void clearNotCompleted() {
        actionsCreator.destroyNotCompleted();
    }

    private void resetMainInput() {
        mainInput.setText("");
    }

    private void resetMainCheck() {
        if (mainCheck.isChecked()) {
            mainCheck.setChecked(false);
        }
    }

    private boolean validateInput() {
        return !TextUtils.isEmpty(getInputText());
    }

    private String getInputText() {
        return mainInput.getText().toString();
    }

}
