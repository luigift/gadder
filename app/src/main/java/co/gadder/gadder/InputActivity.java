package co.gadder.gadder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class InputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        getSupportFragmentManager().beginTransaction()
                .addToBackStack("inputFragment")
                .add(R.id.activity_input, InputFragment.newInstance())
                .commit();
    }
}
