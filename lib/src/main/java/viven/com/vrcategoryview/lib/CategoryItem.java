package viven.com.vrcategoryview.lib;

import android.graphics.drawable.Drawable;

import java.util.List;

/**
 * Created by viventhraarao on 22/11/2016.
 */

public interface CategoryItem {

    String getName();
    Drawable getImage();
    List<CategoryItem> getSubCategoryItems();

}
