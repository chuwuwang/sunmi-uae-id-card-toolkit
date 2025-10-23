package com.sunmi.eid_plugin.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * 将日志打印到文件：
 * <p>
 * 支持设置文件最大大小LOG_SIZE_MAX  KB，超过大小重新创建文件（由于文件名是以毫秒为维度理论不会重复）
 * 支持设置文件总个数LOG_FILE_TOTAL，超过总个数删除最先创建的文件
 * 支持设置日志文件输出目录LOG_FILE_PRINT_DIR
 * 文件打印是采用单线程池队列打印，无需处理线程阻塞问题
 */
public class LogFileUtil {
    // 日志文件总数
    private static final int LOG_FILE_TOTAL = 10;
    // 单日志文件大小上限 KB
    private static final long LOG_SIZE_MAX = 5 * 1024;
    // 日志文件输出文件夹
    private static String LOG_FILE_PRINT_DIR = null;
    private static Context context;

//    static {
    //android 9
//        LOG_FILE_PRINT_DIR = getLogFilePrintDir();
//    }

    private static String getLogFilePrintDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (context == null) {
                return null;
            }
            final File[] dirs = context.getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS);
            final File externalFilesDir = (dirs != null && dirs.length > 0) ? dirs[0] : null;
            if (externalFilesDir != null) {
                return externalFilesDir.getPath() + "/appstore/info/";
            }
        } else {
            File externalFilesDir = Environment.getExternalStorageDirectory();
            Log.e("yiuhet", "externalFilesDir:" + externalFilesDir.getPath());
            if (externalFilesDir != null) {
                return externalFilesDir.getPath() + "/eid/log/";
            }
        }
        return null;
    }

    /**
     * 单元测试中使用
     *
     */
    public static void setContext(Context ctx) {
        if (ctx == null)
            return;
        context = ctx.getApplicationContext();
        try {
            LOG_FILE_PRINT_DIR = getLogFilePrintDir(ctx.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 文件名格式
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");
    //创建一个可重用固定线程数的线程池
    private static java.util.concurrent.ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

    public static final void v(String TAG, String msg) {
        printLog("VERBOSE", TAG, msg);
    }

    public static final void d(String TAG, String msg) {
        printLog("DEBUG", TAG, msg);
    }

    public static final void i(String TAG, String msg) {
        printLog("INFO ", TAG, msg);
    }

    public static final void w(String TAG, String msg) {
        printLog("WARN ", TAG, msg);
    }

    public static final void e(String TAG, String msg) {
        printLog("ERROR", TAG, msg);
    }

    private static void printLog(final String level, final String TAG, final String msg) {
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileWriter fileWriter = new FileWriter(getFile(), true);
                    fileWriter.write(formatLog(level, TAG, msg));
                    fileWriter.close();
                } catch (
                        Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //格式化日志
    private static String formatLog(String level, String TAG, String msg) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        builder.append(dateFormat.format(new Date(System.currentTimeMillis())) + " ");
        builder.append("[" + level + "] ");
        builder.append("[" + TAG + "] ");
        builder.append(msg);
        builder.append("\n");
        return builder.toString();
    }

    // 获取需要输出的日志文件
    private static File getFile() {
        if (TextUtils.isEmpty(LOG_FILE_PRINT_DIR)) {
            // try again get directory
            LOG_FILE_PRINT_DIR = getLogFilePrintDir(context.getApplicationContext());
            if (TextUtils.isEmpty(LOG_FILE_PRINT_DIR)) {
                throw new IllegalStateException("can not get external files dirs");
            }
        }
        // 确认文件夹是否存在
        File fileDir = new File(LOG_FILE_PRINT_DIR);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        // 获取文件夹下的日志文件
        File[] fileList = fileDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".log");
            }
        });
        int fileCount = fileList == null ? 0 : fileList.length;

        // 没有日志文件时，直接创建新文件
        if (fileCount == 0) {
            return createLogFile();
        }
        // 只有一个日志文件时
        if (fileCount == 1) {
            return isCreateLogFile(fileList[0]);
        }
        // 对日志排序，排序结果为升序
        Arrays.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                String file1Name = "";
                String file2Name = "";
                try {
                    file1Name = file1.getName().split(".log")[0];
                    file2Name = file2.getName().split(".log")[0];

                    Date dateFile1 = FILE_NAME_FORMAT.parse(file1Name);
                    Date dateFile2 = FILE_NAME_FORMAT.parse(file2Name);
                    return dateFile1.getTime() < dateFile2.getTime() ? -1 : 1;
                } catch (
                        Exception e) {
                    Log.i("LogUtil", "file1Name:" + file1Name + ",   file2Name:" + file2Name);
                    e.printStackTrace();

                }
                return 0;
            }
        });
        File lastFile = fileList[fileCount - 1];
        // 日志文件未超过最大控制个数
        if (fileCount < LOG_FILE_TOTAL) {
            return isCreateLogFile(lastFile);
        }
        if (sizeOf(lastFile) >= LOG_SIZE_MAX) {
            // 删除时间最早的一个文件
            fileList[0].delete();
        }
        return isCreateLogFile(lastFile);
    }

    // 确认是否需要创建新日志文件
    private static File isCreateLogFile(File file) {
        // 超过日志文件大小上限，需要创建新日志文件
        if (sizeOf(file) >= LOG_SIZE_MAX) {
            return createLogFile();
        }
        return file;
    }

    // 创建一个新的日志文件
    private static File createLogFile() {
        return new File(LOG_FILE_PRINT_DIR + FILE_NAME_FORMAT.format(new Date(System.currentTimeMillis())) + ".log");
    }

    // 计算文件大小，返回单位 KB
    private static long sizeOf(File file) {
        long length = file.length();
        return length / 1024;
    }

}

