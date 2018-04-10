import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;


public class Main {

    public static String baseUrl;

    public static int matrixSize;

    public static int[][] matrix;

    public static SparceMatrix sparceMatrix;


    public static Queue<String> hrefsQueue = new PriorityQueue<>();
    public static ArrayList<String> hrefsArr = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите URL:");
        baseUrl = scanner.nextLine().replaceAll("\\s", "");

        System.out.println("Введите размер матрицы:");
        matrixSize = Integer.valueOf(scanner.nextLine());

        matrix =  new int[matrixSize][matrixSize];

        if (matrixFileExists()) {
            sparceMatrix = new SparceMatrix();
            sparceMatrix.readFromFile(getFilePath(baseUrl, matrixSize));
            sparceMatrix.print();
        } else {
            readFromUrl();
            sparceMatrix = new SparceMatrix(matrix);
            sparceMatrix.writeToFile(getFilePath(baseUrl, matrixSize));
        }

        getPageRank(sparceMatrix, matrixSize);
    }

    public static boolean matrixFileExists() {
        File f = new File(getFilePath(baseUrl, matrixSize));
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


    public static String getFilePath(String url, int matrixSize) {
        return "./" + url.replaceAll("https|http|/|:|\\s", "") + "_" + matrixSize + ".txt";
    }

    private static void getPageRank(SparceMatrix sparceMatrix, int matrixSize) {
        double d = 0.85; //коэфициент затухания
        int c[] = new int[matrixSize]; //общее число ссылок на i-й странице
        double sum; // d * (sum (PR[i] / C[i]))
        double[] pagesRankOld = new double[matrixSize]; //PR на предыдущем шаге
        double[] pagesRank = new double[matrixSize]; //текущий PR

        for (int i = 0; i < matrixSize; i++) {
            pagesRankOld[i] = 1;
            for (int j = 0; j < matrixSize; j++) {
                c[i] += sparceMatrix.get(i,j);
            }
        }

        for (int k = 0; k < 20; k++) {
            for (int j = 0; j < matrixSize; j++) {
                sum = 0;
                pagesRank[j] = 1 - d;
                for (int i = 0; i < matrixSize; i++) {
                    if (sparceMatrix.get(i,j) > 0) {
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

}
