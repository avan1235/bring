
-dontwarn ch.qos.logback.**
-dontwarn io.opentelemetry.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn com.google.auto.value.**
-dontwarn javax.annotation.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.osgi.annotation.bundle.**
-dontwarn org.apache.hc.**
-dontwarn com.nomanr.composables.bottomsheet.**
-dontwarn org.freedesktop.dbus.**
-dontwarn com.sun.jna.**
-dontwarn org.fusesource.jansi.**
-dontwarn com.typesafe.config.**
-dontwarn okhttp3.internal.platform.OpenJSSEPlatform
-dontwarn java.lang.invoke.**

-keepclassmembers class com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder$WithMember {
    public com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder withMember(com.fasterxml.jackson.databind.introspect.AnnotatedMember);
}

-keep class kotlinx.coroutines.** { *; }
-keep class io.opentelemetry.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-keep interface com.fasterxml.jackson.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class com.sun.** { *; }
-keep class org.freedesktop.** { *; }
-keep class com.typesafe.config.** { *; }

-keepnames class kotlinx.serialization.internal.EnumsCache
-keepclassmembers class kotlinx.serialization.internal.EnumsCache {
    <init>();
}

-keep,includedescriptorclasses class kotlinx.coroutines.android.AndroidDispatcherFactory
-keep,includedescriptorclasses class kotlinx.coroutines.android.AndroidExceptionPreHandler
-keep,includedescriptorclasses class kotlinx.coroutines.internal.MainDispatcherFactory
-keep,includedescriptorclasses class kotlinx.coroutines.CoroutineExceptionHandler
-keep,includedescriptorclasses class kotlinx.coroutines.test.internal.TestMainDispatcherFactory
-keep,includedescriptorclasses class kotlinx.coroutines.test.TestCoroutineScheduler
-keepclassmembers class kotlinx.coroutines.internal.MainDispatcherFactory {
   *;
}
-keepclassmembers class kotlinx.coroutines.test.internal.TestMainDispatcherFactory {
   *;
}

-keepclassmembers class ** {
    @kotlin.jvm.JvmField volatile <fields>;
}

-keepclassmembers class kotlin.coroutines.Continuation {
    <init>(...);
}

-keepclassmembers class kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    <init>(...);
}

# Reflective calls
-keepclassmembers,allowshrinking class * {
    @com.fasterxml.jackson.annotation.JsonCreator <methods>;
    @com.fasterxml.jackson.annotation.JsonValue <methods>;
}

# For enclosing method
-keepnames class com.fasterxml.jackson.databind.util.ClassUtil$**

-keepclassmembers class com.fasterxml.jackson.databind.ser.std.StdValueSerializer {
    <init>(...);
}

-keepclassmembers class com.fasterxml.jackson.databind.ser.std.StdSerializer {
    <init>(...);
}

-keepclassmembers class com.fasterxml.jackson.databind.JsonSerializer {
    <init>();
}

-keepclassmembers class com.fasterxml.jackson.databind.JsonDeserializer {
    <init>();
}

-keep class * implements com.fasterxml.jackson.databind.deser.std.StdDeserializer { *; }
-keep class * extends com.fasterxml.jackson.databind.deser.std.StdDeserializer { *; }
-keep class * implements com.fasterxml.jackson.databind.JsonSerializer { *; }
-keep class * extends com.fasterxml.jackson.databind.JsonSerializer { *; }

-keep class org.apache.hc.client5.http.ssl.ConscryptClientTlsStrategy { *; }
-keep class org.apache.hc.core5.http2.ssl.ConscryptSupport { *; }
-keep class org.brotli.dec.BrotliInputStream { *; }

-keepnames class com.sun.jna.** { *; }

-dontnote com.sun.jna.**
-dontnote org.freedesktop.dbus.**
-dontnote org.apache.http.**
-dontnote org.brotli.dec.**
-dontnote org.conscrypt.**

-keep class org.freedesktop.dbus.DBusCore
-keep class org.freedesktop.dbus.DBusCore$Error
-keep class org.freedesktop.dbus.DBusCore$Properties
-keep class org.freedesktop.dbus.DBusCore$Introspectable
-keep class org.freedesktop.dbus.DBusCore$Peer
-keep class org.freedesktop.dbus.DBusMatchRule
-keep class org.freedesktop.dbus.StrongReference
-keep class org.freedesktop.dbus.TypeRef
-keep class org.freedesktop.dbus.bin.** { *; }
-keep class org.freedesktop.dbus.connections.** { *; }
-keep class org.freedesktop.dbus.exceptions.** { *; }
-keep class org.freedesktop.dbus.interfaces.** { *; }
-keep class org.freedesktop.dbus.messages.** { *; }
-keep class org.freedesktop.dbus.propertyref.** { *; }
-keep class org.freedesktop.dbus.spi.** { *; }
-keep class org.freedesktop.dbus.types.** { *; }
-keep class org.freedesktop.dbus.utils.** { *; }
-keep class org.freedesktop.dbus.errors.** { *; }
-keep interface org.freedesktop.dbus.interfaces.DBusInterface {*;}
-keep,allowshrinking,allowobfuscation class * extends java.lang.Enum
-keepclassmembers,allowshrinking,allowobfuscation enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class org.freedesktop.dbus.annotations.** {*;}
-keep @org.freedesktop.dbus.annotations.DBusInterfaceName public interface * {*;}
-keepclassmembers public class * {
@org.freedesktop.dbus.annotations.DBusProperty public *;
}

-keep public class * implements io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider
-keep public class * implements java.util.spi.ResourceBundleProvider
-keep public class * implements java.nio.file.spi.FileSystemProvider
-keep public class * implements java.util.spi.LocaleServiceProvider
-keep public class * implements java.util.spi.CurrencyNameProvider
-keep public class * implements java.util.spi.TimeZoneNameProvider
-keep public class * implements javax.sound.sampled.spi.MixerProvider
-keep public class * implements javax.sound.midi.spi.MidiDeviceProvider
-keep public class * implements javax.print.PrintServiceLookup
-keep public class * implements javax.imageio.spi.ImageReaderSpi
-keep public class * implements javax.imageio.spi.ImageWriterSpi
-keep public class * implements javax.imageio.spi.ImageTranscoderSpi
-keep public class * implements javax.imageio.spi.ImageInputStreamSpi
-keep public class * implements javax.imageio.spi.ImageOutputStreamSpi
-keep public class * implements java.sql.Driver
-keep class * implements io.ktor.client.engine.HttpClientEngine
-keepnames class io.ktor.client.plugins.HttpClientPlugin
-keepnames class io.ktor.client.plugins.api.ClientPlugin

-keepclassmembers class * {
    @io.ktor.util.InternalAPI <fields>;
    @io.ktor.util.InternalAPI <methods>;
    @io.ktor.util.InternalAPI <init>(...);
    @kotlin.Deprecated <fields>;
    @kotlin.Deprecated <methods>;
    @kotlin.Deprecated <init>(...);
}
