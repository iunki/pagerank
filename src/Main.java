import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


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

        matrix = new int[matrixSize][matrixSize];

        if (matrixFileExists()) {       //чтение из файла
            sparceMatrix = new SparceMatrix();
            sparceMatrix.readFromFile(getFilePath(baseUrl, matrixSize));
            sparceMatrix.print();
        } else {                        //чтение по http
            /*в нескольких потоках*/
            System.out.println("Введите кол-во потоков:");
            int threads = Integer.valueOf(scanner.nextLine());

            Date date1 = new Date();
            readFromUrlParallel(threads);
            long time1 = new Date().getTime() - date1.getTime();
            System.out.println("\n" + time1 + " ms");

            clearVariables();

            /*в одном потоке*/
            Date date2 = new Date();
            readFromUrl();
            long time2 = new Date().getTime() - date2.getTime();
            System.out.println("\n" + time2 + " ms");

            System.out.println("Разница: " + (time2 - time1) + " ms");

            /*создание разряженной матрицы*/
            sparceMatrix = new SparceMatrix(matrix);
            /*запись в файл*/
            sparceMatrix.writeToFile(getFilePath(baseUrl, matrixSize));
        }
        /*подсчет pagerank*/
        getPageRank(sparceMatrix, matrixSize);
    }

    public static boolean matrixFileExists() {
        File f = new File(getFilePath(baseUrl, matrixSize));
        return f.exists();
    }

    private static void getHrefsPool() {
        String currLink = "/";
        hrefsQueue.add(currLink);
        hrefsArr.add(currLink);
        boolean flag = true;
        try {
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

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void clearVariables() {
        hrefsQueue = new PriorityQueue<>();
        hrefsArr = new ArrayList<>();
        matrix = new int[matrixSize][matrixSize];
    }

    public static void readFromUrl() {
        System.out.println("Чтение по http...");
        getHrefsPool();
        try {
            for (int i = 0; i < hrefsArr.size(); i++) {
                System.out.print(i + " ");
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

    public static void readFromUrlParallel(int THREADS) { // THREADS кол-во потоков
        System.out.println("Чтение по http, " + THREADS + " потоков...");
        try {
            getHrefsPool();

            ExecutorService pool = Executors.newFixedThreadPool(THREADS);
            List<Callable<Object>> tasks = new ArrayList<>();

            try {
                for (int i = 0; i < hrefsArr.size(); i++) {

                    final int I = i;

                    tasks.add(new Callable<Object>() {

                        public Object call() throws Exception {
                            Document doc = Jsoup.connect(baseUrl + hrefsArr.get(I)).get();
                            for (int j = 0; j < hrefsArr.size(); j++) {

                                Elements siteHrefs = doc.select("a");
                                for (Element tag : siteHrefs) {
                                    if (tag.attr("href").equals(hrefsArr.get(j))) {
                                        matrix[I][j]++;
                                    }
                                }

                            }
                            System.out.print(I + " ");
                            return null;
                        }
                    });
                }
                List<Future<Object>> invokeAll = pool.invokeAll(tasks);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                pool.shutdown();
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
                c[i] += sparceMatrix.get(i, j);
            }
        }

        for (int k = 0; k < 20; k++) {
            for (int j = 0; j < matrixSize; j++) {
                sum = 0;
                pagesRank[j] = 1 - d;
                for (int i = 0; i < matrixSize; i++) {
                    if (sparceMatrix.get(i, j) > 0) {
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
        System.out.println("Pagerank: ");
        for (int i = 0; i < pagesRank.length; i++) {
            System.out.print(String.format("%.2f", pagesRank[i]) + " ");
        }
    }

}
