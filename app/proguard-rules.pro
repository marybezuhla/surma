# Readable crash traces, without the original file names
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# No log lines in the release build, even if a library writes them
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
