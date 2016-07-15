# Measure-View
For Measure Area
<br>
<br>
![](https://github.com/TruthKeeper/Measure-View/blob/master/app/src/main/res/raw/test.gif)  
<br>
```xml
<declare-styleable name="MeasureView">
        <attr name="size" format="dimension" />  <!--大小-->
        <attr name="offAngle" format="float" />  <!--旋转偏移量-->
        <attr name="isLineOn" format="boolean" />  <!--中心辅助线-->
        <attr name="measureSize" format="integer" />  <!--围度-->
        <attr name="pathSize" format="integer" />  <!--蛛网数量-->
        <attr name="contentColor" format="color" />  <!--填充颜色-->
        <attr name="lineColor" format="color" />  <!--线颜色-->
    </declare-styleable>
```
支持一部分自定义属性
<br>
集成在一个view中，可扩展，在后续版本，（如果有）由于较懒，未对一些进行优化处理，比如旋转保存参数，path添加动画效果，更多的自定义.
<br>
