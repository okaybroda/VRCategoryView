package viven.com.vrcategoryview.sample;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import viven.com.vrcategoryview.R;
import viven.com.vrcategoryview.lib.CategoryItem;
import viven.com.vrcategoryview.lib.VRCategoryItemClickListener;
import viven.com.vrcategoryview.lib.VRCategoryView;

public class MainActivity extends AppCompatActivity {

    VRCategoryView vrCategoryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final List<CategoryItem> categoryItemList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CategoryItem categoryItem = new CategoryItem() {
                @Override
                public String getName() {
                    return "Category";
                }

                @Override
                public Drawable getImage() {
                    return getResources().getDrawable(R.drawable.fluwa);
                }

                @Override
                public List<CategoryItem> getSubCategoryItems() {
                    return categoryItemList;
                }
            };
            categoryItemList.add(categoryItem);
        }

        vrCategoryView = (VRCategoryView) findViewById(R.id.vrCategoryView);
        vrCategoryView.setSubCategoryListBackgroundColor(Color.WHITE);
        vrCategoryView.initialize(categoryItemList, new VRCategoryView.VRSubCategoryAdapter<ViewHolder>() {
            List<CategoryItem> categoryItems;

            @Override
            public void setData(List<CategoryItem> categoryItems) {
                this.categoryItems = categoryItems;
                notifyDataSetChanged();
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView textView = new TextView(MainActivity.this);
                textView.setHeight(VRCategoryView.dpToPx(56));
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setTextColor(Color.BLACK);

                return new ViewHolder(textView);
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                ((TextView) holder.itemView).setText(categoryItems.get(position).getName());
            }

            @Override
            public int getItemCount() {
                return categoryItems == null ? 0 : categoryItems.size();
            }
        }, new VRCategoryItemClickListener() {
            @Override
            public void onCategoryItemClicked(CategoryItem categoryItem) {

            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
