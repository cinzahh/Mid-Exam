package com.example.midexam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static class Student {
        public String id;
        public String name;
        public boolean enrolled;

        public Student(String id, String name, boolean enrolled) {
            this.id = id;
            this.name = name;
            this.enrolled = enrolled;
        }
    }

    public DBHelper(Context context) {
        super(context, "student.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE students ("
                + "student_id TEXT PRIMARY KEY, "
                + "name TEXT NOT NULL, "
                + "enrolled INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS students");
        onCreate(db);
    }

    public boolean addStudent(Student s) {
        ContentValues values = new ContentValues();
        values.put("student_id", s.id);
        values.put("name", s.name);
        values.put("enrolled", s.enrolled ? 1 : 0);
        return getWritableDatabase().insert("students", null, values) != -1;
    }

    public boolean updateStudent(Student s) {
        ContentValues values = new ContentValues();
        values.put("name", s.name);
        values.put("enrolled", s.enrolled ? 1 : 0);
        return getWritableDatabase().update("students", values,
                "student_id = ?", new String[]{s.id}) > 0;
    }

    public boolean deleteStudent(String id) {
        return getWritableDatabase().delete("students",
                "student_id = ?", new String[]{id}) > 0;
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT student_id, name, enrolled FROM students ORDER BY name", null);
        while (cursor.moveToNext()) {
            list.add(new Student(cursor.getString(0), cursor.getString(1),
                    cursor.getInt(2) == 1));
        }
        cursor.close();
        return list;
    }
}