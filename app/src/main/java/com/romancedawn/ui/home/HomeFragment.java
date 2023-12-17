package com.romancedawn.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.romancedawn.R;
import com.romancedawn.databinding.FragmentHomeBinding;
import com.romancedawn.module.Factory;
import com.squareup.picasso.Picasso;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private LinearLayout listLayout;
    private SQLiteDatabase db;
    private Cursor sqlCursor, sqlCursor_star, sqlCursor_comp, sqlCursor_mini;
    private Button button_map, button_list;
    private boolean isListVisible = false;
    private MapView mapView;
    private MapObjectCollection mapObjects;
    private Map map;
    private List<PlacemarkMapObject> placemarkList = new ArrayList<>();
    MapKit mapKitInstance = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        // Инициализация MapKit
        if (mapKitInstance == null) {
            MapKitFactory.setApiKey("0b49f13f-fadd-469f-9063-b179bcddc391");
            MapKitFactory.initialize(getActivity());
            mapKitInstance = MapKitFactory.getInstance();
        }
        else {
            MapKitFactory.getInstance().setApiKey("0b49f13f-fadd-469f-9063-b179bcddc391");
        }

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // поиск элементов фрагмента
        listLayout = binding.listLayout;
        button_map = binding.buttonMap;
        button_list = binding.buttonList;

        mapView = binding.mapView;
        mapView.onStart();

        // Получение ссылки на Map и MapObjectCollection
        map = mapView.getMap();
        // ускорее работы карты
        map.setFastTapEnabled(true);
        map.isFastTapEnabled();
        mapObjects = map.getMapObjects();

        //покаать список маршрутов
        showList();

        button_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //показать карту
                if (isListVisible) {
                    hideList();
                    showImage();
                // показать лист
                } else {
                    showList();
                }
            }
        });

        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            // показать карту
            public void onClick(View v) {
                hideList();
                showImage();
            }
        });

        return root;
    }

    private void showList() {
        // отображение списка маршрутов
        listLayout.setVisibility(View.VISIBLE);
        button_map.setVisibility(View.VISIBLE);
        mapView.setVisibility(View.GONE);
        button_list.setVisibility(View.GONE);
        isListVisible = true;

        // считывание данных о всех маршрутов из бд
        db = Factory.getDatabase();
        sqlCursor = db.rawQuery("SELECT routes_name, location, lenght_day, lenght_km, " +
                "complexity, foto_url, routes_id, map_url, foto_map_url, description FROM routes", null);
        sqlCursor.moveToFirst();

        while (!sqlCursor.isAfterLast()) {
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
            TextView textView_nnn = new TextView(getActivity());

            //инициализация полей данными из бд
            String routesName = sqlCursor.getString(0);
            String routesLoc = sqlCursor.getString(1);
            String routesDay = sqlCursor.getString(2);
            String routesKm = sqlCursor.getString(3);
            String routesComp = sqlCursor.getString(4);
            String routesFoto = sqlCursor.getString(5);
            String routes_id = sqlCursor.getString(6);
            String routesMap_url = sqlCursor.getString(7);
            String routesFoto_map_url = sqlCursor.getString(8);
            String routesDes = sqlCursor.getString(9);

            //создание кнопки-надписи для избранных и пройденных маршрутов
            TextView textView_star_button = new TextView(getActivity());
            textView_star_button.setTextSize(17);
            textView_star_button.setTextColor(Color.parseColor("#AE130622"));

            TextView textView_comp_button = new TextView(getActivity());
            textView_comp_button.setTextSize(17);
            textView_comp_button.setTextColor(Color.parseColor("#AE130622"));

            //проверка на наличие в избранных и выставление соответствующей кнопки
            sqlCursor_star = db.rawQuery("SELECT routes_id FROM stars_routes " +
                    "where routes_id = ?", new String[]{routes_id.toString()});
            if (sqlCursor_star.getCount() != 0) {
                textView_star_button.setText("\uD83C\uDF1F Удалить из избранных");
            } else {
                textView_star_button.setText("⭐ Добавить в избранные");
            }

            //проверка на наличие в пройденных и выставление соответствующей кнопки
            sqlCursor_comp = db.rawQuery("SELECT routes_id FROM comleted_routes " +
                    "where routes_id = ?", new String[]{routes_id.toString()});
            if (sqlCursor_comp.getCount() != 0) {
                textView_comp_button.setText("\uD83E\uDD47 Удалить из пройденных");
            } else {
                textView_comp_button.setText("\uD83D\uDC3E Добавить в пройденные");
            }

            //присваивание значений полям
            Picasso.get().load(routesFoto).into(imageView);
            textView_name.setText(routesName);
            textView_day.setText(routesDay + ", " + routesKm);
            textView_place.setText(routesLoc);
            textView_comp.setText(routesComp);

            // отступы
            textView_n.setText("");
            textView_n.setTextSize(7);
            textView_nn.setText("");
            textView_nn.setTextSize(7);
            textView_nnn.setText("");
            textView_nnn.setTextSize(5);

            //отображение полей
            itemLayout.addView(imageView);
            tableLayout.addView(textView_name);
            tableLayout.addView(textView_n);
            tableLayout.addView(textView_place);
            tableLayout.addView(textView_day);
            tableLayout.addView(textView_comp);
            tableLayout.addView(textView_nn);
            tableLayout.addView(textView_star_button);
            tableLayout.addView(textView_nnn);
            tableLayout.addView(textView_comp_button);
            itemLayout.addView(tableLayout);
            listLayout.addView(itemLayout);

            //добавление отступа между элементами
            LinearLayout itemLayout1 = new LinearLayout(getActivity());
            TextView textView_name1 = new TextView(getActivity());
            textView_name1.setText("");
            itemLayout1.addView(textView_name1);
            listLayout.addView(itemLayout1);

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
                    dialog.setCanceledOnTouchOutside(false); // Запретить закрытие диалогового окна при нажатии вне его
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5B5265")));
                    dialog.show();
                }
            });

            //кликабельность кнопки добавления-удаления из избранных маршрутов
            textView_star_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (textView_star_button.getText().toString() == "⭐ Добавить в избранные") {
                        String sql = "INSERT INTO stars_routes(routes_id) " + "VALUES (?)";
                        db.execSQL(sql, new String[]{routes_id});
                        textView_star_button.setText("\uD83C\uDF1F Удалить из избранных");
                    } else if (textView_star_button.getText().toString() == "\uD83C\uDF1F Удалить из избранных") {
                        String sql = "DELETE FROM stars_routes WHERE routes_id = ?";
                        db.execSQL(sql, new String[]{routes_id});
                        textView_star_button.setText("⭐ Добавить в избранные");
                    }
                }
            });

            //кликабельность кнопки добавления-удаления из пройденных маршрутов
            textView_comp_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (textView_comp_button.getText().toString() == "\uD83D\uDC3E Добавить в пройденные") {
                        String sql = "INSERT INTO comleted_routes(routes_id) " + "VALUES (?)";
                        db.execSQL(sql, new String[]{routes_id});
                        textView_comp_button.setText("\uD83E\uDD47 Удалить из пройденных");
                    } else if (textView_comp_button.getText().toString() == "\uD83E\uDD47 Удалить из пройденных") {
                        String sql = "DELETE FROM comleted_routes WHERE routes_id = ?";
                        db.execSQL(sql, new String[]{routes_id});
                        textView_comp_button.setText("\uD83D\uDC3E Добавить в пройденные");
                    }
                }
            });
            sqlCursor_comp.close();
            sqlCursor_star.close();
            sqlCursor.moveToNext();
        }
        sqlCursor.close();
    }

    private void hideList() {
        // прячем список и меняем флаг
        listLayout.removeAllViews();
        listLayout.setVisibility(View.GONE);
        button_list.setVisibility(View.VISIBLE);
        button_map.setVisibility(View.GONE);
        isListVisible = false;
    }

    private void showImage() {
        // отображение карты
        button_map.setVisibility(View.GONE);
        listLayout.setVisibility(View.GONE);
        mapView.setVisibility(View.VISIBLE);

        //считывание данных (с учетом начальной координаты) о всех маршрутов из бд
        db = Factory.getDatabase();
        sqlCursor_mini = db.rawQuery("SELECT routes_name, location, lenght_day, lenght_km, " +
                "complexity, foto_url, routes_id, map_url, foto_map_url, description, routes_begin FROM routes", null);
        sqlCursor_mini.moveToFirst();

        // Добавление меток на карту в цикле
        while (!sqlCursor_mini.isAfterLast()) {
            //инициализация полей данными из бд
            String routesName = sqlCursor_mini.getString(0);
            String routesLoc = sqlCursor_mini.getString(1);
            String routesDay = sqlCursor_mini.getString(2);
            String routesKm = sqlCursor_mini.getString(3);
            String routesComp = sqlCursor_mini.getString(4);
            String routesFoto = sqlCursor_mini.getString(5);
            String routes_id = sqlCursor_mini.getString(6);
            String routesMap_url = sqlCursor_mini.getString(7);
            String routesFoto_map_url = sqlCursor_mini.getString(8);
            String routesDes = sqlCursor_mini.getString(9);

            // Получение координат точки
            String routesBegin = sqlCursor_mini.getString(10);
            String[] parts = routesBegin.split(",");
            // Проверяем, что было введено два числа
            if (parts.length == 2) {
                // Преобразуем строки в числа и выводим их
                double latitude = Double.parseDouble(parts[0].trim());
                double longitude = Double.parseDouble(parts[1].trim());

                // создание метки по полученным координатам
                PlacemarkMapObject placemark = map.getMapObjects().addPlacemark(new Point(latitude, longitude));
                placemarkList.add(placemark);
                placemark.setOpacity(1.0f); // размер метки
                placemark.setDraggable(false); // неподвижность метки
                placemark.setIcon(ImageProvider.fromResource(getActivity(), android.R.drawable.ic_menu_compass));

                // кликабельность метки для открытия небольшого описания маршрута
                placemark.addTapListener((mapObject, point) -> {
                    // Создаем диалоговое окно
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LinearLayout itemLayout = new LinearLayout(getActivity());
                    itemLayout.setOrientation(LinearLayout.VERTICAL);
                    itemLayout.setBackgroundColor(Color.parseColor("#2DBB86FC"));
                    itemLayout.setPadding(50, 50, 50, 50);

                    //создание текстов полей
                    TableLayout tableLayout = new TableLayout(getActivity());
                    tableLayout.setOrientation(TableLayout.VERTICAL);
                    tableLayout.setPadding(30, 0, 0, 0);
                    TextView textView_name = new TextView(getActivity());
                    textView_name.setTextSize(20);
                    Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
                    textView_name.setTextColor(Color.parseColor("#AE130622"));
                    textView_name.setTypeface(boldTypeface);
                    TextView textView_place = new TextView(getActivity());
                    TextView textView_day = new TextView(getActivity());
                    TextView textView_comp = new TextView(getActivity());

                    //отступы
                    TextView textView_n = new TextView(getActivity());
                    TextView textView_nn = new TextView(getActivity());

                    //инициализация полей данными из бд
                    textView_name.setText(routesName);
                    textView_day.setText(routesDay + ", " + routesKm);
                    textView_place.setText(routesLoc);
                    textView_comp.setText(routesComp);

                    //отступы
                    textView_n.setText("");
                    textView_n.setTextSize(7);
                    textView_nn.setText("");
                    textView_nn.setTextSize(5);

                    //отображение полей
                    tableLayout.addView(textView_name);
                    tableLayout.addView(textView_n);
                    tableLayout.addView(textView_place);
                    tableLayout.addView(textView_day);
                    tableLayout.addView(textView_comp);
                    tableLayout.addView(textView_nn);
                    itemLayout.addView(tableLayout);

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
                            dialog.setCanceledOnTouchOutside(false); // Запретить закрытие диалогового окна при нажатии вне его
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5B5265")));
                            dialog.show();
                        }
                    });

                    // Добавляем макет в диалоговое окно
                    builder.setView(itemLayout);

                    // Добавляем кнопку "ОК" и ее обработчик
                    builder.setPositiveButton("ОК", (dialog, which) -> {
                        // Обработка нажатия кнопки "ОК"
                    });

                    // Отображаем диалоговое окно
                    builder.show();

                    return true;
                });
            }
            sqlCursor_mini.moveToNext();
        }
        sqlCursor_mini.close();
    }

    @Override
    public void onStop() {
        // Обязательный вызов метода onStop у MapView
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Обязательный вызов метода onStart у MapView
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
        //showImage();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        placemarkList.clear();
        map.getMapObjects().clear();
    }

}




