package com.romancedawn.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.romancedawn.databinding.FragmentNotificationsBinding;
import com.romancedawn.module.Factory;
import com.romancedawn.module.FileHelper;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private String fullPathFile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //поиск элементов фрагмента
        final WebView webView = binding.htmlTextView;

        //отображение данных через webview
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Разрешить выполнение JavaScript (если необходимо)
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);

        //нахождение полного пути до html-файла на устройстве
        fullPathFile = Factory.getFileHelper().copyFile("files/", "gear_text.html");

        webView.loadUrl("file://"+fullPathFile);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}