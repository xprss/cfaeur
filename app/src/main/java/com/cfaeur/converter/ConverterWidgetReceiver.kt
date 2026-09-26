package com.cfaeur.converter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import android.widget.RemoteViews

class ConverterWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { render(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_KEY) {
            super.onReceive(context, intent)
            return
        }
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val key = intent.getStringExtra(EXTRA_KEY) ?: return
        val manager = AppWidgetManager.getInstance(context)
        val provider = manager.getAppWidgetInfo(widgetId)?.provider ?: return
        if (provider != ComponentName(context, ConverterWidgetReceiver::class.java)) return
        val next = load(context, widgetId).press(key)
        save(context, widgetId, next)
        render(context, manager, widgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val editor = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
        appWidgetIds.forEach {
            editor.remove("$it.input")
            editor.remove("$it.direction")
        }
        editor.apply()
    }

    private fun render(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val value = load(context, widgetId)
        val views = RemoteViews(context.packageName, R.layout.widget_converter)
        views.setTextViewText(R.id.widget_direction, "${value.direction.source} → ${value.direction.target}")
        val inputText = "${MoneyConverter.formatInput(value.input)} ${value.direction.source}"
        val outputText = "${MoneyConverter.format(value.output, value.direction.target)} ${value.direction.target}"
        views.setTextViewText(R.id.widget_input, inputText)
        views.setTextViewText(R.id.widget_output, outputText)
        views.setTextViewTextSize(
            R.id.widget_input, TypedValue.COMPLEX_UNIT_SP, if (inputText.length > 18) 16f else 20f
        )
        views.setTextViewTextSize(
            R.id.widget_output, TypedValue.COMPLEX_UNIT_SP, if (outputText.length > 18) 17f else 23f
        )
        views.setTextColor(
            R.id.key_decimal,
            if (value.direction == Direction.EUR_TO_CFA) Color.WHITE else Color.GRAY,
        )
        KEYS.forEach { (viewId, key) ->
            views.setOnClickPendingIntent(viewId, keyIntent(context, widgetId, key))
        }
        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.key_open, openPendingIntent)
        manager.updateAppWidget(widgetId, views)
    }

    private fun keyIntent(context: Context, widgetId: Int, key: String): PendingIntent {
        val intent = Intent(context, ConverterWidgetReceiver::class.java).apply {
            action = ACTION_KEY
            data = Uri.parse("cfaeur://widget/$widgetId/${Uri.encode(key)}")
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra(EXTRA_KEY, key)
        }
        return PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun load(context: Context, widgetId: Int): WidgetValue {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val input = preferences.getString("$widgetId.input", "0") ?: "0"
        val directionName = preferences.getString("$widgetId.direction", Direction.CFA_TO_EUR.name)
        val direction = Direction.entries.firstOrNull { it.name == directionName } ?: Direction.CFA_TO_EUR
        return WidgetValue(input, direction)
    }

    private fun save(context: Context, widgetId: Int, value: WidgetValue) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            .putString("$widgetId.input", value.input)
            .putString("$widgetId.direction", value.direction.name)
            .apply()
    }

    companion object {
        private const val ACTION_KEY = "com.cfaeur.converter.WIDGET_KEY"
        private const val EXTRA_KEY = "key"
        private const val PREFERENCES = "converter_widgets"
        private val KEYS = listOf(
            R.id.key_0 to "0", R.id.key_1 to "1", R.id.key_2 to "2",
            R.id.key_3 to "3", R.id.key_4 to "4", R.id.key_5 to "5",
            R.id.key_6 to "6", R.id.key_7 to "7", R.id.key_8 to "8",
            R.id.key_9 to "9", R.id.key_00 to "00",
            R.id.key_decimal to "DECIMAL", R.id.key_delete to "DELETE",
            R.id.key_clear to "CLEAR", R.id.key_swap to "SWAP",
        )
    }
}
