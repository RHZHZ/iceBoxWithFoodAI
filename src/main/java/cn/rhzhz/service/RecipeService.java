package cn.rhzhz.service;


import cn.rhzhz.DTO.RecipeQuery;
import cn.rhzhz.DTO.RecipeResponse;
import cn.rhzhz.DTO.Result;
import cn.rhzhz.pojo.PageResult;
import cn.rhzhz.pojo.RecipeEntity;
import org.json.JSONException;
import reactor.core.publisher.Flux;

public interface RecipeService {
    Flux<Result<RecipeResponse>> generateStructuredRecipe(String prompt, String provider, int userId) throws JSONException;
    PageResult<RecipeEntity> listRecipes(RecipeQuery query, int page, int size);
}
