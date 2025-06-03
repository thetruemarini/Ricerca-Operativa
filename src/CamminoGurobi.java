import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gurobi.gurobi.*;

public class CamminoGurobi {
    //costanti:
    private static final String NOME_FILE = "Istanza_Coppia_2_102.txt";

    // Parametri letti dal file
    static int N;              // numero di nodi
    static int s, t;           // sorgente e destinazione
    static int r1, r2, r3;     // possibili numeri di archi
    static int tmax;           // t_max
    static double[][] C;       // matrice dei costi C[i][j]

    //strutture gurobi
    static GRBEnv env;
    static GRBModel model;
    static GRBModel relaxedModel; // per il rilassamento continuo

    public static void main(String[] args){

        try {

            System.out.println("Inizio risoluzione con Gurobi...");

            // lettura della istanza dal file:
            leggiIstanza(NOME_FILE);
            // stampaIstanza(); // per debug

            // Inizializzazione dell'ambiente Gurobi
            env = new GRBEnv(true);
            env.set("logFile", "CamminoGurobi.log");
            env.start();
            model = new GRBModel(env);

            //========  QUESITO 1: Risoluzione del modello intero      =========
            risolviModelloIntero(); //TODO: verificare obj relaxed (che risulta uguale)

            //========  QUESITO 2: Verifica soluzioni ottime multiple  =========
            verificaSoluzioniMultiple();  //TODO: verificare che siano effettivamente diverse

            //========  QUESITO 3: Analisi del rilassamento continuo   =========
            analisiRilassamentoContinuo(); //TODO: verificare sol di base (0-1) 

            // Chiudo il modello e l'ambiente Gurobi
            model.dispose();
            env.dispose();
            System.out.println("Risoluzione completata con successo!");
            
        } catch (GRBException e) {
            System.out.println("Errore Gurobi: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Errore di I/O: " + e.getMessage());
        }
    }

    // Metodo per leggere l' istanza dal file
    static void leggiIstanza(String nomeFile) throws IOException {
        // Inizializza N, s, t, r1, r2, r3, tmax e la matrice costi C
        BufferedReader br = new BufferedReader(new FileReader(nomeFile));
        String linea;
        //leggo la prima riga (N)
        linea = br.readLine().replaceAll("[^0-9]", "");
        N = Integer.parseInt(linea);
        //leggo la seconda riga (s)
        linea = br.readLine().replaceAll("[^0-9]", "");
        s = Integer.parseInt(linea);
        //leggo la terza riga (t)
        linea = br.readLine().replaceAll("[^0-9]", "");
        t = Integer.parseInt(linea);
        //leggo la quarta riga (r1, r2, r3)
        linea = br.readLine().trim();
        String[] tmp = linea.split("\\s+");
        r1 = Integer.parseInt(tmp[3]);
        r2 = Integer.parseInt(tmp[4]);
        r3 = Integer.parseInt(tmp[5]);
        //leggo la quinta riga (tmax)
        linea = br.readLine().replaceAll("[^0-9]", "");
        tmax = Integer.parseInt(linea);
        // Inizializzo la matrice dei costi C
        linea = br.readLine(); //saltando la prima riga
        C = new double[N][N];
        for (int i = 0; i < N; i++) {
            linea = br.readLine().trim();
            String[] vals = linea.split("\\s+");
            for (int j = 0; j < N; j++) {
                C[i][j] = Double.parseDouble(vals[j]);
            }
        }
        br.close();

    }

    //Metodo per stampare a schermo i dati dell'istanza (debug)
    /* static void stampaIstanza() {
        System.out.println("N: " + N);
        System.out.println("Sorgente (s): " + s);
        System.out.println("Destinazione (t): " + t);
        System.out.println("r1: " + r1 + ", r2: " + r2 + ", r3: " + r3);
        System.out.println("tmax: " + tmax);
        System.out.println("Matrice dei costi C:");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                System.out.print(C[i][j] + " ");
            }
            System.out.println();
        }
    } */

