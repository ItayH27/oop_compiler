package types;

public class TypeClassVarDec
{
	public Type t;
	public String name;
	public int offset;
	
	public TypeClassVarDec(Type t, String name)
	{
		this.t = t;
		this.name = name;
		this.offset = 0;
	}
}
