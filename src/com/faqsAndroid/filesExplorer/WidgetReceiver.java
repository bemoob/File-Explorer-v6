package com.faqsAndroid.filesExplorer;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

// El grueso del código se implementa ahora en la clase WidgetService,
// pero como podéis ver, el código es prácticamente el mismo, con alguna salvedad...

public class WidgetReceiver extends AppWidgetProvider 
{								
	// El repintado de los widgets se realizará en el servicio, por lo que simplemente llamamos
	// al procedimiento adecuado en el servicio...
	
	public void onUpdate(Context context, AppWidgetManager app_widget_manager, int [] widget_ids)
	{
		WidgetService.updateWidgets(context);
	}

	// Algún widget se ha eliminado. Borramos sus preferencias...
	
	public void onDeleted(Context context, int [] app_widget_ids)
	{
		SharedPreferences.Editor preferences_editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		for (int i = 0; i < app_widget_ids.length; i ++) 
		{
			preferences_editor.remove(String.format("widget_%d_root_folder", app_widget_ids [i]));
		}
		preferences_editor.commit();
	}
}

