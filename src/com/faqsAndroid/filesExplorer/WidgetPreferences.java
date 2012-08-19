package com.faqsAndroid.filesExplorer;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;

public class WidgetPreferences extends PreferenceActivity implements View.OnClickListener, Preference.OnPreferenceClickListener
{
	private int iWidgetId;
	private String iRootFolder = "";
	
    public void onCreate(Bundle saved_instance_state) 
    {
    	// Llamada al evento onCreate de la superclase.
    	
        super.onCreate(saved_instance_state);
        
        // Inicialmente asignamos el valor de resultado a RESULT_CANCELED
        // para que en caso que se pulse el botón Atrás se cancele la creación del widget.
        
        setResult(RESULT_CANCELED);
        
        // Creamos el layout de la actividad.
        
        addPreferencesFromResource(R.xml.widget_preferences);
        setContentView(R.layout.widget_preferences);
        
        // Capturamos los evento onClick de los botones de aceptación y cancelación.
        
		findViewById(R.id.button_save).setOnClickListener(this);
		findViewById(R.id.button_cancel).setOnClickListener(this);
		
		// Capturamos el evento onClick sobre la preferencia que contiene el directorio raíz...
		
		findPreference("root_folder").setOnPreferenceClickListener(this);
		
		// La actividad recibe como parámetro del id del widget que se está configurando, que necesitamos
		// para saber qué widget estamos configurando...
		
        iWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        
        // En caso que recibamos un id de widget incorrecto (cosa improvable) finalizamos la actividad de configuración...
        
        if (iWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish();
    }

    // Handler para los clicks en los botones de aceptar y cancelar...
    
	public void onClick(View view) 
	{
		boolean accepted = (view.getId() == R.id.button_save);
		if (accepted)
		{
			// Si se acepta la creación del widget almacenamos sus parámetros de configuración...
			
			WidgetService.setRootFolder(this, iWidgetId, iRootFolder);
			
			// Forzamos la actualización del contenido del widget...
			
			WidgetService.updateWidget(this, iWidgetId);

			// Finalmente devolvemos el resultado (RESULT_OK) a la actividad que ha creado ésta...
			
			Intent intent = new Intent();
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, iWidgetId);
			setResult(RESULT_OK, intent);
		}
		finish();
	}

	// Handler para el click en la preferencia de selección del directorio raíz...
	// El código es el mismo que usábamos para seleccionar el directorio raíz de la aplicación 
	// en las preferencias...
	
	public boolean onPreferenceClick(Preference preference) 
	{
		Intent intent = new Intent(this, SetRootFolderActivity.class);
		intent.setAction("select_folder");
		intent.putExtra("root_folder", iRootFolder);
		startActivityForResult(intent, 1);
		return true;
	}
	
	// Handler para el cierre de la ventana de selección del directorio raíz
	
	public void onActivityResult(int request_code, int result_code, Intent intent)
	{
		if (request_code == 1)
		{
			// Es la ventana de selección de directorio raíz...
			
			if (result_code == Activity.RESULT_OK)
			{
				// El usuario ha seleccionado un valor que obtenemos...
				
				iRootFolder = intent.getStringExtra("root_folder");
				findPreference("root_folder").setSummary(iRootFolder + "/");
				
				// Activamos el botón aceptar...

				findViewById(R.id.button_save).setEnabled(true);
			}
		}
	}

}
