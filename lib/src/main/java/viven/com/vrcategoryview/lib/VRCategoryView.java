package viven.com.vrcategoryview.lib;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.util.Pair;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by viventhraarao on 22/11/2016.
 */

public class VRCategoryView extends FrameLayout {

    ScrollView scrollView;
    FrameLayout categoryGridFrame;
    RecyclerView subCategoryList;

    VRSubCategoryAdapter adapter;
    VRCategoryItemClickListener itemClickListener;
    List<CategoryItem> categoryItems;

    List<View> gridItemViews;

    int totalCount;
    int columnSize;
    int xyMatrix[][][]; // [row] [column] [xy]
    int itemPadding;
    int animationDuration;

    View selectedView;

    public VRCategoryView(Context context) {
        super(context);
        init();
    }

    public VRCategoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VRCategoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public VRCategoryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        categoryGridFrame = new FrameLayout(getContext());
        scrollView = new ScrollView(getContext());

        subCategoryList = new RecyclerView(getContext());
        subCategoryList.setLayoutManager(new LinearLayoutManager(getContext()));

        scrollView.addView(categoryGridFrame, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addView(scrollView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        addView(subCategoryList, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        itemPadding = dpToPx(8);
        animationDuration =
                getResources().getInteger(android.R.integer.config_mediumAnimTime);
    }

    public void setSubCategoryListBackgroundColor(@ColorInt int color) {
        subCategoryList.setBackgroundColor(color);
    }

    public void initialize(List<CategoryItem> categoryItemList, VRSubCategoryAdapter adapter,
                    VRCategoryItemClickListener itemClickListener) {
        this.adapter = adapter;
        this.itemClickListener = itemClickListener;

        categoryItems = categoryItemList;
        totalCount = categoryItemList.size();
        gridItemViews = new ArrayList<>();

        for (int i = 0; i < totalCount; i++) {
            CategoryItem categoryItem = categoryItemList.get(i);
            FrameLayout itemView = new FrameLayout(getContext());
            ImageView imageView = new ImageView(getContext());
            TextView textView = new TextView(getContext());
            View view = new View(getContext());

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            itemView.addView(imageView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            view.setBackgroundColor(Color.BLACK);
            view.setAlpha(0f);
            view.setId(R.id.darkenView);
            itemView.addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            textView.setShadowLayer(25, 0, 0, Color.BLACK);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            itemView.addView(textView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            itemView.setTag(R.id.expanded, false);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    boolean expanded = (boolean) view.getTag(R.id.expanded);
                    changeItemState(view, !expanded);
                }
            });

            imageView.setImageDrawable(categoryItem.getImage());
            textView.setText(categoryItem.getName());

            gridItemViews.add(itemView);

            initViews(getLeft(), getTop(), getRight(), getBottom(), 0, 0, 0, 0);

            addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    Log.d("layout", "change");

                    removeOnLayoutChangeListener(this);

                    initViews(left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom);

                    addOnLayoutChangeListener(this);
                }
            });
        }
    }

    /**
     * @return true if there's a category expanded
     */
    public boolean onBackPressed() {
        if (selectedView != null) {
            changeItemState(selectedView, false);
            return true;
        } else {
            return false;
        }
    }

    void changeItemState(View view, boolean expand) {
        if (expand) {
            List<CategoryItem> cats;
            if ((cats = (List<CategoryItem>) view.getTag(R.id.subCategories)) != null) {
                categoryGridFrame.removeView(view);
                categoryGridFrame.addView(view);

                scrollView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return true;
                    }
                });

                adapter.setData(cats);
                subCategoryList.setAdapter(adapter);

                ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
                animation.setDuration(animationDuration);
                animation.setInterpolator(new FastOutSlowInInterpolator());
                animation.addUpdateListener(new CategoryItemOpenAnimatorUpdateListener(view, true));
                animation.start();

                view.setTag(R.id.expanded, true);

                int childCount = categoryGridFrame.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = categoryGridFrame.getChildAt(i);
                    if (child != view) {
                        child.setEnabled(false);

                        if (child.isShown()) {
                            int animateX;
                            int animateY;

                            if (child.getX() > view.getX()) {
                                animateX = getWidth() + 1;
                            } else {
                                animateX = -(child.getWidth() + 1);
                            }

                            if (child.getY() > view.getY()) {
                                animateY = getHeight() + 1;
                            } else {
                                animateY = -(child.getHeight() + 1);
                            }

                            child.animate().x(animateX).y(animateY)
                                    .setDuration(animationDuration).start();
                        }
                    }
                }

                selectedView = view;
            } else {
                if (itemClickListener != null)
                    itemClickListener.onCategoryItemClicked(categoryItems.get(
                            gridItemViews.indexOf(view)));
            }
        } else {
            scrollView.setOnTouchListener(null);

            ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
            animation.setDuration(animationDuration);

            animation.addUpdateListener(new CategoryItemOpenAnimatorUpdateListener(view, false));

            animation.start();

            view.setTag(R.id.expanded, false);

            int childCount = categoryGridFrame.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = categoryGridFrame.getChildAt(i);
                if (child != view) {
                    child.setEnabled(true);

                    Pair<Integer, Integer> rowColPair =
                            (Pair<Integer, Integer>) child.getTag(R.id.rowColumnIndexPair);
                    int[] xy = xyMatrix[rowColPair.first][rowColPair.second];

                    child.animate().x(xy[0]).y(xy[1])
                            .setDuration(animationDuration).start();
                }
            }

            selectedView = null;
        }
    }

    void initViews(int left, int top, int right, int bottom,
                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int oldWidth = oldRight - oldLeft;
        int width = right - left;

        if (width != oldWidth) {
            columnSize = (int) Math.floor(width / dpToPx(190));

            // to store xy positions for all items
            xyMatrix = new int[(int) Math.ceil((double) totalCount / (double) columnSize)]
                    [columnSize][2];

            int height = bottom - top;
            int paddingTotal = itemPadding * (columnSize + 1);
            int perViewWidth = (width - paddingTotal) / columnSize;

            // pushing recyclerview out of view
            subCategoryList.setY(height + 1);
            subCategoryList.getLayoutParams().height = height * 80 / 100;

            categoryGridFrame.removeAllViews();

            int index = 0;
            int rowIndex = 0;
            for (int[][] columnXY : xyMatrix) {
                int colIndex = 0;
                for (int[] xy : columnXY) {
                    if (index < totalCount) {
                        int x = (colIndex * perViewWidth) + ((colIndex + 1) * itemPadding);
                        int y = (rowIndex * perViewWidth) + ((rowIndex + 1) * itemPadding);

                        xyMatrix[rowIndex][colIndex] = new int[]{x, y};

                        View itemView = gridItemViews.get(index);
                        itemView.setTag(R.id.rowColumnIndexPair, new Pair<>((Integer) rowIndex,
                                (Integer) colIndex));

                        CategoryItem categoryItem = categoryItems.get(index);
                        List<CategoryItem> subCats = categoryItem.getSubCategoryItems();
                        itemView.setTag(R.id.subCategories, subCats);

                        FrameLayout.LayoutParams layoutParams =
                                new FrameLayout.LayoutParams(perViewWidth, perViewWidth);
                        layoutParams.topMargin = y;
                        layoutParams.leftMargin = x;

                        if (index == totalCount - 1)
                            layoutParams.bottomMargin = itemPadding;

                        categoryGridFrame.addView(itemView, layoutParams);

                        colIndex++;
                        index++;
                    }
                }
                rowIndex++;
            }

            if (selectedView != null) {
                changeItemState(selectedView, true);
            }
        }
    }

    public static int dpToPx(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    private class CategoryItemOpenAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        boolean isOpen;

        private final View view;
        private final View darkenView;
        FrameLayout.LayoutParams layoutParams;

        int originalTopMargin;
        int toTopMargin;
        int topMarginDiff;

        int originalLeftMargin;
        int toLeftMargin;
        int leftMarginDiff;

        int originalWidth;
        int toWidth;
        int diffWidth;

        int originalHeight;
        int toHeight;
        int diffHeight;

        int originalXList;
        int toXList;
        int diffXList;

        int originalYList;
        int toYList;
        int diffYList;

        public CategoryItemOpenAnimatorUpdateListener(View view, boolean open) {
            this.isOpen = open;
            this.view = view;
            this.darkenView = view.findViewById(R.id.darkenView);
            layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();

            if (open) {
                int rootHeight = getHeight();
                int pct20 = rootHeight * 20 / 100;

                originalTopMargin = layoutParams.topMargin;
                toTopMargin = scrollView.getScrollY() - ((rootHeight - pct20) / 2);
                topMarginDiff = toTopMargin - originalTopMargin;

                originalLeftMargin = layoutParams.leftMargin;
                toLeftMargin = 0;
                leftMarginDiff = toLeftMargin - originalLeftMargin;

                originalWidth = layoutParams.width;
                toWidth = getWidth();
                diffWidth = toWidth - originalWidth;

                originalHeight = layoutParams.height;
                toHeight = getHeight();
                diffHeight = toHeight - originalHeight;

                originalXList = (int) subCategoryList.getX();
                toXList = 0;
                diffXList = toXList - originalXList;

                originalYList = (int) subCategoryList.getY();
                toYList = pct20;
                diffYList = toYList - originalYList;
            } else {
                Pair<Integer, Integer> rowColPair =
                        (Pair<Integer, Integer>) view.getTag(R.id.rowColumnIndexPair);
                int[] xy = xyMatrix[rowColPair.first][rowColPair.second];
                int paddingTotal = itemPadding * (columnSize + 1);
                int perViewWidth = (getWidth() - paddingTotal) / columnSize;

                originalTopMargin = layoutParams.topMargin;
                toTopMargin = xy[1];
                topMarginDiff = toTopMargin - originalTopMargin;

                originalLeftMargin = layoutParams.leftMargin;
                toLeftMargin = xy[0];
                leftMarginDiff = toLeftMargin - originalLeftMargin;

                originalWidth = layoutParams.width;
                toWidth = perViewWidth;
                diffWidth = toWidth - originalWidth;

                originalHeight = layoutParams.height;
                toHeight = perViewWidth;
                diffHeight = toHeight - originalHeight;

                originalXList = (int) subCategoryList.getX();
                toXList = 0;
                diffXList = toXList - originalXList;

                originalYList = (int) subCategoryList.getY();
                toYList = getHeight() + 1;
                diffYList = toYList - originalYList;
            }
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float currentFloat = (float) valueAnimator.getAnimatedValue();

            float darkAlpha = isOpen ? currentFloat * 0.7f : (1 - currentFloat) * 0.7f;
            darkenView.setAlpha(darkAlpha);

            layoutParams.leftMargin = (int) ((leftMarginDiff * currentFloat) + originalLeftMargin);
            double curveFactor = isOpen ? Math.pow(currentFloat, 0.5) : currentFloat;
            layoutParams.topMargin = (int) ((topMarginDiff * curveFactor) + originalTopMargin);
            layoutParams.width = (int) ((diffWidth * currentFloat) + originalWidth);
            layoutParams.height = (int) ((diffHeight * currentFloat) + originalHeight);

            view.setLayoutParams(layoutParams);

            subCategoryList.setX((diffXList * currentFloat) + originalXList);
            subCategoryList.setY((diffYList * currentFloat) + originalYList);
        }
    }

    public static abstract class VRSubCategoryAdapter<T extends RecyclerView.ViewHolder>
            extends RecyclerView.Adapter<T> {

        public abstract void setData(List<CategoryItem> categoryItems);

    }

}
