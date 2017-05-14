//package gr.academic.city.sdmd.foodnetwork.ui.fragment;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.ListAdapter;
//import android.widget.ListView;
//
//import gr.academic.city.sdmd.foodnetwork.R;
//import gr.academic.city.sdmd.foodnetwork.domain.Meal;
//import gr.academic.city.sdmd.foodnetwork.ui.activity.MealsActivity;
//
//public class MealListFragment extends Fragment {
//
//
//    private OnFragmentInteractionListener mListener;
//
//    public MealListFragment() {}
//
//    public static MealListFragment newInstance(long mealTypeId) {
//        MealListFragment fragment = new MealListFragment();
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//        }
//    }
//
//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        ListAdapter adapter = new ArrayAdapter<Meal>(getContext(), android.R.layout.simple_list_item_1, MealsActivity.);
//
//        ListView listView = (ListView) getActivity().findViewById(R.id.lv_students);
//        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                mListener.onMealItemSelected(id);
//            }
//        });
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_meal_list, container, false);
//    }
//
//
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onMealItemSelected(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onMealItemSelected(Uri uri);
//    }
//}
