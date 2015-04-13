# android-singleton-test
Simple demonstration and performance test of various singleton implemetations on Android.
Open the project in Android Studio and run it as Android instrumentation test.

Below are results of test runs on few devices.

<ul>
  <li>Enum or Holder Class implemetations are always the fastest </li>
  <li>Fully Synchronized is always the slowest one</li>
  <li>Results obtained on Art (Android 5.1) and Dalvik (others) show different ranking of implementations.</li>
</ul>

<img src="https://github.com/dtrounine/android-singleton-test/blob/master/pics/s3mini.png?raw=true"/>
<img src="https://github.com/dtrounine/android-singleton-test/blob/master/pics/htconex.png?raw=true"/>
<img src="https://github.com/dtrounine/android-singleton-test/blob/master/pics/nexus5.png?raw=true"/>
