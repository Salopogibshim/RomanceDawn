package com.romancedawn;

import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.romancedawn.databinding.ActivityMainBinding;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Spinner;

import com.romancedawn.module.Factory;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private SQLiteDatabase db;
    private Cursor sqlCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //инициализация главного окна
        Factory.setMainActivity(MainActivity.this);

        //инициализация бд
        Factory.initDatabase(Factory.getFileHelper().copyFile("databases/", "RomanceDawn.db"));
        db = Factory.getDatabase();

        //обновление файлов в случае устаревшей версии
        Factory.getUpdateGuide();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //привязка к остальным фрагментам через меню внизу экрана
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_help, R.id.navigation_notifications,
                R.id.navigation_profile, R.id.navigation_stars).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //фиксация вертикальности экрана

        //проверка регистрации пользователя
        sqlCursor = db.rawQuery("SELECT profile_name FROM profile", null);

        if (sqlCursor.getCount() == 0) {
            // создание всплывающего окна-регистрации
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.fragment_registration, null);

            builder.setView(dialogView);

            // создание полей для ввода данных
            EditText editText = dialogView.findViewById(R.id.editTextText);
            Button button = dialogView.findViewById(R.id.button);
            Spinner spinner_health = dialogView.findViewById(R.id.spinner_health);

            AlertDialog dialog = builder.create();

            button.setOnClickListener( v -> {
                String enteredValue = editText.getText().toString();
                // Проверка на заполненность поля
                if (!TextUtils.isEmpty(enteredValue)) {
                    // Действия, которые нужно выполнить, если поле заполнено
                    String text_name = editText.getText().toString();
                    String text_health  = spinner_health.getSelectedItem().toString();

                    //сохранения данных в бд
                    String sql="INSERT INTO profile(profile_name, health) " + "VALUES (?, ?)";
                    db.execSQL(sql, new String[]{text_name, text_health});

                    dialog.dismiss(); // Закрыть диалоговое окно
                }
                else {
                    editText.setError("Поле не может быть пустым");
                }
            });
            dialog.setCanceledOnTouchOutside(false); // Запретить закрытие диалогового окна при нажатии вне его
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5B5265")));
            dialog.show();
        }
        sqlCursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}