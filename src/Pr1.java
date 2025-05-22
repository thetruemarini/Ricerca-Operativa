import com.gurobi.gurobi.*;

public class Pr1 {

	public static void main(String[] args){
		
	try
	{
		GRBEnv env = new GRBEnv("esempioGurobi.log");
		
		env.set(GRB.IntParam.Presolve, 0);
		
		GRBModel model = new GRBModel(env);
		
		// Creazione delle variabili
		GRBVar x = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "x");
		GRBVar y = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "y");
		GRBVar z = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "z");
		
		// Aggiunta della funzione obiettivo: max x + y + 2 z
		GRBLinExpr expr = new GRBLinExpr();
		expr.addTerm(1.0, x);
		expr.addTerm(1.0, y);
		expr.addTerm(2.0, z);
		model.setObjective(expr, GRB.MAXIMIZE);
		
		// Aggiunta del vincolo: x + 2 y + 3 z <= 4
		expr = new GRBLinExpr();
		expr.addTerm(1.0, x);
		expr.addTerm(2.0, y);
		expr.addTerm(3.0, z);
		model.addConstr(expr, GRB.LESS_EQUAL, 4.0, "c0");
		
		// Aggiunta del vincolo: x + y >= 1
		expr = new GRBLinExpr();
		expr.addTerm(1.0, x);
		expr.addTerm(1.0, y);
		model.addConstr(expr, GRB.LESS_EQUAL, 1.0, "c1");
		
		// Ottimizza il modello
		model.optimize();
		
		System.out.println(x.get(GRB.StringAttr.VarName) + " " + x.get(GRB.DoubleAttr.X));
		System.out.println(y.get(GRB.StringAttr.VarName) + " " + y.get(GRB.DoubleAttr.X));
		System.out.println(z.get(GRB.StringAttr.VarName) + " " + z.get(GRB.DoubleAttr.X));
		System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
		
		// Libera le risorse associate a modello ed env
		model.dispose();
		env.dispose();
		
	} catch (GRBException e)
	{
		System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
	}
	
	}
}