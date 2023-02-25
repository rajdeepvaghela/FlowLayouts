# FlowLayouts
[![Release](https://jitpack.io/v/com.github.rajdeepvaghela/FlowLayouts.svg)](https://jitpack.io/#com.github.rajdeepvaghela/FlowLayouts)
[![Release](https://img.shields.io/github/v/release/rajdeepvaghela/FlowLayouts)](https://github.com/rajdeepvaghela/FlowLayouts/releases)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

FlowLayout and FlowRadioGroup will replicate the FlowLayout from the Java Sprint. 

<div align="center">
<img src="motion_demo.gif" />
</div>

## Installation
Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
    	...
        maven { url 'https://jitpack.io' }
    }
} 
```
Add the dependency
```gradle
dependencies {
    implementation 'com.github.rajdeepvaghela:FlowLayouts:1.0.0'
}
```
## Usage
```xml
    <com.rdapps.flowlayouts.FlowLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:rowSpacing="10dp">

        <View
            android:layout_width="80dp"
            android:layout_height="50dp"
            android:layout_marginEnd="10dp"
            android:background="@android:color/darker_gray" />

        <View
            android:layout_width="80dp"
            android:layout_height="50dp"
            android:layout_marginEnd="10dp"
            android:background="@android:color/holo_orange_dark" />

        <View
            android:layout_width="80dp"
            android:layout_height="50dp"
            android:layout_marginEnd="10dp"
            android:background="@android:color/holo_purple" />

        <View
            android:layout_width="80dp"
            android:layout_height="50dp"
            android:layout_marginEnd="10dp"
            android:background="@android:color/holo_blue_dark" />

        <View
            android:layout_width="80dp"
            android:layout_height="50dp"
            android:layout_marginEnd="10dp"
            android:background="@android:color/holo_blue_bright" />

    </com.rdapps.flowlayouts.FlowLayout>
```

## License
```
Copyright 2023 Rajdeep Vaghela

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
