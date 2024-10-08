package filo.mamdouh.kershhelper.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import filo.mamdouh.kershhelper.contracts.NetworkContract;
import filo.mamdouh.kershhelper.datastorage.local.FileHandler;
import filo.mamdouh.kershhelper.datastorage.local.SharedPrefrenceHandler;
import filo.mamdouh.kershhelper.datastorage.room.savedmeals.SavedMealsDataSource;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.Setter;

public class Repository {
    @Getter
    @Setter
    private User user;
    private final NetworkContract api;
    private final FileHandler fileHandler;
    private final SavedMealsDataSource savedMealsDataSource;
    private final SharedPrefrenceHandler prefs;
    @Getter
    private final Flowable<List<MealsItem>> savedMeals;
    @Getter
    private static final HashMap<String, String> COUNTERIES = new HashMap<>();
    private static final ArrayList<String> AREA = new ArrayList<>(List.of("american", "british", "canadian", "chinese",
            "croatian", "dutch", "egyptian", "filipino", "french", "greek", "irish", "italian", "jamaican", "japanese", "kenyan",
            "malaysian", "mexican", "moroccan", "polish", "portuguese", "russian", "spanish", "thai", "tunisian", "turkish", "ukrainian", "vietnamese"));
    private static Repository repository = null;

    static {
        COUNTERIES.put("American", "https://www.worldometers.info/img/flags/us-flag.gif");
        COUNTERIES.put("British", "https://www.worldometers.info/img/flags/uk-flag.gif");
        COUNTERIES.put("Canadian", "https://www.worldometers.info/img/flags/ca-flag.gif");
        COUNTERIES.put("Chinese", "https://www.worldometers.info/img/flags/ch-flag.gif");
        COUNTERIES.put("Croatian", "https://www.worldometers.info/img/flags/hr-flag.gif");
        COUNTERIES.put("Dutch", "https://www.worldometers.info/img/flags/gm-flag.gif");
        COUNTERIES.put("Egyptian", "https://www.worldometers.info/img/flags/eg-flag.gif");
        COUNTERIES.put("Filipino", "https://www.worldometers.info/img/flags/rp-flag.gif");
        COUNTERIES.put("French", "https://www.worldometers.info/img/flags/fr-flag.gif");
        COUNTERIES.put("Greek", "https://www.worldometers.info/img/flags/gr-flag.gif");
        COUNTERIES.put("Indian", "https://www.worldometers.info/img/flags/in-flag.gif");
        COUNTERIES.put("Irish", "https://www.worldometers.info/img/flags/iz-flag.gif");
        COUNTERIES.put("Italian", "https://www.worldometers.info/img/flags/it-flag.gif");
        COUNTERIES.put("Jamaican", "https://www.worldometers.info/img/flags/jm-flag.gif");
        COUNTERIES.put("Japanese", "https://www.worldometers.info/img/flags/ja-flag.gif");
        COUNTERIES.put("Kenyan", "https://www.worldometers.info/img/flags/ke-flag.gif");
        COUNTERIES.put("Malaysian", "https://www.worldometers.info/img/flags/my-flag.gif");
        COUNTERIES.put("Mexican", "https://www.worldometers.info/img/flags/mx-flag.gif");
        COUNTERIES.put("Moroccan", "https://www.worldometers.info/img/flags/mo-flag.gif");
        COUNTERIES.put("Polish", "https://www.worldometers.info/img/flags/pl-flag.gif");
        COUNTERIES.put("Portuguese", "https://www.worldometers.info/img/flags/po-flag.gif");
        COUNTERIES.put("Russian", "https://www.worldometers.info/img/flags/rs-flag.gif");
        COUNTERIES.put("Spanish", "https://www.worldometers.info/img/flags/sp-flag.gif");
        COUNTERIES.put("Thai", "https://www.worldometers.info/img/flags/th-flag.gif");
        COUNTERIES.put("Tunisian", "https://www.worldometers.info/img/flags/ts-flag.gif");
        COUNTERIES.put("Turkish", "https://www.worldometers.info/img/flags/tu-flag.gif");
        COUNTERIES.put("Ukrainian", "https://www.worldometers.info/img/flags/up-flag.gif");
        COUNTERIES.put("Vietnamese", "https://www.worldometers.info/img/flags/vm-flag.gif");
    }

    private Repository(FileHandler fileHandler, SavedMealsDataSource savedMealsDataSource, NetworkContract api, SharedPrefrenceHandler prefs) {
        this.prefs = prefs;
        this.api = api;
        this.fileHandler = fileHandler;
        this.savedMealsDataSource = savedMealsDataSource;
        savedMeals = savedMealsDataSource.getSavedMeals();
    }

    public static Observable<String> getAREA() {
        return Observable.fromIterable(AREA);
    }
    public static Repository getInstance(FileHandler fileHandler, SavedMealsDataSource savedMealsDataSource, NetworkContract api, SharedPrefrenceHandler prefs) {
        if (repository == null)
            repository = new Repository(fileHandler, savedMealsDataSource , api, prefs);
        return repository;
    }


    // * FILE METHODS
    public Observable<String> getLocalDailyInspiration() {
        return fileHandler.readFile("Daily_Inspiration");
    }
    public Observable<Object> saveLocalDailyInspiration(ArrayList<String> data) {
        return fileHandler.writeFile("Daily_Inspiration", data);
    }
    public Observable<String> removeFile(String name) {
        return fileHandler.removeFile(name);
    }


    //* API METHODS
    public Single<ArrayList<MealsItem>> getDailyInspiration() {

        return api.getDailyInspiration();
    }
    public Observable<List<MealsItem>> getDesserts() {
        return api.getMealByCategory("Dessert");
    }
    public Single<ArrayList<MealsItem>> getMore() {
        return api.getMore();
    }
    public Observable<MealsItem> getMealByID(String ids) {
        return api.getMealByID(ids);
    }
    public Observable<List<IngredientsRoot.Ingredient>> getIngredients() {
        return api.getIngredients();
    }
    public Observable<List<Categories.Category>> getCategories() {
        return api.getCategories();
    }
    public Observable<MealsItem> searchByIngredients(String name) {
        return api.getMealByIngredient(name);
    }
    public Observable<MealsItem> searchByMeal(String name) {
        return api.getMealByName(name);
    }
    public Observable<Object> saveRecentlyViewed(String id) {
        return fileHandler.writeFile("Recently_Viewed", id);
    }
    public Observable<String> getRecentlyViewed() {
        return fileHandler.readFile("Recently_Viewed");
    }
    public Observable<MealsItem> searchByCategory(String category) {
        return api.searchMealByCategory(category);
    }
    public Observable<MealsItem> searchByArea(String area) {
        return api.getMealByArea(area);
    }


    //* Room Methods
    public Flowable<MealsItem> getSavedMealByID(String id) {
        return savedMealsDataSource.getMealByID(id);
    }
    public Flowable<List<MealsItem>> getCalendar() {
        return savedMealsDataSource.getCalendars();
    }
    public Completable saveMeal(MealsItem meal) {
        return savedMealsDataSource.insertMeal(meal);
    }


    //* Login Methods
    public Observable<String> getLoginStatus() {
        return prefs.get("uid");
    }
    public void saveLogin(String uid) {
        prefs.save("uid", uid);
    }
    public Observable<Completable> logout() {
        return Observable.create(emitter ->{
                        emitter.onNext(prefs.clear());
                        emitter.onNext(savedMealsDataSource.clear());
                        emitter.onComplete();
                }
        );
    }
}
