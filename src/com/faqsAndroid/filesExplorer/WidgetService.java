package com.faqsAndroid.filesExplorer;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

// En general este código es el mismo que teníamos anteriormente en la clase WidgetReceiver
// pero ahora adquiere una nueva entidad al configurarse como un servicio de nuestra aplicación...
// Fijáos en el procedimiento "updateWidget", que es el punto de entrada al widget...

public class WidgetService extends Service
{
	private static final String FORZE_WIDGETS_UPDATE_ACTION = WidgetService.class.getName() + ".FORZE_WIDGETS_UPDATE_ACTION";

	private static final int UNKNOWN_WIDGET_ID = -1;

	private class ServiceBroadcastReceiver extends BroadcastReceiver 
	{	
		public void onReceive(Context context, Intent intent) 
		{
			if (intent.getAction().equals(FORZE_WIDGETS_UPDATE_ACTION))
			{
				// Se ha activado el evento de repintado (forzado) de los widgets...
				
				AppWidgetManager app_widget_manager = AppWidgetManager.getInstance(context);
				int widget_id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, UNKNOWN_WIDGET_ID);
				if (widget_id == UNKNOWN_WIDGET_ID) onUpdate(context, app_widget_manager, app_widget_manager.getAppWidgetIds(new ComponentName(context, WidgetReceiver.class)));         		
				else onUpdate(context, app_widget_manager, new int [] { widget_id });
			}
		}
	}

	private BroadcastReceiver iBroadcastReceiver;
	
	// Instanciamos un BroadcastReceiver que servirá de punto de entrada al servicio
	
	public void onCreate()
	{
		iBroadcastReceiver = new ServiceBroadcastReceiver();
		registerReceiver(iBroadcastReceiver, new IntentFilter(FORZE_WIDGETS_UPDATE_ACTION));		
	}
	
	// Eliminamos el BroadcastReceiver. Es obligatorio, ya que en caso de no hacerlo recibiremos FCs
	
	public void onDestroy()
	{
		unregisterReceiver(iBroadcastReceiver);
	}
	
	// Se ejecuta al instanciarse el servicio por primera vez.
	// Simplemente llamamos al BroadcastReceiver
	
	public int onStartCommand(Intent intent, int flags, int start_id)
	{
		if (intent != null) iBroadcastReceiver.onReceive(this, intent);
		return super.onStartCommand(intent, flags, start_id);
	}
	
	// No usaremos esta función, ya que no se trata de un servicio de acceso público...
	
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
			
	// Hay que actualizar algunos widgets...
    // Se ejecuta automáticamente cuando el sistema considera conveniente la actualización (al generarse el evento)
    // Se reciben los ID's de todos los widgets a actualizar...

	public void onUpdate(Context context, AppWidgetManager app_widget_manager, int [] app_widget_ids) 
	{
		for (int i = 0; i < app_widget_ids.length; i ++) updateWidget(context, app_widget_manager, app_widget_ids [i]);
	}
	
	// Hay que actualizar un widget...
	
	private void updateWidget(Context context, AppWidgetManager app_widget_manager, int widget_id) 
	{
		// Obtenemos la vista a partir del layout.
		
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		
		// Añadimos el icono.
		
		views.setImageViewResource(R.id.icon, R.drawable.ic_launcher);
		
		// Añadimos la etiqueta con el nombre del directorio raíz.
		
		String root_folder = getRootFolder(context, widget_id);
		if (root_folder.length() == 0) root_folder = "/";
		else 
		{
			int index = root_folder.lastIndexOf("/");
			root_folder = root_folder.substring(index + 1);
		}
				
		views.setTextViewText(R.id.label, root_folder);

		// Haremos que se abra la actividad principal al clicar el widget
		// pasándole como parámetro el directorio raíz...
		
		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra("root_folder", PreferenceManager.getDefaultSharedPreferences(context).getString(String.format("widget_%d_root_folder", widget_id), ""));
		views.setOnClickPendingIntent(R.id.layout, PendingIntent.getActivity(context, widget_id, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		
		// Aceptamos los cambios.
		
		app_widget_manager.updateAppWidget(widget_id, views);
	}  
	
	// Función que devuelve "true" si el servicio ya se está ejecutando, y "false" en otro caso.
	
	private static boolean isRunning(Context context, String service_name, String class_name)
	{
		try
		{
			for (RunningServiceInfo service : ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) 
			{
				if (service.service.getClassName().equals(class_name))
				{
					return true;
				}
			}
		}
		catch (Exception e)
		{
		}
		return false;
	}

	// Forzamos la actualización de un widget.
	// Este procedimiento estaba en la anterior versión en la clase WidgetReceiver,
	// pero ahora queda mejor aquí.
	// En esta ocasión comprobamos si el servicio ya se está ejecutando, 
	// y le enviamos el mensaje de actualización...
	
	public static synchronized void updateWidget(Context context, int widget_id)
	{
		boolean service_running = isRunning(context, "WidgetService", WidgetService.class.getName());
		Intent intent = service_running ? new Intent() : new Intent(context, WidgetService.class);
		intent.setAction(FORZE_WIDGETS_UPDATE_ACTION);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget_id);
		
		// Enviamos el broadcast o instanciamos el widget, según corresponda...
		
		if (service_running) context.sendBroadcast(intent);
		else context.startService(intent);
	}
	
	// Forzamos la actualización de todos los widgets.
	// Este procedimiento estaba en la anterior versión en la clase WidgetReceiver,
	// pero ahora queda mejor aquí.
	
	public static void updateWidgets(Context context)
	{
		updateWidget(context, UNKNOWN_WIDGET_ID);
	}

	// Obtenemos el directorio raíz de un widget.
	// Este procedimiento estaba en la anterior versión en la clase WidgetReceiver,
	// pero ahora queda mejor aquí.
	
	private static String getRootFolder(Context context, int widget_id)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getString(String.format("widget_%d_root_folder", widget_id), "");
	}
	
	// Fijamos el directorio raíz de un widget.
	// Este procedimiento estaba en la anterior versión en la clase WidgetReceiver,
	// pero ahora queda mejor aquí.
	
	public static void setRootFolder(Context context, int widget_id, String root_folder)
	{
		SharedPreferences.Editor preferences_editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		preferences_editor.putString(String.format("widget_%d_root_folder", widget_id), root_folder);
		preferences_editor.commit();		
	}
}
