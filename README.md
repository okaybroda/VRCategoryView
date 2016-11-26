# VRCategoryView

![View Preview](https://github.com/okaybroda/VRCategoryView/blob/master/preview.gif?raw=true)

VRCategoryView is inspired by Ebay's category animation and design.

The view is capable of showing categories and sub categories. The first level will be shown in a grid with image and title. The second level can be shown however you like with passing in an adapter for RecyclerView.

## Installation
Add Jitpack
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Then add VRCategoryView
```gradle
dependencies {
  compile 'com.viven.vrcategoryview:1.0.0'
}
```
## Usage
Include the layout in your XML:
```xml
<viven.com.vrcategoryview.lib.VRSideCategoryView
        android:id="@+id/vrCategoryView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
Initialize the view by passing in your category data
```java
vrCategoryView = (VRSideCategoryView) findViewById(R.id.vrCategoryView);
vrCategoryView.setSubCategoryListBackgroundColor(Color.WHITE);
vrCategoryView.initialize(categoryItemList, new VRSideCategoryView.VRSubCategoryAdapter<ViewHolder>() { 
        //... put your adapter logic here ...
    });
```
