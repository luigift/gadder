package co.gadder.gadder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextFragment extends Fragment {

    public static final String PRIVACY_POLICY = "privacy_policy";
    public static final String TERMS_OF_SERVICE = "terms_of_service";

    private static final String ARG_TEXT_TYPE = "text_type";

    private String mTextType;

    public TextFragment() {
        // Required empty public constructor
    }

    public static TextFragment newInstance(String textType) {
        TextFragment fragment = new TextFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT_TYPE, textType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTextType = getArguments().getString(ARG_TEXT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mTextType != null && !mTextType.isEmpty()) {

            final TextView title = (TextView) getActivity().findViewById(R.id.textTitle);
            final TextView content = (TextView) getActivity().findViewById(R.id.textContent);

            switch (mTextType) {
                case PRIVACY_POLICY:

                    title.setText(getString(R.string.privacy_policy_title));
                    content.setText(getString(R.string.privacy_policy_content));

                    break;
                case TERMS_OF_SERVICE:

                    title.setText(getString(R.string.terms_of_service_title));
                    content.setText(getString(R.string.terms_of_service_content));

                    break;
            }
        }
    }
}
