import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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

    public static void main(String[] args){

        try {

            System.out.println("Inizio risoluzione con Gurobi...");
            leggiIstanza(NOME_FILE);
            stampaIstanza(); // per debug
            
        } catch (Exception e) {
            System.out.println("Errore durante la lettura dell'istanza: " + e.getMessage());
            return;
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
    static void stampaIstanza() {
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
    }
}
