import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by yunki on 10.04.2018.
 */
public class SparceMatrix {

    public ArrayList<Integer> A = new ArrayList<>(); //значения ненулевых элементов
    public ArrayList<Integer> LJ = new ArrayList<>(); // столбцы ненулевых элементов(j)
    public ArrayList<Integer> LI = new ArrayList<>(); //с какого значения(порядкового номера в А) начинается строка(i)


    public SparceMatrix(){}

    public SparceMatrix(int matrix[][]) {
        generateMatrix(matrix);
    }

    public int get(int i, int j) {
        int element = 0; // значение искомого элемента
        int N1 = LI.get(i);
        int N2 = LI.get(i + 1);
        for (int k = N1; k < N2; k++) {
            if (LJ.get(k) == j) {
                element = A.get(k);
                break;
            }
        }
        return element;
    }

    public void print() {
        System.out.print("A: ");
        for (int i = 0; i < A.size(); i++) {
            System.out.print(A.get(i) + " ");
        }
        System.out.print("\nLJ: ");
        for (int i = 0; i < LJ.size(); i++) {
            System.out.print(LJ.get(i) + " ");
        }
        System.out.print("\nLI: ");
        for (int i = 0; i < LI.size(); i++) {
            System.out.print(LI.get(i) + " ");
        }
        System.out.println();
    }

    public void writeToFile(String fileName) {
        System.out.println("Запись в файл...");

        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));

            for (int i = 0; i < A.size(); i++) {
                writer.append(A.get(i) + " ");
            }
            writer.append("\n");
            for (int i = 0; i < LJ.size(); i++) {
                writer.append(LJ.get(i) + " ");
            }
            writer.append("\n");
            for (int i = 0; i < LI.size(); i++) {
                writer.append(LI.get(i) + " ");
            }

            writer.flush();
            System.out.println("Готово!");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readFromFile(String fileName) {
        System.out.println("Чтение из файла...");
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            String[] arr = line.split(" ");
            for (int i = 0; i< arr.length; i++){
                A.add(Integer.valueOf(arr[i]));
            }
            line = br.readLine();
            arr = line.split(" ");
            for (int i = 0; i< arr.length; i++){
                LJ.add(Integer.valueOf(arr[i]));
            }
            line = br.readLine();
            arr = line.split(" ");
            for (int i = 0; i< arr.length; i++){
                LI.add(Integer.valueOf(arr[i]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateMatrix(int matrix[][]) {
        ArrayList<Integer> rows = new ArrayList<Integer>();
        boolean added = false;
        int k = 0;
        for (int i = 0; i < matrix.length; i++) {
            added = false;
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] != 0) {
                    A.add(matrix[i][j]);
                    LJ.add(j);
                    rows.add(i);
                    if (!added) {
                        LI.add(k);
                        added = true;
                    }
                    k++;
                }
            }
            if (!added) {
                LI.add(-1);
            }
        }
        LI.add(k);
    }


}
