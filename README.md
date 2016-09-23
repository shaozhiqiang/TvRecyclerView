# 欢迎使用 TvRecyclerView

QQ群：20012743

首先感谢lucasr开发出杰出的作品[TwoWayView](https://github.com/lucasr/twoway-view),**TvRecyclerView**就是在[TwoWayView](https://github.com/lucasr/twoway-view)的基础上进行的延伸，即：

> * 修复了一些小bug
> * 针对TV端的特性进行了适配与开发

### 效果

![](https://github.com/zhousuqiang/TvRecyclerView/blob/master/images/img_spannable.png)

![](https://github.com/zhousuqiang/TvRecyclerView/blob/master/images/img_staggered.png)

![](https://github.com/zhousuqiang/TvRecyclerView/blob/master/images/img_grid.png)

![](https://github.com/zhousuqiang/TvRecyclerView/blob/master/images/img_list.png)

### Android Studio 集成

```java
compile 'com.tv.boost:tv-recyclerview:1.0.6.4'
```

### 特性

- [x] 支持焦点快速移动

- [x] 支持Item选中放大时不被叠压(无需手动调用bringChildToFront())

- [x] 支持横/竖排列
    ```java
    android:orientation="horizontal"
    ```

- [x] 支持布局指定LayoutManager
    ```java
    app:tv_layoutManager="SpannableGridLayoutManager"
    ```

- [x] 支持设置选中Item边缘距离/居中
    ```java
    setSelectedItemAtCentered(boolean isCentered)
    setSelectedItemOffset(int offsetStart, int offsetEnd)
    ```

- [x] 支持设置横竖间距
    ```java
    setSpacingWithMargins(int verticalSpacing, int horizontalSpacing)
    ```

- [x] Item监听回调
    ```java
    mRecyclerView.setOnItemListener(new TvRecyclerView.OnItemListener() {
        @Override
        public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                
        }

        @Override
        public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                
        }
        
        @Override
        public void onReviseFocusFollow(TvRecyclerView parent, View itemView, int position) {
            // 此处为了特殊情况时校正移动框
        }

        @Override
        public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                
        }
    });
    
    mRecyclerView.setOnInBorderKeyEventListener(new TvRecyclerView.OnInBorderKeyEventListener() {
                @Override
                public boolean onInBorderKeyEvent(int direction, int keyCode, KeyEvent event) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            // do anything
                            return true;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            // do anything
                            return true;
                    }
                    return false;
                }
            });
    
    mRecyclerView.setOnLoadMoreListener(new TvRecyclerView.OnLoadMoreListener() {
                @Override
                public boolean onLoadMore() {
                    mRecyclerView.setLoadingMore(true); //正在加载数据
                    mLayoutAdapter.appendDatas(); //加载数据
                    mRecyclerView.setLoadingMore(false); //加载数据完毕
                    return true; //是否还有更多数据
                }
            });
           
    ```
### 版本说明
> * 1.0.1
    解决item点击无效问题
> * 1.0.2
    解决移动边框错位问题
> * 1.0.3
    优化了一些细节,删除无用资源
> * 1.0.4
    优化了一些细节,删除无用资源
> * 1.0.5
    1)修复在adapter内为item设置监听无效的问题;
    2)可自由设置是否拦截key事件;
    3)增加焦点移动边缘监听;
    4)增加菜单模式;
    5)自定义属性统一增加前缀tv;
> * 1.0.5.1
    1)修复菜单模式下的bug;
> * 1.0.5.2
    1)修复菜单模式下的bug;
> * 1.0.6
    自动记忆历史焦点,默认选中上次离开时的position;
> * 1.0.6.1
    微调;
> * 1.0.6.2
    1)增加加载更多监听;
    2)修复加载更多数据更新时焦点错乱问题;
> * 1.0.6.3
    1)处理onSaveInstanceState,onRestoreInstanceState;
> * 1.0.6.4
    1)修复glide加载图片焦点错位问题;
    2)增加IsSelectFirstVisiblePosition参数;
    

### 更详细的使用请见exmaple

------


作者 [owen](https://github.com/zhousuqiang)
