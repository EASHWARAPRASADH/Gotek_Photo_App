package com.studentidphotocapture.app.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.studentidphotocapture.app.data.model.PhotoMetadata;
import com.studentidphotocapture.app.data.model.UploadStatus;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PhotoMetadataDao_Impl implements PhotoMetadataDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PhotoMetadata> __insertionAdapterOfPhotoMetadata;

  private final EntityDeletionOrUpdateAdapter<PhotoMetadata> __updateAdapterOfPhotoMetadata;

  private final SharedSQLiteStatement __preparedStmtOfDeletePhotoMetadata;

  public PhotoMetadataDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPhotoMetadata = new EntityInsertionAdapter<PhotoMetadata>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `photometadata` (`id`,`fileName`,`studentId`,`studentName`,`classGrade`,`section`,`rollNumber`,`schoolCode`,`timestamp`,`localPath`,`uploadStatus`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PhotoMetadata entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getFileName());
        statement.bindString(3, entity.getStudentId());
        statement.bindString(4, entity.getStudentName());
        statement.bindString(5, entity.getClassGrade());
        statement.bindString(6, entity.getSection());
        statement.bindString(7, entity.getRollNumber());
        statement.bindString(8, entity.getSchoolCode());
        statement.bindLong(9, entity.getTimestamp());
        statement.bindString(10, entity.getLocalPath());
        statement.bindString(11, __UploadStatus_enumToString(entity.getUploadStatus()));
      }
    };
    this.__updateAdapterOfPhotoMetadata = new EntityDeletionOrUpdateAdapter<PhotoMetadata>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `photometadata` SET `id` = ?,`fileName` = ?,`studentId` = ?,`studentName` = ?,`classGrade` = ?,`section` = ?,`rollNumber` = ?,`schoolCode` = ?,`timestamp` = ?,`localPath` = ?,`uploadStatus` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PhotoMetadata entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getFileName());
        statement.bindString(3, entity.getStudentId());
        statement.bindString(4, entity.getStudentName());
        statement.bindString(5, entity.getClassGrade());
        statement.bindString(6, entity.getSection());
        statement.bindString(7, entity.getRollNumber());
        statement.bindString(8, entity.getSchoolCode());
        statement.bindLong(9, entity.getTimestamp());
        statement.bindString(10, entity.getLocalPath());
        statement.bindString(11, __UploadStatus_enumToString(entity.getUploadStatus()));
        statement.bindString(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeletePhotoMetadata = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM photometadata WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertPhotoMetadata(final PhotoMetadata metadata,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPhotoMetadata.insert(metadata);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePhotoMetadata(final PhotoMetadata metadata,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPhotoMetadata.handle(metadata);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePhotoMetadata(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePhotoMetadata.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePhotoMetadata.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PhotoMetadata>> getPendingUploads() {
    final String _sql = "SELECT * FROM photometadata WHERE uploadStatus = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"photometadata"}, new Callable<List<PhotoMetadata>>() {
      @Override
      @NonNull
      public List<PhotoMetadata> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfStudentName = CursorUtil.getColumnIndexOrThrow(_cursor, "studentName");
          final int _cursorIndexOfClassGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "classGrade");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfSchoolCode = CursorUtil.getColumnIndexOrThrow(_cursor, "schoolCode");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfUploadStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadStatus");
          final List<PhotoMetadata> _result = new ArrayList<PhotoMetadata>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PhotoMetadata _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpStudentId;
            _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            final String _tmpStudentName;
            _tmpStudentName = _cursor.getString(_cursorIndexOfStudentName);
            final String _tmpClassGrade;
            _tmpClassGrade = _cursor.getString(_cursorIndexOfClassGrade);
            final String _tmpSection;
            _tmpSection = _cursor.getString(_cursorIndexOfSection);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpSchoolCode;
            _tmpSchoolCode = _cursor.getString(_cursorIndexOfSchoolCode);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpLocalPath;
            _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            final UploadStatus _tmpUploadStatus;
            _tmpUploadStatus = __UploadStatus_stringToEnum(_cursor.getString(_cursorIndexOfUploadStatus));
            _item = new PhotoMetadata(_tmpId,_tmpFileName,_tmpStudentId,_tmpStudentName,_tmpClassGrade,_tmpSection,_tmpRollNumber,_tmpSchoolCode,_tmpTimestamp,_tmpLocalPath,_tmpUploadStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPendingUploadsSync(final Continuation<? super List<PhotoMetadata>> $completion) {
    final String _sql = "SELECT * FROM photometadata WHERE uploadStatus = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PhotoMetadata>>() {
      @Override
      @NonNull
      public List<PhotoMetadata> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfStudentName = CursorUtil.getColumnIndexOrThrow(_cursor, "studentName");
          final int _cursorIndexOfClassGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "classGrade");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfSchoolCode = CursorUtil.getColumnIndexOrThrow(_cursor, "schoolCode");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfUploadStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadStatus");
          final List<PhotoMetadata> _result = new ArrayList<PhotoMetadata>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PhotoMetadata _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpStudentId;
            _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            final String _tmpStudentName;
            _tmpStudentName = _cursor.getString(_cursorIndexOfStudentName);
            final String _tmpClassGrade;
            _tmpClassGrade = _cursor.getString(_cursorIndexOfClassGrade);
            final String _tmpSection;
            _tmpSection = _cursor.getString(_cursorIndexOfSection);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpSchoolCode;
            _tmpSchoolCode = _cursor.getString(_cursorIndexOfSchoolCode);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpLocalPath;
            _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            final UploadStatus _tmpUploadStatus;
            _tmpUploadStatus = __UploadStatus_stringToEnum(_cursor.getString(_cursorIndexOfUploadStatus));
            _item = new PhotoMetadata(_tmpId,_tmpFileName,_tmpStudentId,_tmpStudentName,_tmpClassGrade,_tmpSection,_tmpRollNumber,_tmpSchoolCode,_tmpTimestamp,_tmpLocalPath,_tmpUploadStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPhotoByStudentId(final String studentId,
      final Continuation<? super PhotoMetadata> $completion) {
    final String _sql = "SELECT * FROM photometadata WHERE studentId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, studentId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PhotoMetadata>() {
      @Override
      @Nullable
      public PhotoMetadata call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfStudentName = CursorUtil.getColumnIndexOrThrow(_cursor, "studentName");
          final int _cursorIndexOfClassGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "classGrade");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfSchoolCode = CursorUtil.getColumnIndexOrThrow(_cursor, "schoolCode");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfUploadStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadStatus");
          final PhotoMetadata _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpStudentId;
            _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            final String _tmpStudentName;
            _tmpStudentName = _cursor.getString(_cursorIndexOfStudentName);
            final String _tmpClassGrade;
            _tmpClassGrade = _cursor.getString(_cursorIndexOfClassGrade);
            final String _tmpSection;
            _tmpSection = _cursor.getString(_cursorIndexOfSection);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpSchoolCode;
            _tmpSchoolCode = _cursor.getString(_cursorIndexOfSchoolCode);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpLocalPath;
            _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            final UploadStatus _tmpUploadStatus;
            _tmpUploadStatus = __UploadStatus_stringToEnum(_cursor.getString(_cursorIndexOfUploadStatus));
            _result = new PhotoMetadata(_tmpId,_tmpFileName,_tmpStudentId,_tmpStudentName,_tmpClassGrade,_tmpSection,_tmpRollNumber,_tmpSchoolCode,_tmpTimestamp,_tmpLocalPath,_tmpUploadStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllPhotos(final Continuation<? super List<PhotoMetadata>> $completion) {
    final String _sql = "SELECT * FROM photometadata ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PhotoMetadata>>() {
      @Override
      @NonNull
      public List<PhotoMetadata> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfStudentId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentId");
          final int _cursorIndexOfStudentName = CursorUtil.getColumnIndexOrThrow(_cursor, "studentName");
          final int _cursorIndexOfClassGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "classGrade");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfSchoolCode = CursorUtil.getColumnIndexOrThrow(_cursor, "schoolCode");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfUploadStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadStatus");
          final List<PhotoMetadata> _result = new ArrayList<PhotoMetadata>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PhotoMetadata _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpStudentId;
            _tmpStudentId = _cursor.getString(_cursorIndexOfStudentId);
            final String _tmpStudentName;
            _tmpStudentName = _cursor.getString(_cursorIndexOfStudentName);
            final String _tmpClassGrade;
            _tmpClassGrade = _cursor.getString(_cursorIndexOfClassGrade);
            final String _tmpSection;
            _tmpSection = _cursor.getString(_cursorIndexOfSection);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpSchoolCode;
            _tmpSchoolCode = _cursor.getString(_cursorIndexOfSchoolCode);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpLocalPath;
            _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            final UploadStatus _tmpUploadStatus;
            _tmpUploadStatus = __UploadStatus_stringToEnum(_cursor.getString(_cursorIndexOfUploadStatus));
            _item = new PhotoMetadata(_tmpId,_tmpFileName,_tmpStudentId,_tmpStudentName,_tmpClassGrade,_tmpSection,_tmpRollNumber,_tmpSchoolCode,_tmpTimestamp,_tmpLocalPath,_tmpUploadStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __UploadStatus_enumToString(@NonNull final UploadStatus _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case UPLOADING: return "UPLOADING";
      case UPLOADED: return "UPLOADED";
      case FAILED: return "FAILED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private UploadStatus __UploadStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PENDING": return UploadStatus.PENDING;
      case "UPLOADING": return UploadStatus.UPLOADING;
      case "UPLOADED": return UploadStatus.UPLOADED;
      case "FAILED": return UploadStatus.FAILED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
