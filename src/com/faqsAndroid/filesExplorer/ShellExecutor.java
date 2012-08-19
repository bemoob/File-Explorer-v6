package com.faqsAndroid.filesExplorer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ShellExecutor 
{	
	private static final String SU_SHELL_COMMAND = "su -c sh";
		
	private static final String GET_UID_COMMAND = "id";
	
	private static final String COMMANDS_SEPARATOR = "\n";

	private static final String SNEAK_STRING = "<@@SNEAK-STRING@@>";
	private static final String [] AUTO_ADDED_EXECUTE_COMMANDS = { String.format("echo \"%s\" >&1", SNEAK_STRING), String.format("echo \"%s\" 1>&2", SNEAK_STRING) };
	
	private static final String EXIT_COMMAND = "exit";
					
	private Process iProcess;
		
	private DataInputStream iDataInputStream;
	private DataOutputStream iDataOutputStream;
	private DataInputStream iDataErrorStream;
	
	private boolean iDataInputStreamAvailable;
	private boolean iDataErrorStreamAvailable;
			
	// Inicialización...
	
	public ShellExecutor()
	{
		iProcess = null;
		iDataInputStream = null;
		iDataOutputStream = null;		
		iDataErrorStream = null;
		iDataInputStreamAvailable = iDataErrorStreamAvailable = false;
	}
	
	// Al acabar cerramos la sesión, si es que estaba abierta...
	
	protected void finalize() throws Exception
	{
		closeSession();
	}
		
	// Función que comprueba si se han obtenido permisos de root en la sesión o no.
	// Simplemente ejecuta el comando "id" y comprueba si éste devuelve "uid=0" en 
	// la respuesta, lo que indicaría que el usuario efectivo del shell es root...
	
	private boolean isRoot()
	{
		boolean is_root = false;
		if (isConnected())
		{
			try
			{
				if (execute(GET_UID_COMMAND))
				{
					List<String> output = getStandardOutput();
					if ((output == null) || (output.isEmpty())) throw new Exception("Can't get root access or denied by user");
					if (! output.toString().contains("uid=0")) throw new Exception("Root access rejected by user or device isn't rooted");
					is_root = true;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return is_root;
	}
	
	// Comprueba si la sesión esta abierta...
	
	public boolean isConnected()
	{
		return (iProcess != null);
	}
		
	// Inicializa la sesión, en caso que no estuviera ya iniciada.
	// Las tareas que se realizan son:
	// - Crear el proceso
	// - Abrir el canal de entrada del proceso (para escribir los comandos)
	// - Abrir el canal de salida estándar del proceso (para recibir los resultados)
	// - Abrir el canal de salida estándar de errores del proceso (para recibir los errores)
	// - Comprobar si se ha concedido permisos de root a la aplicación.
	
	public boolean beginSession()
	{
		if (! isConnected())
		{
			boolean correct = false;
			try
			{
				iProcess = Runtime.getRuntime().exec(SU_SHELL_COMMAND);
				iDataOutputStream = new DataOutputStream(iProcess.getOutputStream());
				iDataInputStream = new DataInputStream(iProcess.getInputStream());
				iDataErrorStream = new DataInputStream(iProcess.getErrorStream());
				if (! isRoot()) closeSession();
				correct = (iProcess != null);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				closeSession();
				iProcess = null;
			}
			return correct;
		}
		return true;
	}
	
	// Finaliza la sesión, y los canales de comunicaciones asociados, si es que ésta estaba abierta...
	
	public boolean closeSession()
	{
		boolean correct = true;
		if (iDataOutputStream != null) 
		{
			try
			{
				if (isConnected())
				{
					try
					{
						iDataOutputStream.writeBytes(COMMANDS_SEPARATOR + EXIT_COMMAND + COMMANDS_SEPARATOR);
						iDataOutputStream.flush();
						correct = (iProcess.waitFor() != 255);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				iDataOutputStream.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (iDataInputStream != null)
		{
			try
			{
				iDataInputStream.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (iDataErrorStream != null)
		{
			try
			{
				iDataErrorStream.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		iProcess = null;
		iDataInputStream = null;
		iDataOutputStream = null;
		iDataErrorStream = null;
		return correct;
	}
		
	// Ejecuta una lista de comandos
	// Notar que los comandos se separan con "\n"
	// Si la sesión está conectada se descarta el contenido de los canales
	// de salida y se envían los comandos.
	// Notar que añadimos 2 comandos extra que forzarán a que al ejecutarse los
	// comandos los canales de salida contengan algo.
	// Esto lo hacemos porque las operaciones de lectura son bloqueantes...
	
	public boolean execute(List<String> commands)
	{
		boolean correct = false;
		if (isConnected()) 
		{
			correct = true;
			if ((commands != null) && (commands.size() > 0))
			{ 
				try
				{
					correct = false;
					flushOutputStreams();
					for (String auto_added_execute_command : AUTO_ADDED_EXECUTE_COMMANDS) commands.add(auto_added_execute_command);
					for (String command : commands)
					{
						iDataOutputStream.writeBytes(command + COMMANDS_SEPARATOR);
						iDataOutputStream.flush();
					}												
					correct = iDataInputStreamAvailable = iDataErrorStreamAvailable = true;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return correct;
	}
	
	// Ejecuta un comando
	// Inicializa una lista de comandos con el comando recibido y llama a la función genérica...
	
	public boolean execute(String command)
	{
		ArrayList<String> commands = new ArrayList<String>();
		commands.add(command);
		return execute(commands);
	}
	
	// Vacía el contenido de los canales de salida,
	// descartando todo lo que hubiera pendiente de leer.
	
	private void flushOutputStreams()
	{
		getStandardOutput();
		getErrorOutput();
	}

	// Obtenemos una línea de un canal de salida.
	// Los canales son de salida del proceso shell, pero de entrada para nuestro código,
	// de ahí que sean de tipo DataInputStream...
	// Lo que hacemos es leer, letra a letra hasta encontrar que no hay datos que leer (algo improbable)
	// o un "\n"
	
	private static String readLine(DataInputStream in) throws Exception 
	{
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    while (true) 
	    {
	      int letter = in.read();
	      if (letter < 0) throw new Exception("Data truncated");
	      if (letter == 0x0A) break;
	      buffer.write(letter);
	    }
	    return new String(buffer.toByteArray(), "UTF-8");
	}

	// Obtenemos todo lo que hay pendiente de leer (la salida de un comando)
	// mediante sucesivas llamadas a readLine...
	
	private static List<String> getOutputStream(DataInputStream stream, boolean include_empty_lines)
	{
		List<String> data = new ArrayList<String>();
		if (stream != null)
		{ 
			try
			{
				String line; 
				while (true)
				{
					line = readLine(stream);
					if ((line == null) || (line.equals(SNEAK_STRING))) break;
					if ((include_empty_lines) || (line.length() > 0)) data.add(line);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public List<String> getStandardOutput() 
	{
		if (iDataInputStreamAvailable)
		{
			iDataInputStreamAvailable = false;
			return getOutputStream(iDataInputStream, true);	
		}
		return null;
	}
		
	public List<String> getErrorOutput()
	{
		if (iDataErrorStreamAvailable)
		{
			iDataErrorStreamAvailable = false;
			return getOutputStream(iDataErrorStream, false);	
		}
		return null;
	}
}
