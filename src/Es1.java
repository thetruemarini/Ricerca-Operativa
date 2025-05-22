import com.gurobi.gurobi.*;

public class Es1 {
    public static void main(String[] args) {
        try{
            GRBEnv env = new GRBEnv("Es1.log");
            env.set(GRB.IntParam.Presolve, 0);
            GRBModel model = new GRBModel(env);
            
            GRBVar x = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "x");
            GRBVar y = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "y");
            GRBVar z = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "z");
            GRBVar w = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "w");

            GRBLinExpr expr = new GRBLinExpr();
            expr.addTerm(0.02, x);
            expr.addTerm(0.05, y);
            expr.addTerm(0.12, z);
            expr.addTerm(0.04, w);
            model.setObjective(expr, GRB.MAXIMIZE);

            expr = new GRBLinExpr();
            expr.addTerm(1.0, x);
            expr.addTerm(1.0, y);   
            expr.addTerm(1.0, z);
            expr.addTerm(1.0, w);
            model.addConstr(expr, GRB.LESS_EQUAL, 90000.0 , "c0");

            expr = new GRBLinExpr();
            expr.addTerm(1.0, x);
            expr.addTerm(1.0, y);
            model.addConstr(expr, GRB.GREATER_EQUAL, 45000.0 , "c1");

            expr = new GRBLinExpr();
            expr.addTerm(1.0, x);
            model.addConstr(expr, GRB.LESS_EQUAL, 30000.0 , "c2");

            expr = new GRBLinExpr();
            expr.addTerm(1.0, y);
            model.addConstr(expr, GRB.LESS_EQUAL, 30000.0 , "c3");

            expr = new GRBLinExpr();    
            expr.addTerm(1.0, z);
            model.addConstr(expr, GRB.LESS_EQUAL, 20000.0 , "c4");

            expr = new GRBLinExpr();
            expr.addTerm(2.0, z);
            expr.addTerm(-1.0, x);
            model.addConstr(expr, GRB.LESS_EQUAL, 0.0 , "c5");

            model.optimize();

            System.out.println(x.get(GRB.StringAttr.VarName) + " " + x.get(GRB.DoubleAttr.X));
            System.out.println(y.get(GRB.StringAttr.VarName) + " " + y.get(GRB.DoubleAttr.X));
            System.out.println(z.get(GRB.StringAttr.VarName) + " " + z.get(GRB.DoubleAttr.X));
            System.out.println(w.get(GRB.StringAttr.VarName) + " " + w.get(GRB.DoubleAttr.X));
            System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
            model.dispose();
            env.dispose();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
