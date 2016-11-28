package co.gadder.gadder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class InputFragment extends Fragment {

    public InputFragment() {
    }

    public static InputFragment newInstance() {
        InputFragment fragment = new InputFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_input, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        FrameLayout layout = (FrameLayout) getActivity().findViewById(R.id.inputLayout);
//        layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getFragmentManager().beginTransaction()
//                        .remove(InputFragment.this)
//                        .commit();
//            }
//        });


        RecyclerView recycler = (RecyclerView) getActivity().findViewById(R.id.inputRecyclerView);
        recycler.setHasFixedSize(true);
        ActivityTypeRecyclerAdapter adapter = new ActivityTypeRecyclerAdapter(getActivity());
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
//
//        FloatingActionButton cancel = (FloatingActionButton) getActivity().findViewById(R.id.cancelFab);
//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getFragmentManager().beginTransaction()
//                        .remove(InputFragment.this)
//                        .commit();
//            }
//        });
    }
}
