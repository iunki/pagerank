import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;


public class Main {

    public static String baseUrl;

    public static int matrixSize = 100;

    public static int[][] matrix = new int[matrixSize][matrixSize];

    public static Queue<String> hrefsQueue = new PriorityQueue<>();
    public static ArrayList<String> hrefsArr = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите URL:");
        baseUrl = scanner.nextLine().replaceAll("\\s", "");

        if (matrixFileExists()) {
            readFromFile();

        } else {
            System.out.println("Введите размер матрицы:");
            matrixSize = Integer.valueOf(scanner.nextLine());

            readFromUrl();
            writeToFile();
        }
        getPageRank(matrix, matrixSize);
    }

    public static boolean matrixFileExists() {
        File f = new File(getFilePath(baseUrl));
        return f.exists();
    }

    public static void readFromUrl() {
        System.out.println("Чтение по http...");
        try {
            String currLink = "/";
            hrefsQueue.add(currLink);
            hrefsArr.add(currLink);
            boolean flag = true;

            while (hrefsArr.size() <= matrixSize && flag) {
                Document doc = Jsoup.connect(baseUrl + currLink).get();

                Elements siteHrefs = doc.select("a");

                for (Element tag : siteHrefs) {
                    String link = tag.attr("href");
                    if (hrefsArr.size() < matrixSize) {
                        if (!hrefsArr.contains(link) && !link.equals("/") && link.length() > 0 && link.charAt(0) == '/') {
                            hrefsQueue.add(link);
                            hrefsArr.add(link);
                        }
                    } else {
                        flag = false;
                    }
                }

                currLink = hrefsQueue.poll();
            }

            for (int i = 0; i < hrefsArr.size(); i++) {
                System.out.println(i + "/" + matrixSize);
                Document doc = Jsoup.connect(baseUrl + hrefsArr.get(i)).get();
                for (int j = 0; j < hrefsArr.size(); j++) {

                    Elements siteHrefs = doc.select("a");
                    for (Element tag : siteHrefs) {
                        if (tag.attr("href").equals(hrefsArr.get(j))) {
                            matrix[i][j]++;
                        }
                    }

                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void readFromFile() {
        System.out.println("Чтение из файла...");
        try (BufferedReader br = new BufferedReader(new FileReader(getFilePath(baseUrl)))) {
            String line;
            matrixSize = Integer.valueOf(br.readLine());
            br.readLine();
            hrefsArr = new ArrayList<String>(Arrays.asList(br.readLine().split(";")));

            int i = 0;
            while ((line = br.readLine()) != null) {
                matrix[i] = Arrays.asList(line.split(" ")).stream().mapToInt(Integer::parseInt).toArray();
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile() {
        System.out.println("Запись в файл...");

        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(getFilePath(baseUrl)));
            writer.append(matrixSize + "\n");
            writer.append(baseUrl + "\n");

            for (int i = 0; i < hrefsArr.size(); i++) {
                writer.append(hrefsArr.get(i) + ";");
            }

            writer.append("\n");

            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    writer.append(matrix[i][j] + " ");
                }
                writer.append("\n");
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

    public static String getFilePath(String url) {
        return "./" + url.replaceAll("https|http|/|:|\\s", "_100") + ".txt";
    }

    private static void getPageRank(int[][] matrix, int matrixSize) {
        double d = 0.85; //коэфициент затухания
        int c[] = new int[matrixSize]; //общее число ссылок на i-й странице
        double sum; // d * (sum (PR[i] / C[i]))
        double[] pagesRankOld = new double[matrixSize]; //PR на предыдущем шаге
        double[] pagesRank = new double[matrixSize]; //текущий PR

        for (int i = 0; i < matrixSize; i++) {
            pagesRankOld[i] = 1;
            for (int j = 0; j < matrixSize; j++) {
                c[i] += matrix[i][j];
            }
        }

        for (int k = 0; k < 10; k++) {
            for (int j = 0; j < matrixSize; j++) {
                sum = 0;
                pagesRank[j] = 1 - d;
                for (int i = 0; i < matrixSize; i++) {
                    if (matrix[i][j] > 0) {
                        sum += pagesRankOld[i] / c[i];
                    }
                }
                sum = d * sum;
                pagesRank[j] += sum;
            }

            pagesRankOld = pagesRank;
        }

        /*вывод pagerank*/
        System.out.println("---------------");
        for (int i = 0; i < pagesRank.length; i++) {
            System.out.println(String.format("%.2f", pagesRank[i]));
        }
    }


    public static double getArrSum(double arr[]) {
        double summ = 0;
        for (int i = 0; i < arr.length; i++) {
            summ += arr[i];
        }
        return summ;
    }
}
