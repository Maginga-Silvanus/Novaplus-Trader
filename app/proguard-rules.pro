# ProGuard rules for NovaPlus Trader
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Firebase
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.functions.** { *; }
-keep class com.google.firebase.messaging.** { *; }
