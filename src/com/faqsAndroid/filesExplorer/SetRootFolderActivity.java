package com.faqsAndroid.filesExplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

// En Android (en principio) sólo puede instanciarse 
// una ventana una sóla vez.
// En nuestro caso necesitamos instanciar 2 veces la ventana principal
// una de ellas para implementar el gestor de archivos y otra para seleccionar
// el directorio principal.
// Tendremos una implementación con la versión principal en MainActivity
// y aquí las funciones necesarias para la selección del directorio...

public class SetRootFolderActivity extends MainActivity implements OnClickListener
{	
	public void onCreate(Bundle saved_instance_state) 
	{
		// Llamada a la superclase (obligatorio que sea la primera instrucción del evento).
		
		super.onCreate(saved_instance_state);
		
		// No tendremos en cuenta las pulsaciones de la tecla Menú en esta ventana...
		
		iAllowMenuKey = false;
		
		// No mostraremos los ficheros en esta ventana, sólo los directorios...
		
		iShowPlainFiles = false;
		
		// Capturaremos los clics sobre los botones "Aceptar" y "Cancelar"
		// El ID de los botones se fija en el archivo que define el layout
		// res/layout/main.xml en este caso...
		
		findViewById(R.id.accept_button).setOnClickListener(this);
		findViewById(R.id.cancel_button).setOnClickListener(this);		
		findViewById(R.id.buttons).setVisibility(View.VISIBLE);
	}

	public void onClick(View view) 
	{
		if (view.getId() == R.id.accept_button)
		{
			// Se ha pulsado el botón "Aceptar".
			// Devolvemos el valor, usando un Intent a la actividad iniciadora...
			
			Intent intent = new Intent();   
			intent.putExtra("root_folder", iCurrentPath);
			
			// Devolvemos el valor de retorno que indica que todo ha ido bien...
			
			setResult(Activity.RESULT_OK, intent);
		}
		else 
		{
			// Se ha pulsado el botón "Cancelar"
			// Devolvemos el resultado a la actividad iniciadora...
			
			setResult(Activity.RESULT_CANCELED);
		}
		// Acabamos la actividad...

		finish();
	}
}