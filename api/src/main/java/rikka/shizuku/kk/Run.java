package rikka.shizuku.kk;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Provides functionality to run privileged shell commands via Shizuku.
 */
public class Run {

    private static final String TAG = "ShizukuRun";

    private static final int TRANSACTION_NEW_PROCESS = 3;
    private static final int TRANSACTION_WAIT_FOR = 5;

    /**
     * Runs a shell command using Shizuku's privileged process and waits for it to complete.
     *
     * @param binder The Shizuku service binder
     * @param cmd    The command and its arguments
     * @return A {@link Result} containing the exit code
     * @throws RemoteException If the remote call fails
     */
    @NonNull
    public static Result run(@NonNull IBinder binder, @NonNull String[] cmd) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("moe.shizuku.server.IShizukuService");
            data.writeStringArray(cmd);
            data.writeStringArray(null);
            data.writeString(null);
            binder.transact(TRANSACTION_NEW_PROCESS, data, reply, 0);
            reply.readException();
            IBinder processBinder = reply.readStrongBinder();
            return waitForResult(processBinder);
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    private static Result waitForResult(@Nullable IBinder processBinder) throws RemoteException {
        if (processBinder == null) {
            return new Result(-1);
        }

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("moe.shizuku.server.IRemoteProcess");
            processBinder.transact(TRANSACTION_WAIT_FOR, data, reply, 0);
            reply.readException();
            return new Result(reply.readInt());
        } catch (RemoteException e) {
            Log.e(TAG, "waitFor failed", e);
            throw e;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    /**
     * Holds the result of a command run via Shizuku.
     */
    public static final class Result {

        private final int exitCode;

        public Result(int exitCode) {
            this.exitCode = exitCode;
        }

        /** Returns the exit code of the command, or {@code -1} if unavailable. */
        public int getExitCode() {
            return exitCode;
        }

        /** Returns {@code true} if the command exited successfully (exit code 0). */
        public boolean isSuccess() {
            return exitCode == 0;
        }
    }
}
