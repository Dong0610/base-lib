# 🚀 Installation Guide

## Step 1: Add the JitPack Repository to Your Build File

### Gradle

Add the JitPack repository to the `repositories` section in your `build.gradle` file (at the end of the repositories block):

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
Maven
For Maven, add the following repository configuration to your pom.xml:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

For SBT, add this to your build.sbt:

```scala
resolvers += Resolver.url("jitpack", url("https://jitpack.io"))(Resolver.ivyStylePatterns)
```
Leiningen
For Leiningen, add the following to your project.clj:

```clojure
:repositories [["jitpack" "https://jitpack.io"]]
```
Step 2: Add the Dependency
Once the repository is added, include the following dependency in your build file.

Gradle
In your build.gradle file, under the dependencies block, add:

```gradle
dependencies {
    implementation("com.github.Dong0610:base-lib:1.0.6")
}
```
Maven
For Maven, add the following dependency to your pom.xml:

```xml
<dependency>
    <groupId>com.github.Dong0610</groupId>
    <artifactId>base-lib</artifactId>
    <version>1.0.6</version>
</dependency>
```
For SBT, add this line to your build.sbt:

```scala
libraryDependencies += "com.github.Dong0610" % "base-lib" % "1.0.6"
```
Leiningen
For Leiningen, add this dependency to your project.clj:

```c
[com.github.Dong0610/base-lib "1.0.6"]
```
Now you are ready to use the base-lib in your project! 🎉

```markdown
### Key Improvements:

- **Step-by-step instructions:** The steps are clearly divided to ensure that users follow each one easily.
- **Multiple build tools support:** Instructions are provided for different build tools (`Gradle`, `Maven`, `SBT`, `Leiningen`), allowing a wider audience to follow.
- **Code Formatting:** The code blocks for Gradle, Maven, SBT, and Leiningen are highlighted, making it clear where the changes should be applied.
- **Consistency:** The markdown is consistent and easy to follow, ensuring a smooth user experience for developers of different tools.
```

# 🌟 UiLinearLayout, UiConstrainLayout

A custom `LinearLayout` for Android with enhanced UI features such as gradient backgrounds, customizable stroke colors, corner radius, and dark mode support.

## 🚀 Features

- 🎨 **Supports gradient and solid backgrounds**
- 🖌️ **Customizable stroke width and color** (including gradient strokes)
- 🌙 **Adaptive dark mode colors**
- 🔲 **Configurable corner radius**
- ✂️ **Clip content support**

## 📥 Installation

1. Add the `UiLinearLayout` class to your Android project.
2. Ensure you have the required attributes defined in `res/values/attrs.xml`.

## 📌 Usage

### 📜 XML

```xml
<com.yourpackage.UiLinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cornerRadius="12dp"
    app:strokeWidth="2dp"
    app:stColorLight="#FF0000"
    app:stColorDark="#00FF00"
    app:bgColorLight="#FFFFFF"
    app:bgColorDark="#000000"
    app:bgGradientStart="#FF5733"
    app:bgGradientEnd="#C70039"
    app:clipContent="true" />
⚡ Kotlin

val uiLinearLayout = UiLinearLayout(context).apply {
    setCornerRadius(12f)
    setStrokeWidth(2)
    stColor(Color.RED, Color.GREEN)
    setBgColor(Color.WHITE, Color.BLACK)
    setGradientBg(Color.RED, Color.BLUE, Color.GREEN)
}
🎨 XML Attributes
Attribute	Description
app:cornerRadius	Sets the corner radius
app:strokeWidth	Defines the width of the border stroke
app:stColorLight	Border color in light mode
app:stColorDark	Border color in dark mode
app:bgColorLight	Background color in light mode
app:bgColorDark	Background color in dark mode
app:bgGradientStart	Start color of gradient background
app:bgGradientEnd	End color of gradient background
app:clipContent	Enables or disables clipping content
🎨 Customization
Gradient Background: Use setGradientBg(startColor, centerColor, endColor) to set a gradient background.
Gradient Stroke: Use setGradientStroke(intArrayOf(Color.RED, Color.BLUE)) to set a gradient stroke.
Stroke Orientation: Customize stroke orientation with setGradientStrokeOrientation(GradientOrientation.LEFT_TO_RIGHT).
Dark Mode Handling: The view automatically switches colors based on the device's dark mode settings.
markdown
Sao chép
Chỉnh sửa

### Key Improvements:

- **Headings and Subheadings:** I used `#` and `##` for a clear title and subtitle structure, improving readability.
- **Bullet Points:** Features are presented using bullet points for better flow and understanding.
- **Code Blocks:** XML and Kotlin examples are formatted inside code blocks for clarity.
- **Tables:** Attributes and descriptions are neatly organized in a table for quick reference.
- **Customization Section:** Clear instructions on how to use specific features (like gradient backgrounds or stroke customizations).



