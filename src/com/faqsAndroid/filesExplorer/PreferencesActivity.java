package com.faqsAndroid.filesExplorer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

// En este caso la ventana hereda propiedades de la superclase "PreferenceActivity" (ventana de
// configuración) e implementa el evento (opcional) OnPreferenceClickListener
// Veremos los eventos en profundidad más adelante...

public class PreferencesActivity extends PreferenceActivity implements OnPreferenceClickListener
{
	// Evento que se ejecuta al crear la actividad
	// En este caso, en lugar de indicarle el layout que contiene
	// los elementos de la ventana le diremos el archivo que contiene
	// las diferentes opciones de configuración
	
	// Ved que en este caso trataremos manualmente la preferencia "root_folder", mientras que 
	// el check (ver archivo res/xml/preferences.xml) se trata automáticamente...
	
	public void onCreate(Bundle saved_instance_state)
	{
		// Siempre hay que llamar al evento onCreate de la superclase en primer lugar
		
		super.onCreate(saved_instance_state);
		
		// Inicializamos la ventana. 
		
		addPreferencesFromResource(R.xml.preferences);
		
		// Tendremos en cuenta el evento onClick sobre la preferencia
		// que fija el directorio raíz
		
		findPreference("root_folder").setOnPreferenceClickListener(this);
		
		setResult(Activity.RESULT_OK);
	}

	// Se llama cuando la ventana se destruye...
	
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	// Cuando hagan clic en la preferencia abriremos una ventana que nos permitirá
	// seleccionar un directorio. 
	// Esto lo haremos usando la pantalla principal de la aplicación, pero pasándole
	// un parámetro especial, para que se comporte de forma diferente.
	// El tipo "Intent" nos sirve para pasar parámetros entre llamadas de sistema...
	
	public boolean onPreferenceClick(Preference preference) 
	{
		// Creamos la instancia de Intent
		// El intent recibe como parámetros un contexto (fijáos que pasamos "this" como contexto ya
		// que la propia ventana es un contexto) y la clase de la ventana que queremos instanciar.
		
		Intent intent = new Intent(this, SetRootFolderActivity.class);
		
		// El parámetro Action nos sirve para saber que operación estamos implementando.
		// En la ventana de destino tendremos en cuenta este valor para saber que parámetros recoger, si es el caso.
		
		intent.setAction("select_folder");
		
		// Introducimos en el intent un parámetro llamado "root_folder" que contendrá el directorio raíz actual.
		// Fijáos como accedemos a la preferencia fijada por el usuario usando la llamada getString sobre el 
		// objeto SharedPreferences. Si el valor fuera un entero usaríamos getInt, etc.
		// Fijáos que la función getString recibe como parámetro el nombre de la preferencia que queremos recuperar
		// y un valor por defecto por si la preferencia no ha sido fijada por el usuario aún (no existe)...
		
		intent.putExtra("root_folder", PreferenceManager.getDefaultSharedPreferences(this).getString("root_folder", ""));
		
		// Iniciamos la ventana.
		// El primer parámetro es el intent, y el segundo un valor entero que identifica el tipo de ventana
		// creado y que tendremos en cuenta cuando recibamos el evento indicativo de que la ventana se ha cerrado...

		startActivityForResult(intent, 1);
		
		// Devolvemos "cierto" para indicar al originador del evento que lo hemos capturado.
		
		return true;
	}
	
	// Evento que se genera cuando una ventana iniciada con "startActivityForResult" acaba...
	// Parámetros:
	// - El código de ventana (enviado en startActivityForResult)
	// - Un entero indicando si la ventana ha acabado bien o no (lo pone la ventana al acabar)
	// - Un intent con valores de retorno, si es el caso...
	
	public void onActivityResult(int request_code, int result_code, Intent intent)
	{
		if (request_code == 1)
		{
			// Es la ventana de selección de directorio raíz...
			
			if (result_code == Activity.RESULT_OK)
			{
				// El usuario ha seleccionado un valor que obtenemos...
				
				String root_folder = intent.getStringExtra("root_folder");
				
				// Instanciamos un objeto SharedPreferences.Editor y guardamos el valor del parámetro...
				
				SharedPreferences.Editor preferences_editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
				preferences_editor.putString("root_folder", root_folder);
				
				// Es importante recordar validar las modificaciones con el correspondiente "commit", que 
				// guardará los cambios y generará los eventos de Preferencias modificadas, si es que algún objeto los
				// está esperando...
				
				preferences_editor.commit();
			}
		}
	}
}
