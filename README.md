# SideslipDemo
侧滑返回

- 演示

  ![https://github.com/zhaokai1033/SideslipDemo/blob/master/show.gif]()

- Fragment接入

  ```java
  //使用方式一:
      @Override
      public final View onCreateView(ViewGroup container, @Nullable Bundle savedInstanceState) {
          View view = inflater.inflate(getLayoutRes(), container, false);
  //        方式 ①: 在此处接入 此处在添加状态页需注意
  //        view = mSwipeClose = SwipeCloseLayout.createFromFragment(view, this, null);//侧滑控件
          return view;
      }


  //使用方式二:
      @Override
      public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
  //        方式②
          mSwipeClose = SwipeCloseLayout.createFromFragment(view, this, null);//侧滑控件
      }
  ```

  ​


- Activity 接入

  ```java
      @Override
      protected final void onPostCreate(@Nullable Bundle savedInstanceState) {
          super.onPostCreate(savedInstanceState);
          mSwipeBack = SwipeCloseLayout.createFromActivity(this, this);//侧滑控件
          mSwipeBack.setSwipeEnabled(mSwipeBackEnable);
      }
  ```

  ​