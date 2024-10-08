package filo.mamdouh.kershhelper.datastorage.network;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import filo.mamdouh.kershhelper.contracts.NetworkContract;
import filo.mamdouh.kershhelper.logic.network.NetworkUtlis;
import filo.mamdouh.kershhelper.models.Categories;
import filo.mamdouh.kershhelper.models.IngredientsRoot;
import filo.mamdouh.kershhelper.models.Meals;
import filo.mamdouh.kershhelper.models.MealsItem;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient implements NetworkContract{
    final int CACHESIZE = 5*1024*1024;
    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";
    private final APIService apiService;
    private static RetrofitClient instance = null;

    private RetrofitClient(Context context){
        Cache cache = new Cache(context.getCacheDir(),CACHESIZE);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cache(cache).addInterceptor(chain ->
                {
                    NetworkUtlis networkUtlis = new NetworkUtlis();
                    Request request = chain.request();
                    CacheControl cacheControl = new CacheControl.Builder().immutable().maxAge(5, TimeUnit.MINUTES).build();
                    CacheControl offlineCache = new CacheControl.Builder().onlyIfCached().maxStale(7, TimeUnit.DAYS).build();
                    if (networkUtlis.isNetworkAvailable(context))
                     request = chain.request().newBuilder().cacheControl(cacheControl).build();
                    else  request.newBuilder().cacheControl(offlineCache).build();
                    return chain.proceed(request);
                })
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
        apiService = retrofit.create(APIService.class);
    }
    public static RetrofitClient getInstance(Context context) {
        if(instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    @Override
    public  Observable<MealsItem> getMealByID(String id) {
        return apiService.getMealByID(id).map(meals -> meals.getMeals().get(0));
    }

    @Override
    public Observable<MealsItem> searchMealByCategory(String category) {
        CompositeDisposable disposable = new CompositeDisposable();
        return Observable.create(emitter -> disposable.add(apiService.getMealByCategory(category).map(Meals::getMeals).subscribeOn(Schedulers.io()).subscribe(
                onNext -> {
                    for (MealsItem meal : onNext)
                        disposable.add(apiService.getMealByID(meal.getIdMeal()).map(Meals::getMeals).subscribeOn(Schedulers.io()).subscribe(
                                item -> emitter.onNext(item.get(0)), emitter::onError , disposable::clear
                        ));
                }, emitter::onError
        )));
    }

    @Override
    public Observable<MealsItem> getMealByName(String name) {
        CompositeDisposable disposable = new CompositeDisposable();
        return Observable.create(emitter -> disposable.add(apiService.getMealByName(name).map(Meals::getMeals).subscribeOn(Schedulers.io()).subscribe(
                                onNext -> {
                                    for (MealsItem meal : onNext)
                                        emitter.onNext(meal);
                                }, emitter::onError , disposable::clear
        )));
    }

    @Override
    public Observable<MealsItem> getMealByLetter(String letter) {
        return null;
    }

    @Override
    public Single<ArrayList<MealsItem>> getDailyInspiration() {
        return getRandomMeal().repeat(5).scan(new ArrayList<MealsItem>(), (list,value)->{
            list.add(value);
            return list;
        }).last(new ArrayList<>(List.of()));
    }


    private Observable<MealsItem> getRandomMeal() {
        return apiService.getRandomMeal().map(meals -> meals.getMeals().get(0));
    }

    @Override
    public Observable<List<MealsItem>> getMealByCategory(String category) {
        return apiService.getMealByCategory(category).map(Meals::getMeals);
    }

    @Override
    public Observable<MealsItem> getMealByIngredient(String ingredient) {
        CompositeDisposable disposable = new CompositeDisposable();
        return Observable.create(emitter -> disposable.add(apiService.getMealByIngredient(ingredient).subscribeOn(Schedulers.io()).map(Meals::getMeals).subscribe(
                mealsItems -> {
                    for (MealsItem meal : mealsItems)
                        disposable.add(apiService.getMealByID(meal.getIdMeal()).map(Meals::getMeals).subscribeOn(Schedulers.io()).subscribe(
                                onNext -> emitter.onNext(onNext.get(0)), emitter::onError , disposable::clear
                        ));
                }
        )));
    }

    @Override
    public Observable<MealsItem> getMealByArea(String area) {
        CompositeDisposable disposable = new CompositeDisposable();
        return Observable.create(emitter -> disposable.add(apiService.getMealByArea(area).subscribeOn(Schedulers.io()).map(Meals::getMeals).subscribe(
                mealsItems -> {
                    for (MealsItem meal : mealsItems)
                        disposable.add(apiService.getMealByID(meal.getIdMeal()).map(Meals::getMeals).subscribeOn(Schedulers.io()).subscribe(
                                onNext -> emitter.onNext(onNext.get(0)), emitter::onError , disposable::clear
                        ));
                }
        )));
    }



    @Override
    public Observable<List<IngredientsRoot.Ingredient>> getIngredients() {
        return apiService.getIngredients().map(IngredientsRoot::getMeals);
    }

    @Override
    public Observable<List<Categories.Category>> getCategories() {
        return apiService.getCategories().map(Categories::getCategoryList);
    }

    @Override
    public Single<ArrayList<MealsItem>> getRanomMeals() {
        return getRandomMeal().repeat(5).scan(new ArrayList<MealsItem>(), (list,value)->{
            list.add(value);
            return list;
        }).last(new ArrayList<>(List.of()));
    }

    @Override
    public Single<ArrayList<MealsItem>> getMore() {
        Log.d("Small list", "getMore: ");
        return getRandomMeal().repeat(20).scan(new ArrayList<MealsItem>(), (list,value)->{
            list.add(value);
            return list;
        }).distinct().last(new ArrayList<>(List.of()));
    }

}
