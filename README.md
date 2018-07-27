### 自定义搜索View
    通过ValueAnimator、Path和PathMeasure实现

![效果](https://github.com/Wzhixiang/SearchViewDemo/blob/master/screenRecord/device-2018-07-27-113156.gif)

#### 如何使用
1.在布局中，duration是周期，strokeColor图标颜色<br>
><com.wzx.searchview.SearchView<br>
>>android:id="@+id/searchView"<br>
  android:layout_width="wrap_content"<br>
  android:layout_height="wrap_content"<br>
  android:background="@color/colorPrimary"<br>
  android:padding="6dp"<br>
  app:duration="1000"<br>
  app:layout_constraintLeft_toRightOf="@+id/ed_search_content"<br>
  app:strokeColor="?attr/colorAccent" /><br>

2.监听SearchView变化<br>
>searchView.addOnSearchListener(object : SearchView.OnSearchListener {<br>
>>override fun onSearchEnd() {<br>
  Log.i("MainActivity", "onSearchEnd")<br>
  searchResultView.text = "与${searchContentView.text}相关信息如下：\n1.xxx\n2.xxx"<br>
  }<br>
  override fun onSearchCancel() {<br>
  Log.i("MainActivity", "onSearchCancel")<br>
  searchResultView.text = "停止搜索：${searchContentView.text}"<br>
  }<br>
  override fun onSearchStart() {<br>
  Log.i("MainActivity", "onSearchStart")<br>
  searchThread.start()<br>
  }<br>
  
>})<br>
  
3.释放SearchView
>override fun onStop() {<br>
>>super.onStop()<br>
  //释放动画<br>
  searchView.release()<br>
  
>}
