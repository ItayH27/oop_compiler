/***********/
/* PACKAGE */
/***********/
package symboltable;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.io.PrintWriter;
import java.util.*;
import types.*;

/****************/
/* SYMBOL TABLE */
/****************/
public class SymbolTable
{
	private int hashArraySize = 13;
	
	/**********************************************/
	/* The actual symbol table data structure ... */
	/**********************************************/
	private SymbolTableEntry[] table = new SymbolTableEntry[hashArraySize];
	private SymbolTableEntry top;
	private int topIndex = 0;
	private final HashMap<String, ArrayDeque<String>> varNameStack = new HashMap<>();

	public enum VarKind { LOCAL, PARAM, FIELD }

	private VarKind currentVarKind = null;
	public void setVarKind(VarKind k) { currentVarKind = k; }
	public void clearVarKind() { currentVarKind = null; }

	private static class FunctionContext {
		int nextParam = 1;
		int nextLocal = 1;
	}

	private static class ClassContext {
		int nextField = 1;
	}

	private final Deque<FunctionContext> functionStack = new ArrayDeque<>();

	private final Deque<ClassContext> classStack = new ArrayDeque<>();
	
	/**************************************************************/
	/* A very primitive hash function for exposition purposes ... */
	/**************************************************************/
	private int hash(String s)
	{
		if (s.charAt(0) == 'l') {return 1;}
		if (s.charAt(0) == 'm') {return 1;}
		if (s.charAt(0) == 'r') {return 3;}
		if (s.charAt(0) == 'i') {return 6;}
		if (s.charAt(0) == 'd') {return 6;}
		if (s.charAt(0) == 'k') {return 6;}
		if (s.charAt(0) == 'f') {return 6;}
		if (s.charAt(0) == 'S') {return 6;}
		return 12;
	}

	/****************************************************************************/
	/* Enter a variable, function, class type or array type to the symbol table */
	/****************************************************************************/
	public void enter(String name, Type t, SymbolTableEntry.VarKind kind, int index)
	{
		/*************************************************/
		/* [1] Compute the hash value for this new entry */
		/*************************************************/
		int hashValue = hash(name);

		/******************************************************************************/
		/* [2] Extract what will eventually be the next entry in the hashed position  */
		/*     NOTE: this entry can very well be null, but the behaviour is identical */
		/******************************************************************************/
		SymbolTableEntry next = table[hashValue];
	
		/**************************************************************************/
		/* [3] Prepare a new symbol table entry with name, type, next and prevtop */
		/**************************************************************************/
		SymbolTableEntry e = new SymbolTableEntry(name,t,hashValue,next,top, topIndex++, kind, index);

		/**********************************************/
		/* [4] Update the top of the symbol table ... */
		/**********************************************/
		top = e;
		
		/****************************************/
		/* [5] Enter the new entry to the table */
		/****************************************/
		table[hashValue] = e;
		
		/**************************/
		/* [6] Print Symbol Table */
		/**************************/
		// printMe();
	}

	public void enterVarByMode(String name, Type t) {
		if (currentVarKind == null) {
			// global var or "no kind" var: keep as plain entry
			enter(name, t, null, 0);
			return;
		}

		switch (currentVarKind) {
			case PARAM -> enterParam(name, t);
			case LOCAL -> enterLocal(name, t);
			case FIELD -> enterField(name, t);
		}
	}

	public void enterParam(String name, Type t) {
		// if (functionStack.isEmpty()) {
		// 	throw new RuntimeException("enterParam called outside function scope");
		// }
		FunctionContext fc = functionStack.peek();
		enter(name, t, SymbolTableEntry.VarKind.PARAM, fc.nextParam++);
	}

	public void enterLocal(String name, Type t) {
		// if (functionStack.isEmpty()) {
		// 	throw new RuntimeException("enterLocal called outside function scope");
		// }
		FunctionContext fc = functionStack.peek();
		enter(name, t, SymbolTableEntry.VarKind.LOCAL, fc.nextLocal++);
	}

	public void enterField(String name, Type t) {
		// if (classStack.isEmpty()) {
		// 	throw new RuntimeException("enterField called outside class scope");
		// }
		ClassContext cc = classStack.peek();
		enter(name, t, SymbolTableEntry.VarKind.FIELD, cc.nextField++);
	}

	/***********************************************/
	/* Find the inner-most scope element with name */
	/***********************************************/
	public Type find(String name)
	{
		SymbolTableEntry e;
				
		for (e = table[hash(name)]; e != null; e = e.next)
		{
			if (name.equals(e.name))
			{
				return e.type;
			}
		}
		
		return null;
	}

	public SymbolTableEntry findEntry(String name) {
		SymbolTableEntry e;
				
		for (e = table[hash(name)]; e != null; e = e.next)
		{
			if (name.equals(e.name))
			{
				return e;
			}
		}
		
		return null;
	}

	/***************************************************************************/
	/* begine scope = Enter the <SCOPE-BOUNDARY> element to the data structure */
	/***************************************************************************/
	public void beginScope()
	{
		/************************************************************************/
		/* Though <SCOPE-BOUNDARY> entries are present inside the symbol table, */
		/* they are not really types. In order to be able to debug print them,  */
		/* a special TYPE_FOR_SCOPE_BOUNDARIES was developed for them. This     */
		/* class only contain their type name which is the bottom sign: _|_     */
		/************************************************************************/
		enter(
			"SCOPE-BOUNDARY",
			new TypeForScopeBoundaries("NONE"),
			null,
			0);

		/*********************************************/
		/* Print the symbol table after every change */
		/*********************************************/
		// printMe();
	}

