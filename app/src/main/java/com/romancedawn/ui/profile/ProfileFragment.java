package com.romancedawn.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.romancedawn.R;
import com.romancedawn.databinding.FragmentProfileBinding;
import com.romancedawn.module.Factory;
import com.romancedawn.module.FileHelper;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private SQLiteDatabase db;
    private Cursor sqlCursor_name, sqlCursor_comp, sqlCursor_uncomp;
    private String profileName, profileHealth;
    private FileHelper fileHelper;
    private String fullPathFile;
    private Boolean click = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //подгрузка бд
        db = Factory.getDatabase();
        //вывод данных профиля на экран
        sqlCursor_name = db.rawQuery("SELECT profile_name, health FROM profile", null);
        sqlCursor_name.moveToFirst();
        while (!sqlCursor_name.isAfterLast()) {
            profileName = sqlCursor_name.getString(0);
            profileHealth = sqlCursor_name.getString(1);
            sqlCursor_name.moveToNext();
        }
        sqlCursor_name.close();

        TextView text_name = binding.profileName;
        text_name.setText("Имя пользователя: " + profileName);

        TextView text_health = binding.profileHealth;
        text_health.setText("Состояние здоровья: " + profileHealth);

        //справочная информация
        WebView webView = binding.htmlTextView;
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Разрешить выполнение JavaScript (если необходимо)
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);

        //поиск полного пути к файлу на устройстве
        fileHelper = Factory.getFileHelper();
        fullPathFile = fileHelper.copyFile("files/", "info.html");

        webView.loadUrl("file://"+fullPathFile);

        //вывод данных о пройденных маршрутах
        LinearLayout panel = binding.panel;
        LinearLayout list = binding.list;

        //пройденные и не пройденные маршруты (заполнение панели-кнопки)
        TextView comp_count = binding.countCompletedRoutes;
        TextView uncomp_count = binding.countUncompletedRoutes;

        sqlCursor_comp = db.rawQuery("SELECT routes_id from comleted_routes", null);

        sqlCursor_uncomp = db.rawQuery("SELECT routes_id FROM routes", null);
        Integer comp = sqlCursor_comp.getCount();
        Integer uncomp = sqlCursor_uncomp.getCount() - sqlCursor_comp.getCount();
        comp_count.setText(comp.toString());
        uncomp_count.setText(uncomp.toString());
        sqlCursor_comp.close();
        sqlCursor_uncomp.close();

        //кликабельность панели с данными о пройденных маршрутах
        panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //свернуть панель
                if (click == false) {
                    click = true;
                    list.removeAllViews();
                //развернуть панель
                } else {
                    sqlCursor_comp = db.rawQuery("SELECT routes_name, location, lenght_day, lenght_km, complexity," +
                            "foto_url, routes_id, map_url, foto_map_url, " +
                            "description FROM routes where routes_id in (select routes_id from comleted_routes)", null);

                    //если список пройденных пуст
                    if (sqlCursor_comp.getCount() == 0) {
                        TextView textView = new TextView(getActivity());
                        textView.setTextSize(18);
                        Typeface bold = Typeface.defaultFromStyle(Typeface.BOLD);
                        textView.setTypeface(bold);
                        textView.setPadding(0, 300, 0, 0);
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextColor(Color.parseColor("#cac1db"));
                        textView.setText("Здесь пока что пусто...");
                        list.addView(textView);
                    }

                    sqlCursor_comp.moveToFirst();
                    while (!sqlCursor_comp.isAfterLast()) {
                        //динамическое отображение маршрутов
                        LinearLayout itemLayout = new LinearLayout(getActivity());
                        itemLayout.setOrientation(LinearLayout.VERTICAL);
                        itemLayout.setBackgroundColor(Color.parseColor("#2DBB86FC"));
                        itemLayout.setPadding(0, 0, 0, 40);
                        ImageView imageView = new ImageView(getActivity());

                        //создание текстов полей
                        TableLayout tableLayout = new TableLayout(getActivity());
                        tableLayout.setOrientation(TableLayout.VERTICAL);
                        tableLayout.setPadding(30, 0, 0, 0);
                        TextView textView_name = new TextView(getActivity());
                        textView_name.setTextSize(20);
                        textView_name.setTextColor(Color.parseColor("#AE130622"));
                        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
                        textView_name.setTypeface(boldTypeface);
                        TextView textView_place = new TextView(getActivity());
                        TextView textView_day = new TextView(getActivity());
                        TextView textView_comp = new TextView(getActivity());

                        //отступы
                        TextView textView_n = new TextView(getActivity());
                        TextView textView_nn = new TextView(getActivity());

                        //инициализация полей данными из бд
                        String routesName = sqlCursor_comp.getString(0);
                        String routesLoc = sqlCursor_comp.getString(1);
                        String routesDay = sqlCursor_comp.getString(2);
                        String routesKm = sqlCursor_comp.getString(3);
                        String routesComp = sqlCursor_comp.getString(4);
                        String routesFoto = sqlCursor_comp.getString(5);
                        String routes_id = sqlCursor_comp.getString(6);
                        String routesMap_url = sqlCursor_comp.getString(7);
                        String routesFoto_map_url = sqlCursor_comp.getString(8);
                        String routesDes = sqlCursor_comp.getString(9);

                        //создание кнопки-надписи для пройденных маршрутов
                        TextView textView_star_button = new TextView(getActivity());
                        textView_star_button.setTextSize(17);
                        textView_star_button.setTextColor(Color.parseColor("#AE130622"));
                        textView_star_button.setText("\uD83E\uDD47 Удалить из пройденных");

                        //присваивание значений полям
                        Picasso.get().load(routesFoto).into(imageView);
                        textView_name.setText(routesName);
                        textView_day.setText(routesDay + ", " + routesKm);
                        textView_place.setText(routesLoc);
                        textView_comp.setText(routesComp);

                        textView_n.setText("");
                        textView_n.setTextSize(7);
                        textView_nn.setText("");
                        textView_nn.setTextSize(7);

                        //отображение полей
                        itemLayout.addView(imageView);
                        tableLayout.addView(textView_name);
                        tableLayout.addView(textView_n);
                        tableLayout.addView(textView_place);
                        tableLayout.addView(textView_day);
                        tableLayout.addView(textView_comp);
                        tableLayout.addView(textView_nn);
                        tableLayout.addView(textView_star_button);
                        itemLayout.addView(tableLayout);
                        list.addView(itemLayout);

                        //добавление отступа между элементами
                        LinearLayout itemLayout1 = new LinearLayout(getActivity());
                        TextView textView_name1 = new TextView(getActivity());
                        textView_name1.setText("");
                        itemLayout1.addView(textView_name1);
                        list.addView(itemLayout1);

                        //кликабельность названия маршрута для открытия полного описания
                        textView_name.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //создание всплывающего окна
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                View dialogView = getLayoutInflater().inflate(R.layout.fragment_discription, null);
                                builder.setView(dialogView);
                                dialogView.setPadding(20, 0, 20, 40);

                                //поиск элементов фрагмента
                                TextView name = dialogView.findViewById(R.id.textView_name);
                                Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
                                name.setTypeface(boldTypeface);
                                Button button = dialogView.findViewById(R.id.button);
                                TextView day = dialogView.findViewById(R.id.textView_day);
                                TextView zap = dialogView.findViewById(R.id.textView_zap);
                                TextView km = dialogView.findViewById(R.id.textView_km);
                                TextView com = dialogView.findViewById(R.id.textView_com);
                                TextView loc = dialogView.findViewById(R.id.textView_loc);
                                TextView dis = dialogView.findViewById(R.id.textView_dis);
                                TextView url_map = dialogView.findViewById(R.id.textView_url_map);
                                ImageView foto = dialogView.findViewById(R.id.imageView_foto);
                                ImageView foto_map = dialogView.findViewById(R.id.imageView_map);

                                //инициализация элементов модели данными из бд
                                name.setText(routesName);
                                day.setText(routesDay);
                                km.setText(routesKm);
                                com.setText(routesComp);
                                loc.setText(routesLoc);
                                Picasso.get().load(routesFoto).into(foto);
                                dis.setText(routesDes);
                                Picasso.get().load(routesFoto_map_url).into(foto_map);

                                //переход по ссылке на сторонний сайт
                                String url_map_text = routesMap_url;
                                url_map.setText("\uD83C\uDF0F Подробная карта маршрута...");
                                url_map.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Uri uriUrl = Uri.parse(url_map_text);
                                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                                        startActivity(launchBrowser);
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                //кликабельность кнопки закрытия всплывающего окна
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5B5265")));
                                dialog.show();
                            }
                        });

                        //кликабельность кнопки добавления-удаления из пройденных маршрутов
                        textView_star_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (textView_star_button.getText().toString() == "\uD83D\uDC3E Вернуть обратно в пройденные") {
                                    String sql = "INSERT INTO comleted_routes(routes_id) " + "VALUES (?)";
                                    db.execSQL(sql, new String[]{routes_id});
                                    textView_star_button.setText("\uD83E\uDD47 Удалить из пройденных");
                                } else if (textView_star_button.getText().toString() == "\uD83E\uDD47 Удалить из пройденных") {
                                    String sql = "DELETE FROM comleted_routes WHERE routes_id = ?";
                                    db.execSQL(sql, new String[]{routes_id});
                                    textView_star_button.setText("\uD83D\uDC3E Вернуть обратно в пройденные");
                                }
                            }
                        });
                        sqlCursor_comp.moveToNext();
                    }
                    sqlCursor_comp.close();
                    click = false;
                }
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}