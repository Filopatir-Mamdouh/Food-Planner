package filo.mamdouh.kershhelper.features.mainappfeatures.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;

import filo.mamdouh.kershhelper.R;
import filo.mamdouh.kershhelper.contracts.HomeContract;
import filo.mamdouh.kershhelper.contracts.SearchItemContract;
import filo.mamdouh.kershhelper.databinding.FragmentSearchItemsBinding;
import filo.mamdouh.kershhelper.datastorage.local.FileHandler;
import filo.mamdouh.kershhelper.datastorage.local.SharedPrefrenceHandler;
import filo.mamdouh.kershhelper.datastorage.network.RetrofitClient;
import filo.mamdouh.kershhelper.datastorage.room.savedmeals.SavedMealsDataSourceImpl;
import filo.mamdouh.kershhelper.features.mainappfeatures.home.Updater;
import filo.mamdouh.kershhelper.features.mainappfeatures.search.presenter.SearchItemsPresenter;
import filo.mamdouh.kershhelper.features.mainappfeatures.smallfoodcard.SmallCardAdapter;
import filo.mamdouh.kershhelper.models.MealsItem;
import filo.mamdouh.kershhelper.models.Repository;

public class SearchItemsFragment extends Fragment implements SearchItemContract.View , SearchItemContract.Listener {
    HomeContract.Activity activity;
    RecyclerView searchRV;
    SmallCardAdapter adapter;
    FragmentSearchItemsBinding binding;
    SearchItemsPresenter presenter;
    EditText searchTxt;
    ImageView backBtn;
    private LocalDateTime lastTime=LocalDateTime.now();
    View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new SearchItemsPresenter(this, Repository.getInstance(FileHandler.getInstance(getContext()),
                SavedMealsDataSourceImpl.getInstance(getContext()), RetrofitClient.getInstance(getContext()), SharedPrefrenceHandler.getInstance(getContext())));
        activity = (HomeContract.Activity) getActivity();
        activity.updateToolBarStatus(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchItemsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        searchRV = binding.searchRecyclerView;
        searchTxt = binding.searchMealsEditText;
        backBtn = binding.searchBackButton;
        adapter = new SmallCardAdapter(requireActivity(),this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        searchRV.setAdapter(adapter);
        searchRV.setLayoutManager(gridLayoutManager);
        Bundle bundle = getArguments();
        if(bundle!=null){
        if(bundle.containsKey("category")) presenter.searchByCategory(bundle.getString("category"));
        if(bundle.containsKey("ingredient")) presenter.searchByIngredient(bundle.getString("ingredient"));
        }
        else {
            searchTxt.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchTxt, InputMethodManager.SHOW_IMPLICIT);
        }
        backBtn.setOnClickListener(l-> Navigation.findNavController(view).navigate(R.id.action_searchItemsFragment_to_searchMainFragment));

        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("Filo", "afterTextChanged: " + lastTime +" ,,"+ LocalDateTime.now());
                if(LocalDateTime.now().isAfter(lastTime.plusSeconds(1))) {
                    lastTime = LocalDateTime.now();
                    removeList();
                    presenter.searchByArea(searchTxt.getText().toString());
                    presenter.searchByMeal(searchTxt.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity.updateToolBarStatus(View.VISIBLE);
    }

    @Override
    public void updateUI(MealsItem mealsItem) {
        adapter.updateUI(mealsItem);
    }

    public void removeList(){
        adapter.removeList();
    }


    @Override
    public void saveItemListener(MealsItem mealsItem, Updater updater) {
        presenter.saveItemListener(mealsItem,updater);
    }

    @Override
    public void onItemClick(String mealID, boolean isSaved) {
        Bundle bundle = new Bundle();
        bundle.putString("mealID",mealID);
        bundle.putBoolean("isSaved",isSaved);
        Navigation.findNavController(view).navigate(R.id.action_homeScreenFragment_to_mealDetailsFragment,bundle);
    }
}