	/********************************************************************************/
	/* end scope = Keep popping elements out of the data structure,                 */
	/* from most recent element entered, until a <NEW-SCOPE> element is encountered */
	/********************************************************************************/
	public void endScope()
	{
		/**************************************************************************/
		/* Pop elements from the symbol table stack until a SCOPE-BOUNDARY is hit */		
		/**************************************************************************/
		while (!top.name.equals("SCOPE-BOUNDARY"))
		{
			unbindVar(top.name);
			
			table[top.index] = top.next;
			topIndex = topIndex -1;
			top = top.prevtop;
		}
		/**************************************/
		/* Pop the SCOPE-BOUNDARY sign itself */		
		/**************************************/
		table[top.index] = top.next;
		topIndex = topIndex -1;
		top = top.prevtop;

		/*********************************************/
		/* Print the symbol table after every change */		
		/*********************************************/
		// printMe();
	}

	public void beginFunctionScope() {
		beginScope();                   // inserts SCOPE-BOUNDARY entry (your existing code)
		functionStack.push(new FunctionContext()); // reset param/local counters for this function
	}

	public void endFunctionScope() {
		endScope();                     // pops until SCOPE-BOUNDARY
		functionStack.pop();
	}

	public void beginClassScope() {
		beginScope();
		classStack.push(new ClassContext()); // reset field counter for this class
	}

	public void endClassScope() {
		endScope();
		classStack.pop();
	}

	
	public static int n=0;
	
	public void printMe()
	{
		int i=0;
		int j=0;
		String dirname="./output/";
		String filename=String.format("SYMBOL_TABLE_%d_IN_GRAPHVIZ_DOT_FORMAT.txt",n++);

		try
		{
			/*******************************************/
			/* [1] Open Graphviz text file for writing */
			/*******************************************/
			PrintWriter fileWriter = new PrintWriter(dirname+filename);

			/*********************************/
			/* [2] Write Graphviz dot prolog */
			/*********************************/
			fileWriter.print("digraph structs {\n");
			fileWriter.print("rankdir = LR\n");
			fileWriter.print("node [shape=record];\n");

			/*******************************/
			/* [3] Write Hash Table Itself */
			/*******************************/
			fileWriter.print("hashTable [label=\"");
			for (i=0;i<hashArraySize-1;i++) { fileWriter.format("<f%d>\n%d\n|",i,i); }
			fileWriter.format("<f%d>\n%d\n\"];\n",hashArraySize-1,hashArraySize-1);
		
			/****************************************************************************/
			/* [4] Loop over hash table array and print all linked lists per array cell */
			/****************************************************************************/
			for (i=0;i<hashArraySize;i++)
			{
				if (table[i] != null)
				{
					/*****************************************************/
					/* [4a] Print hash table array[i] -> entry(i,0) edge */
					/*****************************************************/
					fileWriter.format("hashTable:f%d -> node_%d_0:f0;\n",i,i);
				}
				j=0;
				for (SymbolTableEntry it = table[i]; it!=null; it=it.next)
				{
					/*******************************/
					/* [4b] Print entry(i,it) node */
					/*******************************/
					fileWriter.format("node_%d_%d ",i,j);
					fileWriter.format("[label=\"<f0>%s|<f1>%s|<f2>prevtop=%s|<f3>next\"];\n",
						it.name,
						it.type.name,
						it.offsetLabel,
						it.prevtopIndex);

					if (it.next != null)
					{
						/***************************************************/
						/* [4c] Print entry(i,it) -> entry(i,it.next) edge */
						/***************************************************/
						fileWriter.format(
							"node_%d_%d -> node_%d_%d [style=invis,weight=10];\n",
							i,j,i,j+1);
						fileWriter.format(
							"node_%d_%d:f3 -> node_%d_%d:f0;\n",
							i,j,i,j+1);
					}
					j++;
				}
			}
			fileWriter.print("}\n");
			fileWriter.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static SymbolTable instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected SymbolTable() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static SymbolTable getInstance()
	{
		if (instance == null)
		{
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new SymbolTable();

			/*****************************************/
			/* [1] Enter primitive types int, string */
			/*****************************************/
			instance.enter("int",   TypeInt.getInstance(), null, 0);
			instance.enter("string", TypeString.getInstance(), null, 0);

			/*************************************/
			/* [2] How should we handle void ??? */
			/*************************************/

			instance.enter("void", TypeVoid.getInstance(), null, 0);


			/***************************************/
			/* [3] Enter library function PrintInt */
			/***************************************/
			instance.enter(
				"PrintInt",
				new TypeFunction(
					TypeVoid.getInstance(),
					"PrintInt",
					new TypeList(
						TypeInt.getInstance(),
						null)),
					null, 0);
			
			instance.enter(
    			"PrintString",
    			new TypeFunction(
        			TypeVoid.getInstance(),
        		"PrintString",
        		new TypeList(
            		TypeString.getInstance(),
            		null)),
					null, 0);
			
		}
		return instance;
	}

	public Type findInCurrentScope(String name) {
		SymbolTableEntry it = top;

	
		while (it != null && !it.name.equals("SCOPE-BOUNDARY")) {
			if (it.name.equals(name)) {
				return it.type;
			}
			it = it.prevtop;
		}

		return null; // not found in this scope
	}

	public void bindVar(String sourceName, String uniqueIrName){
		varNameStack.computeIfAbsent(sourceName, k -> new ArrayDeque<>()).push(uniqueIrName);
	}

	public void unbindVar(String sourceName){
		ArrayDeque<String> st = varNameStack.get(sourceName);
		if (st == null || st.isEmpty()) return;
		st.pop();
		if (st.isEmpty()) varNameStack.remove(sourceName);
	}

	public String resolveVar(String sourceName){
		ArrayDeque<String> st = varNameStack.get(sourceName);
		if (st == null || st.isEmpty()) return null;
		return st.peek();
	}


}
