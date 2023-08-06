# Notes_App

📸 Screenshots
<table>
  <tr>
    <td align="center">
      <img src="https://github.com/baris-gungorr/Notes_App/blob/main/app/images/01.jpg" alt="NOTLARIM" width="250">
    </td>
    <td align="center">
      <img src="https://github.com/baris-gungorr/Notes_App/blob/main/app/images/02.jpg" alt="NOTLARIM" width="250">
    </td>
    <td align="center">
      <img src="https://github.com/baris-gungorr/Notes_App/blob/main/app/images/03.jpg" alt="NOTLARIM" width="250">
    </td>
     <td align="center">
       <img src="https://github.com/baris-gungorr/Notes_App/blob/main/app/images/04.jpg" alt="NOTLARIM" width="250">
    </td>
     <td align="center">
      <img src="https://github.com/baris-gungorr/Notes_App/blob/main/app/images/05.jpg" alt="NOTLARIM" width="250">
    </td>
     <td align="center">
      <img src="https://github.com/baris-gungorr/Notes_App/blob/main/app/images/06.jpg" alt="NOTLARIM" width="250">
    </td>
    <td align="center">
     <img src="https://github.com/baris-gungorr/Notes_App/blob/main/app/images/07.jpg" alt="NOTLARIM" width="250">
  </tr>
  
</table>

👇 Structures Used
- View Binding | Data Binding
- Coroutine
- Firebase
- Navigation
- WorkManager
- Glide

For animation : Lottie used

 ✏️ Dependency
 ```gradle
 implementation 'com.google.firebase:firebase-auth-ktx'
 implementation 'com.google.firebase:firebase-firestore-ktx'
 implementation 'com.google.firebase:firebase-storage-ktx'
 implementation 'com.google.firebase:firebase-database:20.2.2'

 implementation 'com.github.bumptech.glide:glide:4.15.1'
 annotationProcessor 'com.github.bumptech.glide:compiler:4.13.2'

 implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
 implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

 implementation 'com.squareup.picasso:picasso:2.71828'

 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

 implementation "androidx.work:work-runtime-ktx:2.8.1"
```

```groovy
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.gms.google-services'
    id("androidx.navigation.safeargs.kotlin")
}
```

❗ Manifest File

```groovy

<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.INTERNET"></uses-permission>

```

