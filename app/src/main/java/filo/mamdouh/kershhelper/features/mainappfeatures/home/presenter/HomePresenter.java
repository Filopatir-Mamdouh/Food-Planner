package filo.mamdouh.kershhelper.features.mainappfeatures.home.presenter;


import android.util.Log;
import java.time.LocalDate;
import java.util.ArrayList;
import filo.mamdouh.kershhelper.contracts.HomeContract;
import filo.mamdouh.kershhelper.features.mainappfeatures.home.Updater;
import filo.mamdouh.kershhelper.models.HomeFragmentRowData;
import filo.mamdouh.kershhelper.models.MealsItem;
import filo.mamdouh.kershhelper.models.Repostiry;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomePresenter {
    private final Repostiry repo;
    HomeContract.View view;
    CompositeDisposable compositeDisposable;
    public HomePresenter(HomeContract.View view,Repostiry repo){
        this.repo = repo;
        this.view = view;
        compositeDisposable = new CompositeDisposable();
    }
    public void getHomeItems(){
        getDailyInspiration();

        compositeDisposable.add(repo.getSavedMeals().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meals -> view.updateUI(new HomeFragmentRowData("Saved Meals",meals)), e-> Log.d("TAG", "getHomeItems: "+e.getMessage())
                ));
        getDesserts();
        getRecentlyViewed();
        compositeDisposable.add(repo.getMore().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meals -> view.updateUI(new HomeFragmentRowData("More You Might Like",meals)), e-> Log.d("TAG", "getHomeItems: "+e.getMessage())
                ));
    }
    public void saveMeal(MealsItem mealsItem, Updater updater){
        if(mealsItem.isSaved()) {
            mealsItem.setSaved(false);
            compositeDisposable.add(repo.saveMeal(mealsItem).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> view.onSave("Removed Successfuly"), e -> view.onSave(e.getMessage())));
            updater.updateUI();
        }
        else {
            mealsItem.setSaved(true);
            compositeDisposable.add(repo.saveMeal(mealsItem).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> view.onSave("Saved Successfuly"), e -> view.onSave(e.getMessage())));
            updater.updateUI();
        }
    }

    private void getDailyInspiration(){
        compositeDisposable.add(Observable.fromCallable(() -> {
            ArrayList<String> ids = new ArrayList<>();
            compositeDisposable.add(repo.getLocalDailyInspiration().subscribeOn(Schedulers.io()).subscribe(
                    ids::add, onError -> {
                        Log.d("Filo", "1: " + onError.getMessage());
                        compositeDisposable.add(repo.getDailyInspiration().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        this::updateUInSaveToFile, e -> Log.d("TAG", "getHomeItems: " + e.getMessage())
                                ));
                    }));
            return ids;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(strings -> {
            if(strings.get(strings.size()-1).equals(LocalDate.now().toString())){
                compositeDisposable.add(
                        Observable.fromCallable(() -> {
                    ArrayList<MealsItem> mealsItems = new ArrayList<>();
                    for (int i = 0 ; i<strings.size()-1 ; i++) {
                        compositeDisposable.add(
                                repo.getMealByID(strings.get(i)).subscribe(mealsItems::add)
                        );
                    }
                    return mealsItems;
                }).subscribe(this::updateUInSaveToFile)
                );
            }
            else{
                Log.d("Filo", "HOME PRESENTER: ");
                compositeDisposable.add(
                        repo.getDailyInspiration().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::updateUInSaveToFile, e -> Log.d("Filo", "getHomeItems: " + e.getMessage())
                        ));
            }
            },onError-> {
            Log.d("Filo", "2: "+onError.getMessage());
            compositeDisposable.add(repo.getDailyInspiration().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::updateUInSaveToFile, e -> Log.d("Filo", "getHomeItems: " + e.getMessage())
                    ));
        }
        ));
    }

    private void getRecentlyViewed(){
        compositeDisposable.add(Observable.fromCallable(() -> {
            ArrayList<MealsItem> mealsItems = new ArrayList<>();
                    compositeDisposable.add(repo.getRecentlyViewed().subscribeOn(Schedulers.io()).subscribe(
                    id -> repo.getMealByID(id).subscribeOn(Schedulers.io()).subscribe(
                            mealsItems::add,e -> Log.d("Filo", "getRecentlyViewed: "+e.getMessage())
                    ),throwable -> Log.d("Filo", "getRecentlyViewed: "+throwable.getMessage())));
            return mealsItems;
        }).observeOn(AndroidSchedulers.mainThread()).subscribe( mealsItems -> view.updateUI(new HomeFragmentRowData("Recently Viewed",mealsItems)),e -> Log.d("TAG", "getRecentlyViewed:"+e.getMessage()))
        );
    }

    private void getDesserts(){
        compositeDisposable.add(Observable.fromCallable(() -> {
                    ArrayList<MealsItem> mealsItems = new ArrayList<>();
            compositeDisposable.add(repo.getDesserts().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
                            id -> {
                                for (MealsItem item : id){
                                    compositeDisposable.add(repo.getMealByID(item.getIdMeal()).subscribeOn(Schedulers.io()).subscribe(
                                            mealsItems::add,e -> Log.d("Filo", "getRecentlyViewed: "+e.getMessage())
                                    ));
                                }
                            },throwable -> Log.d("Filo", "getRecentlyViewed: "+throwable.getMessage())));
                    return mealsItems;
                }).observeOn(AndroidSchedulers.mainThread()).subscribe( mealsItems -> view.updateUI(new HomeFragmentRowData("Desserts",mealsItems)),e -> Log.d("TAG", "getRecentlyViewed:"+e.getMessage()))
        );
    }

    public void onDestroy(){
//        compositeDisposable.dispose();
    }

    private void updateUInSaveToFile(ArrayList<MealsItem> meals){
        view.updateUI(new HomeFragmentRowData("Daily Inspiration", meals));
        ArrayList<String> saveids = new ArrayList<>();
        for (MealsItem meal : meals)  saveids.add(meal.getIdMeal());
        saveids.add(LocalDate.now().toString());
        compositeDisposable.add(repo.saveLocalDailyInspiration(saveids).subscribeOn(Schedulers.io()).subscribe(o -> {},e-> Log.d("Filo", "5: "+e.getMessage())));
    }
}
