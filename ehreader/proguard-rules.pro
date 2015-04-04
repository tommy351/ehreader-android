# EventBus
-keepclassmembers class ** {
    public void onEvent*(**);
}

# ButterKnife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# TwoWayView
-keep class org.lucasr.twowayview.** { *; }