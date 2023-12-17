package com.romancedawn.ui.stars;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.romancedawn.R;
import com.romancedawn.databinding.FragmentStarsBinding;
import com.romancedawn.module.Factory;
import com.squareup.picasso.Picasso;

public class StarsFragment extends Fragment {
    private FragmentStarsBinding binding;
    private LinearLayout listLayout;
    private SQLiteDatabase db;
    private Cursor sqlCursor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StarsViewModel starsViewModel =
                new ViewModelProvider(this).get(StarsViewModel.class);

        binding = FragmentStarsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        listLayout = binding.listLayout;

        //подгрузка бд
        db = Factory.getDatabase();
        sqlCursor = db.rawQuery("SELECT routes_name, location, lenght_day, lenght_km, complexity, " +
                "foto_url, routes_id, map_url, foto_map_url, description FROM routes where routes_id in (select routes_id from stars_routes)", null);
        sqlCursor.moveToFirst();

        //если список избранных пустой
        if (sqlCursor.getCount() == 0) {
            TextView textView = new TextView(getActivity());
            textView.setTextSize(18);
            Typeface bold = Typeface.defaultFromStyle(Typeface.BOLD);
            textView.setTypeface(bold);
            textView.setPadding(0, 800, 0, 0);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.parseColor("#cac1db"));
            textView.setText("Здесь пока что пусто...");
            listLayout.addView(textView);
        }

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

            //создание кнопки-надписи для избранных маршрутов
            TextView textView_star_button = new TextView(getActivity());
            textView_star_button.setTextSize(17);
            textView_star_button.setTextColor(Color.parseColor("#AE130622"));
            textView_star_button.setText("\uD83C\uDF1F Удалить из избранных");

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
                    // создание всплывающего окна
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View dialogView = getLayoutInflater().inflate(R.layout.fragment_discription, null);
                    builder.setView(dialogView);
                    dialogView.setPadding(20, 0, 20, 40);

                    // поиск элементов фрагмента
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

                    // инициализация элементов модели данными из бд
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

            //кликабельность кнопки добавления-удаления из избранных маршрутов
            textView_star_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (textView_star_button.getText().toString() == "⭐ Вернуть обратно в избранные") {
                        String sql = "INSERT INTO stars_routes(routes_id) " + "VALUES (?)";
                        db.execSQL(sql, new String[]{routes_id});
                        textView_star_button.setText("\uD83C\uDF1F Удалить из избранных");
                    } else if (textView_star_button.getText().toString() == "\uD83C\uDF1F Удалить из избранных") {
                        String sql = "DELETE FROM stars_routes WHERE routes_id = ?";
                        db.execSQL(sql, new String[]{routes_id});
                        textView_star_button.setText("⭐ Вернуть обратно в избранные");
                    }
                }
            });
            sqlCursor.moveToNext();
        }
        sqlCursor.close();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}