    // Metodo per risolvere il problema con Gurobi
    // QUESITO 1: implementare e risolvere il modelllo intero
    static void risolviModelloIntero() throws GRBException {
        //creazione variabili x[i][j] per ogni arco (i, j)
        GRBVar[][] x = new GRBVar[N][N];
        for(int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                // evitiamo di creare variabili per archi non esistenti (creiamo la variabile e la impostiamo a 0)
                x[i][j] = model.addVar(0.0, 1.0, C[i][j], GRB.BINARY, "x_" + i + "_" + j);
            }
        }

        //creazione delle variabili y1, y2, y3
        GRBVar y1 = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y1");
        GRBVar y2 = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y2");
        GRBVar y3 = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y3");

        //imposto il vincolo: y1 + y2 + y3 = 1
        GRBLinExpr exprY = new GRBLinExpr();
        exprY.addTerm(1.0, y1);
        exprY.addTerm(1.0, y2);
        exprY.addTerm(1.0, y3);
        model.addConstr(exprY, GRB.EQUAL, 1.0, "sumY");

        //imposto il vincomo: numero archi = r1 * y1 + r2 * y2 + r3 * y3
        GRBLinExpr exprNumArchi = new GRBLinExpr();
        for(int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                exprNumArchi.addTerm(1.0, x[i][j]);
            }
        }
        GRBLinExpr latoDx = new GRBLinExpr();
        latoDx.addTerm(r1, y1);
        latoDx.addTerm(r2, y2);
        latoDx.addTerm(r3, y3);
        model.addConstr(exprNumArchi, GRB.EQUAL, latoDx, "numArchi");

        //vincoli di flusso:
        //vincoli per le uscite dalla sorgente:
        GRBLinExpr expr = new GRBLinExpr();
        for(int j=0; j<N; j++)
            expr.addTerm(1.0, x[s][j]);
        for(int i=0; i<N; i++)
            expr.addTerm(-1.0, x[i][s]);
        model.addConstr(expr, GRB.EQUAL, 1.0, "flusso_sorgente");

        //vincoli per i nodi intermedi:
        for(int v=0; v<N; v++) {
            if(v == s || v == t) continue; // salta sorgente e destinazione
            expr = new GRBLinExpr();
            for(int i=0; i<N; i++) 
                expr.addTerm(-1.0, x[i][v]);
            for(int j=0; j<N; j++) 
                expr.addTerm(1.0, x[v][j]);
            model.addConstr(expr, GRB.EQUAL, 0.0, "flusso_nodo_" + v);
            
        }

        //vincoli per le entrate nella destinazione:
        expr = new GRBLinExpr();
        for(int i=0; i<N; i++)
            expr.addTerm(-1.0, x[i][t]);
        for(int j=0; j<N; j++)
            expr.addTerm(1.0, x[t][j]);
        model.addConstr(expr, GRB.EQUAL, -1.0, "flusso_destinazione");

        //imposto il vincolo di tempo: somma dei costi degli archi <= tmax
        expr = new GRBLinExpr();
        for(int j=0; j<N; j++)
            expr.addTerm(C[s][j], x[s][j]);
        for(int i=0; i<N; i++) 
            expr.addTerm(C[i][t], x[i][t]);
        model.addConstr(expr, GRB.LESS_EQUAL, tmax, "vincolo_destinazione");

        //imposto la funzione obiettivo: minimizzare il costo totale
        model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);

        //fissare x[i][i] = 0 per ogni i
        for(int i = 0; i < N; i++) {
            model.addConstr(x[i][i], GRB.EQUAL, 0.0, "zero_loop_" + i);
        }

        //misuratore tempo
        long tic = System.currentTimeMillis();
        model.optimize();
        long toc = System.currentTimeMillis();

        //estraggo il valore ottimo intero
        double objIntero = model.get(GRB.DoubleAttr.ObjVal);
        double tempoSolv = (toc - tic)/1000.0; // in secondi

        //stampo i risultati richiesti nell'esercizio 1
        System.out.println("GRUPPO <Coppia 102>" );
        System.out.println("Componenti: <Riccardo Marini, Luca Ughini>");
        System.out.println("QUESITO 1: Modello intero risolto con Gurobi");
        System.out.println("Tempo di risoluzione: " + tempoSolv + " secondi");
        System.out.println("Costo cammino ottimo = " + String.format("%.4f", objIntero));

        //ricostruzione del cammino ottimo dal vettore x[i][j]
        List<String> cammino = new ArrayList<>();
        int curr = s;
        while (curr != t) {
            for (int j = 0; j < N; j++) {
                if (x[curr][j].get(GRB.DoubleAttr.X) > 0.5) { // se x[i][j] è 1
                    cammino.add("(" + curr + " -> " + j + ")");
                    curr = j; // passo al nodo successivo
                    break;
                }
            }
        }
        
        //stampo il cammino ottimo
        System.out.println("Cammino ottimo =  ");
        for(int i=0; i<cammino.size(); i++) {
            if (i>0) System.out.println(" ");
            System.out.print(cammino.get(i));
        }
        System.out.println("\n");
        System.out.println("Numero archi utilizzati = " + (cammino.size()));
        System.out.println("Tempo = " + String.format("%.4f", tempoSolv) + " secondi");

        //rilassamento continuo del modello intero

        relaxedModel = model.relax(); //ora x e y sono continue  ed il modello è rilassato in relaxedModel
        relaxedModel.optimize();
        double objRelax = model.get(GRB.DoubleAttr.ObjVal);
        System.out.println("Costo rilassamento continuo = " + String.format("%.4f", objRelax));

        //TODO: nella relazione va segnato se il rilassamento è <,>,=
    }

    //QUESITO 2: verificare la presenza di soluzioni ottime multiple
    static void verificaSoluzioniMultiple() throws GRBException {
        System.out.println("QUESITO 2: Verifica soluzioni ottime multiple");

        //In Gurobi è possibile utilizzare solution pool per raccogliere le soluz ottime
        model.getEnv().set(GRB.IntParam.PoolSearchMode, 2); // ricerca di soluzioni multiple
        model.getEnv().set(GRB.IntParam.PoolSolutions, 10); // numero massimo di soluzioni nel pool
        model.set(GRB.DoubleParam.PoolGap, 0.0); //gap 0 per ottime

        //ottimizza il modello
        model.optimize();

        int numSoluzioni = model.get(GRB.IntAttr.SolCount);
        for(int sol=0; sol<numSoluzioni;sol++){
            model.set(GRB.IntParam.SolutionNumber, sol);

            //ricostruisco il cammino come fatto nel metodo del quesito precedente
            List<String> cammino = new ArrayList<>();
            int curr = s;
            while (curr != t) {
                for (int j = 0; j < N; j++) {
                    if (model.getVarByName("x_" + curr + "_" + j).get(GRB.DoubleAttr.Xn) > 0.5) { // se x[i][j] è 1
                        cammino.add("(" + curr + " -> " + j + ")");
                        curr = j; // passo al nodo successivo
                        break;
                    }
                }
            }

            //stampo il cammino ottimo
            System.out.println("Cammino " + (sol + 1) + " = ");
            for(int i=0; i<cammino.size(); i++) {
                if (i>0) System.out.print(" ");
                System.out.print(cammino.get(i));
            }
            System.out.println();
        }
        //se numeroSol==1 c'è una sola sol ottima altrimenti le stampa tutte 
    }

    //QUESITO 3: Analisi del rilassamento continuo
    static void analisiRilassamentoContinuo() throws GRBException {
        System.out.println("QUESITO 3: Analisi del rilassamento continuo");
        //già fatto nel metodo risolviModelloIntero() quindi 
        //assumiamo che i valori delle variabili siano gia settati 
        //ed il modello corrente in modalita continua
        
        //conto le variabili frazionarie
        int frazCount = 0;
        for(GRBVar var : relaxedModel.getVars()) {
            double val = var.get(GRB.DoubleAttr.X);
            if (var.get(GRB.CharAttr.VType) == GRB.CONTINUOUS) {
                if(val > 1e-6 && val < 1.0 - 1e-6) { // tolleranza per considerare frazionario
                    frazCount++;
                }
            }
        }
        //stampo il numero di variabili frazionarie
        System.out.println("Numero di variabili frazionarie nel rilassamento continuo: " + frazCount);

        //Calcolo variabili in base e fuori base
        StringBuilder baseStatus = new StringBuilder("[");
        for(GRBVar var : relaxedModel.getVars()) {
            int bas = var.get(GRB.IntAttr.VBasis);
            if (bas == 1) baseStatus.append("ECCOLO QUA DIO PORCO, ");
            else baseStatus.append("0, ");
        }
        if (baseStatus.charAt(baseStatus.length() - 1) == ',') {
            baseStatus.deleteCharAt(baseStatus.length() - 1); // rimuovo l'ultima virgola
        }
        baseStatus.append("]");
        System.out.println("Stato delle variabili (1 -> base, 0 -> non in base): " /* + baseStatus.toString() */);

        //Coefficienti di costo ridotto:
        StringBuilder redCost = new StringBuilder("[");
        for(GRBVar var : relaxedModel.getVars()) {
            double rc = var.get(GRB.DoubleAttr.RC);
            redCost.append(String.format("%.4f", rc)).append(", ");
        }
        if (redCost.charAt(redCost.length() - 1) == ',') {
            redCost.deleteCharAt(redCost.length() - 1); // rimuovo l'ultima virgola
        }
        redCost.append("]");
        System.out.println("Coefficienti di costo ridotto: " + redCost.toString());

        //Degenerazione e Molteplicita:
        //degenerazione: se una variabile in base è zero o uno, il modello è degenere:
        boolean degen = false;
        for(GRBVar var : relaxedModel.getVars()){
            int bas = var.get(GRB.IntAttr.VBasis);
            double val = var.get(GRB.DoubleAttr.X);
            if (bas == 1 && Math.abs(val) < 1e-6) {
                degen = true; // se una variabile in base è zero o uno, il modello è degenere
                break;
            }
        }
        //stampo se il modello è degenere
        System.out.println("Degenere:\t" + (degen ? "Sì" : "No"));

        //molteplicita: se il numero di soluzioni ottime è maggiore di 1, allora c'è molteplicita
        boolean multi = false;
        for(GRBVar var : relaxedModel.getVars()){
            int bas = var.get(GRB.IntAttr.VBasis);
            double rc = var.get(GRB.DoubleAttr.RC);
            if (bas == 1 && Math.abs(rc) < 1e-6) {
                multi = true; // se una variabile in base ha coefficiente di costo ridotto zero, c'è molteplicita
                break;
            }
        }
        //stampo se c'è molteplicita
        System.out.println("Molteplicità:\t" + (multi ? "Sì" : "No"));

        //vincoli attivi: per ogni vincolo, se il valore di slack è zero, è attivo
        List<String> vincoliAttivi = new ArrayList<>();
        for (GRBConstr constr : relaxedModel.getConstrs()) {
            double slack = constr.get(GRB.DoubleAttr.Slack);
            if (Math.abs(slack) < 1e-6) { // tolleranza per considerare attivo
                vincoliAttivi.add(constr.get(GRB.StringAttr.ConstrName));
            }
        }
        //stampo i vincoli attivi
        System.out.println("Vincoli attivi: " + vincoliAttivi.size());
        for (String vincolo : vincoliAttivi) {
            System.out.println(" - " + vincolo);
        }
        System.out.println("=========================================");

    }
}
