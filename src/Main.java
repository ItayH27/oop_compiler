   
import java.io.*;

import java_cup.runtime.Symbol;
import java.util.SortedSet;
import java.util.Map;
import ast.*;
import ir.Ir;
import cfg.CFG;
import cfg.LivenessAnalyzer;
import regalloc.*;
import cfg.UseBeforeDefAnalyzer;

public class Main
{
	static public void main(String argv[])
	{
		Lexer l;
		Parser p;
		Symbol s;
		AstProgram ast;
		FileReader fileReader;
		PrintWriter fileWriter;
		String inputFileName = argv[0];
		String outputFileName = argv[1];
		
		try
		{
			/********************************/
			/* [1] Initialize a file reader */
			/********************************/
			fileReader = new FileReader(inputFileName);

			/********************************/
			/* [2] Initialize a file writer */
			/********************************/
			fileWriter = new PrintWriter(outputFileName);
			
			/******************************/
			/* [3] Initialize a new lexer */
			/******************************/
			l = new Lexer(fileReader);
			
			/*******************************/
			/* [4] Initialize a new parser */
			/*******************************/
			Parser.out = fileWriter;
			p = new Parser(l);

			/***********************************/
			/* [5] 3 ... 2 ... 1 ... Parse !!! */
			/***********************************/
			ast = (AstProgram) p.parse().value;

			/**************************/
			/* [6] Semant the AST ... */
			/**************************/
			SemantError.out = fileWriter;
			ast.semantMe();
			

			/**********************/
			/* [8] IR the AST ... */
			/**********************/
			ast.irMe();
			// Ir.getInstance().dump(new PrintWriter("ir_dump.txt"));
			CFG cfg = CFG.getInstance();
			cfg.buildFromIr();
			// cfg.dump(new PrintWriter("cfg_dump.txt"));
			
			/*************************/
			/* [7] Close output file */
			/*************************/
			LivenessAnalyzer.Result live = LivenessAnalyzer.analyze(cfg);

			// System.out.println("live: " + live.nodeToFunc);

			// Allocate registers per function
			Map<String, Map<String, String>> funcRegMaps;
			try {
				funcRegMaps = RegisterAllocator.allocateAll(live);
			} catch (RegisterAllocator.AllocationFailed e) {
				fileWriter.print("Register Allocation Failed");
				fileWriter.close();
				return;
			}

			// System.out.println("funcRegMaps: " + funcRegMaps);


			// pre-pass: compute per-function locals bytes + which runtime handlers are actually needed
			java.util.List<ir.IrCommand> irList = ir.Ir.getInstance().toList();
			java.util.Map<String, Integer> localsBytes = new java.util.HashMap<>();
			String currFunc = null;
			int maxLocalIdx = 0;
			boolean needAccessViolation = false;
			boolean needDivByZero = false;
			boolean needInvalidPtr = false;
			for (ir.IrCommand cmd : irList) {
				if (cmd instanceof ir.IrCommandLabel) {
					ir.IrCommandLabel lblCmd = (ir.IrCommandLabel) cmd;
					String lab = lblCmd.labelName;
					if (lab != null && lab.startsWith("func_end_")) {
						if (currFunc != null) {

							localsBytes.put(currFunc, Math.max(16, maxLocalIdx * 4));
						}
						currFunc = null;
						maxLocalIdx = 0;
					} else if (lab != null && funcRegMaps.containsKey(lab) && !lab.equals("main")) {
						currFunc = lab;
						maxLocalIdx = 0;
					}
				}

				if (currFunc != null && cmd instanceof ir.IrCommandAllocate) {
					ir.IrCommandAllocate a = (ir.IrCommandAllocate) cmd;
					String v = a.varName;
					if (v != null && v.startsWith("local_")) {
						try {
							int idx = Integer.parseInt(v.substring("local_".length()));
							maxLocalIdx = Math.max(maxLocalIdx, idx);
						} catch (NumberFormatException ignored) {}
					}
				}

				if (cmd instanceof ir.IrCommandArrayAccess || cmd instanceof ir.IrCommandArraySet) {
					needAccessViolation = true;
					needInvalidPtr = true;
				}
				if (cmd instanceof ir.IrCommandLoadAccess) {
					needInvalidPtr = true;
				}
				if (cmd instanceof ir.IrCommandBinopDivIntegers) {
					needDivByZero = true;
				}
				if (cmd instanceof ir.IrCommandBinopConcatStrings || cmd instanceof ir.IrCommandBinopEqStrings) {
					needInvalidPtr = true;
				}
				if (cmd instanceof ir.IrCommandFieldAccess || cmd instanceof ir.IrCommandFieldSet) {
					needInvalidPtr = true;
				}
			}

			// configure MIPS generator
			mips.MipsGenerator gen = mips.MipsGenerator.getInstance();
			gen.setFuncRegMaps(funcRegMaps);
			gen.setFileWriter(fileWriter);
			gen.setFuncLocalsBytes(localsBytes);
			if (needAccessViolation) gen.requireAccessViolation();
			if (needDivByZero) gen.requireDivByZero();
			if (needInvalidPtr) gen.requireInvalidPtr();
			gen.emitAllVtables(AstDecClass.allClasses);

			// emit
			ir.Ir.getInstance().mipsMe();

			// finish (writes exit syscall etc.)
			mips.MipsGenerator.getInstance().finalizeFile();

			fileWriter.close();



			// SortedSet<String> analysis = UseBeforeDefAnalyzer.analyze(cfg.CFG.getInstance());
			// if (analysis.size() == 0) {
			// 	fileWriter.print("!OK");
			// } else {
			// 	for (int i = 0; i <  analysis.size(); i++) {
			// 		String varName = (String) analysis.toArray()[i];
			// 		if (i != analysis.size() - 1) {
			// 			fileWriter.print(varName + "\n");
			// 		} else {
			// 			fileWriter.print(varName);
			// 		}
			// 	}
			// }
			// fileWriter.close();

			/*************************/
			/* [8] Print the AST ... */
			/*************************/
			ast.printMe();

			/*************************************/
			/* [9] Finalize AST GRAPHIZ DOT file */
			/*************************************/
			AstGraphviz.getInstance().finalizeFile();
    	}
			     
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}


