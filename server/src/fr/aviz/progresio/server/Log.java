package fr.aviz.progresio.server;

public class Log {

	
	private static int intent = 0;

	public static void cont(Object o, String message)
	{
		if(o != null)
			System.out.print("[" + o.getClass().getSimpleName()+ "] " + intent()+ message);
		else
			System.out.print("[?] " + message);
	}
	public static void cont(String message)
	{
		System.out.print(message);
	}
	public static void nl()
	{
		System.out.println("");
	}
	
	public static void out(Object o, String message)
	{
		if(o != null)
			System.out.println("[" + o.getClass().getSimpleName()+ "] " + intent()+ message);
		else
			System.out.println("[?] " + message);
	}
	public static void out(Object o, String message, boolean increaseDecreaseIntent)
	{
		if(increaseDecreaseIntent)
			intent++;
		else
			intent--;

		if(o != null)
			System.out.println("[" + o.getClass().getSimpleName()+ "] " + intent() + message);
		else
			System.out.println("[?] " + message);
	}
	
	public static void out(String message)
	{
		System.out.println(message);
	}

	
	public static void err(Object o, String message)
	{
		if(o != null)
			System.err.println("[" + o.getClass().getSimpleName() + "] " + intent()+ message);
		else
			System.out.println("[?] " + message);
	}
	
	public static void err(String message)
	{
		System.out.println(message);
	}
	
	protected static String intent(){
		String s = "";
		for(int i=0 ; i<intent ; i++){
			s += "\t";
		}
		return s;
	}

}
