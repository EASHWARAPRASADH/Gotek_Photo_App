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
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.studentidphotocapture.app.data.model.PhotoStatus;
import com.studentidphotocapture.app.data.model.Student;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Long;
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
public final class StudentDao_Impl implements StudentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Student> __insertionAdapterOfStudent;

  private final EntityDeletionOrUpdateAdapter<Student> __updateAdapterOfStudent;

  public StudentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStudent = new EntityInsertionAdapter<Student>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `student` (`id`,`name`,`rollNumber`,`classGrade`,`section`,`schoolCode`,`photoStatus`,`photoUrl`,`photoCapturedAt`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Student entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getRollNumber());
        statement.bindString(4, entity.getClassGrade());
        statement.bindString(5, entity.getSection());
        statement.bindString(6, entity.getSchoolCode());
        statement.bindString(7, __PhotoStatus_enumToString(entity.getPhotoStatus()));
        if (entity.getPhotoUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getPhotoUrl());
        }
        if (entity.getPhotoCapturedAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getPhotoCapturedAt());
        }
      }
    };
    this.__updateAdapterOfStudent = new EntityDeletionOrUpdateAdapter<Student>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `student` SET `id` = ?,`name` = ?,`rollNumber` = ?,`classGrade` = ?,`section` = ?,`schoolCode` = ?,`photoStatus` = ?,`photoUrl` = ?,`photoCapturedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Student entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getRollNumber());
        statement.bindString(4, entity.getClassGrade());
        statement.bindString(5, entity.getSection());
        statement.bindString(6, entity.getSchoolCode());
        statement.bindString(7, __PhotoStatus_enumToString(entity.getPhotoStatus()));
        if (entity.getPhotoUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getPhotoUrl());
        }
        if (entity.getPhotoCapturedAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getPhotoCapturedAt());
        }
        statement.bindString(10, entity.getId());
      }
    };
  }

  @Override
  public Object insertStudent(final Student student, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStudent.insert(student);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertStudents(final List<Student> students,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStudent.insert(students);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStudent(final Student student, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfStudent.handle(student);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Student>> getStudentsByClassSection(final String schoolCode,
      final String classGrade, final String section) {
    final String _sql = "SELECT * FROM student WHERE schoolCode = ? AND classGrade = ? AND section = ? ORDER BY rollNumber ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, schoolCode);
    _argIndex = 2;
    _statement.bindString(_argIndex, classGrade);
    _argIndex = 3;
    _statement.bindString(_argIndex, section);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"student"}, new Callable<List<Student>>() {
      @Override
      @NonNull
      public List<Student> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfClassGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "classGrade");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfSchoolCode = CursorUtil.getColumnIndexOrThrow(_cursor, "schoolCode");
          final int _cursorIndexOfPhotoStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "photoStatus");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfPhotoCapturedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "photoCapturedAt");
          final List<Student> _result = new ArrayList<Student>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Student _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpClassGrade;
            _tmpClassGrade = _cursor.getString(_cursorIndexOfClassGrade);
            final String _tmpSection;
            _tmpSection = _cursor.getString(_cursorIndexOfSection);
            final String _tmpSchoolCode;
            _tmpSchoolCode = _cursor.getString(_cursorIndexOfSchoolCode);
            final PhotoStatus _tmpPhotoStatus;
            _tmpPhotoStatus = __PhotoStatus_stringToEnum(_cursor.getString(_cursorIndexOfPhotoStatus));
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final Long _tmpPhotoCapturedAt;
            if (_cursor.isNull(_cursorIndexOfPhotoCapturedAt)) {
              _tmpPhotoCapturedAt = null;
            } else {
              _tmpPhotoCapturedAt = _cursor.getLong(_cursorIndexOfPhotoCapturedAt);
            }
            _item = new Student(_tmpId,_tmpName,_tmpRollNumber,_tmpClassGrade,_tmpSection,_tmpSchoolCode,_tmpPhotoStatus,_tmpPhotoUrl,_tmpPhotoCapturedAt);
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
  public Object getStudentById(final String id, final Continuation<? super Student> $completion) {
    final String _sql = "SELECT * FROM student WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Student>() {
      @Override
      @Nullable
      public Student call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfClassGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "classGrade");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfSchoolCode = CursorUtil.getColumnIndexOrThrow(_cursor, "schoolCode");
          final int _cursorIndexOfPhotoStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "photoStatus");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfPhotoCapturedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "photoCapturedAt");
          final Student _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpClassGrade;
            _tmpClassGrade = _cursor.getString(_cursorIndexOfClassGrade);
            final String _tmpSection;
            _tmpSection = _cursor.getString(_cursorIndexOfSection);
            final String _tmpSchoolCode;
            _tmpSchoolCode = _cursor.getString(_cursorIndexOfSchoolCode);
            final PhotoStatus _tmpPhotoStatus;
            _tmpPhotoStatus = __PhotoStatus_stringToEnum(_cursor.getString(_cursorIndexOfPhotoStatus));
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final Long _tmpPhotoCapturedAt;
            if (_cursor.isNull(_cursorIndexOfPhotoCapturedAt)) {
              _tmpPhotoCapturedAt = null;
            } else {
              _tmpPhotoCapturedAt = _cursor.getLong(_cursorIndexOfPhotoCapturedAt);
            }
            _result = new Student(_tmpId,_tmpName,_tmpRollNumber,_tmpClassGrade,_tmpSection,_tmpSchoolCode,_tmpPhotoStatus,_tmpPhotoUrl,_tmpPhotoCapturedAt);
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
  public Flow<List<Student>> getStudentsBySchool(final String schoolCode) {
    final String _sql = "SELECT * FROM student WHERE schoolCode = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, schoolCode);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"student"}, new Callable<List<Student>>() {
      @Override
      @NonNull
      public List<Student> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfClassGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "classGrade");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfSchoolCode = CursorUtil.getColumnIndexOrThrow(_cursor, "schoolCode");
          final int _cursorIndexOfPhotoStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "photoStatus");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfPhotoCapturedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "photoCapturedAt");
          final List<Student> _result = new ArrayList<Student>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Student _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpClassGrade;
            _tmpClassGrade = _cursor.getString(_cursorIndexOfClassGrade);
            final String _tmpSection;
            _tmpSection = _cursor.getString(_cursorIndexOfSection);
            final String _tmpSchoolCode;
            _tmpSchoolCode = _cursor.getString(_cursorIndexOfSchoolCode);
            final PhotoStatus _tmpPhotoStatus;
            _tmpPhotoStatus = __PhotoStatus_stringToEnum(_cursor.getString(_cursorIndexOfPhotoStatus));
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final Long _tmpPhotoCapturedAt;
            if (_cursor.isNull(_cursorIndexOfPhotoCapturedAt)) {
              _tmpPhotoCapturedAt = null;
            } else {
              _tmpPhotoCapturedAt = _cursor.getLong(_cursorIndexOfPhotoCapturedAt);
            }
            _item = new Student(_tmpId,_tmpName,_tmpRollNumber,_tmpClassGrade,_tmpSection,_tmpSchoolCode,_tmpPhotoStatus,_tmpPhotoUrl,_tmpPhotoCapturedAt);
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
  public Flow<List<Student>> getPendingStudents(final String schoolCode, final String classGrade,
      final String section) {
    final String _sql = "SELECT * FROM student WHERE schoolCode = ? AND classGrade = ? AND section = ? AND photoStatus = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, schoolCode);
    _argIndex = 2;
    _statement.bindString(_argIndex, classGrade);
    _argIndex = 3;
    _statement.bindString(_argIndex, section);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"student"}, new Callable<List<Student>>() {
      @Override
      @NonNull
      public List<Student> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRollNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "rollNumber");
          final int _cursorIndexOfClassGrade = CursorUtil.getColumnIndexOrThrow(_cursor, "classGrade");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfSchoolCode = CursorUtil.getColumnIndexOrThrow(_cursor, "schoolCode");
          final int _cursorIndexOfPhotoStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "photoStatus");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfPhotoCapturedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "photoCapturedAt");
          final List<Student> _result = new ArrayList<Student>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Student _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRollNumber;
            _tmpRollNumber = _cursor.getString(_cursorIndexOfRollNumber);
            final String _tmpClassGrade;
            _tmpClassGrade = _cursor.getString(_cursorIndexOfClassGrade);
            final String _tmpSection;
            _tmpSection = _cursor.getString(_cursorIndexOfSection);
            final String _tmpSchoolCode;
            _tmpSchoolCode = _cursor.getString(_cursorIndexOfSchoolCode);
            final PhotoStatus _tmpPhotoStatus;
            _tmpPhotoStatus = __PhotoStatus_stringToEnum(_cursor.getString(_cursorIndexOfPhotoStatus));
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final Long _tmpPhotoCapturedAt;
            if (_cursor.isNull(_cursorIndexOfPhotoCapturedAt)) {
              _tmpPhotoCapturedAt = null;
            } else {
              _tmpPhotoCapturedAt = _cursor.getLong(_cursorIndexOfPhotoCapturedAt);
            }
            _item = new Student(_tmpId,_tmpName,_tmpRollNumber,_tmpClassGrade,_tmpSection,_tmpSchoolCode,_tmpPhotoStatus,_tmpPhotoUrl,_tmpPhotoCapturedAt);
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
  public Object getTotalStudentsCount(final String schoolCode, final String classGrade,
      final String section, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM student WHERE schoolCode = ? AND classGrade = ? AND section = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, schoolCode);
    _argIndex = 2;
    _statement.bindString(_argIndex, classGrade);
    _argIndex = 3;
    _statement.bindString(_argIndex, section);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object getCompletedStudentsCount(final String schoolCode, final String classGrade,
      final String section, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM student WHERE schoolCode = ? AND classGrade = ? AND section = ? AND photoStatus != 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, schoolCode);
    _argIndex = 2;
    _statement.bindString(_argIndex, classGrade);
    _argIndex = 3;
    _statement.bindString(_argIndex, section);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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

  private String __PhotoStatus_enumToString(@NonNull final PhotoStatus _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case CAPTURED: return "CAPTURED";
      case UPLOADED: return "UPLOADED";
      case FAILED: return "FAILED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private PhotoStatus __PhotoStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PENDING": return PhotoStatus.PENDING;
      case "CAPTURED": return PhotoStatus.CAPTURED;
      case "UPLOADED": return PhotoStatus.UPLOADED;
      case "FAILED": return PhotoStatus.FAILED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
