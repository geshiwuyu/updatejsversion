
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Akira on 15-1-27.
 * 更新JS/CSS引用版本�?
 */
public class VersionUpdater {
    private static final String SCAN_PATH = "/Users/zyzou001/myWorkspace/NFSpring_web";

    private static final String FILTER_RULE = "apps;common";

    public static void main(String[] args) throws IOException {
        List<String> filePaths = returnFilePath(SCAN_PATH);
        for (String path : filePaths) {
            if (filter(path) && path.endsWith(".html")) {
                System.out.println(path);
                reWriteHtml(path);
            }
        }
        System.out.println("end");
    }

    private static String getMd5ByFile(File file) {
        String value = null;
        try (FileInputStream in = new FileInputStream(file);) {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private static List<String> filePaths = new ArrayList<>();

    private static List<String> returnFilePath(String path) {
        scanPath(path);
        return filePaths;
    }

    private static void scanPath(String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory() && file.listFiles() != null) {
            for (File file2 : file.listFiles()) {
                scanPath(file2.getPath());
            }
        } else {
            filePaths.add(file.getPath());
        }
    }

    private static void reWriteHtml(String path) throws IOException {
        String data = FileUtils.readFileToString(new File(path), "utf-8");

        StringBuffer sb = new StringBuffer();
        Matcher matcher = Pattern.compile("(?<=src=\")[^\"]+").matcher(data);
        while (matcher.find()) {
            String group = matcher.group();
            if (group.indexOf(".js") == -1) {
                continue;
            }
            System.out.println("path:" + path);
            File jsFile = new File(path.substring(0, path.lastIndexOf("/") + 1) + group.substring(group.indexOf("\"") + 1, group.indexOf(".js") + 3));//path.lastIndexOf("/")windows写法
            String jsPath = jsFile.getCanonicalPath();
            if (filter(jsPath)) {
                matcher.appendReplacement(sb, group.substring(0, group.indexOf(".js") + 3) + "?v=" + getMd5ByFile(jsFile));
            }

        }
        matcher.appendTail(sb);

        StringBuffer sb2 = new StringBuffer();
        Matcher matcher2 = Pattern.compile("(?<=href=\")[^\"]+").matcher(sb.toString());
        while (matcher2.find()) {
            String group = matcher2.group();
            if (group.indexOf(".css") == -1) {
                continue;
            }
            File csssFile = new File(path.substring(0, path.lastIndexOf("/") + 1) + group.substring(group.indexOf("\"") + 1, group.indexOf(".css") + 4));//path.lastIndexOf("/")windows写法
            String csssPath = csssFile.getCanonicalPath();

            if (filter(csssPath)) {
                matcher2.appendReplacement(sb2, group.substring(0, group.indexOf(".css") + 4) + "?v=" + getMd5ByFile(csssFile));
            }

        }
        matcher2.appendTail(sb2);
        FileUtils.writeStringToFile(new File(path), sb2.toString(), "utf-8");
    }

    private static boolean filter(String path) {
        String[] filters = FILTER_RULE.split(";");
        boolean flag = false;
        for (String string : filters) {
            flag = flag || path.indexOf("/" + string + "/") > -1;// path.indexOf("\\" + string + "\\")windows写法
        }
        return flag;
    }
}