# 🌟 UiTextView

---

UiTextView extends `AppCompatTextView`, adding advanced customization options such as text/background gradients, strokes, rounded corners, and dark mode support.

## 🚀 Features

- 🎨 **Gradient text & background**
- 🖌️ **Customizable stroke & corner radius**
- 🌙 **Automatic dark mode support**
- 🔗 **Optional underline text**

## 📌 Usage

### 📜 XML

```xml
<com.dong.baselib.widget.UiTextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Hello, World!"
    app:textGradient="true"
    app:textGradientStart="#FF5733"
    app:textGradientEnd="#33FF57"
    app:bgColorDark="#222222"
    app:bgColorLight="#EEEEEE"
    app:cornerRadius="12dp"
    app:strokeWidth="2dp"/>
⚡ Kotlin
val uiTextView = UiTextView(context)
uiTextView.text = "Gradient Text"
uiTextView.setTextColorGradient(Color.RED, Color.BLUE, TextGradientOrientation.LEFT_TO_RIGHT)
🎨 Customization
Attribute	Description
textGradient	Enables text gradient
cornerRadius	Sets corner radius
strokeWidth	Defines border width
bgColorDark	Background color in dark mode
bgColorLight	Background color in light mode
### Key improvements in this markdown:

- **Headings:** I've used `#` and `##` for titles and subheadings to create a clean, hierarchical structure.
- **Bullet Points:** I've listed the features with emojis and bullet points.
- **Code Blocks:** I've used triple backticks (` ``` `) to format both XML and Kotlin code for better readability.
- **Tables:** I've added a table for the customization attributes, making it easier to compare them.
- **Horizontal Line:** The `---` creates a nice break between sections, improving readability.

# 🌟 NativeCanvasView

## Overview

`NativeCanvasView` is a custom `View` for drawing shapes on a Canvas with utility functions for triangles, polygons, and rounded rectangles.

## 🚀 Features

- ✍️ **Custom View for dynamic drawing.**
- 🔶 **Extension functions for various shapes** like circles, triangles, and polygons.
- 🔄 **Supports drawable-to-bitmap conversion.**

## 📌 Usage

### Add NativeCanvasView

You can add the `NativeCanvasView` and use its drawing functions like so:

```kotlin
myViewGroup.nativeCanvas {
    drawCircle(100f, 100f, 50f, Paint().apply { color = Color.RED })
}
Draw a Triangle
To draw a triangle, use the drawTriangle function:

kotlin
Sao chép
Chỉnh sửa
canvas.drawTriangle(Paint().apply { color = Color.BLUE }, 
    PointF(100f, 100f), 
    PointF(200f, 100f), 
    PointF(150f, 200f))
Draw a Rounded Polygon
For a rounded polygon, you can use the drawRoundedPolygon function, like this:

kotlin
Sao chép
Chỉnh sửa
canvas.drawRoundedPolygon(
    listOf(PointF(50f, 50f), PointF(150f, 50f), PointF(100f, 150f)),
    20f,
    Paint().apply { color = Color.GREEN }
)
🎨 Shape Drawing Functions
Circle: drawCircle(x, y, radius, paint)
Triangle: drawTriangle(paint, point1, point2, point3)
Rounded Polygon: drawRoundedPolygon(points, radius, paint)
These functions allow you to draw basic shapes and customize their appearance with Paint objects.

markdown
Sao chép
Chỉnh sửa

### Key Improvements:

- **Headings and Subheadings:** I used `#` for titles and `##` for sections like "Overview" and "Features" to make it easy to navigate.
- **Code Blocks:** Kotlin code examples are formatted in code blocks to preserve indentation and clarity.
- **Bullet Points:** Features and shape functions are neatly listed for easy understanding.
- **Usage Examples:** I provided clear code samples for adding the view and drawing different shapes.
