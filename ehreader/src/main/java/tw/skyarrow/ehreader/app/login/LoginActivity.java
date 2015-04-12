package tw.skyarrow.ehreader.app.login;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.event.LoginEvent;

public class LoginActivity extends ActionBarActivity {
    @InjectView(R.id.username)
    EditText mUsernameInput;

    @InjectView(R.id.password)
    EditText mPasswordInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEvent(LoginEvent event){
        switch (event.getEvent()){
            case LoginEvent.EVENT_SUCCESS:
                finish();
                break;

            case LoginEvent.EVENT_FAILED:
                LoginErrorDialog dialog = LoginErrorDialog.newInstance();
                dialog.show(getSupportFragmentManager(), LoginErrorDialog.TAG);
                break;
        }
    }

    @OnClick(R.id.login_btn)
    void onLoginBtnClick(){
        String username = mUsernameInput.getText().toString();
        String password = mPasswordInput.getText().toString();

        mUsernameInput.setError(null);
        mPasswordInput.setError(null);

        if (username.isEmpty()){
            mUsernameInput.setError(getString(R.string.login_username_required));
        }

        if (password.isEmpty()){
            mPasswordInput.setError(getString(R.string.login_password_required));
        }

        if (mUsernameInput.getError() != null || mPasswordInput.getError() != null){
            return;
        }

        LoginDialog dialog = LoginDialog.newInstance(username, password);
        dialog.show(getSupportFragmentManager(), LoginDialog.TAG);
    }
}
