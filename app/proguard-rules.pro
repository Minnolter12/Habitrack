# Glance AppWidget Keep Rules
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { public <init>(); }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { public <init>(); }
-keep class * implements androidx.glance.appwidget.action.ActionCallback { public <init>(); }
