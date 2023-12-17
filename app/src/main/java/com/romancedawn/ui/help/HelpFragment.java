package com.romancedawn.ui.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.romancedawn.databinding.FragmentHelpBinding;
import com.romancedawn.module.Factory;

public class HelpFragment extends Fragment {
    private FragmentHelpBinding binding;
    private String fullPathFile;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HelpViewModel helpViewModel =
                new ViewModelProvider(this).get(HelpViewModel.class);

        binding = FragmentHelpBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //поиск элементов фрагмента
        final WebView webView = binding.htmlTextView;

        //отображение данных через webview
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Разрешить выполнение JavaScript (если необходимо)
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);

        //нахождение полного пути до html-файла на устройстве
        fullPathFile = Factory.getFileHelper().copyFile("files/", "help_text.html");

        webView.loadUrl("file://"+fullPathFile);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}