package filo.mamdouh.kershhelper.contracts;


import java.util.ArrayList;
import java.util.List;

import filo.mamdouh.kershhelper.models.Categories;
import filo.mamdouh.kershhelper.models.IngredientsRoot;
import filo.mamdouh.kershhelper.models.Meals;
import filo.mamdouh.kershhelper.models.MealsItem;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface NetworkContract {
    Observable<MealsItem> getMealByName(String name);

    Observable<MealsItem> getMealByLetter(String letter);

    Single<ArrayList<MealsItem>> getDailyInspiration();

    Observable<List<MealsItem>> getMealByCategory(String category);

    Observable<MealsItem> getMealByIngredient(String ingredient);

    Observable<MealsItem> getMealByArea(String area);

    Observable<List<IngredientsRoot.Ingredient>> getIngredients();

    Observable<List<Categories.Category>> getCategories();

    Single<ArrayList<MealsItem>> getRanomMeals();

    Single<ArrayList<MealsItem>> getMore();

    Observable<MealsItem> getMealByID(String id);

    Observable<MealsItem> searchMealByCategory(String category);
}
