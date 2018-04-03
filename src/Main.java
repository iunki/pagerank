import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;


public class Main {

    public static String baseUrl = "http://php.net/";

    public static int matrixSize = 100;

    public static int[][] matrix = new int[matrixSize][matrixSize];

    public static Queue<String> hrefsQueue = new PriorityQueue<>();
    public static ArrayList<String> hrefsArr = new ArrayList<>();

    public static void main(String[] args) {

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

            System.out.println(hrefsArr);

            for (int i = 0; i < hrefsArr.size(); i++) {
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

            writeToFile();

            System.out.println(getPageRank(matrix, matrixSize));

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void writeToFile() {

        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("./" + getFileName(baseUrl) + ".txt"));
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

    public static String getFileName(String url) {
        return url.replaceAll("https|http|/|:", "");
    }

    private static double getPageRank(int[][] matrix, int matrixSize) {
        double PR = 0; //PageRank рассматриваемой страницы
        double d = 0.85; //коэфициент затухания
        int c[] = new int[matrixSize]; //общее число ссылок на i-й странице
        double sum; // d * (sum (PR[i] / C[i]))
        double eps; //точность
        double[] pagesRankOld = new double[matrixSize]; //PR на предыдущем шаге
        double[] pagesRank = new double[matrixSize]; //текущий PR

        for (int i = 0; i < matrixSize; i++) {
            pagesRankOld[i] = 1;
            for (int j = 0; j < matrixSize; j++){
                c[i] += matrix[i][j];
            }
        }

        do {
            eps = 0;

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
            for (int i = 0; i < pagesRank.length; i++) {
                PR += pagesRank[i];
            }

            for (int i = 0; i < pagesRank.length; i++) {
                eps += (pagesRank[i] - pagesRankOld[i]) * (pagesRank[i] = pagesRankOld[i]);
            }
            pagesRankOld = pagesRank;

        } while (eps > 0.0001);
        return PR;
    }
}
