package com.bestpractices.base.util;

import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.bestpractices.base.Assert;
import com.bestpractices.base.ContextManager;
import com.bestpractices.base.collection.IntArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public final class FileHelper {
    // Sync IO

    public static final int TYPE_FILE_SYSTEM = 1;
    public static final int TYPE_ASSET = 2;

    public static final String ASSETS = "assets";

    public static InputStream open(String path) {
        return open(TYPE_FILE_SYSTEM, path);
    }

    /**
     * @param type {@link #TYPE_FILE_SYSTEM} or {@link #TYPE_ASSET}
     */
    public static InputStream open(int type, String path) {
        try {
            switch (type) {
                case TYPE_FILE_SYSTEM:
                    return new FileInputStream(path);

                case TYPE_ASSET:
                    return ContextManager.assetManager().open(path);

                default:
                    break;
            }
        } catch (Throwable e) {
        }
        return null;
    }

    public static FileOutputStream write(String path) {
        return write(path, false);
    }

    public static FileOutputStream write(String path, boolean lockFile) {
        // 1) ensure dir
        String[] parts = splitFileName(path);
        if (!TextUtils.isEmpty(parts[0])) {
            if (!ensureDir(parts[0])) {
                return null;
            }
        }

        // 2) open stream;
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(path);
            if (lockFile) {
                while (true) {
                    try {
                        if (stream.getChannel().tryLock() != null) {
                            break;
                        }
                    } catch (Exception ex) {
                    }
                    Thread.sleep(100);
                }
            }
        } catch (Throwable e) {
            return null;
        }

        return stream;
    }

    public static boolean delete(String path) {
        return new File(path).delete();
    }

    public static boolean exists(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * If dir is not existed, create it.
     */
    public static boolean ensureDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    public static XmlResourceParser openResourceXml(String path) throws IOException {
        AssetManager assetMgr = ContextManager.assetManager();

        return assetMgr.openXmlResourceParser(path);
    }

    // Async IO

    public static interface FileAsyncOperationCallback<Input, Result> {
        Result run(Input stream, Object... params);

        void onResult(Result result);
    }

    /**
     * Open file async and pass the {@link #InputStream} to your callback.<br/>
     * ATTENTION: the passed {@link #InputStream} may be null if any exceptions.
     */
    public static <Result> void openAsync(int type, String path, FileAsyncOperationCallback<InputStream, Result> callback, Object... params) {
        new FileOpenAsyncTask<Result>(callback).append(type, path).execute(params);
    }

    /**
     * Write file async and pass the {@link #OutputStream} to your callback.<br/>
     * NOTE: the passed {@link #OutputStream} won't be null.
     */
    public static <Result> void writeAsync(String path, boolean lockFile, FileAsyncOperationCallback<OutputStream, Result> callback, Object... params) {
        new FileWriteAsyncTask<Result>(path, lockFile, callback).execute(params);
    }

    public static void deleteAsync(String path, Runnable callback) {
        new FileDeleteAsyncTask(path, callback).execute();
    }

    /**
     * An async task to open one file and you can call
     * {@link #append(int, String)} to assign multiple filepath candidates. This
     * task will find the first available filepath to open.
     */
    public static class FileOpenAsyncTask<Result> extends AsyncTask<Object, Object, Result> {
        private final ArrayList<String> mFilePathCandidates = new ArrayList<String>();
        private final IntArrayList mFileTypeCandidates = new IntArrayList();
        private final FileAsyncOperationCallback<InputStream, Result> mCallback;

        public FileOpenAsyncTask(FileAsyncOperationCallback<InputStream, Result> callback) {
            Assert.r(callback != null);

            mCallback = callback;
        }

        public FileOpenAsyncTask<Result> append(int fileType, String filePath) {
            mFileTypeCandidates.add(fileType);
            mFilePathCandidates.add(filePath);
            return this;
        }

        @Override
        protected Result doInBackground(Object... params) {
            // 1) open file
            InputStream stream = null;
            for (int i = 0, size = mFileTypeCandidates.size(); i < size && stream == null; ++i) {
                stream = open(mFileTypeCandidates.get(i), mFilePathCandidates.get(i));
            }

            // 2) process
            Result result = mCallback.run(stream, params);

            // 3) close and return
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            mCallback.onResult(result);
        }
    }

    private static class FileWriteAsyncTask<Result> extends AsyncTask<Object, Object, Result> {
        private final String mFilePath;
        private final boolean mLockFile;
        private final FileAsyncOperationCallback<OutputStream, Result> mCallback;

        public FileWriteAsyncTask(String filePath, boolean lockFile, FileAsyncOperationCallback<OutputStream, Result> callback) {
            mFilePath = filePath;
            mLockFile = lockFile;
            mCallback = callback;
        }

        @Override
        protected Result doInBackground(Object... params) {
            // 1) open file
            OutputStream stream = FileHelper.write(mFilePath, mLockFile);
            if (stream == null) {
                return null;
            }

            // 2) process
            Result result = mCallback.run(stream, params);

            // 3) close and return
            try {
                stream.close();
            } catch (IOException e) {
            }

            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            mCallback.onResult(result);
        }
    }

    private static class FileDeleteAsyncTask extends AsyncTask<Object, Object, Object> {
        private final String mFilePath;
        private final Runnable mCallback;

        public FileDeleteAsyncTask(String filePath, Runnable callback) {
            mFilePath = filePath;
            mCallback = callback;
        }

        @Override
        protected Object doInBackground(Object... params) {
            new File(mFilePath).delete();
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (mCallback != null) {
                mCallback.run();
            }
        }
    }

    // Path API

    public static final String INTERNAL_PATH = ContextManager.appContext().getFilesDir().getPath();
    public static final String EXTERNAL_PATH = Environment.getExternalStorageDirectory().getPath();

    /**
     * Build a dir path
     */
    public static String dirPath(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(part);
            sb.append(File.separator);
        }
        return sb.toString();
    }

    /**
     * Build a file path
     */
    public static String filePath(String... parts) {
        if (parts.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int end = parts.length - 1;
        for (int i = 0; i < end; ++i) {
            sb.append(parts[i]);
            sb.append(File.separator);
        }
        sb.append(parts[end]);
        return sb.toString();
    }

    /**
     * Split to get the dir/file parts.
     *
     * @return [dir, filename]
     */
    public static String[] splitFileName(String filePath) {
        if (filePath.endsWith(File.separator)) {
            return new String[]{filePath, ""};
        }

        int index = filePath.lastIndexOf(File.separator);
        if (index < 0) {
            return new String[]{"", filePath};
        }
        return new String[]{filePath.substring(0, index), filePath.substring(index + 1)};
    }

    /**
     * Split file path string to parts.
     */
    public static String[] splitPath(String path) {
        return TextHelper.split(path, File.separator, false, true);
    }
}