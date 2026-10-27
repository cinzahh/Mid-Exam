package com.example.midexam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DBHelper db;
    private EditText etName;
    private EditText etStudentId;
    private CheckBox cbEnrolled;
    private ListView listStudents;
    private List<DBHelper.Student> students = new ArrayList<>();
    private String selectedId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHelper(this);
        etName = findViewById(R.id.etName);
        etStudentId = findViewById(R.id.etStudentId);
        cbEnrolled = findViewById(R.id.cbEnrolled);
        listStudents = findViewById(R.id.listStudents);

        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String id = etStudentId.getText().toString().trim();
            if (name.isEmpty() || id.isEmpty()) {
                toast("Name and Student ID must be filled");
                return;
            }
            boolean enrolled = cbEnrolled.isChecked();
            if (db.addStudent(new DBHelper.Student(id, name, enrolled))) {
                toast("Data added");
                clearForm();
                loadData();
            } else {
                toast("Failed, Student ID already exists");
            }
        });

        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            if (selectedId == null) {
                toast("Click a data in the list first, then edit it");
                return;
            }
            String name = etName.getText().toString().trim();
            String id = etStudentId.getText().toString().trim();
            if (name.isEmpty() || id.isEmpty()) {
                toast("Name and Student ID must be filled");
                return;
            }
            boolean enrolled = cbEnrolled.isChecked();
            if (db.updateStudent(selectedId, new DBHelper.Student(id, name, enrolled))) {
                toast("Data updated");
                clearForm();
                loadData();
            } else {
                toast("Failed, Student ID is already used by another data");
            }
        });

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            String id = selectedId != null ? selectedId
                    : etStudentId.getText().toString().trim();
            if (id.isEmpty()) {
                toast("Click a data in the list or fill Student ID to delete");
                return;
            }
            if (db.deleteStudent(id)) {
                toast("Data deleted");
                clearForm();
                loadData();
            } else {
                toast("Student ID not found");
            }
        });

        listStudents.setOnItemClickListener((parent, view, position, id) -> {
            DBHelper.Student s = students.get(position);
            selectedId = s.id;
            etName.setText(s.name);
            etStudentId.setText(s.id);
            cbEnrolled.setChecked(s.enrolled);
        });

        loadData();
    }

    private void loadData() {
        students = db.getAllStudents();
        List<String> rows = new ArrayList<>();
        for (DBHelper.Student s : students) {
            rows.add(s.id + " - " + s.name + " ("
                    + (s.enrolled ? "Enrolled" : "Not enrolled") + ")");
        }
        listStudents.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, rows));
    }

    private void clearForm() {
        etName.getText().clear();
        etStudentId.getText().clear();
        cbEnrolled.setChecked(false);
        selectedId = null;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public static class DBHelper extends SQLiteOpenHelper {

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

        public boolean updateStudent(String oldId, Student s) {
            ContentValues values = new ContentValues();
            values.put("student_id", s.id);
            values.put("name", s.name);
            values.put("enrolled", s.enrolled ? 1 : 0);
            try {
                return getWritableDatabase().update("students", values,
                        "student_id = ?", new String[]{oldId}) > 0;
            } catch (android.database.sqlite.SQLiteException e) {
                return false;
            }
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
}