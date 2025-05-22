import com.gurobi.gurobi.*;

public class Pr1 {
    public static void main(String[] args) {
       try{

        // Create a Gurobi environment
        GRBEnv env = new GRBEnv("gurobi.log");
        env.set(GRB.IntParam.Presolve, 0);

        GRBModel model = new GRBModel(env);

        // Create variables
        // The variables are defined with a lower bound of 0.0, an upper bound of infinity,
        // an objective coefficient of 0.0, and a type of continuous variable.
        GRBVar x = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "x");
        GRBVar y = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "y");
        GRBVar z = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "z");

        // Set the objective function
        GRBLinExpr expr = new GRBLinExpr();
        expr.addTerm(1.0, x);
        expr.addTerm(1.0, y);
        expr.addTerm(2.0, z);
        // Set the objective to maximize
        model.setObjective(expr, GRB.MAXIMIZE);

        // add the expression to the model
        model.addConstr(expr, GRB.LESS_EQUAL, 4.0, "c0");

        //add the vinculum expression
        GRBLinExpr expr2 = new GRBLinExpr();
        expr2.addTerm(1.0, x);
        expr2.addTerm(1.0, y);
        // add the expression to the model
        model.addConstr(expr2, GRB.LESS_EQUAL, 1.0, "c1");

        //optimize model
        model.optimize();
        // Print the results
        System.out.println(x.get(GRB.StringAttr.VarName) + " " + x.get(GRB.DoubleAttr.X));
		System.out.println(y.get(GRB.StringAttr.VarName) + " " + y.get(GRB.DoubleAttr.X));
		System.out.println(z.get(GRB.StringAttr.VarName) + " " + z.get(GRB.DoubleAttr.X));
		System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
  
        //dispose of the model
        model.dispose();
        //dispose of the environment
        env.dispose();
         } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());

       }
    }
}