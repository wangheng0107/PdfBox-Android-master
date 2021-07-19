PdfBox-Android
==============
参考：https://github.com/TomRoush/PdfBox-Android
学习pdf使用

依赖配置:

```gradle
dependencies {
    implementation 'com.tom-roush:pdfbox-android:1.8.10.3'
}
```
使用之前需要初始化
```java
PDFBoxResourceLoader.init(getApplicationContext());
```

开源java库，可以创建、转换、操作pdf文档。
a、可提取pdf中Unicode文本
b、可拆分多个文件，并可以将它们作为一个文件合并
c、可保存图片（PNG/JPEG）
d、可支持表单
e、可以创建图文类型的pdf
f、可打印
g、支持电子签名
不足：不支持中文，需要额外的导入ttf字体包，字体包过大
导入ttf字体包时容易报这个错：pdfbox No glyph for U+590D in font SimSun-ExtB